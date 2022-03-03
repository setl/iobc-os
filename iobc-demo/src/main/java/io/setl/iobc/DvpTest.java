package io.setl.iobc;

import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import io.setl.iobc.model.address.NewAddress;
import io.setl.iobc.model.tokens.CreateToken;
import io.setl.iobc.model.tokens.GetHoldings;
import io.setl.iobc.model.tokens.MintToken;
import io.setl.iobc.model.tokens.TransferToken;
import io.setl.iobc.model.tokens.dvp.DvpCancel;
import io.setl.iobc.model.tokens.dvp.DvpControllerCommit;
import io.setl.iobc.model.tokens.dvp.DvpControllerCreate;
import io.setl.iobc.model.tokens.dvp.DvpId;
import io.setl.iobc.model.tokens.dvp.Party;
import io.setl.json.CJObject;

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
public class DvpTest implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {


  private static final String SYMBOL = UUID.randomUUID().toString();

  private static final String USER = "demo";


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


  private final IobcApi api;

  private final IobcListeners iobcListeners;

  private final IobcManagedApi managed;

  private final Random random = new Random();

  private final TxIobcListener txIobcListener = new TxIobcListener(USER);

  public int dvpPattern = 4;

  public int walletId = random.nextInt(Integer.MAX_VALUE);

  BuildProperties buildProperties;

  ApplicationContext context;

  private final List<String> addresses = new ArrayList<>();

  private final TreeMap<String, AtomicLong> expectedBalances = new TreeMap<>();


  /**
   * New instance.
   *
   * @param buildProperties the Spring build properties.
   */
  @Autowired
  public DvpTest(
      Optional<BuildProperties> buildProperties,
      IobcApi iobcApi,
      IobcManagedApi managedApi,
      IobcListeners iobcListeners
  ) {
    this.buildProperties = buildProperties.orElse(null);
    api = iobcApi;
    managed = managedApi;
    this.iobcListeners = iobcListeners;
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


  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    System.out.println("Application started");
    if (buildProperties != null) {
      System.out.println("Version: " + buildProperties.getVersion() + ", built at " + DateTimeFormatter.ISO_INSTANT.format(buildProperties.getTime()) + "\n");
    }

    Thread thread = new Thread(this::runDemo);
    // Not starting DVP Test
//    thread.start();
  }


  private <T> void output(T v, Throwable t) {
    if (v != null) {
      System.out.println(v);
    }
    if (t != null) {
      t.printStackTrace();
    }
  }


