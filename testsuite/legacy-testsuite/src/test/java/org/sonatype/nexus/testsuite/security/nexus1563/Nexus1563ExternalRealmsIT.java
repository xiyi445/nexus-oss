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
package org.sonatype.nexus.testsuite.security.nexus1563;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractSecurityTest;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.PlexusUserResource;

import org.junit.Assert;
import org.junit.Test;

import static java.util.Arrays.asList;

public class Nexus1563ExternalRealmsIT
    extends AbstractSecurityTest
{

  @Override
  protected void prepareSecurity() throws Exception {
    super.prepareSecurity();
    GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
    settings.setSecurityRealms(asList("Simple", "NexusAuthenticatingRealm", "NexusAuthorizingRealm"));
    SettingsMessageUtil.save(settings);
  }

  @Test
  public void getExternalUsers()
      throws Exception
  {
    List<PlexusUserResource> users = userUtil.searchPlexusUsers("Simple");
    Assert.assertTrue("User not found", containsUser(users, "admin-simple"));
    Assert.assertTrue("User not found", containsUser(users, "anonymous-simple"));
    Assert.assertTrue("User not found", containsUser(users, "deployment-simple"));
  }

  @Test
  public void getExternalRoles()
      throws Exception
  {
    List<PlexusRoleResource> roles = roleUtil.getRoles("Simple");
    Assert.assertTrue("Role not found", containsRole(roles, "role-123"));
    Assert.assertTrue("Role not found", containsRole(roles, "role-abc"));
    Assert.assertTrue("Role not found", containsRole(roles, "role-xyz"));
  }

  @Test
  public void searchUser()
      throws Exception
  {
    List<PlexusUserResource> users = userUtil.searchPlexusUsers("Simple", "admin-simple");
    Assert.assertEquals("User not found", users.size(), 1);
  }

  private boolean containsRole(List<PlexusRoleResource> roles, String roleName) {
    for (PlexusRoleResource role : roles) {
      if (roleName.equals(role.getRoleId())) {
        return true;
      }
    }
    return false;
  }

  private boolean containsUser(List<PlexusUserResource> users, String userId) {
    for (PlexusUserResource user : users) {
      if (userId.equals(user.getUserId())) {
        return true;
      }
    }
    return false;
  }
}
