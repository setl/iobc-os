package io.setl.iobc.model.address;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import io.setl.bc.pychain.wallet.WalletAddress;
import io.setl.common.AddressType;
import io.setl.iobc.model.ChainBrand;

/**
 * A SETL address with a protected key pair.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public abstract class SetlAddress {

  public static SetlAddressBuilder builder() {
    return new SetlAddressBuilder();
  }

  protected ChainBrand chainBrand;

  protected String chainId;


  /**
   * Create a new builder instance which could re-create this address instance.
   *
   * @return a new builder
   */
  public SetlAddressBuilder asBuilder() {
    return builder()
        .chainAddress(getChainAddress())
        .chainBrand(chainBrand)
        .chainId(getChainId())
        .keyPair(getKeyPair())
        .walletId(getWalletId())
        .type(getType())
        .keyType(getKeyType());
  }


  /**
   * Get the SETL address.
   *
   * @return the SETL address.
   */
  public String getAddress() {
    return getWalletAddress().getAddress();
  }


  /**
   * Get the chain address.
   *
   * @return the chain address
   */
  public abstract String getChainAddress();


  public ChainBrand getChainBrand() {
    return chainBrand;
  }


  /**
   * Get the chain ID.
   *
   * @return the chain ID
   */
  public String getChainId() {
    return chainId;
  }


  /**
   * Get the key pair encapsulated by this address.
   *
   * @return the key pair
   */
  public KeyPair getKeyPair() {
    return new KeyPair(getWalletAddress().getPublicKey(), getWalletAddress().getPrivateKey());
  }


  /**
   * Get the type of the signing algorithm used by the keys (e.g. "EC" or "EDSA").
   *
   * @return the key's algorithm type
   */
  public String getKeyType() {
    return getWalletAddress().getKeyType();
  }


  /**
   * Get the private key. The private key may not be available.
   *
   * @return the private key
   */
  public PrivateKey getPrivateKey() {
    return getWalletAddress().getPrivateKey();
  }


  /**
   * Get the public key.
   *
   * @return the public key
   */
  public PublicKey getPublicKey() {
    return getWalletAddress().getPublicKey();
  }


  /**
   * Get the type of this address.
   *
   * @return the address type. E.g. Normal, Privileged, Contract, External
   */
  public AddressType getType() {
    return getWalletAddress().getAddressType();
  }


  /**
   * Get the SETL wallet address instance.
   *
   * @return the instance
   */
  protected abstract WalletAddress getWalletAddress();


  /**
   * Get the ID of the wallet that contains this address.
   *
   * @return the wallet's ID
   */
  public int getWalletId() {
    return (int) getWalletAddress().getLeiId();
  }


  /**
   * Get the ID of the key material used to initialise the key-wrapping cipher used to protect the private key.
   *
   * @return the ID of the key material
   */
  public String getWrapId() {
    return getWalletAddress().getWrapId();
  }


  /**
   * Set the key material ID for the private key wrapping to a random selection from the currently enabled ones.
   *
   * @throws GeneralSecurityException if the key cannot be unwrapped and re-wrapped using the new key material
   */
  public void setWrapId() throws GeneralSecurityException {
    getWalletAddress().setWrapId();
  }


  /**
   * Set the key material ID for the private key wrapping to a specific instance.
   *
   * @param newWrapId the ID of the new key material
   *
   * @throws GeneralSecurityException if the key cannot be unwrapped and re-wrapped using the new key material
   */
  public void setWrapId(String newWrapId) throws GeneralSecurityException {
    getWalletAddress().setWrapId(newWrapId);
  }

}
