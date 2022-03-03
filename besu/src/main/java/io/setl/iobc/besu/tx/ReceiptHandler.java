package io.setl.iobc.besu.tx;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import io.setl.iobc.model.TransactionResult;

/**
 * A standard transaction receipt handler.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface ReceiptHandler extends Consumer<TransactionReceipt> {

  CompletableFuture<TransactionResult> getResult();

}
