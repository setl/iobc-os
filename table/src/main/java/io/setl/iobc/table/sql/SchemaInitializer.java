package io.setl.iobc.table.sql;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.h2.tools.RunScript;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * Initialize the database schema.
 *
 * @author Simon Greatrix on 21/02/2022.
 */
@Service
@ConditionalOnExpression("${setl.iobc.persistence.initialiseSchema:true}")
public class SchemaInitializer implements InitializingBean {

  private final DataSource dataSource;

  private final String jdbcUrl;


  public SchemaInitializer(
      @Value("${setl.iobc.persistence.jdbcUrl}") String jdbcUrl,
      @Qualifier("iobcTableDataSource") DataSource dataSource
  ) {
    this.jdbcUrl = jdbcUrl;
    this.dataSource = dataSource;
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    if (jdbcUrl.startsWith("jdbc:h2:")) {
      initialiseH2();
    }
  }


  private void initialiseH2() throws SQLException, IOException {
    try (
        Connection connection = dataSource.getConnection();
        Reader reader = new InputStreamReader(TableSqlConfiguration.class.getResourceAsStream("/schema_h2.sql"), StandardCharsets.UTF_8)
    ) {
      RunScript.execute(connection, reader);
    }
  }

}
