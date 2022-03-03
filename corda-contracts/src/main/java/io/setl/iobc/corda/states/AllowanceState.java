package io.setl.iobc.corda.states;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import io.setl.iobc.corda.contracts.TokenContract;
import io.setl.iobc.corda.schema.AllowanceSchemaV1;

/**
 * Allowance state is used in the Allowance Contract.
 *
 * @author Elendu Uche.
 */
@Getter
@BelongsToContract(TokenContract.class)
@AllArgsConstructor(onConstructor_ = {@ConstructorForDeserialization})
public class AllowanceState implements LinearState, QueryableState {

  @With
  private final BigInteger amount;

  /** The corda node that owns this state. */
  private final Party cordaNode;

  private final String externalId;

  private final Instant lastUpdated;

  private final UniqueIdentifier linearId;

  private final String owner;

  private final String spender;

  private final String symbol;


  public AllowanceState changeAmount(BigInteger delta) {
    return withAmount(amount.add(delta));
  }


  @NotNull
  @Override
  public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
    if (schema instanceof AllowanceSchemaV1) {
      return AllowanceSchemaV1.PersistentAllowance.builder()
          .amount(amount)
          .cordaNode(cordaNode.getName().toString())
          .externalId(externalId)
          .lastUpdated(lastUpdated)
          .linearId(linearId.getId())
          .owner(owner)
          .spender(spender)
          .symbol(symbol)
          .build();
    } else {
      throw new IllegalArgumentException("Unrecognised schema $schema");
    }
  }


  @NotNull
  @Override
  public UniqueIdentifier getLinearId() {
    return linearId;
  }


  @NotNull
  @Override
  public List<AbstractParty> getParticipants() {
    return Arrays.asList(cordaNode);
  }


  @NotNull
  @Override
  public Iterable<MappedSchema> supportedSchemas() {
    return ImmutableList.of(new AllowanceSchemaV1());
  }

}
