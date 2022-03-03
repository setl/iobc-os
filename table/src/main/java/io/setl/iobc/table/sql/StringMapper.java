package io.setl.iobc.table.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Extract a string value.
 *
 * @author Simon Greatrix on 15/12/2021.
 */
public class StringMapper implements RowMapper<String> {

  @Override
  public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    return rs.getString(1);
  }

}
