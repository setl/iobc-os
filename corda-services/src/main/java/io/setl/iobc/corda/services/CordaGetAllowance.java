package io.setl.iobc.corda.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import net.corda.core.identity.Party;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.states.AllowanceState;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.tokens.GetAllowance;
import io.setl.iobc.util.ExceptionTranslator;

@Service("cordaGetAllowance")
@Slf4j
public class CordaGetAllowance implements GetAllowance {

  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    NodeConnection corda = (NodeConnection) configuration;
    try {
      final Party owner = corda.getX500Name(input.getOwner());
      final Party spender = corda.getX500Name(input.getSpender());
      final String ownerKey = owner.getName().toString();
      final String spenderKey = spender.getName().toString();
      final String allowanceId = String.format("%s␞%s␞%s", ownerKey, spenderKey, input.getSymbol());

      AllowanceState allowance = corda.getStateValue(allowanceId, AllowanceState.class);

      Output output = Output.builder().amount(allowance.getAmount()).build();
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
