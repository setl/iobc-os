package io.setl.iobc.table;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import io.setl.common.ParameterisedException;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.address.SetlAddressBuilder;
import io.setl.iobc.model.address.Wallet;
import io.setl.json.CJObject;

/**
 * Address data table.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public interface AddressTable {

  /**
   * Get the SETL address record by its ID.
   *
   * @param addressId the SETL address ID
   *
   * @return the SETL address instance, or null if none exists
   */
  SetlAddress getAddress(String addressId);


  /**
   * Get the SETL address record by it ID. If the address does not exist, throw an exception.
   *
   * @param addressId the SETL address ID
   *
   * @return the SETL address instance
   *
   * @throws ParameterisedException if the SETL address does not exist.
   */
  @NotNull
  default SetlAddress getAddressSafe(String addressId) throws ParameterisedException {
    SetlAddress setlAddress = getAddress(addressId);
    if (setlAddress == null) {
      CJObject cjObject = new CJObject();
      cjObject.put("address", addressId);
      throw new ParameterisedException("Address \"{}\" does not exist.", "iobc:unknown-address", cjObject);
    }
    return setlAddress;
  }


  /**
   * Get all the addresses in a wallet.
   *
   * @param id the wallet's ID
   *
   * @return null if the wallet does not exist, otherwise a list of addresses that are in the wallet and exist
   */
  @Nullable
  default Map<String, SetlAddress> getAllAddresses(Integer id) {
    Wallet wallet = getWallet(id);
    if (wallet == null) {
      return null;
    }
    Set<String> addresses = wallet.getAddresses();
    Map<String, SetlAddress> map = new HashMap<>();
    for (String a : addresses) {
      SetlAddress address = getAddress(a);
      if (address != null) {
        map.put(a, address);
      }
    }
    return map;
  }


  /**
   * Get the Wallet by its ID.
   *
   * @param id the wallet ID
   *
   * @return the wallet, or null
   */
  Wallet getWallet(Integer id);


  /**
   * Insert a new address into this table. Some persistence mechanisms may take a few moments to work.
   *
   * @param builder builder for the new address
   *
   * @return the address that was constructed
   */
  CompletableFuture<SetlAddress> insert(SetlAddressBuilder builder) throws GeneralSecurityException;


  /**
   * Get the SETL address ID for a block-chain address.
   *
   * @param chainAddress the block-chain address
   *
   * @return the SETL address, or null if not known
   */
  String lookupAddress(String chainAddress);

}
