package io.setl.iobc.corda.schema;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

/**
 * A TokenState schema (version 1).
 *
 * @author Elendu Uche.
 */
public class TokenSchemaV1 extends MappedSchema {

  @Entity
  @Table(
      name = "token_states",
      indexes = {@Index(columnList = "external_id")}
  )
  @Getter
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PersistentToken extends PersistentState {

    @Column(name = "controller")
    String controller;

    @Column(name = "corda_node")
    String cordaNode;

    @Column(name = "external_id")
    String externalId;

    @Column(name = "last_updated")
    Instant lastUpdated;

    @Column(name = "linear_id")
    UUID linearId;

    @Column(name = "name")
    String name;

    @Column(name = "total_supply")
    BigInteger totalSupply;

  }


  public TokenSchemaV1() {
    super(TokenSchema.class, 1, ImmutableList.of(PersistentToken.class));
  }

}
