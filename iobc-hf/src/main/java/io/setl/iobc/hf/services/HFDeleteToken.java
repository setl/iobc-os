package io.setl.iobc.hf.services;

import java.util.concurrent.CompletableFuture;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.DeleteToken;

public class HFDeleteToken implements DeleteToken {

  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    throw new UnsupportedOperationException();
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.FABRIC;
  }

}
