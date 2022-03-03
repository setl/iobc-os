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
import io.setl.iobc.model.tokens.MintToken.Input;

/**
 * Create a new amount of tokens from one address to another in the block-chain. Must be invoked by the token owner.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
public interface MintToken extends IobcDelegate<Input, TransactionResult> {

  String NAME = "TOKEN.MINT";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** The amount of tokens to create. */
    @NotNull
    @Min(0)
    BigInteger amount;

    /** The symbol of the token to create. */
    @NotEmpty
    String symbol;

    /** The address that receives the new tokens. */
    @NotEmpty
    String to;

    /** If specified and true, mint into locked tokens. */
    Boolean toLocked;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


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
