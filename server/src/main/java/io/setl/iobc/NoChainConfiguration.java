package io.setl.iobc;

import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.config.ChainConfiguration;

/**
 * Configuration for delegates that do not operate on a chain.
 *
 * @author Simon Greatrix on 25/01/2022.
 */
public class NoChainConfiguration implements ChainConfiguration {

  private final String iobcId;


  public NoChainConfiguration(String iobcId) {
    this.iobcId = iobcId;
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.NONE;
  }


  @Override
  public String getIobcId() {
    return iobcId;
  }

}
