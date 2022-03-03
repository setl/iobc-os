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

import io.setl.iobc.model.tokens.GetTotalSupply;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Get the available supply for a token.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/supply")
@RestController
@RequestMapping(path = "/api/tokens/supply")
public class GetTotalSupplyEndpoint extends BaseEndpoint {

  /** Handle POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public GetTotalSupply.Output handle(@RequestBody @Valid GetTotalSupply.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(GetTotalSupply.NAME, input);
  }


  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tokens/supply",
      summary = "Get the total number of tokens in existence",
      description = "Get the total number of tokens in existence. Optionally, a specific chain block may be specified to query"
  )
  public GetTotalSupply.Output handle(
      @Parameter(description = "The symbol that identifies the token", required = true)
      @NotEmpty @RequestParam("symbol") @QueryParam("symbol") String symbol,
      @Parameter(description = "The block to query (optional).")
      @RequestParam(value = "block", required = false) @QueryParam("block") Long block
  ) throws ParameterisedWebException, InterruptedException {
    GetTotalSupply.Input.InputBuilder input = GetTotalSupply.Input.builder()
        .symbol(symbol);
    if (block != null) {
      input.block(block);
    }
    return handle(GetTotalSupply.NAME, input.build());
  }

}
