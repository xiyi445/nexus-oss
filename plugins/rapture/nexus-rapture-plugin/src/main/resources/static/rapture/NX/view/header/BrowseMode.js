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
/*global Ext*/

/**
 * "browse" mode button.
 *
 * @since 3.0
 */
Ext.define('NX.view.header.BrowseMode', {
  extend: 'Ext.button.Button',
  alias: 'widget.nx-header-browse-mode',
  requires: [
    'NX.I18n'
  ],
  ui: 'nx-header',
  cls: 'nx-modebutton',
  scale: 'medium',
  height: 39,
  width: 39,

  /**
   * @cfg mode Mode name
   */
  mode: 'browse',
  toggleGroup: 'mode',
  allowDepress: false,

  /**
   * @cfg autoHide If button should auto hide when no features are available for selected mode
   */
  autoHide: true,

  /**
   * @cfg collapseMenu If menu should be collapsed automatically when mode is selected
   */
  collapseMenu: true,
  title: NX.I18n.get('Header_BrowseMode_Title'),
  tooltip: NX.I18n.get('Header_BrowseMode_Tooltip'),
  glyph: 'xf1b2@FontAwesome' /* fa-cube */
});
