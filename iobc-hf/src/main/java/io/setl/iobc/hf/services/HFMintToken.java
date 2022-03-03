package io.setl.iobc.hf.services;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.MintToken;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

/**
 * Mint a token on Hyperledger Fabric (HF).
 *
 * @author Elendu Uche
 */
@Service("hfMintToken")
@Slf4j
public class HFMintToken implements MintToken {

  private static final String FUNC_NAME = "Mint";

  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;

    try {
      return hfClientService.executeTransaction(input.getTxProcessingMode(), input.getTo(),
          FUNC_NAME, input.getSymbol(), input.getAmount().toString()
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
