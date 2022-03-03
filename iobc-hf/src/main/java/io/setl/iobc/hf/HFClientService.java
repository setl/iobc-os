package io.setl.iobc.hf;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Transaction;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.protos.peer.TransactionPackage.TxValidationCode;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;

import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionInput.TxProcessingMode;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.util.ExceptionTranslator;

/**
 * Client specification.
 */
public class HFClientService implements ChainConfiguration {

  private final String channelName;

  private final HFCAClientService hfcaClientService;

  private final String iobcId;

  private final Path networkConfigFile;

  private final String paramsSummary;

  private final Wallet wallet;


  public HFClientService(
      String iobcId,
      String orgName,
      String ccpFilePath,
      String walletDir,
      String channelName,
      HFCAClientService hfcaClientService
  ) throws IOException {
    this.iobcId = iobcId;
    this.hfcaClientService = hfcaClientService;

    paramsSummary = String.format("HFClientService class=> OrgName: %s; ccpFilePath: %s, wallet dir: %s", orgName, ccpFilePath, walletDir);
    this.channelName = channelName;

    // Path to a common connection profile describing the network.
    networkConfigFile = Paths.get(ccpFilePath);

    Path walletDirectory = Paths.get(walletDir);
    wallet = Wallets.newFileSystemWallet(walletDirectory);
  }


  public CompletableFuture<TransactionResult> executeTransaction(TxProcessingMode mode, String userId, String funcName, String... contractArgs)
      throws IOException {
    final String contractName = "setl-multi-erc20";

    // Configure the gateway connection used to access the network.
    Gateway gateway = Gateway.createBuilder()
        .identity(wallet, userId)
        .networkConfig(networkConfigFile)
        .connect();

    // Obtain a smart contract deployed on the network.
    Network network = gateway.getNetwork(channelName);
    Contract contract = network.getContract(contractName);

    // Submit transactions that store state to the ledger.
    Transaction trxn = contract.createTransaction(funcName);
    String trxnId = trxn.getTransactionId();

    CompletableFuture<TransactionResult> result = new CompletableFuture<>();
    CompletableFuture<TransactionResult> output;
    if (mode == TxProcessingMode.RETURN_ID) {
      CompletableFuture<TransactionResult> pending = CompletableFuture.completedFuture(
          TransactionResult.builder()
              .transactionId(trxnId)
              .txStatus(TransactionResult.TxStatus.PENDING)
              .continuation(result)
              .build()
      );
      output = pending;
      HFClientServiceFactory.executorService.submit(() -> submit(result, trxn, contractArgs));
    } else {
      submit(result, trxn, contractArgs);
      output = result;
    }

    return output;
  }


  public HFCAClientService getHfcaClientService() {
    return hfcaClientService;
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.FABRIC;
  }


  @Override
  public String getIobcId() {
    return iobcId;
  }


  public byte[] queryTransaction(String userId, String funcName, String... contractArgs)
      throws ContractException, IOException {
    final String contractName = "setl-multi-erc20";

    // Configure the gateway connection used to access the network.
    Gateway.Builder builder = Gateway.createBuilder()
        .identity(wallet, userId)
        .networkConfig(networkConfigFile);

    // Create a gateway connection
    try (Gateway gateway = builder.connect()) {

      // Obtain a smart contract deployed on the network.
      Network network = gateway.getNetwork(channelName);
      Contract contract = network.getContract(contractName);

      // Evaluate transactions that query state from the ledger.
      return contract.evaluateTransaction(funcName, contractArgs);

    } catch (ContractException e) {
      e.printStackTrace();
      throw e;
    }
  }


  private void submit(CompletableFuture<TransactionResult> result, Transaction trxn, String[] contractArgs) {
    try {
      trxn.submit(contractArgs);
      result.complete(TransactionResult.builder().transactionId(trxn.getTransactionId())
          .txStatus(TransactionResult.TxStatus.SUCCESS)
          .message("Transaction successful")
          .build());
    } catch (ContractException cex) {
      cex.printStackTrace();
      if (cex.getCause() instanceof TransactionEventException) {
        TransactionEventException tee = (TransactionEventException) cex.getCause();
        TransactionEvent event = tee.getTransactionEvent();
        TxValidationCode code = TxValidationCode.forNumber(event.getValidationCode());
        result.complete(TransactionResult.builder()
            .transactionId(event.getTransactionID())
            .txStatus(TransactionResult.TxStatus.FAILURE)
            .message("Validation code: " + code)
            .build());
        return;
      }

      ProposalResponse response = cex.getProposalResponses().stream().findFirst().orElseThrow();
      result.complete(TransactionResult.builder()
          .transactionId(response.getTransactionID())
          .txStatus(TransactionResult.TxStatus.FAILURE)
          .message(response.getMessage())
          .build());
    } catch (TimeoutException | RuntimeException e2) {
      result.completeExceptionally(ExceptionTranslator.convert(e2));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      result.completeExceptionally(e);
    }
  }


  @Override
  public String toString() {
    return paramsSummary;
  }

}
