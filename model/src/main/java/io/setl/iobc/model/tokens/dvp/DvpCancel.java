package io.setl.iobc.model.tokens.dvp;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public interface DvpCancel extends IobcDelegate<DvpCancel.Input, TransactionResult> {

  String NAME = "TOKENS.DVP.CANCEL";



  /** Input to the cancel operations. */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** The address whose permission to use to access the trade. Specify either this or {@link #symbol}. */
    String address;

    /** ID of the trade. */
    @NotEmpty
    String dvpId;

    /** The token whose controller is used to access the trade. Specify either this or {@link #address}. */
    String symbol;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TransactionInput.TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return address != null ? MessagePrincipals.forAddress(address) : MessagePrincipals.forToken(symbol);
    }


    @AssertTrue
    @JsonIgnore
    public boolean specifiesAddressOrSymbol() {
      boolean noAddress = (address == null || address.isEmpty());
      boolean noSymbol = (symbol == null || symbol.isEmpty());
      return noAddress ^ noSymbol;
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
