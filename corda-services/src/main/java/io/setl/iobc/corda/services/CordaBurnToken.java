package io.setl.iobc.corda.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import net.corda.core.messaging.FlowProgressHandle;
import net.corda.core.transactions.SignedTransaction;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.corda.flows.TokenBurnFlow;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.BurnToken;
import io.setl.iobc.util.ExceptionTranslator;

@Service("cordaBurnToken")
@Slf4j
public class CordaBurnToken implements BurnToken {


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    NodeConnection corda = (NodeConnection) configuration;
    try {
      FlowProgressHandle<SignedTransaction> flow = corda.getProxy()
          .startTrackedFlowDynamic(TokenBurnFlow.Initiator.class, input.getSymbol(), input.getAmount(), input.getFrom());
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
