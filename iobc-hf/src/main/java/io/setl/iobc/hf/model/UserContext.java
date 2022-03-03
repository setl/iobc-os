package io.setl.iobc.hf.model;

import java.util.Set;

import lombok.Setter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

/**
 * Specification of the acting user.
 */
@Setter
public class UserContext implements User {

  private String account;

  private String affiliation;

  private Enrollment enrollment;

  private String mspId;

  private String name;

  private Set<String> roles;


  /**
   * Get the user's account.
   *
   * @return the account name
   */
  @Override
  public String getAccount() {
    return account;
  }


  /**
   * Get the user's affiliation.
   *
   * @return the affiliation.
   */
  @Override
  public String getAffiliation() {
    return affiliation;
  }


  /**
   * Get the user's enrollment certificate information.
   *
   * @return the enrollment information.
   */
  @Override
  public Enrollment getEnrollment() {
    return enrollment;
  }


  /**
   * Get the Membership Service Provider Identifier provided by the user's organization.
   *
   * @return MSP Id.
   */
  @Override
  public String getMspId() {
    return mspId;
  }


  /**
   * Get the name that identifies the user.
   *
   * @return the user name.
   */
  @Override
  public String getName() {
    return name;
  }


  /**
   * Get the roles to which the user belongs.
   *
   * @return role names.
   */
  @Override
  public Set<String> getRoles() {
    return roles;
  }

}

