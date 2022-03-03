package io.setl.iobc.rest.tx;

import java.time.Instant;
import javax.json.JsonValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.tx.GetBlockForTime;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Handler for getting block numbers that correspond to specific points in time.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Path("/api/tx/blockForTime")
@RestController
@RequestMapping(path = "/api/tx/blockForTime")
public class GetBlockForTimeEndpoint extends BaseEndpoint {

  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tx/blockForTime",
      summary = "Get the block that corresponds to a specific timestamp",
      description = "Get the block height the corresponds to a specific epoch-second or ISO-8601 instant. Specify only the epoch-second or the ISO-8601 "
          + "instant, not both."
  )
  public GetBlockForTime.Output handle(
      @Parameter(description = "The symbol that identifies the token", required = true)
      @RequestParam(value = "symbol") @QueryParam("symbol") String symbol,
      @Parameter(name = "dateTime", in = ParameterIn.QUERY, description = "The ISO-8601 instant to match to a block",
          schema = @Schema(type = "string", format = "date-time"))
      @RequestParam(value = "dateTime", required = false) Instant dateTime,
      @Parameter(description = "The epoch-second to match to a block")
      @RequestParam(value = "epochSecond", required = false) @QueryParam("epochSecond") Long epochSecond
  ) throws ParameterisedWebException, InterruptedException {
    if (((dateTime != null) == (epochSecond != null))) {
      throw new ParameterisedWebException(HttpStatus.BAD_REQUEST, "iobc:missing-parameter", JsonValue.EMPTY_JSON_OBJECT);
    }

    GetBlockForTime.Input input = GetBlockForTime.Input.builder()
        .symbol(symbol)
        .dateTime(dateTime)
        .epochSecond(epochSecond)
        .build();

    return handle(GetBlockForTime.NAME, input);
  }

}
