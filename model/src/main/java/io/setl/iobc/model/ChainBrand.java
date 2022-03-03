package io.setl.iobc.model;

/**
 * The kind of chain.
 *
 * @author Simon Greatrix on 25/01/2022.
 */
public enum ChainBrand {
  /** Hyperledger Besu. */
  BESU,

  /** Corda. */
  CORDA,

  /** DAML. */
  DAML,

  /** Hyperledger Fabric. */
  FABRIC,

  /** Special value used for delegates that do not reference a chain. */
  NONE,

  /** The SETL block chain. */
  SETL,

  /** Two or more block-chains which are updated in parallel. */
  SYNC
}
