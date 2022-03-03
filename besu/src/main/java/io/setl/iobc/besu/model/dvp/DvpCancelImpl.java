package io.setl.iobc.besu.model.dvp;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.DvpManager;
import io.setl.iobc.besu.FreeGasProvider;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.besu.tx.GetBlockNumberImpl;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.dvp.DvpCancel;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.DVP;
import io.setl.iobc.web3j.ITokenExtensions;

/**
 * Cancel a DVP trade.
 *
 * @author Simon Greatrix on 01/12/2021.
 */
@Component
@Slf4j
public class DvpCancelImpl implements DvpCancel {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public DvpCancelImpl(
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    this.addressTable = addressTable;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    BigInteger dvpId = DvpManager.getInternalId(input.getDvpId());
    String dvpContract = besu.getDvpManager().getId();

    log.info("Cancelling DVP trade {}", input.getDvpId());

    RemoteFunctionCall<TransactionReceipt> call;
    if (input.getSymbol() != null) {
      // Controller cancel
      String tokenId = input.getSymbol();
      TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
      TokenSpecification.check(tokenId, token);
      BesuTokenData spec = new BesuTokenData(token.getChainData());
      SetlAddress setlAddress = addressTable.getAddressSafe(token.getController());
      Credentials credentials = Web3KeyConversion.convert(setlAddress);
      TransactionManager manager = besu.getManager(credentials);
      ITokenExtensions contract = ITokenExtensions.load(spec.getContract(), besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
      call = contract.controllerDVPCancel(dvpContract, dvpId);
    } else if (input.getAddress() != null) {
      // Regular cancel
      SetlAddress setlAddress = addressTable.getAddressSafe(input.getAddress());
      Credentials credentials = Web3KeyConversion.convert(setlAddress);
      TransactionManager manager = besu.getManager(credentials);
      DVP dvpInstance = DVP.load(dvpContract, besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
      call = dvpInstance.cancel(dvpId);
    } else {
      // Bad input
      throw new IllegalArgumentException("Input must specify token or address");
    }

    BigInteger recentBlock = GetBlockNumberImpl.getRecentBlock(besu.getWeb3j(), input.getTxProcessingMode());

    TransactionReceipt receipt;
    try {
      receipt = call.send();
    } catch (Exception e) {
      // Failed to send transfer request
      log.error("Failed to invoke DVP cancel operation", e);
      throw ExceptionTranslator.convert(e);
    }

    return besu.getReceiptHandlerFactory().prepare(input.getTxProcessingMode(), recentBlock, receipt.getTransactionHash());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
