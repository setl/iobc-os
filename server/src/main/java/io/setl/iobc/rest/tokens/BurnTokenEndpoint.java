package io.setl.iobc.rest.tokens;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.BurnToken;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Endpoint for invoking "burn token".
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/burn")
@RestController
@RequestMapping(path = "/api/tokens/burn")
public class BurnTokenEndpoint extends BaseEndpoint {

  /**
   * Handle a POST request.
   */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public TransactionResult handle(@RequestBody @Valid BurnToken.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(BurnToken.NAME, input);
  }

}
