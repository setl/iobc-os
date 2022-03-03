package io.setl.iobc.model.tx;

import java.math.BigInteger;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.TransactionResult;

/**
 * Get the result of a transaction by its hash.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface GetTransactionResult extends IobcDelegate<GetTransactionResult.Input, TransactionResult> {

  String NAME = "TX.GET_TRANSACTION_RESULT";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    /** The minimum block number that may contain the transaction. (Optional) */
    @Min(0)
    @Default
    BigInteger blockNumber = BigInteger.ZERO;

    /** The symbol and primary identifier of the token. */
    @NotEmpty
    String transactionId;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forPublic();
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
