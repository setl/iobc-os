package io.setl.iobc;

import org.springframework.stereotype.Component;

import io.setl.iobc.api.AddressApi;
import io.setl.iobc.api.TokenApi;
import io.setl.iobc.api.UtilityApi;

/**
 * Access the IOBC API.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Component
public class IobcApi {

  private final AddressApi addressApi;

  private final TokenApi tokenApi;

  private final UtilityApi utilityApi;


  /** New instance. */
  public IobcApi(AddressApi addressApi, TokenApi tokenApi, UtilityApi utilityApi) {
    this.addressApi = addressApi;
    this.tokenApi = tokenApi;
    this.utilityApi = utilityApi;
  }


  public AddressApi getAddressApi() {
    return addressApi;
  }


  public TokenApi getTokenApi() {
    return tokenApi;
  }


  public UtilityApi getUtilityApi() {
    return utilityApi;
  }

}
