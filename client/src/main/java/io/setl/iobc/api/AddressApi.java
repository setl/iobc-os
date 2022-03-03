package io.setl.iobc.api;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.setl.iobc.inbound.InboundProducer;
import io.setl.iobc.model.address.GetWallet;
import io.setl.iobc.model.address.ListAddresses;
import io.setl.iobc.model.address.NewAddress;

/**
 * The IOBC "Address" API.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Component
public class AddressApi {

  private final InboundProducer producer;


  @Autowired
  public AddressApi(InboundProducer producer) {
    this.producer = producer;
  }


  /**
   * Get all the addresses in a wallet.
   *
   * @param userId the requesting user
   * @param input  the wallet ID.
   *
   * @return the wallet details
   */
  public CompletableFuture<GetWallet.Output> getWallet(String userId, GetWallet.Input input) {
    return producer.send(userId, GetWallet.NAME, input, GetWallet.Output.class);
  }


  /**
   * List the addresses in a wallet in full. This includes public keys.
   *
   * @param userId the requesting user
   * @param input  the wallet ID.
   *
   * @return the full addresses
   */
  public CompletableFuture<ListAddresses.Output> listAddresses(String userId, ListAddresses.Input input) {
    return producer.send(userId, ListAddresses.NAME, input, ListAddresses.Output.class);
  }


  /**
   * Create a new address in a wallet.
   *
   * @param userId the requesting user
   * @param input  the wallet ID.
   *
   * @return the new address identifier
   */
  public CompletableFuture<NewAddress.Output> newAddress(String userId, NewAddress.Input input) {
    return producer.send(userId, NewAddress.NAME, input, NewAddress.Output.class);
  }

}
