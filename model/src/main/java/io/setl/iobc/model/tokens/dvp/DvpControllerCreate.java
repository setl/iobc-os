package io.setl.iobc.model.tokens.dvp;

import javax.validation.Valid;
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
 * Create a DVP trade as the controller of one of the tokens involved.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
public interface DvpControllerCreate extends IobcDelegate<DvpControllerCreate.Input, TransactionResult> {

  String NAME = "TOKENS.DVP.CONTROLLER_CREATE";



  /** Input to the trade creation. */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** Should the creator commit immediately to the trade?. (Defaults to false) */
    @Default
    boolean autoCommit = false;

    /** Unique ID of the DVP trade. */
    @NotEmpty
    String dvpId;

    /** If true, draw the tokens from the locked area. (Defaults to false) */
    @Default
    boolean fromLocked = false;

    /** The party associated with the controller. This is the party that gets auto-committed if requested. */
    @NotNull @Valid
    Party localParty;

    /** The other party to the trade. */
    @NotNull @Valid
    Party remoteParty;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TransactionInput.TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forToken(localParty.getSymbol());
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
