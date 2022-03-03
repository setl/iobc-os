package io.setl.iobc.config;

import java.util.Map;

import io.setl.iobc.model.ChainBrand;

/**
 * A factory for generating a suitable chain configuration from a specification.
 *
 * @author Simon Greatrix on 25/01/2022.
 */
public interface ChainConfigurationFactory {

  /**
   * Create the configuration from the properties.
   *
   * @param iobcId     the ID IOBC will use to refer to the chain in its API
   * @param properties the properties provided to configure the chain
   *
   * @return the configuration instance
   */
  ChainConfiguration create(String iobcId, Map<String, Object> properties);


  /**
   * Get the IOBC chain brand supported by this factory.
   *
   * @return the IOBC chain brand
   */
  ChainBrand getIobcBrand();

}
