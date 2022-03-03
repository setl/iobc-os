package io.setl.iobc.corda.services;

import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import net.corda.core.messaging.FlowProgressHandle;
import net.corda.core.transactions.SignedTransaction;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.corda.flows.TokenAddFlow;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;
import io.setl.iobc.model.tokens.CreateToken;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.json.CJObject;
import io.setl.json.Canonical;

@Service("cordaCreateToken")
@Slf4j
public class CordaCreateToken implements CreateToken {

  private class Finisher implements Consumer<TransactionResult> {

    private final TokenSpecification specification;


    public Finisher(TokenSpecification specification) {
      this.specification = specification;
    }


    @Override
    public void accept(TransactionResult result) {
      if (result.getTxStatus() == TxStatus.SUCCESS) {
        try {
          tokenTable.insertToken(specification.getSymbol(), specification);
        } catch (ParameterisedException e) {
          log.error("Failed to record new token definition", e);
        }
      } else if (result.getContinuation() != null) {
        result.getContinuation().thenAccept(this);
      }
    }

  }



  private final TokenTable tokenTable;


  public CordaCreateToken(TokenTable tokenTable) {
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    final NodeConnection corda = (NodeConnection) configuration;

    String tokenId = input.getSymbol();
    TokenSpecification spec = tokenTable.getTokenSpecification(tokenId);
    if (spec != null) {
      CJObject cjObject = new CJObject();
      cjObject.put("token", tokenId);
      cjObject.put("owner", spec.getController());
      cjObject.put("createdAt", spec.getCreateTime().toString());
      cjObject.put("httpStatus", 409);
      throw new ParameterisedException("The token " + tokenId + " has already been created.", "iobc:token-exists", cjObject);
    }

    CJObject cjObject = (CJObject) Canonical.cast(input.getParameters());
    BigInteger supply = cjObject.getBigInteger("supply", BigInteger.ZERO);

    try {
      final FlowProgressHandle<SignedTransaction> flow = corda.getProxy()
          .startTrackedFlowDynamic(
              TokenAddFlow.Initiator.class,
              input.getController(),
              tokenId,
              input.getName(),
              supply
          );

      CompletableFuture<TransactionResult> result = NodeConnection.toFuture(flow, input.getTxProcessingMode());

      TokenSpecification specification = TokenSpecification.builder()
          .brand(ChainBrand.CORDA)
          .createTime(Instant.now())
          .loading(false)
          .chainId(corda.getIobcId())
          .symbol(input.getSymbol())
          .name(input.getName())
          .controller(input.getController())
          .build();

      result.thenAccept(new Finisher(specification));

      return result;
    } catch (Throwable ex) {
      log.error("Internal failure of CordaCreateToken", ex);
      throw ExceptionTranslator.convert(ex);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.CORDA;
  }

}
