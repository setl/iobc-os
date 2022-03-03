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
import io.setl.iobc.corda.schema.TokenSchemaV1;

/**
 * Token state is used in the Token Contract.
 *
 * @author Elendu Uche.
 */
@Getter
@BelongsToContract(TokenContract.class)
@AllArgsConstructor(onConstructor_ = {@ConstructorForDeserialization})
public class TokenState implements LinearState, QueryableState {

  /** ID of the controlling user. */
  private final String controller;

  /** The corda node that owns this state. */
  private final Party cordaNode;

  /** The external ID of this state. Typically: X500 [rs] token. */
  private final String externalId;

  /** When this state item was last updated. */
  private final Instant lastUpdated;

  /** Internal ID of this state. */
  private final UniqueIdentifier linearId;

  /** The token's friendly name. */
  private final String name;

  /** The total supply of this token. */
  @With
  private final BigInteger totalSupply;


  /**
   * First time creator.
   *
   * @param cordaNode   the node that owns the state
   * @param symbol      the token's symbol
   * @param name        the amount held
   * @param totalSupply the amount locked
   */
  public TokenState(Party cordaNode, String symbol, String controller, String name, BigInteger totalSupply) {
    this.cordaNode = cordaNode;
    this.controller = controller;
    externalId = externalId(cordaNode, symbol);
    linearId = new UniqueIdentifier(externalId, UUID.randomUUID());
    this.name = name;
    this.totalSupply = totalSupply;
    lastUpdated = Instant.now();
  }


  public TokenState changeSupply(BigInteger delta) {
    return withTotalSupply(totalSupply.add(delta));
  }


  @NotNull
  @Override
  public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
    if (schema instanceof TokenSchemaV1) {
      return TokenSchemaV1.PersistentToken.builder()
          .controller(controller)
          .cordaNode(cordaNode.getName().toString())
          .externalId(externalId)
          .lastUpdated(lastUpdated)
          .linearId(linearId.getId())
          .name(name)
          .totalSupply(totalSupply)
          .build();
    } else {
      throw new IllegalArgumentException("Unrecognised schema $schema");
    }
  }


  public String getController() {
    return controller;
  }


  public Party getCordaNode() {
    return cordaNode;
  }


  public String getExternalId() {
    return externalId;
  }


  public Instant getLastUpdated() {
    return lastUpdated;
  }


  @NotNull
  @Override
  public UniqueIdentifier getLinearId() {
    return linearId;
  }


  public String getName() {
    return name;
  }


  @NotNull
  @Override
  public List<AbstractParty> getParticipants() {
    return Arrays.asList(cordaNode);
  }


  public BigInteger getTotalSupply() {
    return totalSupply;
  }


  @NotNull
  @Override
  public Iterable<MappedSchema> supportedSchemas() {
    return ImmutableList.of(new TokenSchemaV1());
  }

}
