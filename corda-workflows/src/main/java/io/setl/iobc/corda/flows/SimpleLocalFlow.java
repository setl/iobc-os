package io.setl.iobc.corda.flows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

/**
 * Simple flow using only the local node.
 *
 * @author Simon Greatrix on 24/02/2022.
 */
public class SimpleLocalFlow extends FlowLogic<SignedTransaction> {

  private final Step stepFinalisingTransaction = new Step("Obtaining notary signature and recording transaction.") {
    @Override
    public ProgressTracker childProgressTracker() {
      return FinalityFlow.Companion.tracker();
    }
  };

  private final Step stepGeneratingTransaction = new Step("Generating transaction based on new Token.");

  private final Step stepInitialisingFlow = new Step("Initialising flow.");

  private final Step stepSigningTransaction = new Step("Signing transaction with our private key.");

  private final Step stepVerifyingTransaction = new Step("Verifying contract constraints.");

  // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
  // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
  // function.
  private final ProgressTracker progressTracker = new ProgressTracker(
      stepInitialisingFlow,
      stepGeneratingTransaction,
      stepVerifyingTransaction,
      stepSigningTransaction,
      stepFinalisingTransaction
  );


  @Override
  public SignedTransaction call() throws FlowException {
    progressTracker.setCurrentStep(stepInitialisingFlow);
    Party notary = getNotary();
    TransactionBuilder txBuilder = new TransactionBuilder(notary);
    initialiseFlow(txBuilder);

    // Step #2 - Create the transaction
    progressTracker.setCurrentStep(stepGeneratingTransaction);
    generateTransaction(txBuilder);

    //Step #3 - Verify the transaction
    progressTracker.setCurrentStep(stepVerifyingTransaction);
    txBuilder.verify(getServiceHub());

    //Step #4 - Sign the transaction
    progressTracker.setCurrentStep(stepSigningTransaction);
    final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

    //Step #5 - Finalize the transaction
    progressTracker.setCurrentStep(stepFinalisingTransaction);
    return subFlow(new FinalityFlow(partSignedTx, Collections.emptyList()));
  }


  protected void generateTransaction(TransactionBuilder txBuilder) throws FlowException {
    // Must be implemented
  }


  private Party getNotary() {
    List<Party> notaries = getServiceHub()
        .getNetworkMapCache()
        .getNotaryIdentities();
    return notaries.isEmpty() ? null : notaries.get(0);
  }


  @Override
  public ProgressTracker getProgressTracker() {
    return progressTracker;
  }


  public <T extends ContractState> StateAndRef<T> getStateRef(String externalId, Class<T> type) throws FlowException {
    return getStateRef(externalId, type, true);
  }


  public <T extends ContractState> StateAndRef<T> getStateRef(String externalId, Class<T> type, boolean required) throws FlowException {
    QueryCriteria criteria =
        new QueryCriteria.LinearStateQueryCriteria().withParticipants(Arrays.asList(getOurIdentity()))
            .withExternalId(Arrays.asList(externalId))
            .withStatus(Vault.StateStatus.UNCONSUMED);

    List<StateAndRef<T>> stateAndRefs = getServiceHub().getVaultService().queryBy(type, criteria).getStates();

    int size = stateAndRefs.size();
    if (size > 1) {
      throw new FlowException("External ID returned " + size + " results for id \"" + externalId + "\" with type " + type);
    }
    if (size == 0) {
      if (required) {
        throw new FlowException("External ID returned no results for id \"" + externalId + "\" with type " + type + " when one was required.");
      }
      return null;
    }

    return stateAndRefs.get(0);
  }


  public <T extends ContractState> T getStateValue(String externalId, Class<T> type, boolean required) throws FlowException {
    StateAndRef<T> stateAndRef = getStateRef(externalId, type, required);
    return (stateAndRef != null) ? stateAndRef.getState().getData() : null;
  }


  public <T extends ContractState> T getStateValue(String externalId, Class<T> type) throws FlowException {
    return getStateRef(externalId, type, true).getState().getData();
  }


  protected void initialiseFlow(TransactionBuilder txBuilder) throws FlowException {
    // may be implemented
  }

}
