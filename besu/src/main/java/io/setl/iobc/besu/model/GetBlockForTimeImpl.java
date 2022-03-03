package io.setl.iobc.besu.model;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlock;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.tx.GetBlockForTime;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.BlockForTimeHelper;
import io.setl.iobc.util.BlockForTimeHelper.BlockTime;
import io.setl.json.CJObject;

/**
 * Implementation of the "get for time" delegate.
 *
 * @author Simon Greatrix on 25/11/2021.
 */
@Service
public class GetBlockForTimeImpl implements GetBlockForTime {

  private static class BesuTimeFetcher implements BlockForTimeHelper.TimeFetcher {

    private final Web3j web3j;


    public BesuTimeFetcher(Web3j web3j) {
      this.web3j = web3j;
    }


    private BlockTime getBlockTime(DefaultBlockParameter param) throws ParameterisedException {
      Request<?, EthBlock> request = web3j.ethGetBlockByNumber(param, false);
      EthBlock response;
      try {
        response = request.send();
      } catch (IOException e) {
        CJObject cjObject = new CJObject();
        cjObject.put("operation", "ethGetBlockByNumber");
        cjObject.put("parameter", param.getValue());
        throw new ParameterisedException("Web3j IO Exception", "iobc:besu-web3j-io", cjObject, e);
      }

      if (response.hasError()) {
        Response.Error error = response.getError();
        CJObject cjObject = new CJObject();
        cjObject.put("operation", "ethGetBlockByNumber");
        cjObject.put("parameter", param.getValue());
        cjObject.put("code", error.getCode());
        cjObject.put("message", error.getMessage());
        cjObject.put("data", error.getData());
        throw new ParameterisedException("Web3J JSON-RPC failure: " + error.getMessage(), "iobc:besu-web3j-rpc", cjObject);
      }

      EthBlock.Block block = response.getBlock();
      return new BlockTime(block.getNumber().longValue(), block.getTimestamp().longValue());
    }


    @Override
    public long getEpochTime(long blockNumber) throws ParameterisedException {
      return getBlockTime(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber))).getEpochTime();
    }


    @Override
    public BlockTime getLatestBlock() throws ParameterisedException {
      return getBlockTime(DefaultBlockParameterName.LATEST);
    }

  }



  private final BlockForTimeHelper blockForTimeHelper;

  private final TokenTable tokenTable;


  /**
   * New instance.
   */
  @Autowired
  public GetBlockForTimeImpl(BlockForTimeHelper blockForTimeHelper, TokenTable tokenTable) {
    this.blockForTimeHelper = blockForTimeHelper;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;
    BlockForTimeHelper.TimeFetcher timeFetcher = new BesuTimeFetcher(besu.getWeb3j());

    // Check the token does exist
    String tokenId = input.getSymbol();
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    long horizonBlock = Math.max(1, spec.getCreationBlock());
    long epochSecond = input.getEffectiveEpochSecond();
    long blockNumber = blockForTimeHelper.findBlock(horizonBlock, epochSecond, timeFetcher);

    Output output = Output.builder().blockNumber(BigInteger.valueOf(blockNumber)).build();
    return CompletableFuture.completedFuture(output);
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
