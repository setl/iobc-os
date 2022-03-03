package io.setl.iobc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.setl.iobc.inbound.InboundProducer;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.iobc.model.address.GetWallet;
import io.setl.iobc.model.address.ListAddresses;
import io.setl.iobc.model.address.NewAddress;

/**
 * The IOBC "Address" API.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Component
public class AddressManagedApi {

  private final InboundProducer producer;


  @Autowired
  public AddressManagedApi(InboundProducer producer) {
    this.producer = producer;
  }


  /**
   * Get all the addresses in a wallet.
   *
   * @param userId the requesting user
   * @param input  the wallet ID.
   *
   * @return the message ID
   */
  public InReplyTo getWallet(String userId, GetWallet.Input input) {
    return producer.sendMessage(userId, GetWallet.NAME, input);
  }


  /**
   * List the addresses in a wallet in full. This includes public keys.
   *
   * @param userId the requesting user
   * @param input  the wallet ID.
   *
   * @return the message ID
   */
  public InReplyTo listAddresses(String userId, ListAddresses.Input input) {
    return producer.sendMessage(userId, ListAddresses.NAME, input);
  }


  /**
   * Create a new address in a wallet.
   *
   * @param userId the requesting user
   * @param input  the wallet ID.
   *
   * @return the message ID
   */
  public InReplyTo newAddress(String userId, NewAddress.Input input) {
    return producer.sendMessage(userId, NewAddress.NAME, input);
  }

}
