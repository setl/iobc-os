package io.setl.iobc.corda.services;

import static io.setl.common.StringUtils.logSafe;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.concurrent.CompletableFuture;
import javax.json.JsonValue;
import javax.security.auth.x500.X500Principal;

import lombok.extern.slf4j.Slf4j;
import net.corda.core.identity.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.bc.pychain.wallet.WalletAddress;
import io.setl.common.AddressType;
import io.setl.common.ParameterisedException;
import io.setl.crypto.provider.SetlProvider;
import io.setl.crypto.provider.facade.FacadePublicKey;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.corda.NodeConnection;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.address.NewAddress;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.address.SetlAddressBuilder;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

/**
 * Create an IOBC address which links to a Corda party.
 *
 * @author Simon Greatrix on 16/02/2022.
 */
@Service
@Slf4j
public class CordaNewAddress implements NewAddress {

  private final AddressTable addressTable;


  /** New instance. */
  @Autowired
  public CordaNewAddress(
      AddressTable addressTable
  ) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration chain, Input input) throws ParameterisedException {
    final NodeConnection corda = (NodeConnection) chain;

    final int walletId = input.getWalletId();

    byte[] randomId = new byte[32];
    SetlProvider.getSecureRandom().nextBytes(randomId);

    SetlAddressBuilder builder = SetlAddress.builder()
        .chainBrand(ChainBrand.CORDA)
        .chainId(corda.getIobcId())
        .keyPair(new KeyPair(new FacadePublicKey(randomId), null))
        .walletId(walletId)
        .type(AddressType.NORMAL);

    try {
      WalletAddress address = builder.build();
      builder.chainAddress(address.getAddress());
      Output output = Output.builder().address(address.getAddress()).build();

      log.info("Corda address for {} created in wallet {}", address.getAddress(), address.getLeiId());
      return addressTable.insert(builder).thenApply(sa -> output);
    } catch (GeneralSecurityException e) {
      throw ExceptionTranslator.convert(e);
    }
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.CORDA;
  }

}
