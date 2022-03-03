package io.setl.iobc.besu.model.dvp;

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
import io.setl.iobc.model.tokens.dvp.DvpControllerCommit;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.ILockable;

/**
 * Implementation of DVP Controller create.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
@Service
@Slf4j
public class DvpControllerCommitImpl implements DvpControllerCommit {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public DvpControllerCommitImpl(
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    this.addressTable = addressTable;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    String symbol = input.getSymbol();
    BigInteger dvpId = DvpManager.getInternalId(input.getDvpId());
    String dvpContract = besu.getDvpManager().getId();

    log.info("Controller of \"{}\" is committing to a DVP Trade {}", symbol, input.getDvpId());

    // Get the token specification
    TokenSpecification token = tokenTable.getTokenSpecification(symbol);
    TokenSpecification.check(symbol, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    // Get token owner's address
    SetlAddress setlAddress = addressTable.getAddressSafe(token.getController());
    Credentials ownerCredentials = Web3KeyConversion.convert(setlAddress);

    // Call the contract. Note: This assumes the contract supports ISetlLockable.
    TransactionManager manager = besu.getManager(ownerCredentials);
    ILockable bnyToken = ILockable.load(spec.getContract(), besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
    RemoteFunctionCall<TransactionReceipt> call = bnyToken.controllerDVPCommit(
        dvpContract, dvpId, input.isFromLocked()
    );

    BigInteger recentBlock = GetBlockNumberImpl.getRecentBlock(besu.getWeb3j(), input.getTxProcessingMode());

    TransactionReceipt receipt;
    try {
      receipt = call.send();
    } catch (Exception e) {
      // Failed to send transfer request
      log.error("Failed to invoke controller DVP create function on smart contract {} for token {}",
          spec.getContract(), symbol, e
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
