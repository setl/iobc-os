package io.setl.iobc.delegate;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.tokens.GetName;
import io.setl.iobc.table.TokenTable;

/**
 * Implementation of a delegate to get a token's name.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Service("besuGetName")
public class GetNameImpl implements GetName {

  private final TokenTable tokenTable;


  @Autowired
  public GetNameImpl(TokenTable tokenTable) {
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, TokenId input) throws ParameterisedException {
    String tokenId = input.getSymbol();
    TokenSpecification spec = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, spec);

    return CompletableFuture.completedFuture(
        Output.builder()
            .name(spec.getName())
            .build());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.NONE;
  }

}
