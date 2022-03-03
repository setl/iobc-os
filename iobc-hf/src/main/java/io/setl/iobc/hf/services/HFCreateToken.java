package io.setl.iobc.hf.services;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;
import io.setl.iobc.model.tokens.CreateToken;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.json.CJObject;

@Service("hfCreateToken")
@Slf4j
public class HFCreateToken implements CreateToken {

  private static final String FUNC_NAME = "InitContract";

  private static final Logger logger = LoggerFactory.getLogger(HFCreateToken.class);



  private class Finisher implements Consumer<TransactionResult> {

    private final TokenSpecification specification;


    public Finisher(TokenSpecification specification) {
      this.specification = specification;
    }


    @Override
    public void accept(TransactionResult result) {
      if (result.getTxStatus() == TxStatus.SUCCESS) {
        try {
          tokenTable.insertToken(specification.getSymbol(), specification);
        } catch (ParameterisedException e) {
          logger.error("Failed to record new token definition", e);
        }
      } else if (result.getContinuation() != null) {
        result.getContinuation().thenAccept(this);
      }
    }

  }

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public HFCreateToken(TokenTable tokenTable) {
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    HFClientService hfClientService = (HFClientService) configuration;

    String tokenId = input.getSymbol();
    TokenSpecification spec = tokenTable.getTokenSpecification(tokenId);
    if (spec != null) {
      CJObject cjObject = new CJObject();
      cjObject.put("token", tokenId);
      cjObject.put("owner", spec.getController());
      cjObject.put("createdAt", spec.getCreateTime().toString());
      cjObject.put("httpStatus", 409);
      throw new ParameterisedException("The token " + tokenId + " has already been created.", "iobc:token-exists", cjObject);
    }

    try {
      CompletableFuture<TransactionResult> result = hfClientService.executeTransaction(
          input.getTxProcessingMode(), input.getController(), FUNC_NAME, input.getName(), input.getSymbol(), "18");

      TokenSpecification specification = TokenSpecification.builder()
          .brand(ChainBrand.FABRIC)
          .chainId(hfClientService.getIobcId())
          .controller(input.getController())
          .createTime(Instant.now())
          .name(input.getName())
          .symbol(input.getSymbol())
          .loading(false)
          .build();

      result.thenAccept(new Finisher(specification));

      return result;
    } catch (Exception e) {
      throw ExceptionTranslator.convert(e);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.FABRIC;
  }

}
