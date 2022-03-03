package io.setl.iobc.model.tokens.dvp;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.MessageInput;

/** Input to a DVP transaction that just required the DVP ID. */
@Builder
@Value
@Jacksonized
@Schema(description = "The identity of a DvP trade and the identity used to access it.")
public class DvpId implements MessageInput {

  /** The address whose permission to use to access the trade. Specify either this or {@link #symbol}. */
  @Schema(description = "The address whose permission to use to access the trade. Specify either this or 'symbol'.")
  String address;

  /** ID of the trade. */
  @Schema(description = "The unique identifier for the DVP trade", required = true)
  @NotEmpty
  String dvpId;

  /** The token whose controller is used to access the trade. Specify either this or {@link #address}. */
  @Schema(description = "The token whose controller is used to access the trade. Specify either this or 'address'.")
  String symbol;


  @Override
  public MessagePrincipals resolvePrincipal() {
    return address != null ? MessagePrincipals.forAddress(address) : MessagePrincipals.forToken(symbol);
  }


  /** Verify that the input specifies either an addres or a symbol, but not both. */
  @AssertTrue
  @JsonIgnore
  public boolean specifiesAddressOrSymbol() {
    boolean noAddress = (address == null || address.isEmpty());
    boolean noSymbol = (symbol == null || symbol.isEmpty());
    return noAddress ^ noSymbol;
  }

}
