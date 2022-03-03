package io.setl.iobc.rest.tokens;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.tokens.GetAllTokens;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Endpoint to retrieve all known tokes.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Path("/api/tokens/all")
@RestController
@RequestMapping(path = "/api/tokens/all")
public class GetAllTokensEndpoint extends BaseEndpoint {

  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tokens/all",
      summary = "Get the specifications of all known tokens",
      description = "Get the specifications of all tokens known to the system"
  )
  public GetAllTokens.Output handle() throws ParameterisedWebException, InterruptedException {
    return handle(GetAllTokens.NAME, null);
  }

}
