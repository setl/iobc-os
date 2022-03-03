package io.setl.iobc.model.address;

import java.util.Set;

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
 * Delegate to get the contents of a wallet.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public interface GetWallet extends IobcDelegate<GetWallet.Input, GetWallet.Output> {

  String NAME = "ADDRESS.GET_WALLET";



  /**
   * Specification of a wallet ID.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    @Min(NewAddress.WALLET_ID_MINIMUM)
    int walletId;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forWallet(getWalletId());
    }

  }



  /** Tuple linking a wallet ID to a set of addresses. */
  @Builder
  @Value
  @Jacksonized
  @Schema(
      name = "GetWallet_Output",
      description = "The result of a request for the contents of a wallet."
  )
  class Output implements MessageContent {

    @Schema(description = "The addresses contained in the wallet.", required = true)
    Set<String> addresses;

    @Schema(description = "The wallet's ID.", required = true)
    int walletId;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
