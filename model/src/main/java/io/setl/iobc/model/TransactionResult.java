package io.setl.iobc.model;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * A response to a request that triggers a transaction.
 *
 * @author Simon Greatrix on 16/11/2021.
 */
@Data
@Builder
@Jacksonized
public class TransactionResult implements ContinuedMessage<TransactionResult> {

  /** Possible statuses for a transaction. */
  public enum TxStatus {
    /** The transaction succeeded. */
    SUCCESS,

    /** The transaction failed. */
    FAILURE,

    /** The transaction has been submitted to the block chain, but the result is not known yet. */
    PENDING,

    /** The transaction status could not be checked for some reason. See the additional data for more information. */
    UNKNOWN
  }



  /** Additional data associated with the transaction result. */
  @Default
  @Schema(description = "Additional data, if available", ref = "#/components/schemas/unstructured.data")
  JsonObject additionalData = JsonValue.EMPTY_JSON_OBJECT;

  /** The block the transaction is in. For pending, the transaction will appear in a block after this number. */
  @Schema(description = "The block the transaction is in, or if the transaction is still pending then the earliest block it could appear in.")
  BigInteger blockNumber;

  /** A continuation, if sending hash only initially. */
  @JsonIgnore
  @Hidden
  CompletableFuture<TransactionResult> continuation;

  /** Get the transaction's unique ID, such as its hash. */
  @Schema(description = "The transaction's unique ID, such as its hash.")
  String transactionId;

  /** Did the transaction succeed or fail?. */
  @Schema(description = "The transactions status, if known")
  TxStatus txStatus;

  /** Did the transaction return an additional message. **/
  @JsonInclude(Include.NON_EMPTY)
  String message;
}
