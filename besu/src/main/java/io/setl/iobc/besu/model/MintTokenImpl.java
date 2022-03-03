package io.setl.iobc.besu.model;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.FreeGasProvider;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.besu.tx.GetBlockNumberImpl;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.MintToken;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.ILockableMintable;
import io.setl.iobc.web3j.IMintable;

/**
 * Mint a token.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class MintTokenImpl implements MintToken {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public MintTokenImpl(
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    this.addressTable = addressTable;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;
    String tokenId = input.getSymbol();
    String toAddress = input.getTo();
    BigInteger amount = input.getAmount();

    log.info("Minting {} of token \"{}\" to \"{}\"", amount, tokenId, toAddress);

    // Check address exists
    SetlAddress setlAddress = addressTable.getAddressSafe(toAddress);
    Credentials toCredential = Web3KeyConversion.convert(setlAddress);
    String toBesuAdress = toCredential.getAddress();

    // Get the token specification
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    // Get token owner's address
    setlAddress = addressTable.getAddressSafe(token.getController());
    Credentials ownerCredentials = Web3KeyConversion.convert(setlAddress);

    BigInteger recentBlock = GetBlockNumberImpl.getRecentBlock(besu.getWeb3j(), input.getTxProcessingMode());

    // Call the contract
    TransactionManager manager = besu.getManager(ownerCredentials);
    RemoteFunctionCall<TransactionReceipt> call;
    if (input.getToLocked() == null || !input.getToLocked()) {
      IMintable bnyToken = IMintable.load(spec.getContract(), besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
      call = bnyToken.mint(toBesuAdress, amount);
    } else {
      ILockableMintable bnyToken = ILockableMintable.load(spec.getContract(), besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
      call = bnyToken.mint(toBesuAdress, amount, Boolean.TRUE);
    }

    TransactionReceipt receipt;
    try {
      receipt = call.send();
    } catch (Exception e) {
      // Failed to send mint request
      log.error("Failed to invoke mint function on smart contract {} for token {} with amount {}",
          spec.getContract(), tokenId, amount, e
      );
      throw ExceptionTranslator.convert(e);
    }

    return besu.getReceiptHandlerFactory().prepare(input.getTxProcessingMode(), recentBlock, receipt.getTransactionHash());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }


}
