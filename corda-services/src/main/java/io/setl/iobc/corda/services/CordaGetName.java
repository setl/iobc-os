package io.setl.iobc.corda.services;

import static io.setl.iobc.corda.contracts.TokenContract.externalId;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.states.TokenState;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.tokens.GetName;
import io.setl.iobc.util.ExceptionTranslator;

@Service("cordaGetName")
@Slf4j
public class CordaGetName implements GetName {


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, TokenId input) throws ParameterisedException {
    NodeConnection corda = (NodeConnection) configuration;
    try {
      final String tokenId = externalId(corda.getParty(), input.getSymbol());
      TokenState token = corda.getStateValue(tokenId, TokenState.class);
      GetName.Output output = GetName.Output.builder().name(token.getName()).build();
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
