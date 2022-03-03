package io.setl.iobc.model.tokens.dvp;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.TransactionInput;
import io.setl.iobc.model.TransactionResult;

/**
 * Commit to a DVP trade as the controller of one of the tokens involved.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
public interface DvpControllerCommit extends IobcDelegate<DvpControllerCommit.Input, TransactionResult> {

  String NAME = "TOKENS.DVP.CONTROLLER_COMMIT";



  /** Input to a DVP transaction that just required the DVP ID. */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** ID of the trade. */
    @NotEmpty
    String dvpId;

    /** If specified true, draw the tokens from the locked area. (Defaults to false) */
    @Default
    boolean fromLocked = false;

    /** The symbol which will commit. */
    @NotEmpty
    String symbol;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TransactionInput.TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forToken(symbol);
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
