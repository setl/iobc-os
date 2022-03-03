package io.setl.iobc.corda.flows;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.List;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.identity.Party;
import net.corda.core.transactions.TransactionBuilder;

import io.setl.iobc.corda.contracts.TokenContract;
import io.setl.iobc.corda.contracts.TokenContract.Commands;
import io.setl.iobc.corda.states.HoldingState;
import io.setl.iobc.corda.states.TokenState;

/**
 * Add Token Flow managed by the Token Contract.
 *
 * @author Elendu Uche.
 */
public class TokenAddFlow {

  @InitiatingFlow
  @StartableByRPC
  @StartableByService
  public static class Initiator extends SimpleLocalFlow {

    private final String address;

    private final String name;

    private final String symbol;

    private final BigInteger totalSupply;

    /** The ID of the holding state. Set by initialise. */
    private HoldingState holdingState;

    /** The ID of the token state. Set by initialise. */
    private TokenState tokenState;


    /** New instance. */
    public Initiator(String address, String symbol, String name, BigInteger totalSupply) {
      this.address = address;
      this.name = name;
      this.symbol = symbol;
      this.totalSupply = totalSupply;
    }


    @Override
    protected void generateTransaction(TransactionBuilder txBuilder) {
      List<PublicKey> signers = ImmutableList.of(getOurIdentity().getOwningKey());
      final Command<Commands> txCommand = new Command<>(Commands.CREATE, signers);
      txBuilder
          .addOutputState(tokenState, TokenContract.ID)
          .addOutputState(holdingState, TokenContract.ID)
          .addCommand(txCommand);
    }


    @Override
    protected void initialiseFlow(TransactionBuilder txBuilder) {
      Party me = getOurIdentity();
      tokenState = new TokenState(me, symbol, address, name, totalSupply);
      holdingState = new HoldingState(me, symbol, address, totalSupply, BigInteger.ZERO);
    }

  }

}
