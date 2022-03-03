package io.setl.iobc.delegate;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.tokens.GetAllTokens;
import io.setl.iobc.table.TokenTable;

/**
 * Get all the tokens known to IOBC.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Service("besuGetAllTokens")
public class GetAllTokensImpl implements GetAllTokens {

  private final TokenTable tokenTable;


  @Autowired
  public GetAllTokensImpl(TokenTable tokenTable) {
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, MessageInput input) throws ParameterisedException {
    return CompletableFuture.completedFuture(
        Output.builder()
            .tokens(tokenTable.getAllTokens())
            .build());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.NONE;
  }

}
