package io.setl.iobc.rest.address;

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

import io.setl.iobc.model.address.GetWallet;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Get the contents of a wallet.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/address/wallet")
@RestController
@RequestMapping(path = "/api/address/wallet")
public class GetWalletEndpoint extends BaseEndpoint {


  /** Handle POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public GetWallet.Output handle(@RequestBody @Valid GetWallet.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(GetWallet.NAME, input);
  }


  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "address/wallet",
      summary = "Get the contents of a wallet",
      description = "Get a list of the addresses contained in a wallet."
  )
  public GetWallet.Output handle(
      @NotEmpty
      @RequestParam("walletId")
      @QueryParam("walletId")
      @Parameter(description = "The ID of the wallet to retrieve.", required = true)
          int walletId
  ) throws ParameterisedWebException, InterruptedException {
    GetWallet.Input input = GetWallet.Input.builder()
        .walletId(walletId)
        .build();
    return handle(GetWallet.NAME, input);
  }

}
