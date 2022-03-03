package io.setl.iobc.table.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;

import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SQL tables.
 *
 * @author Simon Greatrix on 17/11/2021.
 */
@Configuration
@ConditionalOnProperty(name = "setl.iobc.persistence.nature", havingValue = "sql")
public class TableSqlConfiguration {


  /**
   * Create a Data Source that provides access to the persistence DB.
   *
   * @param jdbcUrl  the JDBC URL for the database
   * @param username the database user's name
   * @param password the database user's password
   *
   * @return a Data Source.
   */
  @Bean
  public DataSource iobcTableDataSource(
      @Value("${setl.iobc.persistence.jdbcUrl}") String jdbcUrl,
      @Value("${setl.iobc.persistence.username:}") String username,
      @Value("${setl.iobc.persistence.password:}") String password
  ) {
    Properties properties = new Properties();
    properties.setProperty("url", jdbcUrl);
    properties.setProperty("user", username);
    properties.setProperty("password", password);

    AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
    if (jdbcUrl.startsWith("jdbc:postgresql:")) {
      dataSource.setXaDataSourceClassName(PGXADataSource.class.getName());
    } else if (jdbcUrl.startsWith("jdbc:h2:")) {
      dataSource.setXaDataSourceClassName(org.h2.jdbcx.JdbcDataSource.class.getName());
    } else {
      throw new IllegalArgumentException("Unknown database type. Expected 'postgresql' or 'h2'. URL was " + jdbcUrl);
    }
    dataSource.setXaProperties(properties);
    dataSource.setMaxPoolSize(10);

    return dataSource;
  }

}
