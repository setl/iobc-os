package io.setl.iobc.rest.tx;

import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.tx.VerifyCreateToken;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Verify if a token creation succeeded.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Path("/api/tx/verifyCreateToken")
@RestController
@RequestMapping(path = "/api/tx/verifyCreateToken")
public class VerifyCreateTokenEndpoint extends BaseEndpoint {

  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tx/verifyCreateToken",
      summary = "Check if a token has been created",
      description = "Check what stage of the process to create a token has been reached."
  )
  public VerifyCreateToken.Output handle(
      @Parameter(description = "The symbol that identifies the token", required = true)
      @NotEmpty @RequestParam("symbol") @QueryParam("symbol") String symbol
  ) throws ParameterisedWebException, InterruptedException {
    TokenId input = TokenId.builder().symbol(symbol).build();
    return handle(VerifyCreateToken.NAME, input);
  }

}
