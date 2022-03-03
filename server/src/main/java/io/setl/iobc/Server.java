package io.setl.iobc;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Server start-up.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    ArtemisAutoConfiguration.class,
    LiquibaseAutoConfiguration.class
})
@Component
public class Server implements ApplicationListener<ApplicationReadyEvent> {

  /** Name used in messages. */
  public static final String NAME = "iobc-server";


  /**
   * Bootstrap the spring application.
   */
  public static void main(String[] args) {

    //Application requires UTC.
    System.setProperty("user.timezone", "UTC");
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    // Bootstrap spring
    new SpringApplicationBuilder(SpringConfiguration.class).headless(true).run(args);
  }


  final BuildProperties buildProperties;


  /**
   * New instance.
   *
   * @param buildProperties the Spring build properties.
   */
  @Autowired
  public Server(
      Optional<BuildProperties> buildProperties
  ) {
    this.buildProperties = buildProperties.orElse(null);
  }


  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    // Generated using the FIGlet "Doom" font.
    System.out.println("\n\n\n"
        + "\n"
        + " _____   _____   ______   _____      _____\n"
        + "|_   _| |  _  |  | ___ \\ /  __ \\    /  ___|\n"
        + "  | |   | | | |  | |_/ / | /  \\/    \\ `--.   ___  _ __ __   __ ___  _ __\n"
        + "  | |   | | | |  | ___ \\ | |         `--. \\ / _ \\| '__|\\ \\ / // _ \\| '__|\n"
        + " _| |_ _\\ \\_/ /_ | |_/ /_| \\__/\\ _  /\\__/ /|  __/| |    \\ V /|  __/| |\n"
        + " \\___/(_)\\___/(_)\\____/(_)\\____/(_) \\____/  \\___||_|     \\_/  \\___||_|\n"
        + "\n"
        + "\n"
        + "  __              ______  _____ ______  _____  _\n"
        + " / _|             | ___ \\|  _  || ___ \\|_   _|| |\n"
        + "| |_  ___   _ __  | |_/ /| | | || |_/ /  | |  | |\n"
        + "|  _|/ _ \\ | '__| |  __/ | | | ||    /   | |  | |\n"
        + "| | | (_) || |    | |    \\ \\_/ /| |\\ \\   | |  | |____\n"
        + "|_|  \\___/ |_|    \\_|     \\___/ \\_| \\_|  \\_/  \\_____/\n"
        + "\n\n\n\n");
    if (buildProperties != null) {
      System.out.println("Version: " + buildProperties.getVersion() + ", built at " + DateTimeFormatter.ISO_INSTANT.format(buildProperties.getTime()) + "\n");
    }

    startNode();
  }


  private void startNode() {
    // initialise this node
  }

}
