package io.setl.iobc.corda.schema;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

/**
 * An AllowanceState schema (version 1).
 *
 * @author Elendu Uche.
 */
public class AllowanceSchemaV1 extends MappedSchema {

  @Entity
  @Table(name = "allowance_states")
  @Getter
  @Builder(toBuilder = true)
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PersistentAllowance extends PersistentState {

    @Column(name = "amount")
    BigInteger amount;

    @Column(name = "corda_node")
    String cordaNode;

    @Column(name = "external_id")
    String externalId;

    @Column(name = "last_updated")
    Instant lastUpdated;

    @Column(name = "linear_id")
    UUID linearId;

    @Column(name = "owner")
    String owner;

    @Column(name = "spender")
    String spender;

    @Column(name = "token_symbol")
    String symbol;

  }


  public AllowanceSchemaV1() {
    super(AllowanceSchema.class, 1, ImmutableList.of(AllowanceSchemaV1.PersistentAllowance.class));
  }

}
