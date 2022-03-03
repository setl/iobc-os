package io.setl.iobc.config;

import io.setl.iobc.model.ChainBrand;

/**
 * Configuration of a chain that provides everything a delegate needs to perform its task.
 *
 * @author Simon Greatrix on 25/01/2022.
 */
public interface ChainConfiguration {

  /** The name of the "internal" chain used when an API does not actually refer to a chain at all. */
  String INTERNAL_CHAIN = "$INTERNAL$";


  /**
   * Get the Brand of this chain for delegate matching. It can be assumed that there is only one implementation of configuration per brand, so an instance
   * can be safely cast to the corresponding brand specific instance.
   *
   * @return the chain brand
   */
  ChainBrand getIobcBrand();


  /**
   * Get the ID of this chain as it is known to IOBC.
   *
   * @return the ID used in the IOBC API
   */
  String getIobcId();

}
