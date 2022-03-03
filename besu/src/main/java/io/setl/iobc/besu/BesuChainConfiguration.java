package io.setl.iobc.besu;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import io.setl.iobc.besu.tx.CommonTransactionReceiptHandler;
import io.setl.iobc.besu.tx.ReceiptHandlerFactory;
import io.setl.iobc.besu.tx.SimpleReceiptHandlerFactory;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;

/**
 * Configuration for a BESU chain.
 *
 * @author Simon Greatrix on 25/01/2022.
 */
public class BesuChainConfiguration implements ChainConfiguration {

  private final int chainId;

  private final String iobcId;

  private final CommonTransactionReceiptHandler receiptHandler;

  private final ReceiptHandlerFactory receiptHandlerFactory;

  private final Web3j web3j;

  private DvpManager dvpManager;

  /**
   * New instance.
   *
   * @param iobcId the IOBC ID
   * @param web3j  the Web3J connector
   */
  public BesuChainConfiguration(
      String iobcId,
      int chainId,
      Web3j web3j,
      CommonTransactionReceiptHandler receiptHandler
  ) {
    this.iobcId = iobcId;
    this.chainId = chainId;
    this.receiptHandler = receiptHandler;
    this.web3j = web3j;
    receiptHandlerFactory = new SimpleReceiptHandlerFactory(receiptHandler);
  }


  public int getChainId() {
    return chainId;
  }


  public DvpManager getDvpManager() {
    return dvpManager;
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.BESU;
  }


  public String getIobcId() {
    return iobcId;
  }


  public TransactionManager getManager(Credentials credentials) {
    return new RawTransactionManager(web3j, credentials, chainId, receiptHandler);
  }


  public CommonTransactionReceiptHandler getReceiptHandler() {
    return receiptHandler;
  }


  public ReceiptHandlerFactory getReceiptHandlerFactory() {
    return receiptHandlerFactory;
  }


  public Web3j getWeb3j() {
    return web3j;
  }


  public void setDvpManager(DvpManager dvpManager) {
    this.dvpManager = dvpManager;
  }

}
