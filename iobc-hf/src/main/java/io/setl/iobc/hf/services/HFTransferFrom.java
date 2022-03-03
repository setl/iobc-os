package io.setl.iobc.hf.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.TransferFrom;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

@Service("hfTransferFrom")
@Slf4j
public class HFTransferFrom implements TransferFrom {

  private static final String funcName = "TransferFrom";

  private final AddressTable addressTable;


  public HFTransferFrom(AddressTable addressTable) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, TransferFrom.Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;

    String fromId = addressTable.getAddressSafe(input.getFrom()).getChainAddress();
    String toId = addressTable.getAddressSafe(input.getTo()).getChainAddress();
    try {
      return hfClientService.executeTransaction(input.getTxProcessingMode(), input.getAddress(),
          funcName, input.getSymbol(), fromId, toId, input.getAmount().toString()
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
