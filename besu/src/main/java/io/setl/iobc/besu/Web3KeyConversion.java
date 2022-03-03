package io.setl.iobc.besu;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import io.setl.iobc.model.address.SetlAddress;

/**
 * Conversion utility between Web3 key pairs and Java key pairs.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public class Web3KeyConversion {

  public static Credentials convert(KeyPair keyPair) {
    ECKeyPair ecKeyPair = toEcKeyPair(keyPair);
    return Credentials.create(ecKeyPair);
  }


  public static Credentials convert(SetlAddress address) {
    ECKeyPair ecKeyPair = toEcKeyPair(address.getKeyPair());
    return Credentials.create(ecKeyPair);
  }


  /**
   * Convert a Java Cryptographic Extension Key Pair to a Web3J ECKeyPair.
   *
   * @param keyPair the JCE Key Pair
   *
   * @return the Web3J ECKeyPair
   */
  public static ECKeyPair toEcKeyPair(KeyPair keyPair) {
    PublicKey originalPublic = keyPair.getPublic();
    PrivateKey originalPrivate = keyPair.getPrivate();

    try {
      KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
      BCECPublicKey bcPublic = (BCECPublicKey) keyFactory.translateKey(originalPublic);
      BCECPrivateKey bcPrivate = (BCECPrivateKey) keyFactory.translateKey(originalPrivate);
      return ECKeyPair.create(new KeyPair(bcPublic, bcPrivate));
    } catch (GeneralSecurityException e) {
      throw new InternalError("Internal cryptographic failure", e);
    }
  }

}
