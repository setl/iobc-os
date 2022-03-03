package io.setl.iobc.table.sql;

import java.sql.PreparedStatement;
import java.util.List;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;

import io.setl.iobc.table.ConfigurationTable;

/**
 * Implementation of a configuration table stored in an SQL database.
 *
 * @author Simon Greatrix on 15/12/2021.
 */
@Service
@ConditionalOnProperty(name = "setl.iobc.persistence.nature", havingValue = "sql")
@Slf4j
public class SqlConfigurationTable implements ConfigurationTable {

  private final JdbcTemplate template;


  public SqlConfigurationTable(DataSource dataSource) {
    template = new JdbcTemplate(dataSource);
  }


  @Override
  public String get(String key) {
    PreparedStatementCreator select = con -> {
      PreparedStatement statement = con.prepareStatement("SELECT pair_value FROM iobc.CONFIGURATION WHERE pair_name=?");
      statement.setString(1, key);
      return statement;
    };
    List<String> values = template.query(select, new StringMapper());
    return values.isEmpty() ? null : values.get(0);
  }


  @Override
  public void put(String key, String value) {
    PreparedStatementCreator delete = con -> {
      PreparedStatement statement = con.prepareStatement("DELETE FROM iobc.CONFIGURATION WHERE pair_name=?");
      statement.setString(1, key);
      return statement;
    };

    PreparedStatementCreator insert = con -> {
      PreparedStatement statement = con.prepareStatement("INSERT INTO iobc.CONFIGURATION ( pair_name, pair_value ) VALUES ( ?, ? )");
      statement.setString(1, key);
      statement.setString(2, value);
      return statement;
    };

    template.update(delete);
    template.update(insert);
  }

}
