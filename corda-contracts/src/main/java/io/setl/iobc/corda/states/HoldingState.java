package io.setl.iobc.corda.states;

import static io.setl.iobc.corda.contracts.TokenContract.externalId;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import io.setl.iobc.corda.schema.HoldingSchemaV1;

/**
 * Holding state is used in the Holding Contract.
 *
 * @author Elendu Uche.
 */
@Getter
@BelongsToContract(TokenContract.class)
@AllArgsConstructor(onConstructor_ = {@ConstructorForDeserialization})
public class HoldingState implements LinearState, QueryableState {

  /** The total number of tokens held. */
  @With
  private final BigInteger amount;

  /** The corda node that owns this state. */
  private final Party cordaNode;

  /** The external ID of this state. Typically: X500 [rs] token [rs] address. */
  private final String externalId;

  /** When this state item was last updated. */
  private final Instant lastUpdated;

  /** Internal ID of this state. */
  private final UniqueIdentifier linearId;

  /** The number of tokens currently locked. */
  @With
  private final BigInteger locked;

  /**
   * First time creator.
   *
   * @param cordaNode the node that owns the state
   * @param symbol    the token's symbol
   * @param address   the holder's address
   * @param amount    the amount held
   * @param locked    the amount locked
   */
  public HoldingState(
      Party cordaNode,
      String symbol,
      String address,
      BigInteger amount,
      BigInteger locked
  ) {
    this.cordaNode = cordaNode;
    externalId = externalId(cordaNode, symbol, address);
    linearId = new UniqueIdentifier(externalId, UUID.randomUUID());
    this.amount = amount;
    this.locked = locked;
    lastUpdated = Instant.now();
  }


  public HoldingState changeAmount(BigInteger delta) {
    return withAmount(amount.add(delta));
  }


  public HoldingState changeLocked(BigInteger delta) {
    return withLocked(locked.add(delta));
  }


  @NotNull
  @Override
  public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
    if (schema instanceof HoldingSchemaV1) {
      return HoldingSchemaV1.PersistentHolding.builder()
          .amount(amount)
          .cordaNode(cordaNode.getName().toString())
          .externalId(externalId)
          .lastUpdated(lastUpdated)
          .linearId(linearId.getId())
          .lockedAmount(locked)
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
    return ImmutableList.of(new HoldingSchemaV1());
  }

}
