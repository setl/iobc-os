package io.setl.iobc.besu.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.TransactionManager;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.FreeGasProvider;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.GetHoldings;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.ITokenExtensions;
import io.setl.iobc.web3j.ITokenExtensions.Balance;

/**
 * Get the holdings for a token.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class GetHoldingsImpl implements GetHoldings {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public GetHoldingsImpl(
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
    int start = input.getStart();
    int end = input.getEnd();
    long blockNumber = input.getBlock();

    log.info("Get Holdings for token \"{}\" from \"{}\" to \"{}\" at block {}", tokenId, start, end, blockNumber);

    // Get the token specification
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    // Get token owner's address
    SetlAddress setlAddress = addressTable.getAddressSafe(token.getController());
    Credentials ownerCredentials = Web3KeyConversion.convert(setlAddress);

    // Load the contract
    TransactionManager manager = besu.getManager(ownerCredentials);
    ITokenExtensions bnyToken = ITokenExtensions.load(spec.getContract(), besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);

    // Are we using a specific block?
    if (input.getBlock() != -1) {
      bnyToken.setDefaultBlockParameter(new DefaultBlockParameterNumber(input.getBlock()));
    }

    // Call the contract
    RemoteFunctionCall<Tuple4<List<Balance>, BigInteger, BigInteger, BigInteger>> call = bnyToken.holdings(BigInteger.valueOf(start), BigInteger.valueOf(end));
    Tuple4<List<Balance>, BigInteger, BigInteger, BigInteger> tuple;
    try {
      tuple = call.send();
    } catch (Exception e) {
      // Failed to send transfer request
      log.error("Failed to invoke holdings on smart contract {} for token {}",
          spec.getContract(), tokenId
      );
      throw ExceptionTranslator.convert(e);
    }

    Output.OutputBuilder builder = Output.builder()
        .controller(token.getController())
        .start(tuple.component2().intValue())
        .end(tuple.component3().intValue())
        .size(tuple.component4().intValue());

    String dvpId = besu.getDvpManager().getId();

    List<Balance> balances = tuple.component1();
    ArrayList<Holding> holdings = new ArrayList<>(balances.size());
    for (Balance balance : balances) {
      String address = addressTable.lookupAddress(balance._account);
      if (address == null) {
        if (balance._account.equals(dvpId)) {
          address = "[DVP Escrow]";
        } else {
          address = "[Internal: " + balance._account + "]";
        }
      }
      holdings.add(Holding.builder()
          .amount(balance._amount)
          .address(address)
          .build());
    }
    builder.holdings(holdings);

    return CompletableFuture.completedFuture(builder.build());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }


}
