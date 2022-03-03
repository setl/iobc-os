package io.setl.iobc.model.address;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import io.setl.iobc.util.SerdeSupport;

/**
 * @author Simon Greatrix on 19/11/2021.
 */
public class NewAddressTest {

  @Test
  public void test() throws JsonProcessingException {
    ObjectMapper objectMapper = SerdeSupport.getObjectMapper();
    String json = "{\"walletId\":1}";
    NewAddress.Input input = objectMapper.readValue(json, NewAddress.Input.class);

  }
}