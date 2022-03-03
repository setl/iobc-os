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
import io.setl.iobc.model.tokens.TransferFrom;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.IERC20;

/**
 * Transfer From implementation.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class TransferFromImpl implements TransferFrom {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public TransferFromImpl(
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
    String address = input.getAddress();
    String toAddress = input.getTo();
    String fromAddress = input.getFrom();
    BigInteger amount = input.getAmount();

    log.info("Controller transfer {} of token \"{}\" from \"{}\" to \"{}\"", amount, tokenId, fromAddress, toAddress);

    // Check addresses exists
    SetlAddress setlAddress = addressTable.getAddressSafe(fromAddress);
    String fromBesuAdress = Web3KeyConversion.convert(setlAddress).getAddress();

    setlAddress = addressTable.getAddressSafe(toAddress);
    String toBesuAdress = Web3KeyConversion.convert(setlAddress).getAddress();

    // Get the acting address
    setlAddress = addressTable.getAddressSafe(address);
    Credentials credentials = Web3KeyConversion.convert(setlAddress);

    // Get the token specification
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    BigInteger recentBlock = GetBlockNumberImpl.getRecentBlock(besu.getWeb3j(), input.getTxProcessingMode());

    // Call the contract
    TransactionManager manager = besu.getManager(credentials);
    IERC20 bnyToken = IERC20.load(spec.getContract(), besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
    RemoteFunctionCall<TransactionReceipt> call = bnyToken.transferFrom(fromBesuAdress, toBesuAdress, amount);
    TransactionReceipt receipt;
    try {
      receipt = call.send();
    } catch (Exception e) {
      // Failed to send transfer request
      log.error("Failed to invoke controller transfer function on smart contract {} for token {} with amount {}",
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
