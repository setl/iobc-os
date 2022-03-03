package io.setl.iobc.authenticate;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.SetlAddress;

/**
 * A principal associated with a message input.
 *
 * @author Simon Greatrix on 27/01/2022.
 */
public class MessagePrincipals implements Iterable<MessagePrincipal> {

  public static MessagePrincipals forAddress(final String address) {
    return new MessagePrincipals(MessagePrincipal.forAddress(address));
  }


  public static MessagePrincipals forPublic() {
    return new MessagePrincipals(MessagePrincipal.forPublic());
  }


  public static MessagePrincipals forToken(final String symbol) {
    return new MessagePrincipals(MessagePrincipal.forToken(symbol));
  }


  public static MessagePrincipals forWallet(int myWalletId) {
    return new MessagePrincipals(MessagePrincipal.forWallet(myWalletId));
  }


  public static MessagePrincipals forWallet(int myWalletId, String myChainId) {
    return new MessagePrincipals(MessagePrincipal.forWallet(myWalletId, myChainId));
  }


  protected final List<MessagePrincipal> principalSet;


  public MessagePrincipals(MessagePrincipal... principal) {
    principalSet = List.of(principal);
  }


  @NotNull
  @Override
  public Iterator<MessagePrincipal> iterator() {
    return principalSet.iterator();
  }


  /**
   * Resolve the principal.
   *
   * @param addressFunction            maps address IDs to their instances.
   * @param tokenSpecificationFunction maps token symbols to their instances.
   *
   * @return this
   */
  public MessagePrincipals resolve(Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction) {
    for (MessagePrincipal principal : principalSet) {
      principal.resolve(addressFunction, tokenSpecificationFunction);
    }
    return this;
  }

}
