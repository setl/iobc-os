package io.setl.iobc.table.sql;

import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;

import io.setl.iobc.model.address.SetlAddress;
import io.setl.iobc.model.address.SetlAddressBuilder;
import io.setl.iobc.model.address.Wallet;
import io.setl.iobc.table.AddressTable;

/**
 * Address table implementation that uses an SQL database.
 *
 * @author Simon Greatrix on 14/12/2021.
 */
@Service
@ConditionalOnProperty(name = "setl.iobc.persistence.nature", havingValue = "sql")
@Slf4j
public class SqlAddressTable implements AddressTable {

  private final JdbcTemplate template;


  public SqlAddressTable(DataSource dataSource) {
    template = new JdbcTemplate(dataSource);
  }


  @Override
  public SetlAddress getAddress(String addressId) {
    AddressMapper mapper = new AddressMapper(addressId);
    List<SetlAddress> list = template.query(mapper, mapper);
    return list.isEmpty() ? null : list.get(0);
  }


  @Override
  public Wallet getWallet(Integer id) {
    PreparedStatementCreator creator = conn -> {
      PreparedStatement statement = conn.prepareStatement("SELECT setl_address FROM iobc.ADDRESS WHERE walletId = ?");
      statement.setInt(1, id);
      return statement;
    };

    List<String> addresses = template.query(creator, new StringMapper());
    if (addresses.isEmpty()) {
      return null;
    }

    Wallet wallet = new Wallet(id);
    for (String a : addresses) {
      wallet.addAddress(a);
    }
    return wallet;
  }


  @Override
  public CompletableFuture<SetlAddress> insert(SetlAddressBuilder builder) throws GeneralSecurityException {
    AddressInsertCreator creator = new AddressInsertCreator(builder);
    template.update(creator);
    return CompletableFuture.completedFuture(creator.getSetlAddress());
  }


  @Override
  public String lookupAddress(String chainAddress) {
    PreparedStatementCreator creator = conn -> {
      PreparedStatement statement = conn.prepareStatement("SELECT setl_address FROM iobc.ADDRESS WHERE chain_address = ?");
      statement.setString(1, chainAddress);
      return statement;
    };

    List<String> addresses = template.query(creator, new StringMapper());
    if (addresses.isEmpty()) {
      return null;
    }

    return addresses.get(0);
  }

}
