package io.setl.iobc.rest.tokens.dvp;

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
import io.setl.iobc.model.tokens.dvp.DvpControllerCommit;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Commit to a DVP trade.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/dvp/controllerCommit")
@RestController
@RequestMapping(path = "/api/tokens/dvp/controllerCommit")
public class DvpControllerCommitEndpoint extends BaseEndpoint {

  /**
   * Accept a POST request.
   *
   * @param input the input
   *
   * @return the transaction result
   *
   * @throws ParameterisedWebException on failure
   * @throws InterruptedException      if interrupted whilst waiting for transaction to complete
   */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public TransactionResult handle(@RequestBody @Valid DvpControllerCommit.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(DvpControllerCommit.NAME, input);
  }

}
