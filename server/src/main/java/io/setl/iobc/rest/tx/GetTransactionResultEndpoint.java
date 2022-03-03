package io.setl.iobc.rest.tx;

import java.math.BigInteger;
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

import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tx.GetTransactionResult;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Get the result of a transaction.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Path("/api/tx/transactionResult")
@RestController
@RequestMapping(path = "/api/tx/transactionResult")
public class GetTransactionResultEndpoint extends BaseEndpoint {

  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(operationId = "tx/transactionResult",
      summary = "Get the outcome of a transaction",
      description = "Get the outcome of a transaction")
  public TransactionResult handle(
      @Parameter(description = "The ID of the transaction, typically its hash.", required = true)
      @NotEmpty @RequestParam("id") @QueryParam("id") String txHash,
      @Parameter(description = "The earliest block to look in for a matching transaction (optional).")
      @RequestParam(value = "block", required = false) @QueryParam("block") BigInteger blockNumber
  ) throws ParameterisedWebException, InterruptedException {
    GetTransactionResult.Input input = GetTransactionResult.Input.builder()
        .transactionId(txHash)
        .blockNumber(blockNumber)
        .build();
    return handle(GetTransactionResult.NAME, input);
  }

}
