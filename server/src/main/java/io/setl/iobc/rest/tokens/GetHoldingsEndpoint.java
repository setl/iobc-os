package io.setl.iobc.rest.tokens;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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

import io.setl.iobc.model.tokens.GetHoldings;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Get the holding of a token.
 *
 * @author Simon Greatrix on 28/11/2021.
 */
@Path("/api/tokens/holdings")
@RestController
@RequestMapping(path = "/api/tokens/holdings")
public class GetHoldingsEndpoint extends BaseEndpoint {

  /** Handle POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public GetHoldings.Output handle(@RequestBody @Valid GetHoldings.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(GetHoldings.NAME, input);
  }


  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tokens/holdings",
      summary = "Get which addresses hold some of a token",
      description = "Get all the addresses who hold some of a token and how much of that token they hold. For tokens held by large numbers of addresses, "
          + "specific range of addresses can be requested."
  )
  public GetHoldings.Output handle(
      @Parameter(description = "The token's identifying symbol", required = true)
      @NotEmpty @RequestParam("symbol") @QueryParam("symbol") String symbol,
      @Parameter(description = "The index of the first address holding to return")
      @RequestParam(value = "start", required = false) @QueryParam("start") Integer start,
      @Parameter(description = "The index of the last address holding to return (exculsive)")
      @RequestParam(value = "end", required = false) @QueryParam("end") Integer end,
      @Parameter(description = "The block height at which to query the block chain.")
      @RequestParam(value = "block", required = false) @QueryParam("block") Integer block
  ) throws ParameterisedWebException, InterruptedException {
    GetHoldings.Input.InputBuilder builder = GetHoldings.Input.builder()
        .symbol(symbol);
    if (start != null) {
      builder.start(start);
    }
    if (end != null) {
      builder.end(end);
    }
    if (block != null) {
      builder.block(block);
    }
    return handle(GetHoldings.NAME, builder.build());
  }

}