  private void runDemo() {
    try {
      runDemoInternal();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (context != null) {
      System.out.println("Shutting down");
      ((ConfigurableApplicationContext) context).close();
    } else {
      System.out.println("No context available for shutdown");
    }
  }


  /** Run the demo. */
  public void runDemoInternal() throws ExecutionException, InterruptedException {
    System.out.println("Starting test");

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

    NewAddress.Input newAddress = NewAddress.Input.builder().walletId(walletId).build();
    String addr1 = api.getAddressApi().newAddress(USER, newAddress).get().getAddress();
    String addr2 = api.getAddressApi().newAddress(USER, newAddress).get().getAddress();
    String addr3 = api.getAddressApi().newAddress(USER, newAddress).get().getAddress();
    String addr4 = api.getAddressApi().newAddress(USER, newAddress).get().getAddress();

    String bondId = "BOND-" + walletId;
    String cashId = "CASH-" + walletId;

    // Mintable cash
    CreateToken.Input createToken = CreateToken.Input.builder().symbol(cashId).controller(addr1).name("Cash").type("cash").build();
    System.out.println(api.getTokenApi().createToken(USER, createToken).get());

    // Fixed supply bond
    CJObject cjObject = new CJObject();
    cjObject.put("supply", 1000000);
    createToken = CreateToken.Input.builder().symbol(bondId).controller(addr2).name("Bond").type("bond").parameters(cjObject).build();
    System.out.println(api.getTokenApi().createToken(USER, createToken).get());

    MintToken.Input mintToken = MintToken.Input.builder().symbol(cashId).amount(BigInteger.valueOf(3000)).to(addr3).build();
    System.out.println(api.getTokenApi().mintTokens(USER, mintToken).get());

    mintToken = MintToken.Input.builder().symbol(cashId).amount(BigInteger.valueOf(4000)).to(addr4).build();
    System.out.println(api.getTokenApi().mintTokens(USER, mintToken).get());

    TransferToken.Input transfer = TransferToken.Input.builder().from(addr2).to(addr3).amount(BigInteger.valueOf(300)).symbol(bondId).build();
    System.out.println(api.getTokenApi().transferTokens(USER, transfer).get());

    transfer = TransferToken.Input.builder().from(addr2).to(addr4).amount(BigInteger.valueOf(400)).symbol(bondId).build();
    System.out.println(api.getTokenApi().transferTokens(USER, transfer).get());

    System.out.println("\n\n\n#####################################################################################\n\n");
    UUID uuid = UUID.randomUUID();
    System.out.println("UUID = " + uuid);
    if (dvpPattern == 1) {
      // Two controller creates
      DvpControllerCreate.Input dvpCreate = DvpControllerCreate.Input.builder().dvpId(uuid.toString())
          .autoCommit(true)
          .localParty(Party.builder().address(addr3).amount(BigInteger.valueOf(30)).symbol(bondId).build())
          .remoteParty(Party.builder().address(addr4).amount(BigInteger.valueOf(40)).symbol(cashId).build())
          .build();
      System.out.println(api.getTokenApi().dvpControllerCreate(USER, dvpCreate).get());

      DvpId dvpId = DvpId.builder().dvpId(uuid.toString()).symbol(cashId).build();
      System.out.println(api.getTokenApi().getDvpTrade(USER, dvpId).get());

      GetHoldings.Input getHoldings = GetHoldings.Input.builder().symbol(cashId).build();
      System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());
      getHoldings = GetHoldings.Input.builder().symbol(bondId).build();
      System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());

      dvpCreate = DvpControllerCreate.Input.builder().dvpId(uuid.toString())
          .autoCommit(true)
          .remoteParty(Party.builder().address(addr3).amount(BigInteger.valueOf(30)).symbol(bondId).build())
          .localParty(Party.builder().address(addr4).amount(BigInteger.valueOf(40)).symbol(cashId).build())
          .build();
      System.out.println(api.getTokenApi().dvpControllerCreate(USER, dvpCreate).get());
    } else if (dvpPattern == 2) {
      //
      //
      // Controller create and controller commit
      DvpControllerCreate.Input dvpCreate = DvpControllerCreate.Input.builder()
          .dvpId(uuid.toString())                 // Unique ID of the trade. Must be globally unique. I used a random UUID.
          .autoCommit(true)                       // If true, the local party commits immediately as part of the create.
          .fromLocked(false)                      // If true, use previously locked tokens for the local party's commit
          .localParty(Party.builder()
              .address(addr3)                     // Address of the local party
              .amount(BigInteger.valueOf(30))     // Amount sent by local party
              .symbol(bondId)                     // Token sent by local party
              .build())
          .remoteParty(Party.builder()
              .address(addr4)                     // Address of the other party
              .amount(BigInteger.valueOf(40))     // Amount sent by the other party
              .symbol(cashId)                     // Token sent by the other party
              .build())
          .build();

      System.out.println(api.getTokenApi().dvpControllerCreate(USER, dvpCreate).get());

      DvpId dvpId = DvpId.builder().dvpId(uuid.toString()).symbol(cashId).build();
      System.out.println(api.getTokenApi().getDvpTrade(USER, dvpId).get());

      GetHoldings.Input getHoldings = GetHoldings.Input.builder().symbol(cashId).build();
      System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());
      getHoldings = GetHoldings.Input.builder().symbol(bondId).build();
      System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());

      DvpControllerCommit.Input dvpCommit = DvpControllerCommit.Input.builder()
          .dvpId(uuid.toString())   // Unique ID of the trade
          .symbol(cashId)           // Specify symbol to indicate which controller you are
          .fromLocked(false)        // If true, draw tokens from locked amount
          .build();
      System.out.println(api.getTokenApi().dvpControllerCommit(USER, dvpCommit).get());
    } else if (dvpPattern == 3) {
      //
      //
      // Controller create and controller cancel
      DvpControllerCreate.Input dvpCreate = DvpControllerCreate.Input.builder().dvpId(uuid.toString())
          .autoCommit(true)
          .localParty(Party.builder().address(addr3).amount(BigInteger.valueOf(30)).symbol(bondId).build())
          .remoteParty(Party.builder().address(addr4).amount(BigInteger.valueOf(40)).symbol(cashId).build())
          .build();
      System.out.println(api.getTokenApi().dvpControllerCreate(USER, dvpCreate).get());

      DvpId dvpId = DvpId.builder()
          .dvpId(uuid.toString()) // Unique trade ID
          .symbol(cashId)         // Symbol to identify which controller you are
          .build();

      System.out.println(api.getTokenApi().getDvpTrade(USER, dvpId).get());

      GetHoldings.Input getHoldings = GetHoldings.Input.builder().symbol(cashId).build();
      System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());
      getHoldings = GetHoldings.Input.builder().symbol(bondId).build();
      System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());

      DvpCancel.Input dvpCancel = DvpCancel.Input.builder().dvpId(uuid.toString()).symbol(bondId).build();
      System.out.println(api.getTokenApi().dvpCancel(USER, dvpCancel).get());
      System.out.println(api.getTokenApi().getDvpTrade(USER, dvpId).get());
    } else {
      // Try a managed API call
      DvpControllerCreate.Input dvpCreate = DvpControllerCreate.Input.builder().dvpId(uuid.toString())
          .autoCommit(true)
          .localParty(Party.builder().address(addr3).amount(BigInteger.valueOf(30)).symbol(bondId).build())
          .remoteParty(Party.builder().address(addr4).amount(BigInteger.valueOf(40)).symbol(cashId).build())
          .build();
      InReplyTo inReplyTo = managed.getTokenApi().dvpControllerCreate(USER, dvpCreate);
      txIobcListener.waitFor(inReplyTo)
          .thenCompose(handler(inReplyTo))
          .get();
    }

    GetHoldings.Input getHoldings = GetHoldings.Input.builder().symbol(cashId).build();
    System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());
    getHoldings = GetHoldings.Input.builder().symbol(bondId).build();
    System.out.println(api.getTokenApi().getHoldings(USER, getHoldings).get());
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

}
