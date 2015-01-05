/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.model;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * A Role is a bucket for privileges, can also contain other roles.
 *
 * Note: This class has been generated by modello and copied over
 */
@SuppressWarnings("all")
public class CRole
    implements java.io.Serializable, java.lang.Cloneable
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * The ID used to reference this role.
   */
  private String id;

  /**
   * A descriptive name for the role.
   */
  private String name;

  /**
   * An explanation of the use of the role.
   */
  private String description;

  /**
   * Field privileges.
   */
  private Set<String> privileges;

  /**
   * Field roles.
   */
  private Set<String> roles;

  /**
   * Marks if this role is editable.
   */
  private boolean readOnly = false;

  private String version;

  //-----------/
  //- Methods -/
  //-----------/

  /**
   * Method addPrivilege.
   */
  public void addPrivilege(String string) {
    getPrivileges().add(string);
  } //-- void addPrivilege( String )

  /**
   * Method addRole.
   */
  public void addRole(String string) {
    getRoles().add(string);
  } //-- void addRole( String )

  /**
   * Method clone.
   *
   * @return CRole
   */
  public CRole clone() {
    try {
      CRole copy = (CRole) super.clone();

      if (this.privileges != null) {
        copy.privileges = Sets.newHashSet(this.privileges);
      }

      if (this.roles != null) {
        copy.roles = Sets.newHashSet(this.roles);
      }

      return copy;
    }
    catch (java.lang.Exception ex) {
      throw (java.lang.RuntimeException) new java.lang.UnsupportedOperationException(getClass().getName()
          + " does not support clone()").initCause(ex);
    }
  } //-- CRole clone()

  /**
   * Get an explanation of the use of the role.
   *
   * @return String
   */
  public String getDescription() {
    return this.description;
  } //-- String getDescription()

  /**
   * Get the ID used to reference this role.
   *
   * @return String
   */
  public String getId() {
    return this.id;
  } //-- String getId()

  /**
   * Get a descriptive name for the role.
   *
   * @return String
   */
  public String getName() {
    return this.name;
  } //-- String getName()

  public Set<String> getPrivileges() {
    if (this.privileges == null) {
      this.privileges = Sets.newHashSet();
    }

    return this.privileges;
  } //-- java.util.List<String> getPrivileges()

  public Set<String> getRoles() {
    if (this.roles == null) {
      this.roles = Sets.newHashSet();
    }

    return this.roles;
  }

  /**
   * Get marks if this role is editable.
   *
   * @return boolean
   */
  public boolean isReadOnly() {
    return this.readOnly;
  } //-- boolean isReadOnly()

  /**
   * Method removePrivilege.
   */
  public void removePrivilege(String string) {
    getPrivileges().remove(string);
  } //-- void removePrivilege( String )

  /**
   * Method removeRole.
   */
  public void removeRole(String string) {
    getRoles().remove(string);
  } //-- void removeRole( String )

  /**
   * Set an explanation of the use of the role.
   */
  public void setDescription(String description) {
    this.description = description;
  } //-- void setDescription( String )

  /**
   * Set the ID used to reference this role.
   */
  public void setId(String id) {
    this.id = id;
  } //-- void setId( String )

  /**
   * Set a descriptive name for the role.
   */
  public void setName(String name) {
    this.name = name;
  } //-- void setName( String )

  public void setPrivileges(final Set<String> privileges) {
    this.privileges = privileges;
  }

  /**
   * Set marks if this role is editable.
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  } //-- void setReadOnly( boolean )

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

}
