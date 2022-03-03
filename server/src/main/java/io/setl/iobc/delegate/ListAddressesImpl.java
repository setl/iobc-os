package io.setl.iobc.delegate;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.address.ListAddresses;
import io.setl.iobc.table.AddressTable;

/**
 * List all the addresses in a wallet.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
public class ListAddressesImpl implements ListAddresses {


  private final AddressTable addressTable;


  @Autowired
  public ListAddressesImpl(AddressTable addressTable) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration ignored, Input input) {
    int id = input.getWalletId();
    Output output = Output.builder()
        .addresses(addressTable.getAllAddresses(id))
        .walletId(id)
        .build();
    return CompletableFuture.completedFuture(output);
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.NONE;
  }


}
