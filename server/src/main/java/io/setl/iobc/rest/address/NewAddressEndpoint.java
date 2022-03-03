package io.setl.iobc.rest.address;

import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.common.io.BaseEncoding;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.setl.common.ParameterisedException;
import io.setl.iobc.model.address.ListAddresses;
import io.setl.iobc.model.address.NewAddress;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.iobc.rest.WebExceptionTranslator;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Create a new address.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/address/create")
@RestController
@RequestMapping(path = "/api/address/create")
public class NewAddressEndpoint extends BaseEndpoint {

  /** Handle POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public NewAddress.Output handle(@RequestBody @Valid NewAddress.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(NewAddress.NAME, input);
  }

}
