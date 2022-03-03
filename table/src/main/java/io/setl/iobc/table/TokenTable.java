package io.setl.iobc.table;

import java.util.Map;

import io.setl.common.ParameterisedException;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.SetlAddress;

/**
 * Utility methods for reading token information.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public interface TokenTable {


  /**
   * Delete a token definition from the persistent storage.
   *
   * @param tokenId the token's ID
   */
  void deleteToken(String tokenId);


  /**
   * Get all the tokens known to IOBC.
   *
   * @return the tokens.
   */
  Map<String, TokenSpecification> getAllTokens() throws ParameterisedException;


  /**
   * Get the controller for a token.
   *
   * @param table   the address table
   * @param tokenId the token's ID
   *
   * @return the controller or null.
   */
  default SetlAddress getController(AddressTable table, String tokenId) throws ParameterisedException {
    TokenSpecification specification = getTokenSpecification(tokenId);
    if (specification == null) {
      return null;
    }
    return table.getAddress(specification.getController());
  }


  /**
   * Get the specification for a token as text.
   *
   * @param tokenId the token's ID
   *
   * @return the token's specification, or null
   */
  TokenSpecification getTokenSpecification(String tokenId) throws ParameterisedException;


  /**
   * Insert a new token definition into the persistent storage.
   *
   * @param tokenId the token's ID
   * @param data    the token specification
   */
  void insertToken(String tokenId, TokenSpecification data) throws ParameterisedException;


  /**
   * Update an existing token definition in persistent storage.
   *
   * @param tokenId the token's ID
   * @param newData the token specification
   */
  void updateToken(String tokenId, TokenSpecification newData) throws ParameterisedException;

}
