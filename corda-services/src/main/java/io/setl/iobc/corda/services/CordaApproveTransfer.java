package io.setl.iobc.corda.services;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.ApproveTransfer;

@Service
public class CordaApproveTransfer implements ApproveTransfer {

  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    throw new UnsupportedOperationException();
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.CORDA;
  }

}
