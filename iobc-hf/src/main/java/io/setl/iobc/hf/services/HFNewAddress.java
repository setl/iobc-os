package io.setl.iobc.hf.services;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.setl.common.AddressType;
import io.setl.common.AddressUtil;
import io.setl.common.ParameterisedException;
import io.setl.crypto.KeyGen.Type;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.hf.HFClientService;
import io.setl.iobc.hf.util.HfX500NameStyle;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.address.NewAddress;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.address.SetlAddressBuilder;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.util.ExceptionTranslator;

/**
 * @author Simon Greatrix on 03/02/2022.
 */
@Slf4j
@Service
public class HFNewAddress implements NewAddress {

  private final AddressTable addressTable;


  /** New instance. */
  @Autowired
  public HFNewAddress(
      AddressTable addressTable
  ) {
    this.addressTable = addressTable;
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration chain, Input input) throws ParameterisedException {
    HFClientService service = (HFClientService) chain;

    KeyPair keyPair = Type.EC_NIST_P256.generate();
    String address = AddressUtil.publicKeyToAddress(keyPair.getPublic(), AddressType.NORMAL);

    log.info("Creating new Fabric identity on chain {} for address {}", service.getIobcId(), address);

    org.hyperledger.fabric.gateway.X509Identity id = service.getHfcaClientService().registerAndEnrollNewIdentity(address, keyPair);
    X509Certificate x509Certificate = id.getCertificate();
    X500Name subject = JcaX500NameUtil.getSubject(x509Certificate);
    X500Name issuer = JcaX500NameUtil.getIssuer(x509Certificate);
    String subjectDn = HfX500NameStyle.INSTANCE.toString(subject);
    String issuerDn = HfX500NameStyle.INSTANCE.toString(issuer);

    SetlAddressBuilder builder = SetlAddress.builder()
        .chainAddress(String.format("x509::%s::%s", subjectDn, issuerDn))
        .chainBrand(ChainBrand.FABRIC)
        .chainId(service.getIobcId())
        .keyPair(keyPair)
        .walletId(input.getWalletId())
        .type(AddressType.NORMAL);

    try {
      addressTable.insert(builder).get();
    } catch (InterruptedException | GeneralSecurityException | ExecutionException e) {
      log.error("Failed to store address", e);
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw ExceptionTranslator.convert(e);
    }

    return CompletableFuture.completedFuture(Output.builder().address(address).build());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.FABRIC;
  }

}
