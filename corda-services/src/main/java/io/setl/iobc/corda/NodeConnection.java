package io.setl.iobc.corda;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowProgressHandle;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.NetworkHostAndPort;
import rx.Observable;

import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionInput.TxProcessingMode;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;

/**
 * Wraps an RPC connection to a Corda node.
 *
 * <p>The RPC connection is configured using command line arguments.
 */
@Slf4j
public class NodeConnection implements ChainConfiguration {

  public static CompletableFuture<TransactionResult> toFuture(FlowProgressHandle<SignedTransaction> flow, TxProcessingMode mode) {
    final String trxnId = flow.getId().getUuid().toString();
    CompletableFuture<TransactionResult> result = new CompletableFuture<>();
    CompletableFuture<TransactionResult> output;
    if (mode == TxProcessingMode.RETURN_ID) {
      output = CompletableFuture.completedFuture(
          TransactionResult.builder()
              .transactionId(trxnId)
              .txStatus(TransactionResult.TxStatus.PENDING)
              .continuation(result)
              .build()
      );
    } else {
      output = result;
    }

    Observable<String> observable = flow.getProgress();
    observable.subscribe(
        s -> log.debug("Corda flow {} has reached state {}", trxnId, s),
        e -> {
          log.debug("Corda flow {} has failed with error", trxnId, e);
          result.complete(TransactionResult.builder().transactionId(trxnId)
              .txStatus(TxStatus.FAILURE)
              .message("Flow failed: " + e.getMessage())
              .build());
        },
        () -> {
          log.debug("Corda flow {} has completed successfully", trxnId);
          result.complete(TransactionResult.builder().transactionId(trxnId)
              .txStatus(TransactionResult.TxStatus.SUCCESS)
              .message("Transaction successful")
              .build());
        }
    );

    return output;
  }


  private final String iobcId;

  private final String legalName;

  private final Party party;

  private final CordaRPCOps proxy;

  private final CordaRPCConnection rpcConnection;


  /**
   * Create new instance.
   *
   * @param iobcId   the identifier for this block chain.
   * @param rpcHost  the RPC server's name
   * @param rpcPort  the RPC port on the server
   * @param country  the country for the organisation
   * @param locality the locality for the organisation
   * @param orgName  the organisation's name
   * @param username the username for the connection
   * @param password the password for the connection
   */
  public NodeConnection(
      String iobcId,
      String rpcHost,
      int rpcPort,
      String country,
      String locality,
      String orgName,
      String username,
      String password
  ) {
    this.iobcId = iobcId;
    NetworkHostAndPort rpcAddress = new NetworkHostAndPort(rpcHost, rpcPort);
    CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
    rpcConnection = rpcClient.start(username, password);
    proxy = rpcConnection.getProxy();
    legalName = String.format("O=%s,L=%s,C=%s", orgName, locality, country);
    party = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(legalName));
  }


  public void close() {
    rpcConnection.notifyServerAndClose();
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.CORDA;
  }


  public String getIobcId() {
    return iobcId;
  }


  public String getLegalName() {
    return legalName;
  }


  public Party getParty() {
    return party;
  }


  public CordaRPCOps getProxy() {
    return proxy;
  }


  public <T extends ContractState> Optional<T> getStateOptional(String externalId, Class<T> type) throws FlowException {
    QueryCriteria criteria =
        new QueryCriteria.LinearStateQueryCriteria().withParticipants(List.of(party))
            .withExternalId(List.of(externalId))
            .withStatus(Vault.StateStatus.UNCONSUMED);

    List<StateAndRef<T>> stateAndRefs = getProxy().vaultQueryByCriteria(criteria, type).getStates();

    int size = stateAndRefs.size();
    if (size == 0) {
      return Optional.empty();
    }

    if (size != 1) {
      throw new FlowException("External ID returned " + size + " results for id \"" + externalId + "\" with type " + type);
    }

    return Optional.of(stateAndRefs.get(0).getState().getData());
  }


  public <T extends ContractState> T getStateValue(String externalId, Class<T> type) throws FlowException {
    return getStateOptional(externalId, type).orElseThrow(
        () -> new FlowException("External ID \"" + externalId + "\" did not match any records of type " + type)
    );
  }


  public Party getX500Name(String nodeInfo) {
    return proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(nodeInfo));
  }


  public String toString() {
    return getClass().getSimpleName() + "[" + legalName + " / " + iobcId + "]";
  }


}
