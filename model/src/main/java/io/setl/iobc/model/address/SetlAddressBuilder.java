package io.setl.iobc.model.address;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Base64;

import io.setl.bc.pychain.wallet.WalletAddress;
import io.setl.common.AddressType;
import io.setl.common.AddressUtil;
import io.setl.iobc.model.ChainBrand;

/**
 * Builder for new SETL addresses.
 *
 * @author Simon Greatrix on 13/12/2021.
 */
public class SetlAddressBuilder {

  private String chainAddress;

  private ChainBrand chainBrand;

  private String chainId;

  private byte[] encryptedKey;

  private KeyPair keyPair;

  private String keyType;

  private byte[] publicKey;

  private AddressType type = AddressType.NORMAL;

  private int walletId;

  private String wrapId;


  /**
   * Combine the provided data to produce a SETL Wallet Address which can then be used as input to a SetlAddress constructor.
   *
   * @return the new Wallet Address (NOT a SetlAddress!)
   */
  public WalletAddress build() throws GeneralSecurityException {
    if (keyPair != null) {
      return new WalletAddress(walletId, keyPair.getPublic(), keyPair.getPrivate(), type, 0);
    }

    String address = AddressUtil.publicKeyToAddress(publicKey, type.getId());
    return new WalletAddress(walletId, type.getId(), address, 0, keyType, wrapId, publicKey, encryptedKey);
  }


  /**
   * Set the chain address associated with the address.
   *
   * @param chainAddress the chain address
   *
   * @return this
   */
  public SetlAddressBuilder chainAddress(String chainAddress) {
    this.chainAddress = chainAddress;
    return this;
  }


  /**
   * Set the chain brand associated with the address.
   *
   * @param brand the chain's brand
   *
   * @return this
   */
  public SetlAddressBuilder chainBrand(ChainBrand brand) {
    chainBrand = brand;
    return this;
  }


  /**
   * Set the ID of the chain associated with the address.
   *
   * @param chainId the chain's ID
   *
   * @return this
   */
  public SetlAddressBuilder chainId(String chainId) {
    this.chainId = chainId;
    return this;
  }


  /**
   * Set the encrypted key using the Base-64 URL encoded representation of its encrypted binary. Note: unsets any 'key pair'.
   *
   * @param encryptedKey the key
   *
   * @return this
   */
  public SetlAddressBuilder encryptedKey(String encryptedKey) {
    keyPair = null;
    this.encryptedKey = Base64.getUrlDecoder().decode(encryptedKey);
    return this;
  }


  /**
   * Set the encrypted key using its encrypted binary. Note: unsets any 'key pair'.
   *
   * @param encryptedKey the key
   *
   * @return this
   */
  public SetlAddressBuilder encryptedKey(byte[] encryptedKey) {
    keyPair = null;
    this.encryptedKey = encryptedKey;
    return this;
  }


  public String getChainAddress() {
    return chainAddress;
  }


  public ChainBrand getChainBrand() {
    return chainBrand;
  }


  public String getChainId() {
    return chainId;
  }


  public byte[] getEncryptedKey() {
    return encryptedKey;
  }


  public String getEncryptedKeyB64() {
    return encryptedKey != null ? Base64.getUrlEncoder().encodeToString(encryptedKey) : null;
  }


  public KeyPair getKeyPair() {
    return keyPair;
  }


  public String getKeyType() {
    return keyType;
  }


  public byte[] getPublicKey() {
    return publicKey;
  }


  public String getPublicKeyB64() {
    return publicKey != null ? Base64.getUrlEncoder().encodeToString(publicKey) : null;
  }


  public AddressType getType() {
    return type;
  }


  public int getWalletId() {
    return walletId;
  }


  public String getWrapId() {
    return wrapId;
  }


  /**
   * Set the public and (unencrypted) private key. Note: unsets any separately set public key or encrypted private key.
   *
   * @param keyPair the key pair
   *
   * @return this
   */
  public SetlAddressBuilder keyPair(KeyPair keyPair) {
    if (keyPair != null) {
      this.keyPair = keyPair;
      keyType = keyPair.getPublic().getAlgorithm();
      publicKey = null;
      encryptedKey = null;
    } else {
      this.keyPair = null;
    }
    return this;
  }


  /**
   * Set the key type. For example: 'EC' for Elliptic Curve. Note: unsets any 'key pair'
   *
   * @param keyType the key type
   *
   * @return this
   */
  public SetlAddressBuilder keyType(String keyType) {
    this.keyType = keyType;
    keyPair = null;
    return this;
  }


  /**
   * Set the public key via its Base-64 URL encoded form. Note: unsets any 'key pair'
   *
   * @param publicKey the public key
   *
   * @return this
   */
  public SetlAddressBuilder publicKey(String publicKey) {
    keyPair = null;
    this.publicKey = Base64.getUrlDecoder().decode(publicKey);
    return this;
  }


  /**
   * Set the public key via its binary representation. Note: unsets any 'key pair'
   *
   * @param publicKey the public key
   *
   * @return this
   */
  public SetlAddressBuilder publicKey(byte[] publicKey) {
    keyPair = null;
    this.publicKey = publicKey;
    return this;
  }


  public SetlAddressBuilder type(AddressType type) {
    this.type = type;
    return this;
  }


  public SetlAddressBuilder walletId(int walletId) {
    this.walletId = walletId;
    return this;
  }


  public SetlAddressBuilder wrapId(String wrapId) {
    this.wrapId = wrapId;
    return this;
  }

}
