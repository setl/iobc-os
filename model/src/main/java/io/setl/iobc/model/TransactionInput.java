package io.setl.iobc.model;

/**
 * Standard input to a transaction submission. This ensures that the processing mode can be set for any transaction input.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface TransactionInput extends MessageInput {

  /**
   * Possible processing modes for transactions.
   */
  enum TxProcessingMode {
    /** Return the result of the transaction. */
    RETURN_RESULT,

    /** Return the transaction's hash immediately, followed by the result when it is known. */
    RETURN_ID
  }


  TransactionInput withTxProcessingMode(TxProcessingMode mode);

}
