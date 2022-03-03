package io.setl.iobc.besu.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.FreeGasProvider;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.besu.tx.ContractCreatedHandler;
import io.setl.iobc.besu.tx.GetBlockNumberImpl;
import io.setl.iobc.besu.tx.ReceiptHandlerFactory;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TransactionInput.TxProcessingMode;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.CreateToken;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.BNYBond;
import io.setl.iobc.web3j.BNYCash;
import io.setl.json.CJObject;
import io.setl.json.Canonical;

/**
 * Create a token on the Besu block chain.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class CreateTokenImpl implements CreateToken {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public CreateTokenImpl(
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    this.addressTable = addressTable;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    final String tokenId = input.getSymbol();
    log.info("Creating token {}", tokenId);

    // Check controller exists
    SetlAddress controller = addressTable.getAddressSafe(input.getController());
    Credentials credentials = Web3KeyConversion.convert(controller);

    // Check the token does not already exist
    TokenSpecification spec = tokenTable.getTokenSpecification(tokenId);
    if (spec != null) {
      CJObject cjObject = new CJObject();
      cjObject.put("token", tokenId);
      cjObject.put("owner", spec.getController());
      cjObject.put("createdAt", spec.getCreateTime().toString());
      cjObject.put("httpStatus", 409);
      throw new ParameterisedException("The token " + tokenId + " has already been created.", "iobc:token-exists", cjObject);
    }

    BigInteger recentBlock = GetBlockNumberImpl.getRecentBlock(besu.getWeb3j(), input.getTxProcessingMode());

    // Deploy the smart contract for the token
    TransactionManager manager = besu.getManager(credentials);
    RemoteCall<? extends Contract> call = getContract(besu.getWeb3j(), manager, input);
    Contract bnyToken;
    try {
      bnyToken = call.send();
    } catch (Exception exception) {
      // remove token ID from Kafka
      log.error("Failed to deploy smart contract for token {}", tokenId, exception);
      throw ExceptionTranslator.convert(exception);
    }

    // If the deployment transaction succeeds, the token is created.
    // If the deployment transaction fails, the token is not created.
    // All we have right now is the transaction hash, which we will have to check on.
    // If the update to Kafka fails, we will have an orphaned contract in the blockchain.
    Optional<TransactionReceipt> receiptOptional = bnyToken.getTransactionReceipt();
    assert receiptOptional.isPresent();
    String txHash = receiptOptional.get().getTransactionHash();

    // Mark the token as in-progress, with the transaction ID
    BesuTokenData chainData = new BesuTokenData();
    chainData.setCreationBlock(recentBlock.longValue());
    chainData.setContract(null);
    chainData.setTransactionId(txHash);
    spec = TokenSpecification.builder()
        .brand(ChainBrand.BESU)
        .chainData(chainData)
        .chainId(besu.getIobcId())
        .controller(input.getController())
        .createTime(Instant.now())
        .name(input.getName())
        .symbol(tokenId)
        .loading(true)
        .build();
    tokenTable.insertToken(tokenId, spec);

    // Register a handler to process the transaction results.
    ContractCreatedHandler handler = new ContractCreatedHandler(tokenTable, tokenId, spec);
    besu.getReceiptHandler().register(txHash, handler);

    return (input.getTxProcessingMode() == TxProcessingMode.RETURN_ID)
        ? ReceiptHandlerFactory.prepareIdOnly(recentBlock, txHash, handler.getResult())
        : handler.getResult();
  }


  private RemoteCall<BNYBond> getBondContract(Web3j web3j, TransactionManager manager, Input input) {
    CJObject cjObject = (CJObject) Canonical.cast(input.getParameters());
    BigInteger supply = cjObject.getBigInteger("supply", BigInteger.ZERO);
    int decimals = cjObject.getInt("decimals", 0);
    return BNYBond.deploy(web3j, manager, FreeGasProvider.INSTANCE, input.getName(), input.getSymbol(), BigInteger.valueOf(decimals), supply);
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }


  private RemoteCall<BNYCash> getCashContract(Web3j web3j, TransactionManager manager, Input input) {
    CJObject cjObject = (CJObject) Canonical.cast(input.getParameters());
    int decimals = cjObject.getInt("decimals", 0);
    return BNYCash.deploy(web3j, manager, FreeGasProvider.INSTANCE, input.getName(), input.getSymbol(), BigInteger.valueOf(decimals));
  }


  private RemoteCall<? extends Contract> getContract(Web3j web3j, TransactionManager manager, Input input) throws ParameterisedException {
    String type = input.getType().toUpperCase(Locale.ROOT);
    switch (type) {
      case "CASH":
        return getCashContract(web3j, manager, input);
      case "BOND":
        return getBondContract(web3j, manager, input);
      default: {
        CJObject cjObject = new CJObject();
        cjObject.put("type", type);
        throw new ParameterisedException("The token type '" + type + "' is not recognised.", "iobc/unknown-token-type", cjObject);
      }
    }
  }

}
