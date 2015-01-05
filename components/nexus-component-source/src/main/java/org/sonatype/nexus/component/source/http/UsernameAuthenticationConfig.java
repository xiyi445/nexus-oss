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
package org.sonatype.nexus.component.source.http;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;

/**
 * HTTP client username authentication configuration.
 *
 * @since 3.0
 */
public class UsernameAuthenticationConfig
    implements AuthenticationConfig
{

  public static final String TYPE = "username";

  private String username;

  private String password;

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public UsernameAuthenticationConfig withUsername(final String username) {
    this.username = username;
    return this;
  }

  public UsernameAuthenticationConfig withPassword(final String password) {
    this.password = password;
    return this;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Credentials getCredentials() {
    return new UsernamePasswordCredentials(username, password);
  }

  @Override
  public List<String> getPreferredAuthSchemes() {
    return Lists.newArrayList(AuthSchemes.DIGEST, AuthSchemes.BASIC);
  }

  @Override
  public String toString() {
    return "UsernameAuthenticationConfig{" +
        "username='" + username + '\'' +
        '}';
  }

}
