package io.setl.iobc.model.address;

import java.util.function.Function;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipal;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.NewAddress.Input;
import io.setl.iobc.model.address.NewAddress.Output;

/**
 * Create a new address.
 *
 * @author Simon Greatrix on 17/11/2021.
 */
public interface NewAddress extends IobcDelegate<Input, Output> {

  String NAME = "ADDRESS.NEW";

  /** The minimum allowed wallet ID. Wallet IDs below this value are reserved. (Note: this value is <strong>NEGATIVE</strong>) */
  int WALLET_ID_MINIMUM = 0x8000_0100;

  /** A magic wallet ID used to indicate that in this instance no wallet ID is applicable. */
  int WALLET_ID_NOT_APPLICABLE = 0x8000_0000;



  /** Input specify the wallet ID where the address should be created. */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    String chainId;

    /** Parameters to the address creation. */
    @Default
    @NotNull
    JsonObject parameters = JsonValue.EMPTY_JSON_OBJECT;

    @Min(WALLET_ID_MINIMUM)
    int walletId;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return new MessagePrincipals(new MessagePrincipal() {
        @Override
        public void resolve(
            Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction
        ) {
          chainId = Input.this.chainId;
          walletId = Input.this.walletId;
        }
      });
    }

  }



  /** Output, specifying the SETL address created. */
  @Builder
  @Value
  @Jacksonized
  class Output implements MessageContent {

    String address;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
