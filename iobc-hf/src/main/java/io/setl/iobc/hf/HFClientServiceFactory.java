package io.setl.iobc.hf;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import io.setl.common.TypeSafeMap;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.config.ChainConfigurationFactory;
import io.setl.iobc.model.ChainBrand;

/**
 * Factory to create Hyperledger Fabric configuration instances.
 *
 * @author Simon Greatrix on 01/02/2022.
 */
@Service
public class HFClientServiceFactory implements ChainConfigurationFactory {

  static final ExecutorService executorService;


  private static String require(TypeSafeMap map, String key) {
    return requireNonNull(map.getString(key), "The property '" + key + "' is required.");
  }

  static {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));
    executor.allowCoreThreadTimeOut(true);
    executorService = executor;
  }


  @Override
  public ChainConfiguration create(String iobcId, Map<String, Object> properties) {
    TypeSafeMap map = new TypeSafeMap(properties);

    // Normal chain properties
    String orgName = require(map, "orgName");
    String ccpFilePath = require(map, "ccpFilePath");
    String walletDir = require(map, "walletDir");
    String channelName = require(map, "channelName");

    // Certificate authority properties
    String adminUserId = require(map, "adminUserId");
    String adminUserSecret = require(map, "adminUserSecret");
    String caName = require(map, "caName");
    String caUrl = require(map, "caUrl");
    String tlsPem = map.getString("tlsPem");
    String orgMspId = require(map, "orgMspId");

    try {
      // Load the certificate
      String tlsPemText = (tlsPem != null && !tlsPem.isBlank()) ? Files.readString(Path.of(tlsPem)) : null;

      HFCAClientService hfcaClientService = new HFCAClientService(orgName, walletDir, adminUserId, adminUserSecret, caName, caUrl, tlsPemText, orgMspId);
      return new HFClientService(iobcId, orgName, ccpFilePath, walletDir, channelName, hfcaClientService);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.FABRIC;
  }

}
