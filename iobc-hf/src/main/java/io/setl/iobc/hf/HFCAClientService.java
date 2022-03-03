package io.setl.iobc.hf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.util.Properties;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import io.setl.common.ParameterisedException;
import io.setl.iobc.hf.model.UserContext;
import io.setl.iobc.util.ExceptionTranslator;


public class HFCAClientService {

  private final String adminUserId;

  private final String adminUserSecret;

  private final HFCAClient caClient;

  private final String orgMspId;

  private final String orgName;

  private final Wallet wallet;


  public HFCAClientService(
      String orgName,
      String wallerDir,
      String adminUserId,
      String adminUserSecret,
      String caName,
      String caUrl,
      String tlsPem,
      String orgMspId
  ) {
    this.orgName = orgName;
    this.adminUserId = adminUserId;
    this.adminUserSecret = adminUserSecret;
    this.orgMspId = orgMspId;

    try {
      Path walletDirectory = Paths.get(wallerDir);
      wallet = Wallets.newFileSystemWallet(walletDirectory);

      Properties caProperties = new Properties();
      caProperties.put("allowAllHostNames", "true");
      caProperties.put("pemBytes", tlsPem.getBytes());

      caClient = HFCAClient.createNewInstance(caName, caUrl, caProperties);

      CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
      caClient.setCryptoSuite(cryptoSuite);
    } catch (IOException
        | InvalidArgumentException
        | IllegalAccessException
        | InstantiationException
        | ClassNotFoundException
        | CryptoException
        | org.hyperledger.fabric.sdk.exception.InvalidArgumentException
        | NoSuchMethodException
        | InvocationTargetException e
    ) {
      throw new IllegalArgumentException("Hyperledger fabric configuration is invalid", e);
    }
  }


  private Enrollment convertIdentityToEnrollment(X509Identity identity) {
    String certPem = Identities.toPemString(identity.getCertificate());
    return new X509Enrollment(identity.getPrivateKey(), certPem);
  }


  public X509Identity enrollAdmin() throws ParameterisedException {
    try {
      Enrollment enrollment = caClient.enroll(adminUserId, adminUserSecret);
      X509Identity identity = Identities.newX509Identity(orgMspId, enrollment);
      wallet.put(adminUserId, identity);
      return identity;
    } catch (Exception e) {
      throw ExceptionTranslator.convert(e);
    }
  }


  public X509Identity getIdentity(String userId) throws IOException {
    return (X509Identity) wallet.get(userId);
  }


  public X509Identity registerAndEnrollNewIdentity(String userId) throws ParameterisedException {
    return registerAndEnrollNewIdentity(userId, null);
  }


  public X509Identity registerAndEnrollNewIdentity(String userId, KeyPair keyPair) throws ParameterisedException {
    try {
      X509Identity adminIdentity = (X509Identity) wallet.get(adminUserId);
      if (adminIdentity == null) {
        adminIdentity = enrollAdmin();
      }
      UserContext adminUserContext = new UserContext();
      adminUserContext.setName(adminUserId);
      adminUserContext.setEnrollment(convertIdentityToEnrollment(adminIdentity));

      RegistrationRequest registerReq = new RegistrationRequest(userId);
      String userSecret = caClient.register(registerReq, adminUserContext);
      Enrollment enrollment;
      if (keyPair != null) {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setKeyPair(keyPair);
        enrollment = caClient.enroll(userId, userSecret, request);
      } else {
        enrollment = caClient.enroll(userId, userSecret);
      }
      return saveIdentity(userId, enrollment);
    } catch (Exception e) {
      e.printStackTrace(); // TODO REMOVE THIS LINE
      throw ExceptionTranslator.convert(e);
    }
  }


  private X509Identity saveIdentity(String userId, Enrollment enrollment) throws CertificateException, IOException {
    X509Identity identity = Identities.newX509Identity(orgMspId, enrollment);
    wallet.put(userId, identity);
    return identity;
  }


  @Override
  public String toString() {
    return String.format("HFCAClientService class=> orgName:%s; adminUserId: %s", orgName, adminUserId);
  }

}
