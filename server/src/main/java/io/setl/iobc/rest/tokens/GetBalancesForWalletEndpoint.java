package io.setl.iobc.rest.tokens;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.setl.iobc.model.address.GetWallet;
import io.setl.iobc.model.tokens.GetBalance;
import io.setl.iobc.model.tokens.GetLocked;
import io.setl.iobc.rest.BaseEndpoint;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Get the balances for all the addresses in a wallet.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Path("/api/tokens/balancesForWallet")
@RestController
@RequestMapping(path = "/api/tokens/balancesForWallet")
public class GetBalancesForWalletEndpoint extends BaseEndpoint {

  /**
   * A tuple linking a balance and a proportion which is locked.
   */
  @Value
  public static class Balance {

    BigInteger balance;

    BigInteger locked;

  }


  /** Handler GET requests. */
  @GET
  @Secured("ROLE_USER")
  @Produces(MediaType.APPLICATION_JSON_VALUE)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(operationId = "tokens/balancesForWallet",
      summary = "Get the balances and locked amount for every address in the wallet",
      description = "Get the number of tokens held and the number of those tokens currently locked against transfer for every address in the wallet",
      responses = {
          @ApiResponse(
              description = "A map showing for every address in the wallet the number of tokens held and how many are locked against transfer",
              content = @Content(
                  schema = @Schema(
                      description = "A map showing for every address in the wallet the number of tokens held and how many are locked against transfer",
                      ref = "#/components/schemas/balancesForWallet"
                  )
              )
          )
      }
  )
  public Map<String, Balance> handle(
      @Parameter(description = "The ID of the wallet whose addresses to query", required = true)
      @NotEmpty @RequestParam("walletId") @QueryParam("walletId") int walletId,
      @Parameter(description = "The symbol identifying the token whose balances to query", required = true)
      @NotEmpty @RequestParam("symbol") @QueryParam("symbol") String symbol
  ) throws ParameterisedWebException, InterruptedException {
    TreeMap<String, Balance> map = new TreeMap<>();

    GetWallet.Input input1 = GetWallet.Input.builder()
        .walletId(walletId)
        .build();
    GetWallet.Output output1 = handle(GetWallet.NAME, input1);

    for (String address : output1.getAddresses()) {
      GetBalance.Input input2 = GetBalance.Input.builder()
          .address(address)
          .symbol(symbol)
          .build();
      BigInteger balance = ((GetBalance.Output) handle(GetBalance.NAME, input2)).getAmount();

      GetLocked.Input input3 = GetLocked.Input.builder()
          .address(address)
          .symbol(symbol)
          .build();
      BigInteger locked = ((GetLocked.Output) handle(GetLocked.NAME, input3)).getAmount();

      map.put(address, new Balance(balance, locked));
    }

    return map;
  }

}
