package io.setl.iobc.corda.services;

import java.util.concurrent.CompletableFuture;

import net.corda.core.messaging.FlowProgressHandle;
import net.corda.core.transactions.SignedTransaction;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.corda.flows.TransferFlow;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.TransferToken;
import io.setl.iobc.util.ExceptionTranslator;

@Service("cordaTransfer")
public class CordaTransferToken implements TransferToken {


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    NodeConnection corda = (NodeConnection) configuration;
    try {
      FlowProgressHandle<SignedTransaction> flow = corda.getProxy()
          .startTrackedFlowDynamic(TransferFlow.Initiator.class, input.getSymbol(), input.getAmount(), input.getFrom(), input.getTo());
      return NodeConnection.toFuture(flow, input.getTxProcessingMode());
    } catch (Throwable ex) {
      ex.printStackTrace();
      throw ExceptionTranslator.convert(ex);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.CORDA;
  }

}
