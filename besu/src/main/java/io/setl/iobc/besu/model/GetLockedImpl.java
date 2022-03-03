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
import io.setl.iobc.model.tokens.GetLocked;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.ILockable;

/**
 * Get a balance.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class GetLockedImpl implements GetLocked {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public GetLockedImpl(
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

    // Check the token does exist
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    // Call the contract
    ILockable bnyToken = ILockable.load(spec.getContract(), besu.getWeb3j(), credentials, FreeGasProvider.INSTANCE);
    RemoteFunctionCall<BigInteger> call = bnyToken.locked(credentials.getAddress());
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
