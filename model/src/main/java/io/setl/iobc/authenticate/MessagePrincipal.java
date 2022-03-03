package io.setl.iobc.authenticate;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.address.NewAddress;
import io.setl.iobc.model.address.SetlAddress;

/**
 * A principal associated with a message input.
 *
 * @author Simon Greatrix on 27/01/2022.
 */
public abstract class MessagePrincipal {
  private static final Logger logger = LoggerFactory.getLogger(MessagePrincipal.class);

  public static MessagePrincipal forAddress(final String address) {
    return new MessagePrincipal() {
      @Override
      public void resolve(
          Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction
      ) {
        logger.debug("Granting access for address {}",address);
        set(addressFunction.apply(address));
      }
    };
  }


  public static MessagePrincipal forPublic() {
    return new MessagePrincipal() {
      @Override
      public void resolve(
          Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction
      ) {
        logger.debug("Access granted to all.");
        walletId = NewAddress.WALLET_ID_NOT_APPLICABLE;
        chainId = ChainConfiguration.INTERNAL_CHAIN;
      }
    };
  }


  public static MessagePrincipal forToken(final String symbol) {
    return new MessagePrincipal() {
      @Override
      public void resolve(
          Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction
      ) {
        TokenSpecification specification = tokenSpecificationFunction.apply(symbol);
        if (specification != null) {
          logger.debug("Access path is via token {} and controller {}",specification,specification.getController());
          set(addressFunction.apply(specification.getController()));
        } else {
          logger.info("Access denied as token {} is not known",symbol);
          valid = false;
        }
      }
    };
  }


  public static MessagePrincipal forWallet(int myWalletId) {
    return new MessagePrincipal() {
      @Override
      public void resolve(
          Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction
      ) {
        logger.debug("Granting access via wallet {} on the internal chain",myWalletId);
        walletId = myWalletId;
        chainId = ChainConfiguration.INTERNAL_CHAIN;
      }
    };
  }


  public static MessagePrincipal forWallet(int myWalletId, String myChainId) {
    return new MessagePrincipal() {
      @Override
      public void resolve(
          Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction
      ) {
        logger.debug("Granting access via wallet {} on the chain {}",myWalletId,myChainId);
        walletId = myWalletId;
        chainId = myChainId;
      }
    };
  }


  protected String chainId = "$UNSET$";

  protected boolean valid = true;

  protected int walletId = NewAddress.WALLET_ID_NOT_APPLICABLE;


  /** The chain ID associated with the request. */
  public String getChainId() {
    return chainId;
  }


  /** The wallet associated with the request. */
  public int getWalletId() {
    return walletId;
  }


  public boolean isValid() {
    return valid;
  }


  /**
   * Resolve the principal.
   *
   * @param addressFunction            maps address IDs to their instances.
   * @param tokenSpecificationFunction maps token symbols to their instances.
   */
  public abstract void resolve(Function<String, SetlAddress> addressFunction, Function<String, TokenSpecification> tokenSpecificationFunction);


  protected void set(SetlAddress address) {
    if (address != null) {
      logger.debug("Access is granted via address {}",address.getAddress());
      chainId = address.getChainId();
      walletId = address.getWalletId();
    } else {
      logger.info("Denying access as address is not known");
      valid = false;
    }
  }

}
