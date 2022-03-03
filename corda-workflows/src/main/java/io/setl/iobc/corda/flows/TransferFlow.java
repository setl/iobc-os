package io.setl.iobc.corda.flows;

import static io.setl.iobc.corda.contracts.TokenContract.externalId;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.List;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.transactions.TransactionBuilder;

import io.setl.iobc.corda.contracts.TokenContract;
import io.setl.iobc.corda.contracts.TokenContract.Commands;
import io.setl.iobc.corda.states.HoldingState;

public class TransferFlow {

  @InitiatingFlow
  @StartableByRPC
  @StartableByService
  public static class Initiator extends SimpleLocalFlow {

    private final BigInteger amount;

    private final String from;

    private final String symbol;

    private final String to;

    private StateAndRef<HoldingState> holdingFromIn;

    private HoldingState holdingFromOut;

    private StateAndRef<HoldingState> holdingToIn;

    private HoldingState holdingToOut;


    public Initiator(String symbol, BigInteger amount, String from, String to) {
      this.symbol = symbol;
      this.amount = amount;
      this.from = from;
      this.to = to;
    }


    @Override
    protected void generateTransaction(TransactionBuilder txBuilder) throws FlowException {
      List<PublicKey> signers = ImmutableList.of(getOurIdentity().getOwningKey());
      Command<TokenContract.Commands> txCommand = new Command<>(Commands.TRANSFER, signers);
      txBuilder.addCommand(txCommand);

      // "To" may not exist at input, but must exist on output.
      if (holdingToIn != null) {
        txBuilder.addInputState(holdingToIn);
      }
      txBuilder.addOutputState(holdingToOut);

      // "From" must exist on input, but may not exist on output.
      txBuilder.addInputState(holdingFromIn);
      if (holdingFromOut != null) {
        txBuilder.addOutputState(holdingFromOut);
      }
    }


    @Override
    protected void initialiseFlow(TransactionBuilder txBuilder) throws FlowException {
      holdingFromIn = getStateRef(externalId(getOurIdentity(), symbol, from), HoldingState.class);
      holdingToIn = getStateRef(externalId(getOurIdentity(), symbol, to), HoldingState.class, false);

      holdingFromOut = holdingFromIn.getState().getData().changeAmount(amount.negate());
      if (holdingFromOut.getAmount().signum() != 1) {
        holdingFromOut = null;
      }

      if (holdingToIn != null) {
        holdingToOut = holdingToIn.getState().getData().changeAmount(amount);
      } else {
        holdingToOut = new HoldingState(getOurIdentity(), symbol, to, amount, BigInteger.ZERO);
      }
    }

  }

}
