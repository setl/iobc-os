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
import io.setl.iobc.besu.tx.GetBlockNumberImpl;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.dvp.DvpCommit;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.DVP;
import io.setl.iobc.web3j.ITokenExtensions;

/**
 * Implementation of DVP Controller create.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
@Service
@Slf4j
public class DvpCommitImpl implements DvpCommit {

  private final AddressTable addressTable;

  /** New instance. */
  @Autowired
  public DvpCommitImpl(
      AddressTable addressTable
  ) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    BigInteger dvpId = DvpManager.getInternalId(input.getDvpId());
    String dvpContract = besu.getDvpManager().getId();

    log.info("Address \"{}\" is committing to a DVP Trade {}", input.getAddress(), input.getDvpId());

    // Get user's address
    SetlAddress setlAddress = addressTable.getAddressSafe(input.getAddress());
    Credentials credentials = Web3KeyConversion.convert(setlAddress);
    TransactionManager manager = besu.getManager(credentials);

    // Find out the details of the trade party
    DVP.Party dvpParty;
    DVP dvp = DVP.load(dvpContract, besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
    RemoteFunctionCall<DVP.Party> getPartyCall = dvp.party(dvpId);
    try {
      dvpParty = getPartyCall.send();
    } catch (Exception e) {
      // Failed to send transfer request
      log.error("Failed to retrieve DVP party details for trade {} for address {}", input.getDvpId(), input.getAddress(), e);
      throw ExceptionTranslator.convert(e);
    }

    // Load the token contract
    ITokenExtensions bnyToken = ITokenExtensions.load(dvpParty.token, besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
    RemoteFunctionCall<TransactionReceipt> call = bnyToken.dvpCommit(dvpContract, dvpId);

    BigInteger recentBlock = GetBlockNumberImpl.getRecentBlock(besu.getWeb3j(), input.getTxProcessingMode());

    // Send the commit
    TransactionReceipt receipt;
    try {
      receipt = call.send();
    } catch (Exception e) {
      // Failed to send transfer request
      log.error("Failed to invoke DVP commit function on trade {} for address {}", input.getDvpId(), input.getAddress(), e);
      throw ExceptionTranslator.convert(e);
    }

    return besu.getReceiptHandlerFactory().prepare(input.getTxProcessingMode(), recentBlock, receipt.getTransactionHash());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
