package io.setl.iobc.corda.services;

import static io.setl.iobc.corda.contracts.TokenContract.externalId;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import net.corda.core.identity.Party;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.corda.states.HoldingState;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.tokens.GetLocked;
import io.setl.iobc.util.ExceptionTranslator;

@Service("cordaGetLocked")
@Slf4j
public class CordaGetLocked implements GetLocked {


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    NodeConnection corda = (NodeConnection) configuration;
    try {
      final String holdingId = externalId(corda.getParty(), input.getSymbol(), input.getAddress());

      HoldingState holding = corda.getStateValue(holdingId, HoldingState.class);

      GetLocked.Output output = GetLocked.Output.builder().amount(holding.getLocked()).build();
      return CompletableFuture.completedFuture(output);
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
