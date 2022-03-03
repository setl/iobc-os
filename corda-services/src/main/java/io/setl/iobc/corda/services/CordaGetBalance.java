package io.setl.iobc.corda.services;

import static io.setl.iobc.corda.contracts.TokenContract.externalId;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.corda.states.HoldingState;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.tokens.GetBalance;
import io.setl.iobc.util.ExceptionTranslator;

@Service("cordaGetBalance")
@Slf4j
public class CordaGetBalance implements GetBalance {


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    NodeConnection corda = (NodeConnection) configuration;
    try {
      final String holdingId = externalId(corda.getParty(), input.getSymbol(), input.getAddress());
      Optional<HoldingState> holding = corda.getStateOptional(holdingId, HoldingState.class);
      GetBalance.Output output = GetBalance.Output.builder().amount(holding.map(HoldingState::getAmount).orElse(BigInteger.ZERO)).build();
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
