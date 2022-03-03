package io.setl.iobc.delegate;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.address.GetWallet;
import io.setl.iobc.model.address.Wallet;
import io.setl.iobc.table.AddressTable;

/**
 * Get the contents of a wallet.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
public class GetWalletImpl implements GetWallet {


  private final AddressTable addressTable;


  @Autowired
  public GetWalletImpl(AddressTable addressTable) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration ignored, Input input) {
    int id = input.getWalletId();
    Wallet wallet = addressTable.getWallet(id);
    Output output;
    if (wallet != null) {
      output = Output.builder()
          .addresses(wallet.getAddresses())
          .walletId(id)
          .build();
    } else {
      // wallet is not known
      output = Output.builder()
          .addresses(Set.of())
          .walletId(id)
          .build();
    }
    return CompletableFuture.completedFuture(output);
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.NONE;
  }


  @Override
  public String getType() {
    return NAME;
  }

}
