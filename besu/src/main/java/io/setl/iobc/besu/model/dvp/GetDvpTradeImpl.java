package io.setl.iobc.besu.model.dvp;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.TransactionManager;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.DvpManager;
import io.setl.iobc.besu.FreeGasProvider;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.dvp.DvpId;
import io.setl.iobc.model.tokens.dvp.GetDvpTrade;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.DVP;

/**
 * Implementation of DVP Controller create.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
@Service
@Slf4j
public class GetDvpTradeImpl implements GetDvpTrade {

  private static String na(String i) {
    return (i == null || i.isEmpty()) ? "N/A" : i;
  }


  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /** New instance. */
  @Autowired
  public GetDvpTradeImpl(
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    this.addressTable = addressTable;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<GetDvpTrade.Output> apply(ChainConfiguration configuration, DvpId input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    BigInteger dvpId = DvpManager.getInternalId(input.getDvpId());
    String dvpContract = besu.getDvpManager().getId();

    log.info("Fetching details of DVP trade {}", input.getDvpId());

    Credentials credentials;
    if (input.getSymbol() != null) {
      String tokenId = input.getSymbol();
      TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
      TokenSpecification.check(tokenId, token);
      SetlAddress setlAddress = addressTable.getAddressSafe(token.getController());
      credentials = Web3KeyConversion.convert(setlAddress);
    } else if (input.getAddress() != null) {
      SetlAddress setlAddress = addressTable.getAddressSafe(input.getAddress());
      credentials = Web3KeyConversion.convert(setlAddress);
    } else {
      throw new IllegalArgumentException("Input must specify token or address");
    }

    // Call the contract.
    TransactionManager manager = besu.getManager(credentials);
    DVP dvp = DVP.load(dvpContract, besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);

    // Workaround for https://github.com/web3j/web3j/issues/1503
    RemoteFunctionCall<DVP.TradeDetailsWorkaround> call = dvp.getTradeWorkaround(dvpId);

    DVP.TradeDetailsWorkaround trade;
    try {
      trade = call.send();
    } catch (Exception e) {
      // Failed to get trade details
      log.error("Failed to get trade details for {}", input.getDvpId(), e);
      throw ExceptionTranslator.convert(e);
    }

    GetDvpTrade.Output output = Output.builder()
        .dvpId(input.getDvpId())
        .exists(trade.exists)
        .party1(
            PartyDetails.builder()
                .address(na(trade.externalId1))
                .amount(trade.amount1)
                .committed(trade.isCommitted1)
                .symbol(na(trade.symbol1))
                .build()
        )
        .party2(
            PartyDetails.builder()
                .address(na(trade.externalId2))
                .amount(trade.amount2)
                .committed(trade.isCommitted2)
                .symbol(na(trade.symbol2))
                .build()
        )
        .build();

    return CompletableFuture.completedFuture(output);
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
