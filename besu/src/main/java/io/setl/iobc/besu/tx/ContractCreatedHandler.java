package io.setl.iobc.besu.tx;

import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import io.setl.common.ParameterisedException;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.table.TokenTable;

/**
 * Special handler for the creation of a Token contract.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Slf4j
public class ContractCreatedHandler extends SimpleReceiptHandler {

  /**
   * Mark an attempt to create a token as bad. This removed it from IOBC.
   *
   * @param tokenTable the token persistent storage
   * @param tokenId    the token ID
   */
  public static void markTokenAsBad(TokenTable tokenTable, String tokenId) {
    tokenTable.deleteToken(tokenId);
  }


  /**
   * Mark a token as successfully created.
   *
   * @param tokenTable      the token persistent storage
   * @param tokenId         the token's ID
   * @param spec            the token's specification
   * @param contractAddress the address of the token's contract
   */
  public static void markTokenAsGood(
      TokenTable tokenTable,
      String tokenId,
      TokenSpecification spec,
      String contractAddress,
      long creationBlock
  ) {
    BesuTokenData data = new BesuTokenData(spec.getChainData());
    data.setContract(contractAddress);
    data.setCreationBlock(creationBlock);
    TokenSpecification outSpec = spec.toBuilder()
        .chainData(data)
        .loading(false)
        .build();
    try {
      tokenTable.updateToken(tokenId, outSpec);
    } catch (ParameterisedException e) {
      log.error("Critical failure preparing token specification", e.getCause());
    }
  }


  private final TokenSpecification spec;

  private final String tokenId;

  private final TokenTable tokenTable;


  /**
   * New instance.
   */
  public ContractCreatedHandler(TokenTable tokenTable, String tokenId, TokenSpecification spec) {
    this.tokenId = tokenId;
    this.spec = spec;
    this.tokenTable = tokenTable;
  }


  @Override
  protected void handleFailure(TransactionReceipt transactionReceipt) {
    markTokenAsBad(tokenTable, tokenId);
    super.handleFailure(transactionReceipt);
  }


  @Override
  protected void handleSuccess(TransactionReceipt transactionReceipt) {
    String contract = transactionReceipt.getContractAddress();
    markTokenAsGood(tokenTable, tokenId, spec, contract, transactionReceipt.getBlockNumber().longValue());
    super.handleSuccess(transactionReceipt);
  }

}
