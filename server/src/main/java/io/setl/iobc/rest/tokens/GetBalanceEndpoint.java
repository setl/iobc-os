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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.tokens.GetBalance;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Retrieve an addresses balance for a token.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/balance")
@RestController
@RequestMapping(path = "/api/tokens/balance")
public class GetBalanceEndpoint extends BaseEndpoint {


  /** Handle POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public GetBalance.Output handle(@RequestBody @Valid GetBalance.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(GetBalance.NAME, input);
  }


  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tokens/balance",
      summary = "Get the number of tokens held by an address",
      description = "Get the number of tokens held by an address now, or at some historic block"
  )
  public GetBalance.Output handle(
      @Parameter(description = "The address that holds the tokens", required = true)
      @NotEmpty @RequestParam("address") @QueryParam("address") String address,
      @Parameter(description = "The symbol that identifies the token", required = true)
      @NotEmpty @RequestParam("symbol") @QueryParam("symbol") String symbol,
      @Parameter(description = "The block to query. (optional)")
      @RequestParam(value = "block", required = false) @QueryParam("block") Integer block
  ) throws ParameterisedWebException, InterruptedException {
    GetBalance.Input.InputBuilder builder = GetBalance.Input.builder()
        .address(address)
        .symbol(symbol);
    if (block != null) {
      builder.block(block);
    }
    return handle(GetBalance.NAME, builder.build());
  }

}
