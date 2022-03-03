package io.setl.iobc.model.tokens.dvp;

import java.util.Base64;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import io.setl.crypto.provider.SetlProvider;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.TransactionInput;
import io.setl.iobc.model.TransactionResult;

/**
 * Create DVP trade.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
public interface DvpCreate extends IobcDelegate<DvpCreate.Input, TransactionResult> {

  String NAME = "TOKENS.DVP.CREATE";



  /** Input to the trade creation. */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** Should the creator commit immediately to the trade?. */
    @Default
    boolean autoCommit = false;

    /** Unique ID of the DVP trade. */
    @NotEmpty
    String dvpId;

    /** First party to the trade. */
    @NotNull @Valid
    Party myParty;

    /** Second party to the trade. */
    @NotNull @Valid
    Party otherParty;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TransactionInput.TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forAddress(myParty.getAddress());
    }

  }


  /**
   * Create a random ID.
   */
  static String generateRandomId() {
    byte[] source = new byte[33];
    SetlProvider.getSecureRandom().nextBytes(source);
    return Base64.getEncoder().encodeToString(source);
  }


  @Override
  default String getType() {
    return NAME;
  }

}
