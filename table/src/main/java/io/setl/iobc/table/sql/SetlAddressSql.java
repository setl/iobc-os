package io.setl.iobc.table.sql;

import io.setl.bc.pychain.wallet.WalletAddress;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.address.SetlAddress;

/**
 * A simple SETL Address implementation.
 *
 * @author Simon Greatrix on 14/12/2021.
 */
public class SetlAddressSql extends SetlAddress {

  private final String chainAddress;

  private final WalletAddress walletAddress;


  public SetlAddressSql(WalletAddress walletAddress, String chainAddress, ChainBrand chainBrand, String chainId) {
    this.walletAddress = walletAddress;
    this.chainAddress = chainAddress;
    this.chainBrand = chainBrand;
    this.chainId = chainId;
  }


  @Override
  public String getChainAddress() {
    return chainAddress;
  }


  /**
   * Get the bytes that will be persisted into the database.
   *
   * @return the encrypted private key
   */
  public byte[] getPrivateKeyBytes() {
    return walletAddress.getPrivateKeyBytes();
  }


  /**
   * Get the bytes that will be persisted into the database.
   *
   * @return the encoded public key
   */
  public byte[] getPublicKeyBytes() {
    return walletAddress.getPublicKeyBytes();
  }


  @Override
  protected WalletAddress getWalletAddress() {
    return walletAddress;
  }

}
