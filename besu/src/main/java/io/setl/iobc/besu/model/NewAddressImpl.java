package io.setl.iobc.besu.model;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;

import io.setl.common.AddressType;
import io.setl.common.ParameterisedException;
import io.setl.crypto.KeyGen.Type;
import io.setl.iobc.besu.Web3KeyConversion;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.address.NewAddress;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.address.SetlAddressBuilder;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

/**
 * Create a new BESU compatible address. BESU requires use of the secp256k1 curve.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Service
@Slf4j
public class NewAddressImpl implements NewAddress {

  private final AddressTable addressTable;


  /** New instance. */
  @Autowired
  public NewAddressImpl(
      AddressTable addressTable
  ) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) throws ParameterisedException {
    // Create the address's key pair
    int walletId = input.getWalletId();
    KeyPair keyPair = Type.EC_SECP_256K1.generate();
    Credentials credentials = Web3KeyConversion.convert(keyPair);

    SetlAddressBuilder builder = SetlAddress.builder()
        .chainAddress(credentials.getAddress())
        .chainBrand(ChainBrand.BESU)
        .chainId(configuration.getIobcId())
        .keyPair(keyPair)
        .walletId(walletId)
        .type(AddressType.NORMAL);

    try {
      return addressTable.insert(builder).thenApply(sa -> Output.builder().address(sa.getAddress()).build());
    } catch (GeneralSecurityException e) {
      throw ExceptionTranslator.convert(e);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
