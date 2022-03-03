package io.setl.iobc.model.address;

import static io.setl.iobc.model.address.NewAddress.WALLET_ID_MINIMUM;

import java.util.Map;

import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;

/**
 * List all the addresses in a wallet.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public interface ListAddresses extends IobcDelegate<ListAddresses.Input, ListAddresses.Output> {

  String NAME = "ADDRESS.LIST_ADDRESSES";



  /** Input specifying a wallet ID. */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    @Min(WALLET_ID_MINIMUM)
    int walletId;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forWallet(getWalletId());
    }

  }



  /** Output specifying a wallet ID and the full details of all the addresses in it. */
  @Builder
  @Value
  @Jacksonized
  @Schema(
      name = "ListAddresses_Output",
      description = "Results of a request for the details of the addresses in a wallet"
  )
  class Output implements MessageContent {

    @Schema(description = "Map of SETL addresses to their full specifications.", required = true)
    Map<String, SetlAddress> addresses;

    @Schema(description = "The wallet's ID.", required = true)
    int walletId;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
