package io.setl.iobc.rest.tokens;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.ApproveTransfer;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Approve a transfer, creating a SETL encumbrance, an ERC-20 approval, or similar.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/approve")
@RestController
@RequestMapping(path = "/api/tokens/approve")
public class ApproveTransferEndpoint extends BaseEndpoint {

  /**
   * Accept a POST request.
   *
   * @param input the input
   *
   * @return the transaction result
   *
   * @throws ParameterisedWebException on failure
   * @throws InterruptedException      if interruptes whilst waiting for transaction to complete
   */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public TransactionResult handle(@RequestBody @Valid ApproveTransfer.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(ApproveTransfer.NAME, input);
  }

}
