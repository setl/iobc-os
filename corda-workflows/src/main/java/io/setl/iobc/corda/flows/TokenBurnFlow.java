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
import io.setl.iobc.corda.states.TokenState;

public class TokenBurnFlow {

  @InitiatingFlow
  @StartableByRPC
  @StartableByService
  public static class Initiator extends SimpleLocalFlow {

    private final BigInteger amount;

    private final String from;

    private final String symbol;

    private StateAndRef<HoldingState> holdingIn;

    private HoldingState holdingOut;

    private StateAndRef<TokenState> tokenIn;

    private TokenState tokenOut;


    public Initiator(String symbol, BigInteger amount, String from) {
      this.symbol = symbol;
      this.amount = amount;
      this.from = from;
    }


    @Override
    protected void generateTransaction(TransactionBuilder txBuilder) {
      List<PublicKey> signers = ImmutableList.of(getOurIdentity().getOwningKey());
      Command<TokenContract.Commands> txCommand = new Command<>(Commands.BURN, signers);

      if (holdingOut != null) {
        txBuilder.addOutputState(holdingOut);
      }
      txBuilder.addInputState(tokenIn)
          .addInputState(holdingIn)
          .addOutputState(tokenOut)
          .addCommand(txCommand);
    }


    @Override
    protected void initialiseFlow(TransactionBuilder txBuilder) throws FlowException {
      holdingIn = getStateRef(externalId(getOurIdentity(), symbol, from), HoldingState.class);
      tokenIn = getStateRef(externalId(getOurIdentity(), symbol), TokenState.class);

      BigInteger delta = amount.negate();
      tokenOut = tokenIn.getState().getData().changeSupply(delta);
      holdingOut = holdingIn.getState().getData().changeAmount(delta);
      if (holdingOut.getAmount().signum() == 0) {
        holdingOut = null;
      }
    }

  }

}
