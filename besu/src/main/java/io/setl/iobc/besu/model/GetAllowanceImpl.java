package io.setl.iobc.besu.model;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
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
import io.setl.iobc.model.tokens.GetAllowance;
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
public class GetAllowanceImpl implements GetAllowance {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public GetAllowanceImpl(
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
    String owner = input.getOwner();
    String spender = input.getSpender();
    log.info("Checking allowance of token {} for {} spent by {}", tokenId, owner, spender);

    // Check address exists
    SetlAddress setlAddress = addressTable.getAddressSafe(owner);
    Credentials credentials = Web3KeyConversion.convert(setlAddress);

    // Get owner's address
    setlAddress = addressTable.getAddressSafe(spender);
    String spenderAddress = Web3KeyConversion.convert(setlAddress).getAddress();

    // Get the token specification
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    // Call the contract
    IERC20 bnyToken = IERC20.load(spec.getContract(), besu.getWeb3j(), credentials, FreeGasProvider.INSTANCE);
    RemoteFunctionCall<BigInteger> call = bnyToken.allowance(credentials.getAddress(), spenderAddress);
    try {
      return CompletableFuture.completedFuture(Output.builder().amount(call.send()).build());
    } catch (Exception e) {
      log.error("Failed to fetch allowance of {} for {}", tokenId, spender, e);
      throw ExceptionTranslator.convert(e);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
