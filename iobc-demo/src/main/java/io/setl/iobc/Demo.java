package io.setl.iobc;

import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.ErrorDetails;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;
import io.setl.iobc.model.address.NewAddress;
import io.setl.iobc.model.tokens.BurnToken;
import io.setl.iobc.model.tokens.CreateToken;
import io.setl.iobc.model.tokens.GetBalance;
import io.setl.iobc.model.tokens.GetHoldings;
import io.setl.iobc.model.tokens.GetHoldings.Holding;
import io.setl.iobc.model.tokens.MintToken;
import io.setl.iobc.model.tokens.TransferToken;
import io.setl.util.RuntimeInterruptedException;

/**
 * Demonstration and test of the Kafka API.
 *
 * @author Simon Greatrix on 17/11/2021.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class
})
@Component
public class Demo implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {


  public static final String USER = "demo";

  private static final String SYMBOL = UUID.randomUUID().toString();


  /**
   * Bootstrap the spring application.
   */
  public static void main(String[] args) {

    //Application requires UTC.
    System.setProperty("user.timezone", "UTC");
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    // Bootstrap spring
    ConfigurableApplicationContext context = new SpringApplicationBuilder(SpringConfiguration.class).headless(true).web(WebApplicationType.NONE).run(args);
  }


  private final List<String> addresses = new ArrayList<>();

  private final TreeMap<String, AtomicLong> expectedBalances = new TreeMap<>();

  private final IobcListeners iobcListeners;

  private final IobcManagedApi managedApi;

  private final Random random = new Random();

  private final TxIobcListener txIobcListener = new TxIobcListener(USER);

  public boolean useManagedApi = true;

  public int walletId = random.nextInt(Integer.MAX_VALUE);

  IobcApi api;

  BuildProperties buildProperties;

  ApplicationContext context;

  private String chainId = "corda";


  /**
   * New instance.
   *
   * @param buildProperties the Spring build properties.
   */
  @Autowired
  public Demo(
      Optional<BuildProperties> buildProperties,
      IobcApi iobcApi,
      IobcManagedApi iobcManagedApi,
      IobcListeners iobcListeners
  ) {
    this.buildProperties = buildProperties.orElse(null);
    api = iobcApi;
    managedApi = iobcManagedApi;
    this.iobcListeners = iobcListeners;
  }


  private void checkResult(TransactionResult result, Throwable thrown) {
    if (thrown != null) {
      thrown.printStackTrace();
      return;
    }
    System.out.println(result);
    if (result.getTxStatus() != TxStatus.SUCCESS) {
      throw new IllegalStateException("Transaction failed: " + result.getTxStatus());
    }
  }


  private CompletableFuture<?> getBalances(Object x) {
    List<CompletableFuture<?>> futures = new ArrayList<>();

    for (String a : addresses) {
      System.out.println("Testing balance of " + a + ". Expected value = " + expectedBalances.get(a).get());
      final String addr = a;
      GetBalance.Input input = GetBalance.Input.builder().symbol(SYMBOL).address(addr).build();
      CompletableFuture<?> f1 = api.getTokenApi().getBalance(USER, input).whenComplete(this::output)
          .thenAccept(o -> {
            long reported = o.getAmount().longValue();
            if (expectedBalances.get(addr).get() != reported) {
              System.out.format("Address balance %s: Reported %d. Expected %d -- WRONG%n", addr, reported, expectedBalances.get(addr).get());
            } else {
              System.out.format("Address balance %s: Reported %d. OK%n", addr, reported);
            }
          });
      futures.add(f1);
    }

    return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
  }


  private CompletableFuture<?> getHoldings(Object x) {
    GetHoldings.Input input = GetHoldings.Input.builder().symbol(SYMBOL).build();

    return api.getTokenApi().getHoldings(USER, input)
        .thenAccept(o -> {
          for (Holding h : o.getHoldings()) {
            String addr = h.getAddress();
            long reported = h.getAmount().longValue();
            long expected;
            AtomicLong al = expectedBalances.get(addr);
            if (al == null) {
              System.out.println("UNKNOWN ADDRESS IN HOLDINGS " + addr);
              al = new AtomicLong();
            }
            if (expectedBalances.get(addr).get() != reported) {
              System.out.format("Address balance %s: Reported %d. Expected %d -- WRONG%n", addr, reported, expectedBalances.get(addr).get());
            } else {
              System.out.format("Address balance %s: Reported %d. OK%n", addr, reported);
            }
          }

          long count = expectedBalances.values().stream().filter(a -> a.get() != 0).count();
          if (count == o.getHoldings().size()) {
            System.out.println("All holdings reported");
          } else {
            System.out.format("Reported %d holdings. Expected %d -- WRONG%n", o.getHoldings().size(), count);
          }
        });
  }


  private Function<MessageContent, CompletableFuture<MessageContent>> handler(InReplyTo inReplyTo) {
    return content -> {
      // Got the first reply which is just the transaction hash.
      TransactionResult result = (TransactionResult) content;
      String txHash = result.getTransactionId();
      System.out.format("%s : Tx Hash = %s%n", inReplyTo, txHash);

      // Now to wait for the transaction to complete
      return txIobcListener.waitFor(inReplyTo);
    };
  }


  private CompletableFuture<?> loadAddresses() {
    NewAddress.Input newAddress = NewAddress.Input.builder().walletId(walletId).chainId(chainId).build();
    List<CompletableFuture<?>> futures = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      CompletableFuture<?> f = api.getAddressApi().newAddress(USER, newAddress).whenComplete(this::output).thenAccept(o -> {
        addresses.add(o.getAddress());
        expectedBalances.put(o.getAddress(), new AtomicLong(0));
      });
      waitFor(f);
      futures.add(f);
    }
    return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
  }


  private CompletableFuture<?> managedBurnToken(Object x) {
    System.out.println("Sending burn commands");
    List<CompletableFuture<?>> futures = new ArrayList<>();
    for (int i = 1; i < addresses.size(); i++) {
      if (random.nextBoolean()) {
        continue;
      }

      final String addr = addresses.get(i);
      final BigInteger amount = BigInteger.valueOf(expectedBalances.get(addr).get());
      expectedBalances.put(addr, new AtomicLong(0));
      System.out.format("Burning %d assets from %s%n", amount, addr);
      BurnToken.Input input = BurnToken.Input.builder().symbol(SYMBOL).amount(amount).from(addr).build();
      InReplyTo inReplyTo = managedApi.getTokenApi().burnToken(USER, input);
      CompletableFuture<?> f = txIobcListener.waitFor(inReplyTo)
          .thenCompose(handler(inReplyTo)).whenComplete((c, t) -> checkResult((TransactionResult) c, t))
          .whenComplete((b, c) -> System.out.format("Burn token complete for %d from %s%n", amount, addr));
      futures.add(f);
      waitFor(f);
    }

    return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
  }


  private CompletableFuture<?> managedCreateCash(Object x) {
    CreateToken.Input input = CreateToken.Input.builder().controller(addresses.get(0)).name("My amazing asset").symbol(SYMBOL).type("CASH").build();
    final InReplyTo inReplyTo = managedApi.getTokenApi().createToken(USER, input);
    return txIobcListener.waitFor(inReplyTo)
        .thenCompose(handler(inReplyTo)).whenComplete((c, t) -> checkResult((TransactionResult) c, t))
        .whenComplete((b, c) -> {
          System.out.println("Create token complete");
          if (c == null) {
            System.out.println("Waiting 5 seconds for token to fully commit");
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new RuntimeInterruptedException(e);
            }
          }
        });
  }


  private CompletableFuture<?> managedMintToken(Object x) {
    System.out.println("Sending mint commands");
    List<CompletableFuture<?>> futures = new ArrayList<>();
    for (int i = 1; i < addresses.size(); i++) {
      final String addr = addresses.get(i);
      final BigInteger amount = BigInteger.valueOf(8000 + random.nextInt(10000));
      expectedBalances.put(addr, new AtomicLong(amount.longValue()));
      System.out.format("Minting %d assets to %s%n", amount, addr);
      MintToken.Input input = MintToken.Input.builder().symbol(SYMBOL).amount(amount).to(addr).build();
      InReplyTo inReplyTo = managedApi.getTokenApi().mintTokens(USER, input);
      CompletableFuture<?> f = txIobcListener.waitFor(inReplyTo)
          .thenCompose(handler(inReplyTo)).whenComplete((c, t) -> checkResult((TransactionResult) c, t))
          .whenComplete((b, c) -> System.out.format("Mint token complete for %d to %s%n", amount, addr));
      waitFor(f);

      futures.add(f);
    }

    return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
  }


  private CompletableFuture<?> managedTransfer(Object x) {
    List<CompletableFuture<?>> futures = new ArrayList<>();

    HashMap<String, Long> available = new HashMap<>();
    for (var e : expectedBalances.entrySet()) {
      available.put(e.getKey(), e.getValue().longValue());
    }

    for (int i = 0; i < 10; i++) {
      int toIndex = 1 + random.nextInt(addresses.size() - 1);
      String toAddr = addresses.get(toIndex);
      int fromIndex;
      String tryAddr;
      do {
        fromIndex = 1 + random.nextInt(addresses.size() - 1);
        tryAddr = addresses.get(fromIndex);
      } while (toIndex == fromIndex || available.get(tryAddr) < 10);
      final String fromAddr = tryAddr;
      System.out.format("Transferring from %s with holding %d%n", fromAddr, available.get(fromAddr));
      int bound = Math.min(100, (int) (available.get(fromAddr) / 2));
      System.out.println("Upper bound is " + bound);
      BigInteger amount = BigInteger.valueOf(random.nextInt(bound));
      System.out.format("Transferring %d out of %d from %s to %s%n", amount, expectedBalances.get(fromAddr).get(), toAddr, fromAddr);
      available.put(tryAddr, available.get(tryAddr) - amount.longValue());

      expectedBalances.get(toAddr).addAndGet(amount.longValue());
      expectedBalances.get(fromAddr).addAndGet(-amount.longValue());
      TransferToken.Input input = TransferToken.Input.builder().symbol(SYMBOL).amount(amount).to(toAddr).from(fromAddr).build();
      InReplyTo inReplyTo = managedApi.getTokenApi().transferTokens(USER, input);
      CompletableFuture<?> f1 = txIobcListener.waitFor(inReplyTo)
          .thenCompose(handler(inReplyTo)).whenComplete((c, t) -> checkResult((TransactionResult) c, t))
          .whenComplete(
              (b, c) -> {
                if (c == null) {
                  System.out.format("Transfer token complete: %d from %s to %s%n", amount, toAddr, fromAddr);
                } else {
                  System.out.format("Transfer token FAILED: %d from %s to %s%n", amount, toAddr, fromAddr);
                }
              }
          );
      waitFor(f1);

      futures.add(f1);
    }

    return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
  }


  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    System.out.println("Application DEMO started");
    if (buildProperties != null) {
      System.out.println("Version: " + buildProperties.getVersion() + ", built at " + DateTimeFormatter.ISO_INSTANT.format(buildProperties.getTime()) + "\n");
    }

    Thread thread = new Thread(this::runDemo);
    thread.start();
  }


  private <T> void output(T v, Throwable t) {
    if (v != null) {
      System.out.println(v);
    }
    if (t != null) {
      t.printStackTrace();
    }
  }


  /** Run the demo. */
  public void runDemo() {
    System.out.println("Starting demo");

    iobcListeners.addListener(new IobcExtendedListener() {
      @Override
      public void acceptFailure(InReplyTo inReplyTo, String type, ErrorDetails errorDetails) {
        System.out.println("Failed");
      }


      @Override
      public void acceptSuccess(InReplyTo inReplyTo, String type, MessageContent content) {
        System.out.println("Success");
      }
    });
    iobcListeners.addListener(txIobcListener);

    loadAddresses()
        .thenComposeAsync(this::managedCreateCash)
        .thenComposeAsync(this::managedMintToken)
        .thenComposeAsync(this::getBalances)
        .thenComposeAsync(this::managedTransfer)
        .thenComposeAsync(this::getBalances)
//        .thenComposeAsync(this::getHoldings)
        .thenComposeAsync(this::managedBurnToken)
//        .thenComposeAsync(this::getHoldings)
        .thenComposeAsync(this::managedTransfer)
        .thenComposeAsync(this::getBalances)
//        .thenComposeAsync(this::getHoldings)
        .whenComplete((c, t) -> {
          if (t != null) {
            t.printStackTrace();
          } else {
            System.out.println("Finished");
          }
          if (context != null) {
            System.out.println("Shutting down");
            ((ConfigurableApplicationContext) context).close();
          } else {
            System.out.println("No context available for shutdown");
          }
        })
    ;

  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }


  private void waitFor(CompletableFuture<?> f) {
    try {
      f.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

}
