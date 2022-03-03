package io.setl.iobc.model.address;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Storage for a wallet.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@ToString
@EqualsAndHashCode
public class Wallet {

  private final Set<String> addresses;

  private final int id;


  /**
   * Create a new empty wallet.
   *
   * @param id the wallet's ID.
   */
  public Wallet(int id) {
    this.id = id;
    addresses = new HashSet<>();
  }


  @JsonCreator
  public Wallet(
      @JsonProperty(value = "id", required = true) int id,
      @JsonProperty(value = "addresses", required = true) Set<String> addresses
  ) {
    this.id = id;
    this.addresses = new HashSet<>(addresses);
  }


  /**
   * Add an address to this wallet.
   *
   * @param newAddress the new address
   */
  public void addAddress(String newAddress) {
    synchronized (addresses) {
      addresses.add(newAddress);
    }
  }


  /**
   * Get all the addresses in this wallet.
   *
   * @return the addresses
   */
  public Set<String> getAddresses() {
    synchronized (addresses) {
      return Set.copyOf(addresses);
    }
  }


  public int getId() {
    return id;
  }


  /** Remove an address from this wallet. */
  public void removeAddress(String oldAddress) {
    synchronized (addresses) {
      addresses.remove(oldAddress);
    }
  }

}
