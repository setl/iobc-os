package io.setl.iobc.besu.model;

import java.util.concurrent.CompletableFuture;
import javax.json.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.besu.tx.ContractCreatedHandler;
import io.setl.iobc.besu.tx.GetTransactionResultImpl;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tx.VerifyCreateToken;
import io.setl.iobc.table.TokenTable;
import io.setl.json.Canonical;

/**
 * Verify if the transaction that was supposed to create a token was successful.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Service
public class VerifyCreateTokenImpl implements VerifyCreateToken {

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public VerifyCreateTokenImpl(
      TokenTable tokenTable
  ) {
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<VerifyCreateToken.Output> apply(ChainConfiguration configuration, TokenId input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    String tokenId = input.getSymbol();
    VerifyCreateToken.Output.OutputBuilder builder = VerifyCreateToken.Output.builder().symbol(tokenId);

    TokenSpecification spec = tokenTable.getTokenSpecification(tokenId);
    if (spec == null) {
      builder.status(TokenStatus.UNKNOWN);
      return CompletableFuture.completedFuture(builder.build());
    }

    if (!spec.isLoading()) {
      builder.status(TokenStatus.SUCCESS);
      return CompletableFuture.completedFuture(builder.build());
    }

    // Need to check the transaction result.
    BesuTokenData besuSpec = new BesuTokenData(spec.getChainData());
    String txHash = besuSpec.getTransactionId();
    TransactionResult result = GetTransactionResultImpl.getResult(besu.getWeb3j(), txHash);
    builder.result(result);
    switch (result.getTxStatus()) {
      case FAILURE:
        builder.status(TokenStatus.FAILED);
        ContractCreatedHandler.markTokenAsBad(tokenTable, tokenId);
        break;
      case PENDING:
        builder.status(TokenStatus.IN_PROGRESS);
        break;
      case SUCCESS: {
        JsonObject data = result.getAdditionalData();
        String contractAddress = data.getString("contractAddress");
        if (contractAddress == null) {
          data.put("error", Canonical.cast("Contract address is required for token registration"));
          builder.status(TokenStatus.FAILED);
          ContractCreatedHandler.markTokenAsBad(tokenTable, tokenId);
        } else {
          builder.status(TokenStatus.SUCCESS);
          ContractCreatedHandler.markTokenAsGood(tokenTable, tokenId, spec, contractAddress, result.getBlockNumber().longValue());
        }
        break;
      }
      default:
        builder.status(TokenStatus.ERROR);
        break;
    }

    return CompletableFuture.completedFuture(builder.build());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
