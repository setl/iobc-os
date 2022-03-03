package io.setl.iobc.corda.contracts;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import io.setl.iobc.corda.states.HoldingState;
import io.setl.iobc.corda.states.TokenState;

/**
 * Token contract for Corda. It supports commands to Add and Update the Token.
 *
 * @author Elendu Uche.
 */
public class TokenContract implements Contract {

  // This is used to identify our contract when building a transaction.
  public static final String ID = TokenContract.class.getName();

  private static final String ID_SEPARATOR = "␞";



  // Used to indicate the transaction's intent.
  public enum Commands implements CommandData {
    BURN,
    CREATE,
    MINT,
    TRANSFER
  }


  /**
   * Build an external ID from the provided elements.
   *
   * @param args the elements of the ID
   *
   * @return the external ID
   */
  public static String externalId(String... args) {
    StringBuilder builder = new StringBuilder(args[0]);
    for (int i = 1; i < args.length; i++) {
      builder.append(ID_SEPARATOR).append(args[i]);
    }
    return builder.toString();
  }


  /**
   * Build an external ID from the provided elements.
   *
   * @param party the party which is the first part of the ID
   * @param args  the additional elements of the ID
   *
   * @return the external ID
   */
  public static String externalId(Party party, String... args) {
    StringBuilder builder = new StringBuilder(party.getName().toString());
    for (int i = 0; i < args.length; i++) {
      builder.append(ID_SEPARATOR).append(args[i]);
    }
    return builder.toString();
  }


  // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
  // does not throw an exception.
  @Override
  public void verify(LedgerTransaction tx) {
    final CommandWithParties<TokenContract.Commands> command = requireSingleCommand(tx.getCommands(), TokenContract.Commands.class);
    TokenContract.Commands cmdData = command.getValue();

    if (cmdData.equals(Commands.CREATE)) {
      verifyCreate(tx);
    } else if (cmdData.equals(Commands.MINT)) {
      verifyMint(tx);
    } else if (cmdData.equals(Commands.BURN)) {
      verifyBurn(tx);
    } else if (cmdData.equals(Commands.TRANSFER)) {
      verifyTransfer(tx);
    } else {
      throw new UnsupportedOperationException(String.format("Unrecognised command: %s", cmdData));
    }
  }


  private void verifyBurn(@NotNull LedgerTransaction tx) {
    final List<TokenState> tokensIn = tx.inputsOfType(TokenState.class);
    final List<TokenState> tokensOut = tx.outputsOfType(TokenState.class);
    final List<HoldingState> holdingsIn = tx.inputsOfType(HoldingState.class);
    final List<HoldingState> holdingsOut = tx.outputsOfType(HoldingState.class);

    requireThat(check -> {
      check.using("Must be one or two input states", tx.getOutputs().size() == tokensOut.size() + holdingsOut.size());
      check.using("Must be two input states", tx.getInputs().size() == 2);

      check.using("Must be one input token", tokensIn.size() == 1);
      check.using("Must be one output token", tokensOut.size() == 1);
      check.using("Must no more than one output holding", holdingsOut.size() <= 1);
      check.using("Must be one input holding", holdingsIn.size() == 1);

      TokenState tokenIn = tokensIn.get(0);
      TokenState tokenOut = tokensOut.get(0);
      check.using("Token must not change ID", tokenIn.getExternalId().equals(tokenOut.getExternalId()));

      BigInteger amount = tokenIn.getTotalSupply().subtract(tokenOut.getTotalSupply());
      check.using("Total supply must decrease", amount.signum() == 1);

      HoldingState holdingIn = holdingsIn.get(0);
      HoldingState holdingOut = holdingsOut.isEmpty() ? null : holdingsOut.get(0);
      BigInteger amountIn = holdingIn.getAmount();
      BigInteger amountOut;
      if (holdingOut != null) {
        amountOut = holdingOut.getAmount();
        check.using("Holding must not change ID", holdingIn.getExternalId().equals(holdingOut.getExternalId()));
      } else {
        amountOut = BigInteger.ZERO;
      }
      check.using(
          "Holding amount must decrease by amount burnt: " + amountIn + " - " + amount + " = " + amountOut,
          amountIn.subtract(amount).equals(amountOut)
      );
      return null;
    });
  }


