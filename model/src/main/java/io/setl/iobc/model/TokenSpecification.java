package io.setl.iobc.model;

import java.time.Instant;
import javax.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.common.ParameterisedException;
import io.setl.json.CJObject;
import io.setl.json.Canonical;

/**
 * Specification for a token. Each chain brand may extend this to add chain specific information.
 *
 * @author Simon Greatrix on 26/01/2022.
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenSpecification {

  /**
   * Chain specific data.
   */
  public static class ChainData {

    protected final CJObject data = new CJObject();


    public ChainData(ChainData chainData) {
      data.putAll(chainData.data.copy());
    }


    public ChainData() {
      // do nothing
    }


    @JsonValue
    public JsonObject getJsonValue() {
      return data;
    }


    @JsonAnySetter
    public void set(String key, Object value) {
      data.put(key, Canonical.cast(value));
    }


    public String toString() {
      return getJsonValue().toString();
    }

  }


  /**
   * Check if a token ID and its specification are OK.
   *
   * @param tokenId the token's ID
   * @param spec    the token's specification
   *
   * @throws ParameterisedException if it is not OK
   */
  public static void check(String tokenId, TokenSpecification spec) throws ParameterisedException {
    if (spec == null) {
      CJObject cjObject = new CJObject();
      cjObject.put("token", tokenId);
      throw new ParameterisedException("The token \"" + tokenId + "\" does not exist.", "iobc:unknown-token", cjObject);
    }
    if (spec.isLoading()) {
      CJObject cjObject = new CJObject();
      cjObject.put("token", tokenId);
      cjObject.put("inProgress", spec.getCreateTime().toString());
      throw new ParameterisedException("The token \"" + tokenId + "\" is in the process of being created.", "iobc:unknown-token", cjObject);
    }
  }


  /** The chain's brand. */
  ChainBrand brand;

  /** Chain specific data. */
  @Default
  ChainData chainData = new ChainData();

  /** The chain's ID. */
  String chainId;

  /** The SETL address that owns the contract. */
  String controller;

  /** The time at which the contract was created. */
  Instant createTime;

  /** If true, the token is in the process of being created on the block-chain. */
  boolean loading;

  /** The token's name. */
  String name;

  /** The token's symbol. */
  String symbol;

}
