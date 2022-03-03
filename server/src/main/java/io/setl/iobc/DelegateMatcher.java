package io.setl.iobc;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.common.TypeSafeMap;
import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipal;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.config.ChainConfigurationFactory;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.table.AddressTable;
import io.setl.iobc.table.TokenTable;
import io.setl.iobc.util.ExceptionTranslator;

/**
 * Identify a suitable delegate for any request.
 *
 * @author Simon Greatrix on 26/01/2022.
 */
@Service
@Slf4j
public class DelegateMatcher {

  static class Key {

    private final ChainBrand brand;

    private final int hc;

    private final Class<? extends MessageContent> input;

    private final String type;


    public Key(String type, Class<? extends MessageInput> input, ChainBrand brand) {
      this.type = type;
      this.input = input;
      this.brand = brand;
      hc = Objects.hash(type, input, brand);
    }


    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      if (!(other instanceof Key)) {
        return false;
      }
      Key that = (Key) other;
      return hc == that.hc && brand == that.brand && input.equals(that.input) && type.equals(that.type);
    }


    @Override
    public int hashCode() {
      return hc;
    }


    public String toString() {
      return "[" + type + " <" + input + "> for " + brand + "]";
    }

  }



  private final Function<String, SetlAddress> addressFunction;

  /** Map of chain configurations. */
  private final Map<String, ChainConfiguration> chainConfiguration;

  private final ChainConfiguration defaultChain;

  /** Map of delegates. */
  private final Map<Key, IobcDelegate<?, ?>> delegates;

  private final Function<String, TokenSpecification> tokenFunction;


  /**
   * New instance.
   *
   * @param delegates the available delegates
   */
  public DelegateMatcher(
      List<IobcDelegate<?, ?>> delegates,
      List<ChainConfigurationFactory> configurationFactories,
      RawChainConfiguration configuration,
      AddressTable addressTable,
      TokenTable tokenTable
  ) {
    addressFunction = addressTable::getAddress;
    tokenFunction = symbol -> {
      try {
        return tokenTable.getTokenSpecification(symbol);
      } catch (ParameterisedException exception) {
        log.error("Failed to decode token specification: {}", symbol, exception);
        return null;
      }
    };

    // Load all the delegates and identify what inputs they will process
    TypeFactory typeFactory = TypeFactory.defaultInstance();
    HashMap<Key, IobcDelegate<?, ?>> delegateMap = new HashMap<>();
    for (IobcDelegate<?, ?> delegate : delegates) {
      JavaType javaType = typeFactory.constructType(delegate.getClass());
      JavaType interfaceType = javaType.findSuperType(IobcDelegate.class);
      JavaType inputType = interfaceType.getBindings().findBoundType("InputType");
      Class<? extends MessageInput> inputClass = inputType.getRawClass().asSubclass(MessageInput.class);
      delegateMap.put(new Key(delegate.getType(), inputClass, delegate.getBrandSupported()), delegate);
    }
    this.delegates = Collections.unmodifiableMap(delegateMap);

    // Identify the chain factories
    EnumMap<ChainBrand, ChainConfigurationFactory> factories = new EnumMap<>(ChainBrand.class);
    configurationFactories.forEach(ccf -> factories.put(ccf.getIobcBrand(), ccf));

    // Load the chain configurations to pass to the delegates.
    HashMap<String, ChainConfiguration> chainMap = new HashMap<>();
    chainMap.put(ChainConfiguration.INTERNAL_CHAIN, new NoChainConfiguration(ChainConfiguration.INTERNAL_CHAIN));

    ChainConfiguration myDefaultChain = null;
    for (var entry : configuration.getChain().entrySet()) {
      // convert the configuration into a chain configuration instance
      TypeSafeMap map = requireNonNull(TypeSafeMap.asMap(entry.getValue()), "'setl.iobc.chain' entries must be maps: " + entry.getKey());
      String type = requireNonNull(map.getString("iobcBrand", null), "'setl.iobc.chain' entries must specify an 'iobcBrand': " + entry.getKey());
      ChainBrand brand = ChainBrand.valueOf(type.toUpperCase(Locale.ROOT));
      ChainConfigurationFactory factory = requireNonNull(factories.get(brand), "No chain factory for " + brand + ": " + entry.getKey());
      ChainConfiguration myConfiguration = factory.create(entry.getKey(), map);
      chainMap.put(entry.getKey(), myConfiguration);

      // Check for default chain
      if (map.getBoolean("isIobcDefault", false)) {
        if (myDefaultChain == null) {
          myDefaultChain = myConfiguration;
        } else {
          throw new IllegalArgumentException("More than one chain is specified as the default chain: " + entry.getKey() + " and " + myDefaultChain.getIobcId());
        }
      }
    }

    // ensure we have a default chain
    if (myDefaultChain == null) {
      // Since we always insert the internal chain, there will always be at least one chain to be the default
      myDefaultChain = chainMap.values().stream().findFirst().orElseThrow();
    }
    defaultChain = myDefaultChain;

    chainConfiguration = Collections.unmodifiableMap(chainMap);
  }


  public ChainConfiguration getChainConfiguration(String chainId) {
    if (chainId == null || chainId.isBlank()) {
      return defaultChain;
    }
    return chainConfiguration.get(chainId);
  }


  public ChainConfiguration getDefaultChain() {
    return defaultChain;
  }


  public CompletableFuture<MessageContent> invoke(String name, MessageInput input) {
    ChainConfiguration myConfiguration = defaultChain;
    Class<? extends MessageInput> inputType;
    if (input != null) {
      inputType = input.getClass();

      // TODO - scan through the principals to identify one the caller can use.
      Iterator<MessagePrincipal> iterator = input.resolvePrincipal().resolve(addressFunction, tokenFunction).iterator();
      boolean isUnauthorised = true;
      while (iterator.hasNext() && isUnauthorised) {
        MessagePrincipal principal = iterator.next();
        if (!principal.isValid()) {
          continue;
        }

        String chainId = principal.getChainId();
        myConfiguration = getChainConfiguration(chainId);
        isUnauthorised = false;
        if (myConfiguration == null) {
          log.error("No such chain: {}", chainId);
          JsonObject json = Json.createObjectBuilder()
              .add("type", name)
              .add("input", inputType.toString())
              .add("chainId", chainId)
              .build();
          ParameterisedException exception = new ParameterisedException("Access denied for " + name, "iobc:no-such-chain", json);
          return CompletableFuture.failedFuture(exception);
        }
      }
      if (isUnauthorised) {
        log.error("Access denied to delegate implementation for {}", name);
        JsonObject json = Json.createObjectBuilder()
            .add("type", name)
            .add("input", inputType.toString())
            .build();
        ParameterisedException exception = new ParameterisedException("Access denied for " + name, "iobc:delegate-access-denied", json);
        return CompletableFuture.failedFuture(exception);
      }
    } else {
      inputType = MessageInput.class;
    }

    ChainBrand brand = myConfiguration.getIobcBrand();
    Key key = new Key(name, inputType, brand);
    IobcDelegate<?, ?> delegate = delegates.get(key);
    if (delegate == null && brand != ChainBrand.NONE) {
      // Try the NONE chain for chain agnostic delegates
      delegate = delegates.get(new Key(name, inputType, ChainBrand.NONE));
    }
    if (delegate == null) {
      log.error("No delegate implementation for {}", key);
      JsonObject json = Json.createObjectBuilder()
          .add("type", name)
          .add("input", inputType.toString())
          .add("chainBrand", myConfiguration.getIobcBrand().toString())
          .build();
      ParameterisedException exception = new ParameterisedException("No implementation for " + key, "iobc:no-delegate-implementation", json);
      return CompletableFuture.failedFuture(exception);
    }

    return invoke(delegate, myConfiguration, input);
  }


  @SuppressWarnings("unchecked")
  private <I extends MessageInput> CompletableFuture<MessageContent> invoke(IobcDelegate<?, ?> delegate, ChainConfiguration configuration, I input) {
    IobcDelegate<I, ?> castDelegate = (IobcDelegate<I, ?>) delegate;
    I castInput = (I) input;
    try {
      log.debug("Invoking delegate {} with {} on {}", castDelegate.getClass(), castInput, configuration.getIobcId());
      return (CompletableFuture<MessageContent>) castDelegate.apply(configuration, castInput);
    } catch (ParameterisedException parameterisedException) {
      return CompletableFuture.failedFuture(parameterisedException);
    } catch (RuntimeException e) {
      return CompletableFuture.failedFuture(ExceptionTranslator.convert(e));
    }
  }

}
