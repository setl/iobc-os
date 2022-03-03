package io.setl.iobc.table.sql;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Service;

import io.setl.common.ParameterisedException;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.table.TokenTable;
import io.setl.json.Canonical;

/**
 * Implementation of a token table stored in an SQL database.
 *
 * @author Simon Greatrix on 15/12/2021.
 */
@Service
@ConditionalOnProperty(name = "setl.iobc.persistence.nature", havingValue = "sql", matchIfMissing = true)
@Slf4j
public class SqlTokenTable implements TokenTable {

  private final JdbcTemplate template;


  public SqlTokenTable(@Qualifier("iobcTableDataSource") DataSource dataSource) {
    template = new JdbcTemplate(dataSource);
  }


  @Override
  public void deleteToken(String tokenId) {
    PreparedStatementCreator delete = con -> {
      PreparedStatement statement = con.prepareStatement("DELETE FROM iobc.TOKEN WHERE symbol=?");
      statement.setString(1, tokenId);
      return statement;
    };
    template.update(delete);
  }


  @Override
  public Map<String, TokenSpecification> getAllTokens() {
    final HashMap<String, TokenSpecification> map = new HashMap<>();
    TokenMapper mapper = new TokenMapper(null);
    template.query(TokenMapper.SQL_SELECT_ALL, new RowMapperResultSetExtractor<>(mapper)).forEach(t -> map.put(t.getSymbol(), t));
    return map;
  }


  @Override
  public TokenSpecification getTokenSpecification(String tokenId) {
    TokenMapper select = new TokenMapper(tokenId);
    List<TokenSpecification> values = template.query(select, select);
    return values.isEmpty() ? null : values.get(0);
  }


  @Override
  public void insertToken(String tokenId, TokenSpecification data) throws ParameterisedException {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("UTC")));
    calendar.setTimeInMillis(data.getCreateTime().toEpochMilli());

    PreparedStatementCreator insert = con -> {
      PreparedStatement statement = con.prepareStatement("INSERT INTO iobc.TOKEN ("
          + "symbol, controller, chain_id, "
          + "chain_brand, create_time, is_loading, "
          + "name, additional_data"
          + ") VALUES ("
          + "?, ?, ?, "
          + "? ,?, ?, "
          + "?, ?"
          + ")");
      statement.setString(1, data.getSymbol());
      statement.setString(2, data.getController());
      statement.setString(3, data.getChainId());
      statement.setString(4, data.getBrand().name());
      statement.setTimestamp(5, Timestamp.from(data.getCreateTime()), calendar);
      statement.setBoolean(6, data.isLoading());
      statement.setString(7, data.getName());
      statement.setString(8, Canonical.cast(data.getChainData().getJsonValue()).toCanonicalString());
      return statement;
    };

    template.update(insert);
  }


  @Override
  public void updateToken(String tokenId, TokenSpecification data) throws ParameterisedException {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("UTC")));
    calendar.setTimeInMillis(data.getCreateTime().toEpochMilli());

    PreparedStatementCreator update = con -> {
      PreparedStatement statement = con.prepareStatement("UPDATE iobc.TOKEN SET\n"
          + "controller = ?, chain_id = ?, "
          + "chain_brand = ?, create_time = ?, is_loading = ?, "
          + "name = ?, additional_data =? "
          + "WHERE symbol = ?");
      statement.setString(1, data.getController());
      statement.setString(2, data.getChainId());
      statement.setString(3, data.getBrand().name());
      statement.setTimestamp(4, Timestamp.from(data.getCreateTime()), calendar);
      statement.setBoolean(5, data.isLoading());
      statement.setString(6, data.getName());
      statement.setString(7, Canonical.cast(data.getChainData().getJsonValue()).toCanonicalString());
      statement.setString(8, data.getSymbol());
      return statement;
    };

    template.update(update);
  }


}
