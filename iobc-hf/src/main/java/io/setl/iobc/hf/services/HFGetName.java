package io.setl.iobc.hf.services;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.tokens.GetName;
import io.setl.iobc.util.ExceptionTranslator;

@Service("hfGetName")
@Slf4j
public class HFGetName implements GetName {

  private static final String FUNC_NAME = "GetName";


  @Override
  public CompletableFuture<GetName.Output> apply(ChainConfiguration configuration, TokenId input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;
    try {
      String[] contractArgs = {input.getSymbol()};
      byte[] result = hfClientService.queryTransaction("admin", FUNC_NAME, contractArgs);
      String name = new String(result, StandardCharsets.UTF_8);
      GetName.Output output = GetName.Output.builder().name(name).build();
      return CompletableFuture.completedFuture(output);
    } catch (Exception e) {
      throw ExceptionTranslator.convert(e);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.FABRIC;
  }

}
