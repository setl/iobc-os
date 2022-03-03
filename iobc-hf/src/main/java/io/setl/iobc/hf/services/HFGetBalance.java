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
import io.setl.iobc.model.tokens.GetBalance;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

@Service("hfGetBalance")
@Slf4j
public class HFGetBalance implements GetBalance {

  private static final String FUNC_NAME = "ClientAccountBalance";


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;

    try {
      String[] contractArgs = {input.getSymbol()};
      byte[] result = hfClientService.queryTransaction(input.getAddress(), FUNC_NAME, contractArgs);
      String balStr = new String(result, StandardCharsets.UTF_8);
      Output output = Output.builder().amount(new BigInteger(balStr)).build();
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
