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
 * Commit to a DVP trade.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
public interface DvpCommit extends IobcDelegate<DvpCommit.Input, TransactionResult> {

  String NAME = "TOKENS.DVP.COMMIT";



  /** Input to a DVP transaction that just required the DVP ID. */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** The address which is committing to the trade. */
    @NotEmpty
    String address;

    /** ID of the trade. */
    @NotEmpty
    String dvpId;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TransactionInput.TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;

    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forAddress(address);
    }
  }


  @Override
  default String getType() {
    return NAME;
  }

}
