package io.setl.iobc.besu.model;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;

import io.setl.common.ParameterisedException;
import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.besu.FreeGasProvider;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.besu.tx.BesuTokenData;
import io.setl.iobc.besu.tx.GetBlockNumberImpl;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.tokens.ApproveTransfer;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.web3j.IERC20;

/**
 * Mint a token.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class ApproveTransferImpl implements ApproveTransfer {

  private final AddressTable addressTable;

  private final TokenTable tokenTable;


  /**
   * New instance.
   */
  public ApproveTransferImpl(
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    this.addressTable = addressTable;
    this.tokenTable = tokenTable;
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    String tokenId = input.getSymbol();
    String owner = input.getOwner();
    String spender = input.getSpender();
    BigInteger amount = input.getAmount();
    // TODO : should use the "compare-and-set" method
    // BigInteger expected = input.getExpected();

    log.info("Setting allowance of token {} for {} spent by {}", tokenId, owner, spender);

    // Check address exists
    SetlAddress setlAddress = addressTable.getAddressSafe(owner);
    Credentials credentials = Web3KeyConversion.convert(setlAddress);

    // Get owner's address
    setlAddress = addressTable.getAddressSafe(spender);
    String spenderAddress = Web3KeyConversion.convert(setlAddress).getAddress();

    // Get the token details
    TokenSpecification token = tokenTable.getTokenSpecification(tokenId);
    TokenSpecification.check(tokenId, token);
    BesuTokenData spec = new BesuTokenData(token.getChainData());

    BigInteger recentBlock = GetBlockNumberImpl.getRecentBlock(besu.getWeb3j(), input.getTxProcessingMode());

    // Call the contract
    TransactionManager manager = besu.getManager(credentials);
    IERC20 bnyToken = IERC20.load(spec.getContract(), besu.getWeb3j(), manager, FreeGasProvider.INSTANCE);
    RemoteFunctionCall<TransactionReceipt> call = bnyToken.approve(spenderAddress, amount);
    TransactionReceipt receipt;
    try {
      receipt = call.send();
    } catch (Exception e) {
      // Failed to send mint request
      log.error("Failed to invoke approve function on smart contract {} for token {} with amount {}",
          spec.getContract(), tokenId, amount, e
      );
      throw ExceptionTranslator.convert(e);
    }

    return besu.getReceiptHandlerFactory().prepare(input.getTxProcessingMode(), recentBlock, receipt.getTransactionHash());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }


}
