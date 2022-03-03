package io.setl.iobc.hf.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.TransferToken;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

@Service("hfTransfer")
@Slf4j
public class HFTransferToken implements TransferToken {

  private static final String FUNC_NAME = "Transfer";

  private final AddressTable addressTable;


  @Autowired
  public HFTransferToken(AddressTable addressTable) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, TransferToken.Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;
    String toId = addressTable.getAddressSafe(input.getTo()).getChainAddress();
    try {
      return hfClientService.executeTransaction(input.getTxProcessingMode(), input.getFrom(),
          FUNC_NAME, input.getSymbol(), toId, input.getAmount().toString()
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
