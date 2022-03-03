package io.setl.iobc.table.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import io.setl.common.ParameterisedException;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.TokenSpecification.ChainData;
import io.setl.iobc.util.SerdeSupport;

/**
 * Row mapper for token specifications.
 *
 * @author Simon Greatrix on 20/02/2022.
 */
@Slf4j
public class TokenMapper implements RowMapper<TokenSpecification>, PreparedStatementCreator {

  public static final String SQL_SELECT_ALL =
      "SELECT SYMBOL, CONTROLLER ,CHAIN_ID, "
          + "CHAIN_BRAND, CREATE_TIME, IS_LOADING, "
          + "NAME, ADDITIONAL_DATA "
          + "FROM IOBC.TOKEN";

  private static final String SQL_SELECT_BY_SYMBOL = SQL_SELECT_ALL + " WHERE SYMBOL=?";

  private final String symbol;


  public TokenMapper(String symbol) {
    this.symbol = symbol;
  }


  @Override
  public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
    PreparedStatement statement = con.prepareStatement(SQL_SELECT_BY_SYMBOL);
    statement.setString(1, symbol);
    return statement;
  }


  @Override
  public TokenSpecification mapRow(ResultSet rs, int rowNum) throws SQLException {
    ChainData chainData = null;
    String mySymbol = rs.getString(1);
    String rawData = rs.getString(8);
    try {
      chainData = SerdeSupport.getInstance(rawData, ChainData.class);
    } catch (ParameterisedException e) {
      log.error("Invalid data for token {}: {}", mySymbol, rawData);
      throw new SQLDataException("Invalid additional token data for " + mySymbol, e);
    }
    return TokenSpecification.builder()
        .symbol(mySymbol)
        .controller(rs.getString(2))
        .chainId(rs.getString(3))
        .brand(ChainBrand.valueOf(rs.getString(4)))
        .createTime(rs.getTimestamp(5).toInstant())
        .loading(rs.getBoolean(6))
        .name(rs.getString(7))
        .chainData(chainData)
        .build();
  }

}
