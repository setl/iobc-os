package io.setl.iobc.hf.services;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.tokens.GetTotalSupply;
import io.setl.iobc.util.ExceptionTranslator;

@Service("hfTotalSupply")
@Slf4j
public class HFGetTotalSupply implements GetTotalSupply {

  private static final String FUNC_NAME = "TotalSupply";


  @Override
  public CompletableFuture<GetTotalSupply.Output> apply(ChainConfiguration configuration, GetTotalSupply.Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;

    try {
      String[] contractArgs = {input.getSymbol()};
      byte[] result = hfClientService.queryTransaction("admin", FUNC_NAME, contractArgs);
      String supply = new String(result, StandardCharsets.UTF_8);
      GetTotalSupply.Output output = GetTotalSupply.Output.builder().amount(new BigInteger(supply)).build();
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
