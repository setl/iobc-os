package io.setl.iobc.besu.model;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteFunctionCall;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.FreeGasProvider;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.GetBalance;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.IERC20;

/**
 * Get a balance.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class GetBalanceImpl implements GetBalance {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public GetBalanceImpl(
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    this.addressTable = addressTable;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    String tokenId = input.getSymbol();
    String address = input.getAddress();
    log.info("Checking balance of token {} for {}", tokenId, address);

    // Check address exists
    SetlAddress setlAddress = addressTable.getAddressSafe(address);
    Credentials credentials = Web3KeyConversion.convert(setlAddress);

    // Check the token does not already exist
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    // Load the contract API
    IERC20 bnyToken = IERC20.load(spec.getContract(), besu.getWeb3j(), credentials, FreeGasProvider.INSTANCE);

    // Are we using a specific block?
    if (input.getBlock() != -1) {
      bnyToken.setDefaultBlockParameter(new DefaultBlockParameterNumber(input.getBlock()));
    }

    // Create the call to Ethereum
    RemoteFunctionCall<BigInteger> call = bnyToken.balanceOf(credentials.getAddress());
    try {
      return CompletableFuture.completedFuture(Output.builder().amount(call.send()).build());
    } catch (Exception e) {
      log.error("Failed to fetch balance of {} for {}", tokenId, address, e);
      throw ExceptionTranslator.convert(e);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
