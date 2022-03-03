package io.setl.iobc.besu.tx;

import io.setl.iobc.model.TokenSpecification.ChainData;

/**
 * Specification for a token contract.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public class BesuTokenData extends ChainData {

  private static final String P_CONTRACT = "contract";

  private static final String P_CREATION_BLOCK = "creationBlock";

  private static final String P_TRANSACTION_ID = "transactionId";


  public BesuTokenData(ChainData data) {
    super(data);
  }


  public BesuTokenData() {
    // do nothing
  }


  /** The address of the token's contract. */
  public String getContract() {
    return data.optString(P_CONTRACT);
  }


  /** The block in which the contract was created. */
  public long getCreationBlock() {
    return data.getLong(P_CREATION_BLOCK, -1);
  }


  /** The ID of the transaction that is supposed to register the token. Only present during loading. */
  public String getTransactionId() {
    return data.optString(P_TRANSACTION_ID);
  }


  public void setContract(String contract) {
    data.put(P_CONTRACT, contract);
  }


  public void setCreationBlock(long block) {
    data.put(P_CREATION_BLOCK, block);
  }


  public void setTransactionId(String id) {
    data.put(P_TRANSACTION_ID, id);
  }

}
