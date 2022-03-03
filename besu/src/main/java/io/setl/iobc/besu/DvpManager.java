package io.setl.iobc.besu;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.transaction.SystemException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

import io.setl.crypto.KeyGen.Type;
import io.setl.iobc.besu.tx.SimpleReceiptHandler;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;
import io.setl.iobc.table.ConfigurationTable;
import io.setl.iobc.util.SerdeSupport;
import io.setl.iobc.web3j.DVP;
import io.setl.json.CJObject;

/**
 * Ensures a DVP contract is present on the Ethereum block-chain.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
@Slf4j
public class DvpManager {

  /**
   * Convert the external ID into a UINT256 as used by the Ethereum contract.
   *
   * @param externalId the external ID
   *
   * @return the internal ID
   */
  public static BigInteger getInternalId(String externalId) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512/256");
      byte[] bytes = digest.digest(externalId.getBytes(StandardCharsets.UTF_8));
      return new BigInteger(1, bytes);
    } catch (GeneralSecurityException e) {
      throw new InternalError("General security failure", e);
    }
  }


  private final BesuChainConfiguration chainConfiguration;

  private final ConfigurationTable configurationTable;

  private final String dvpContractKey;

  private final AtomicBoolean isInitialising = new AtomicBoolean(true);

  private final Consumer<String> shutdownHook;

  private final JtaTransactionManager transactionManager;

  private Credentials dvpCredentials;

  private String dvpIdentifier;


  /** New instance. */
  public DvpManager(
      BesuChainConfiguration chainConfiguration,
      ConfigurationTable configurationTable,
      JtaTransactionManager transactionManager,
      Consumer<String> shutdownHook
  ) {
    this.configurationTable = configurationTable;
    this.chainConfiguration = chainConfiguration;
    this.transactionManager = transactionManager;
    this.shutdownHook = shutdownHook;
    dvpContractKey = String.format("%s:%s:dvp:contact-address", ChainBrand.BESU.name(), chainConfiguration.getIobcId());
  }


  private void finishInitialisation(TransactionResult result, Throwable thrown) {
    if (thrown != null) {
      log.error("Initialisation of DVP on BESU block-chain {} failed with exception", chainConfiguration.getIobcId(), thrown);
      initialiseFailed();
      return;
    }

    CJObject cjObject = SerdeSupport.getObjectMapper().convertValue(result, CJObject.class);
    log.info("Loaded DVP contract for BESU block-chain {}. Transaction result:\n{})", chainConfiguration.getIobcId(), cjObject.toPrettyString());

    if (result.getTxStatus() != TxStatus.SUCCESS) {
      log.error("Failed to load DVP support contract to BESU block-chain {}.", chainConfiguration.getIobcId());
      initialiseFailed();
      return;
    }

    JsonValue jsonValue = result.getAdditionalData().get("contractAddress");
    if (jsonValue == null || jsonValue.getValueType() != ValueType.STRING) {
      log.error(
          "Failed to load DVP support contract to BESU block-chain {}. Transaction result did not specify contract address.",
          chainConfiguration.getIobcId()
      );
      initialiseFailed();
      return;
    }

    dvpIdentifier = ((JsonString) jsonValue).getString();
    ECKeyPair ecKeyPair = dvpCredentials.getEcKeyPair();
    String specifier = dvpIdentifier + "\n" + Numeric.encodeQuantity(ecKeyPair.getPublicKey()) + "\n" + Numeric.encodeQuantity(ecKeyPair.getPrivateKey());

    try {
      configurationTable.put(dvpContractKey, specifier);
    } catch (IllegalStateException e) {
      log.error("Failed to load DVP support contract to BESU block-chain {}. Could not persist its identity.", chainConfiguration.getIobcId(), e);
      Thread.currentThread().interrupt();
      initialiseFailed();
    }

    isInitialising.set(false);
  }


  /**
   * Get the identity address of the DVP contract in the block-chain.
   *
   * @return the identity.
   *
   * @throws IllegalStateException if the DVP contract is not yet loaded.
   */
  public Credentials getCredentials() {
    if (isInitialising.get()) {
      throw new IllegalStateException("DVP support on BESU chain \"" + chainConfiguration.getIobcId() + "\" is still initialising. Try again later.");
    }
    return dvpCredentials;
  }


  /**
   * Get the identity address of the DVP contract in the block-chain.
   *
   * @return the identity.
   *
   * @throws IllegalStateException if the DVP contract is not yet loaded.
   */
  public String getId() {
    if (isInitialising.get()) {
      throw new IllegalStateException("DVP support on BESU chain \"" + chainConfiguration.getIobcId() + "\" is still initialising. Try again later.");
    }
    return dvpIdentifier;
  }


  public void initialise() {
    Thread thread = new Thread(() -> {
      javax.transaction.Transaction transaction = null;
      try {
        transaction = transactionManager.createTransaction(dvpContractKey, 0);
        initialiseInternal();
        transaction.commit();
      } catch (Exception e) {
        if (transaction != null) {
          try {
            transaction.rollback();
          } catch (SystemException ex) {
            log.error("DVP XA Transaction failed", ex);
          }
        }
        log.error("DVP on BESU chain \"" + chainConfiguration.getIobcId() + "\" initialisation failed", e);
        initialiseFailed();
      }
    }, "Besu-DVP-Initialisation:" + chainConfiguration.getIobcId());
    thread.start();
  }


  private void initialiseFailed() {
    shutdownHook.accept("DVP initialisation on BESU chain \"" + chainConfiguration.getIobcId() + "\" failed. Shutting down.");
  }


  private void initialiseInternal() throws Exception {
    if(chainConfiguration.getWeb3j().ethGasPrice().send().getGasPrice().signum() != 0){
      throw new IllegalStateException("Minimum gas price should be zero");
    }
    String id = configurationTable.get(dvpContractKey);
    if (id != null) {
      log.info("DVP contract on chain {} is at {}", chainConfiguration.getIobcId(), id);
      String[] params = id.split("\n");
      if (params.length != 3) {
        throw new InternalError("Bad DVP record for " + dvpContractKey);
      }
      dvpIdentifier = params[0];
      dvpCredentials = Credentials.create(params[1], params[2]);
      isInitialising.set(false);
      return;
    }

    // Need to load the DVP contract
    log.error("DVP Contract on BESU chain {} is not loaded. Loading now", chainConfiguration.getIobcId());
    KeyPair keyPair = Type.EC_SECP_256K1.generate();
    ECKeyPair ecKeyPair = Web3KeyConversion.toEcKeyPair(keyPair);
    dvpCredentials = Credentials.create(ecKeyPair);
    TransactionManager transactionManager = chainConfiguration.getManager(dvpCredentials);
    RemoteCall<DVP> dvpRemoteCall = DVP.deploy(chainConfiguration.getWeb3j(), transactionManager, FreeGasProvider.INSTANCE);
    DVP dvp = dvpRemoteCall.send();
    Optional<TransactionReceipt> receiptOptional = dvp.getTransactionReceipt();
    assert receiptOptional.isPresent();
    String txHash = receiptOptional.get().getTransactionHash();
    SimpleReceiptHandler handler = new SimpleReceiptHandler();
    handler.getResult().whenComplete(this::finishInitialisation);
    chainConfiguration.getReceiptHandler().register(txHash, handler);
  }

}