  private void verifyCreate(@NotNull LedgerTransaction tx) {
    final List<TokenState> tokenStates = tx.outputsOfType(TokenState.class);
    final List<HoldingState> holdingStates = tx.outputsOfType(HoldingState.class);
    requireThat(check -> {
      check.using("No inputs should be consumed when adding a token", tx.getInputs().isEmpty());
      check.using("There should be two output states.", tx.getOutputs().size() == 2);
      check.using("There should be one token output.", tokenStates.size() == 1);
      check.using("There should be one holding output", holdingStates.size() == 1);

      TokenState tokenState = tokenStates.get(0);
      HoldingState holdingState = holdingStates.get(0);
      check.using("Issuer must not be null", tokenState.getCordaNode() != null);
      check.using("Same node must issue both states", tokenState.getCordaNode().toString().equals(holdingState.getCordaNode().toString()));
      check.using("Total supply must not be less than 0", tokenState.getTotalSupply().signum() != -1);
      check.using("Supply must be held by controller.", tokenState.getTotalSupply().equals(holdingState.getAmount()));
      check.using(
          "Token Controller and Holding state owner are the same",
          holdingState.getExternalId().equals(externalId(tokenState.getExternalId(), tokenState.getController()))
      );
      return null;
    });
  }


  private void verifyMint(@NotNull LedgerTransaction tx) {
    final List<TokenState> tokensIn = tx.inputsOfType(TokenState.class);
    final List<TokenState> tokensOut = tx.outputsOfType(TokenState.class);
    final List<HoldingState> holdingsIn = tx.inputsOfType(HoldingState.class);
    final List<HoldingState> holdingsOut = tx.outputsOfType(HoldingState.class);

    requireThat(check -> {
      check.using("Must be one or two input states", tx.getInputs().size() == tokensIn.size() + holdingsIn.size());
      check.using("Must be two output states", tx.getOutputs().size() == 2);
      check.using("Must be one input token", tokensIn.size() == 1);
      check.using("Must be one output token", tokensOut.size() == 1);
      check.using("Must no more than one input holding", holdingsIn.size() <= 1);
      check.using("Must be one output holding", holdingsOut.size() == 1);

      TokenState tokenIn = tokensIn.get(0);
      TokenState tokenOut = tokensOut.get(0);
      check.using("Token must not change ID", tokenIn.getExternalId().equals(tokenOut.getExternalId()));

      BigInteger amount = tokenOut.getTotalSupply().subtract(tokenIn.getTotalSupply());
      check.using("Total supply must increase", amount.signum() == 1);

      HoldingState holdingIn = (holdingsIn.isEmpty()) ? null : holdingsIn.get(0);
      HoldingState holdingOut = holdingsOut.get(0);
      if (holdingIn != null) {
        check.using("Holding must not change ID", holdingIn.getExternalId().equals(holdingOut.getExternalId()));
        check.using("Holding amount must increase by amount minted", holdingIn.getAmount().add(amount).equals(holdingOut.getAmount()));
      } else {
        check.using("Holding amount must increase by amount minted", amount.equals(holdingOut.getAmount()));
      }
      return null;
    });
  }


  private void verifyTransfer(@NotNull LedgerTransaction tx) {
    final List<HoldingState> holdingsIn = tx.inputsOfType(HoldingState.class);
    final List<HoldingState> holdingsOut = tx.outputsOfType(HoldingState.class);

    requireThat(check -> {
      check.using("Only holdings for input states", tx.getInputs().size() == holdingsIn.size());
      check.using("Only holdings for output states", tx.getOutputs().size() == holdingsOut.size());
      check.using("One or two input holdings", holdingsIn.size() == 1 || holdingsIn.size() == 2);
      check.using("One or two output holdings", holdingsOut.size() == 1 || holdingsOut.size() == 2);

      Optional<HoldingState> inA = Optional.of(holdingsIn.get(0));
      Optional<HoldingState> inB = holdingsIn.size() == 2 ? Optional.of(holdingsIn.get(1)) : Optional.empty();

      Optional<HoldingState> outA = Optional.of(holdingsOut.get(0));
      Optional<HoldingState> outB = holdingsOut.size() == 2 ? Optional.of(holdingsOut.get(1)) : Optional.empty();

      // Out states may be the wrong way around
      if (!inA.get().getExternalId().equals(outA.get().getExternalId())) {
        Optional<HoldingState> tmp = outA;
        outA = outB;
        outB = tmp;
      }

      BigInteger amountInA = inA.map(HoldingState::getAmount).orElse(BigInteger.ZERO);
      BigInteger amountInB = inB.map(HoldingState::getAmount).orElse(BigInteger.ZERO);
      BigInteger amountOutA = outA.map(HoldingState::getAmount).orElse(BigInteger.ZERO);
      BigInteger amountOutB = outB.map(HoldingState::getAmount).orElse(BigInteger.ZERO);

      BigInteger changeA = amountOutA.subtract(amountInA);
      BigInteger changeB = amountOutB.subtract(amountInB);

      check.using("Output balance for A must be non-negative", amountOutA.signum() != -1);
      check.using("Output balance for B must be non-negative", amountOutB.signum() != -1);

      check.using(
          "Accounts must balance: A="
              + amountInA + " -> " + amountOutA + " (" + changeA + "), B="
              + amountInB + " -> " + amountOutB + " (" + changeB + ")",
          changeA.equals(changeB.negate())
      );
      return null;
    });
  }

}
