package io.setl.iobc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.setl.iobc.api.AddressManagedApi;
import io.setl.iobc.api.TokenManagedApi;
import io.setl.iobc.api.UtilityManagedApi;

/**
 * Access the "managed" API where the caller takes on the responsibility of linking messages up with their replies.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Component
public class IobcManagedApi {

  private final AddressManagedApi addressApi;

  private final TokenManagedApi tokenApi;

  private final UtilityManagedApi utilityApi;


  /** New instance. */
  @Autowired
  public IobcManagedApi(AddressManagedApi addressApi, TokenManagedApi tokenApi, UtilityManagedApi utilityApi) {
    this.addressApi = addressApi;
    this.tokenApi = tokenApi;
    this.utilityApi = utilityApi;
  }


  public AddressManagedApi getAddressApi() {
    return addressApi;
  }


  public TokenManagedApi getTokenApi() {
    return tokenApi;
  }


  public UtilityManagedApi getUtilityApi() {
    return utilityApi;
  }

}
