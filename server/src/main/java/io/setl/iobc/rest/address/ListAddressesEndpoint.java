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
import io.setl.iobc.model.address.ListAddresses;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * List the addresses in a wallet.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/address/list")
@RestController
@RequestMapping(path = "/api/address/list")
public class ListAddressesEndpoint extends BaseEndpoint {

  /** Handler POST requests. */
  @Hidden
  @POST
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ListAddresses.Output handle(@RequestBody @Valid ListAddresses.Input input) throws ParameterisedWebException, InterruptedException {
    return handle(ListAddresses.NAME, input);
  }


  /** Handle GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "address/list",
      summary = "Get the detailed contents of a wallet",
      description = "Get a detailed list of the addresses contained in a wallet."
  )
  public ListAddresses.Output handle(
      @NotEmpty
      @QueryParam("walletId")
      @RequestParam("walletId")
      @Parameter(description = "The ID of the wallet to retrieve.", required = true)
          int walletId
  ) throws ParameterisedWebException, InterruptedException {
    ListAddresses.Input input = ListAddresses.Input.builder()
        .walletId(walletId)
        .build();
    return handle(ListAddresses.NAME, input);
  }

}

