package io.setl.iobc.table.sql;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementCreator;

import io.setl.iobc.model.address.SetlAddressBuilder;

/**
 * Create a prepared statement that will insert a Wallet Address instance.
 *
 * @author Simon Greatrix on 14/12/2021.
 */
public class AddressInsertCreator implements PreparedStatementCreator {

  private final SetlAddressSql setlAddress;


  public AddressInsertCreator(SetlAddressBuilder builder) throws GeneralSecurityException {
    this.setlAddress = new SetlAddressSql(builder.build(), builder.getChainAddress(), builder.getChainBrand(), builder.getChainId());
  }


  @Override
  public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
    PreparedStatement statement = con.prepareStatement(
        "INSERT INTO iobc.ADDRESS "
            + "( wallet_id, address_type, setl_address, chain_address, chain_brand, chain_id, public_key, wrap_id, private_key, key_type ) "
            + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
    statement.setInt(1, setlAddress.getWalletId());
    statement.setInt(2, setlAddress.getType().getId());
    statement.setString(3, setlAddress.getAddress());
    statement.setString(4, setlAddress.getChainAddress());
    statement.setString(5, setlAddress.getChainBrand().name());
    statement.setString(6, setlAddress.getChainId());
    statement.setBytes(7, setlAddress.getPublicKeyBytes());
    statement.setString(8, setlAddress.getWrapId());
    statement.setBytes(9, setlAddress.getPrivateKeyBytes());
    statement.setString(10, setlAddress.getKeyType());
    return statement;
  }


  public SetlAddressSql getSetlAddress() {
    return setlAddress;
  }

}
