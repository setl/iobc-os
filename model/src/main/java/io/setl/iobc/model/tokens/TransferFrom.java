package io.setl.iobc.model.tokens;

import java.math.BigInteger;
import javax.validation.constraints.Min;
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
import io.setl.iobc.model.tokens.TransferFrom.Input;

/**
 * Transfer an amount of tokens from one address to another in the block-chain.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
public interface TransferFrom extends IobcDelegate<Input, TransactionResult> {

  String NAME = "TOKEN.TRANSFER_FROM";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** The address that performs the operation. */
    @NotEmpty
    String address;

    /** Amount to transfer. */
    @NotNull
    @Min(0)
    BigInteger amount;

    /** The address that supplies the tokens. */
    @NotEmpty
    String from;

    /** The symbol for the tokens that are transferred. */
    @NotEmpty
    String symbol;

    /** The address that receives the tokens. */
    @NotEmpty
    String to;

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
