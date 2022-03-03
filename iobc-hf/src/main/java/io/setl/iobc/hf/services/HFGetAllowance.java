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
import io.setl.iobc.model.tokens.GetAllowance;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

@Service("hfGetAllowance")
@Slf4j
public class HFGetAllowance implements GetAllowance {

  private static final String FUNC_NAME = "GetAllowance";

  private final AddressTable addressTable;


  public HFGetAllowance(AddressTable addressTable) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<GetAllowance.Output> apply(ChainConfiguration configuration, GetAllowance.Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;

    String ownerId = addressTable.getAddressSafe(input.getOwner()).getChainAddress();
    String spenderId = addressTable.getAddressSafe(input.getSpender()).getChainAddress();

    try {
      String[] contractArgs = {ownerId, spenderId, input.getSymbol()};
      byte[] result = hfClientService.queryTransaction(input.getSpender(), FUNC_NAME, contractArgs);
      String balStr = new String(result, StandardCharsets.UTF_8);
      GetAllowance.Output output = GetAllowance.Output.builder().amount(new BigInteger(balStr)).build();
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
