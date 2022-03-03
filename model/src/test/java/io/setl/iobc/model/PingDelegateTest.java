package io.setl.iobc.model;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import io.setl.iobc.authenticate.AuthenticatedMessage;

/**
 * @author Simon Greatrix on 17/11/2021.
 */
public class PingDelegateTest {

  @Test
  public void testCreate() throws JsonProcessingException {
    PingDelegate.Input input = PingDelegate.Input.builder().message("Time is " + Instant.now()).build();
    AuthenticatedMessage message = new AuthenticatedMessage("client", new PingDelegate().getType(), input);
    System.out.println(new ObjectMapper().writeValueAsString(message));
  }


  @Test
  public void testDeclaration() {
    Class<?> c = PingDelegate.class;
    Method m = Arrays.stream(c.getDeclaredMethods())
        .filter(y -> y.getName().equals("apply"))
        .findAny().get();
    System.out.println(m);
    Type[] types = c.getGenericInterfaces();
    System.out.println(Arrays.toString(types));

    ObjectMapper objectMapper = new ObjectMapper();
    JavaType javaType = objectMapper.constructType(PingDelegate.class);
    JavaType delType = javaType.findSuperType(IobcDelegate.class);
    System.out.println(delType);
    System.out.println(delType.getBindings().findBoundType("InputType"));
  }

}