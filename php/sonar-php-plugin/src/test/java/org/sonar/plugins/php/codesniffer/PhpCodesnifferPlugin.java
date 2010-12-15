/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.codesniffer;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.php.core.PhpPlugin;

/**
 * The PhpCodesniffer plugin. It uses PhpCodeSniffer to analyze classes. The plugin class declares all sensors use for this plugin and all
 * configuration properties.
 */
@Properties({
    @Property(key = PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
        defaultValue = PhpCodeSnifferConfiguration.PHPCS_DEFAULT_REPORT_FILE_PATH, name = "PhpCodesniffer log directory",
        description = "The relative path to the PhpCodeSniffer log directory.", project = true),
    @Property(key = PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_PROPERTY_KEY,
        defaultValue = PhpCodeSnifferConfiguration.PHPCS_DEFAULT_REPORT_FILE_NAME, name = "PhpCodesniffer log file name",
        description = "The PhpCodeSniffer log file name.", project = true),
    @Property(key = PhpCodeSnifferConfiguration.PHPCS_STANDARD_ARGUMENT_KEY,
        defaultValue = PhpCodeSnifferConfiguration.PHPCS_DEFAULT_STANDARD_ARGUMENT, name = "The code sniffer standard argument line",
        description = "The standard to be used by PhpCodeSniffer", project = true),
    @Property(key = PhpCodeSnifferConfiguration.PHPCS_ARGUMENT_LINE_KEY, defaultValue = PhpCodeSnifferConfiguration.PHPCS_DEFAULT_ARGUMENT_LINE,
        name = "The other code sniffer argument line", description = "PhpCodeSniffer will be launched with this arguments", project = true),
    @Property(key = PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_KEY, defaultValue = PhpCodeSnifferConfiguration.PHPCS_DEFAULT_ANALYZE_ONLY,
        name = "Should the plugin only parse analysis report.", description = PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_DESCRIPTION,
        project = true),
    @Property(key = PhpCodeSnifferConfiguration.PHPCS_SHOULD_RUN_KEY, defaultValue = PhpCodeSnifferConfiguration.PHPCS_DEFAULT_SHOULD_RUN,
        name = "Should the plugin run on this project.",
        description = "If set to false, the plugin will not execute itself for this project.", project = true) })
public class PhpCodesnifferPlugin implements Plugin {

  /**
   * Gets the description.
   * 
   * @return the description
   * 
   * @see org.sonar.api.Plugin#getDescription()
   */
  public String getDescription() {
    return "A plugin to cover the PHP_CodeSniffer";
  }

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * 
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    extensions.add(PhpCodesnifferSensor.class);
    extensions.add(PhpCodeSnifferRuleRepository.class);
    return extensions;
  }

  /**
   * Gets the key.
   * 
   * @return the key
   * 
   * @see org.sonar.api.Plugin#getKey()
   */
  public String getKey() {
    return PhpPlugin.CODESNIFFER_PLUGIN_KEY;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   * 
   * @see org.sonar.api.Plugin#getName()
   */
  public String getName() {
    return "PHP_CodeSniffer";
  }

  /**
   * To string.
   * 
   * @return the string
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getKey();
  }
}
