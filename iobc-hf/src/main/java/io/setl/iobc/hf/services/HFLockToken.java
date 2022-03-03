package io.setl.iobc.hf.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.LockToken;

@Service("hfLockToken")
@Slf4j
public class HFLockToken implements LockToken {

  private static final String FUNC_NAME = "LockToken";


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, LockToken.Input input) throws ParameterisedException {
    throw new UnsupportedOperationException();
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.FABRIC;
  }

}
