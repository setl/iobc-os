package io.setl.iobc;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.config.ChainConfigurationFactory;

/**
 * Factory for the NONE chain type.
 *
 * @author Simon Greatrix on 25/01/2022.
 */
@Service
public class NoChainConfigurationFactory implements ChainConfigurationFactory {

  @Override
  public ChainConfiguration create(String iobcId, Map<String, Object> properties) {
    return new NoChainConfiguration(iobcId);
  }


  @Override
  public ChainBrand getIobcBrand() {
    return ChainBrand.NONE;
  }

}
