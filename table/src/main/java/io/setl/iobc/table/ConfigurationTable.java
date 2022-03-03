package io.setl.iobc.table;

/**
 * Configuration data type.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
public interface ConfigurationTable {


  /**
   * Get a configuration value.
   *
   * @param key the configuration property's key
   *
   * @return the configuration property's value, or null
   */
  String get(String key);


  /**
   * Set a configuration value.
   *
   * @param key   the configuration property's key
   * @param value the configuration property's new value
   */
  void put(String key, String value);

}
