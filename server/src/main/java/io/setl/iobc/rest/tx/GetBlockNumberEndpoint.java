package io.setl.iobc.rest.tx;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.tx.GetBlockNumber;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Endpoint to retrieve the latest block number.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Path("/api/tx/blockNumber")
@RestController
@RequestMapping(path = "/api/tx/blockNumber")
public class GetBlockNumberEndpoint extends BaseEndpoint {

  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "tx/blockNumber",
      summary = "Get the current block number.",
      description = "Get the current block number."
  )
  public GetBlockNumber.Output handle() throws ParameterisedWebException, InterruptedException {
    return handle(GetBlockNumber.NAME, null);
  }

}
