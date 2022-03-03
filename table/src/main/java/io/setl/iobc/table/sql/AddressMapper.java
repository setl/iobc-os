package io.setl.iobc.table.sql;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import io.setl.common.AddressType;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.address.SetlAddressBuilder;

/**
 * Load a SETL address from the database.
 *
 * @author Simon Greatrix on 14/12/2021.
 */
public class AddressMapper implements RowMapper<SetlAddress>, PreparedStatementCreator {

  public static final String SELECT_BY_ADDRESS = "SELECT "
      + "wallet_id, address_type, setl_address, chain_address, chain_brand, chain_id, key_type, public_key, wrap_id, private_key "
      + "FROM iobc.ADDRESS WHERE setl_address=?";

  private String address;


  public AddressMapper(String address) {
    this.address = address;
  }


  @Override
  public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
    PreparedStatement statement = con.prepareStatement(SELECT_BY_ADDRESS);
    statement.setString(1, address);
    return statement;
  }


  @Override
  public SetlAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
    String rsAddress = rs.getString("setl_address");
    SetlAddressBuilder builder = new SetlAddressBuilder()
        .walletId(rs.getInt("wallet_id"))
        .type(AddressType.get(rs.getInt("address_type")))
        .chainAddress(rs.getString("chain_address"))
        .chainId(rs.getString("chain_id"))
        .chainBrand(ChainBrand.valueOf(rs.getString("chain_brand")))
        .publicKey(rs.getBytes("public_key"))
        .wrapId(rs.getString("wrap_id"))
        .encryptedKey(rs.getBytes("private_key"))
        .keyType(rs.getString("key_type"));

    try {
      SetlAddressSql setlAddress = new SetlAddressSql(builder.build(), builder.getChainAddress(), builder.getChainBrand(), builder.getChainId());
      if (!(address.equals(setlAddress.getAddress()) && address.equals(rsAddress))) {
        throw new SQLException("Invalid address. Expected " + address + ", but was " + setlAddress.getAddress() + " @ " + rsAddress);
      }
      return setlAddress;
    } catch (GeneralSecurityException e) {
      throw new SQLException("Invalid encrypted data in database", e);
    }

  }

}
