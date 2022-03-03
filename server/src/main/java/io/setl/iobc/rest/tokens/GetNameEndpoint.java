package io.setl.iobc.rest.tokens;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.tokens.GetName;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Retrieve a token's name.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/name")
@RestController
@RequestMapping(path = "/api/tokens/name")
public class GetNameEndpoint extends BaseEndpoint {

  /** Handle POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public GetName.Output handle(@RequestBody @Valid TokenId input) throws ParameterisedWebException, InterruptedException {
    return handle(GetName.NAME, input);
  }


  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tokens/name",
      summary = "Get the name of the token",
      description = "Get the name of the token"
  )
  public GetName.Output handle(
      @Parameter(description = "The symbol that identifies the token", required = true)
      @NotEmpty @RequestParam("symbol") @QueryParam("symbol") String symbol
  ) throws ParameterisedWebException, InterruptedException {
    TokenId input = TokenId.builder()
        .symbol(symbol)
        .build();
    return handle(GetName.NAME, input);
  }

}
