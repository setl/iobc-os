package io.setl.iobc.besu;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;

import io.setl.common.TypeSafeMap;
import io.setl.iobc.besu.tx.CommonTransactionReceiptHandler;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.config.ChainConfigurationFactory;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.table.ConfigurationTable;

/**
 * Factory for creating instances of BesuChainConfiguration.
 *
 * @author Simon Greatrix on 25/01/2022.
 */
@Service
@Slf4j
public class BesuChainConfigurationFactory implements ChainConfigurationFactory, ApplicationContextAware, InitializingBean {

  private final ConfigurationTable configurationTable;

  private final ExecutorService service;

  private final JtaTransactionManager transactionManager;

  private ConfigurableApplicationContext applicationContext;

  private LinkedList<DvpManager> dvpList = new LinkedList<>();

  private boolean isInitialised = false;

  private Object lock = new Object();

  private CopyOnWriteArrayList<CommonTransactionReceiptHandler> receiptHandlers = new CopyOnWriteArrayList<>();


  @Autowired
  public BesuChainConfigurationFactory(
      @Qualifier("besuExecutorService") ExecutorService service,
      JtaTransactionManager transactionManager,
      ConfigurationTable configurationTable
  ) {
    this.service = service;
    this.transactionManager = transactionManager;
    this.configurationTable = configurationTable;
  }


  @Override
  public void afterPropertiesSet() {
    synchronized (lock) {
      isInitialised = true;
      while (!dvpList.isEmpty()) {
        dvpList.removeFirst().initialise();
      }
    }
  }


  public void applicationAbort(String message) {
    log.error(message);
    applicationContext.close();
  }


  @Override
  public ChainConfiguration create(String iobcId, Map<String, Object> properties) {
    TypeSafeMap map = new TypeSafeMap(properties);
    int chainId = map.getInt("chainId", 1337);
    String address = map.getString("address", "http://localhost:8545/");

    Web3jService web3jService = new HttpService(address);
    Web3j web3j = Web3j.build(web3jService);

    CommonTransactionReceiptHandler handler = new CommonTransactionReceiptHandler(web3j, service);
    receiptHandlers.add(handler);

    BesuChainConfiguration configuration = new BesuChainConfiguration(iobcId, chainId, web3j, handler);
    DvpManager dvpManager = new DvpManager(configuration, configurationTable, transactionManager, this::applicationAbort);
    configuration.setDvpManager(dvpManager);

    synchronized (lock) {
      if (isInitialised) {
        dvpManager.initialise();
      } else {
        dvpList.add(dvpManager);
      }
    }

    return configuration;
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.BESU;
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = (ConfigurableApplicationContext) applicationContext;
  }


  /**
   * Periodic update to fetch transactions.
   */
  @Scheduled(fixedRate = 3600)
  public void updateHandlers() {
    receiptHandlers.forEach(CommonTransactionReceiptHandler::scan);
  }

}
