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

import io.setl.iobc.model.tokens.GetAllowance;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Endpoint for retrieving ERC-20 allowances or SETL encumbrances.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/allowance")
@RestController
@RequestMapping(path = "/api/tokens/allowance")
public class GetAllowanceEndpoint extends BaseEndpoint {

  /** Handle POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public GetAllowance.Output handle(@RequestBody @Valid GetAllowance.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(GetAllowance.NAME, input);
  }


  /** Handler GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tokens/allowance",
      summary = "Get the current transfer allowance",
      description = "Get the current transfer allowance for a given token owner and given spender"
  )
  public GetAllowance.Output handle(
      @Parameter(description = "The owner of the tokens that may be transferred.", required = true)
      @NotEmpty @RequestParam("owner") @QueryParam("owner") String owner,
      @Parameter(description = "The address may performing the token transfer.", required = true)
      @NotEmpty @RequestParam("spender") @QueryParam("spender") String spender,
      @Parameter(description = "The symbol that identifies the tokens.", required = true)
      @NotEmpty @RequestParam("symbol") @QueryParam("symbol") String symbol
  ) throws ParameterisedWebException, InterruptedException {
    GetAllowance.Input input = GetAllowance.Input.builder()
        .owner(owner)
        .spender(spender)
        .symbol(symbol)
        .build();
    return handle(GetAllowance.NAME, input);
  }

}
