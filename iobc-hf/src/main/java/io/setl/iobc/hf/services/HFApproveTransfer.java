package io.setl.iobc.hf.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.ApproveTransfer;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

@Service("hfApproveTransfer")
@Slf4j
public class HFApproveTransfer implements ApproveTransfer {

  private static final String FUNC_NAME = "Approve";

  private final AddressTable addressTable;


  public HFApproveTransfer(AddressTable addressTable) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, ApproveTransfer.Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;

    String ownerId = addressTable.getAddressSafe(input.getOwner()).getChainAddress();
    String spenderId = addressTable.getAddressSafe(input.getSpender()).getChainAddress();

    try {
      return hfClientService.executeTransaction(
          input.getTxProcessingMode(),
          ownerId,
          FUNC_NAME,
          input.getSymbol(),
          spenderId,
          input.getAmount().toString()
      );
    } catch (Exception e) {
      throw ExceptionTranslator.convert(e);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.FABRIC;
  }

}
