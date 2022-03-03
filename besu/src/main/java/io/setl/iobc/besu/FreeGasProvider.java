package io.setl.iobc.besu;

import java.math.BigInteger;

import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

/**
 * A Gas provider which has an infinite supply of free gas.
 *
 * @author Simon Greatrix on 14/11/2021.
 */
public class FreeGasProvider implements ContractGasProvider {

  public static final ContractGasProvider INSTANCE = new FreeGasProvider();


  @Override
  public BigInteger getGasLimit(String contractFunc) {
    return DefaultGasProvider.GAS_LIMIT;
  }


  /**
   * {@inheritDoc}
   *
   * @deprecated Deprecated in parent interface.
   */
  @Deprecated(since = "Creation")
  @Override
  public BigInteger getGasLimit() {
    return DefaultGasProvider.GAS_LIMIT;
  }


  @Override
  public BigInteger getGasPrice(String contractFunc) {
    return BigInteger.ZERO;
  }


  /**
   * {@inheritDoc}
   *
   * @deprecated Deprecated in parent interface.
   */
  @Deprecated(since = "Creation")
  @Override
  public BigInteger getGasPrice() {
    return BigInteger.ZERO;
  }

}
