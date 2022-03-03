package io.setl.iobc.hf.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.tokens.GetLocked;

@Service("hfGetLocked")
@Slf4j
public class HFGetLocked implements GetLocked {

  @Override
  public CompletableFuture<GetLocked.Output> apply(ChainConfiguration configuration, GetLocked.Input input) throws ParameterisedException {
    throw new UnsupportedOperationException();
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.FABRIC;
  }

}
