package io.setl.iobc.corda;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import io.setl.common.TypeSafeMap;
import io.setl.iobc.config.ChainConfigurationFactory;
import io.setl.iobc.model.ChainBrand;
import io.setl.util.CopyOnWriteMap;

/**
 * Factory to create Corda RPC Connection instances.
 *
 * @author Simon Greatrix on 15/02/2022.
 */
@Service
public class ConnectionFactory implements ChainConfigurationFactory, DisposableBean {

  static final ExecutorService executorService;


  private static String require(TypeSafeMap map, String parameter) {
    return Objects.requireNonNull(map.getString(parameter), "Corda configuration requires that the \"" + parameter + "\" parameter be set.");
  }

  static {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));
    executor.allowCoreThreadTimeOut(true);
    executorService = executor;
  }

  private final CopyOnWriteMap<String, NodeConnection> instances = new CopyOnWriteMap<>();


  @Override
  public NodeConnection create(String iobcId, Map<String, Object> properties) {
    return instances.computeIfAbsent(
        iobcId,
        ignored -> {
          TypeSafeMap map = new TypeSafeMap(properties);
          return new NodeConnection(
              iobcId,
              require(map, "rpcHost"),
              Objects.requireNonNull(map.getInt("rpcPort"), "Corda configuration requires that that \"rpcPort\" parameter be set."),
              require(map, "country"),
              require(map, "locality"),
              require(map, "orgName"),
              require(map, "username"),
              require(map, "password")
          );
        }
    );
  }


  @Override
  public void destroy() {
    instances.values().forEach(NodeConnection::close);
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.CORDA;
  }

}
