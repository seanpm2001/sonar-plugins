/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractViolationsStaxParser;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.RulesManager;
import org.sonar.plugins.php.core.PhpFile;

/**
 * The Class PmdViolationsXmlParser.
 */
public class PhpCodesnifferViolationsXmlParser extends AbstractViolationsStaxParser {

  private static final String FILE_NAME_ATTRIBUTE_NAME = "name";

  private static final String RULE_NAME_ATTRIBUTE_NAME = "source";

  private static final String LINE_NUMBER_ATTRIBUTE_NAME = "line";

  private static final String VIOLATION_NODE_NAME = "error";

  private static final String FILE_NODE_NAME = "file";

  private static final String MESSAGE_ATTRIBUTE_NAME = "message";

  /** The plugin KEY. */
  public static final String KEY = "PHP_CodeSniffer";

  private static final Logger LOG = LoggerFactory.getLogger(PhpCodesnifferViolationsXmlParser.class);

  /** The project. */
  private Project project;

  /**
   * Instantiates a new checkstyle violations xml parser.
   * 
   * @param project
   * @param context
   * @param rulesManager
   */
  public PhpCodesnifferViolationsXmlParser(Project project, SensorContext context, RulesManager rulesManager, RulesProfile profile) {
    super(context, rulesManager, profile);
    this.project = project;
  }

  /**
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#keyForPlugin()
   */
  @Override
  protected String keyForPlugin() {
    return KEY;
  }

  /**
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#cursorForResources(org .codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected SMInputCursor cursorForResources(SMInputCursor rootCursor) throws XMLStreamException {
    return rootCursor.descendantElementCursor(FILE_NODE_NAME);
  }

  /**
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#cursorForViolations(org.codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected SMInputCursor cursorForViolations(SMInputCursor resourcesCursor) throws XMLStreamException {
    return resourcesCursor.descendantElementCursor(VIOLATION_NODE_NAME);
  }

  /**
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#lineNumberForViolation (org.codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected String lineNumberForViolation(SMInputCursor violationCursor) throws XMLStreamException {
    return violationCursor.getAttrValue(LINE_NUMBER_ATTRIBUTE_NAME);
  }

  /**
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#messageFor(org.codehaus .staxmate.in.SMInputCursor)
   */
  @Override
  protected String messageFor(SMInputCursor violationCursor) throws XMLStreamException {
    return violationCursor.getAttrValue(MESSAGE_ATTRIBUTE_NAME);
  }

  /**
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#ruleKey(org.codehaus .staxmate.in.SMInputCursor)
   */
  @Override
  protected String ruleKey(SMInputCursor violationCursor) throws XMLStreamException {
    return violationCursor.getAttrValue(RULE_NAME_ATTRIBUTE_NAME);
  }

  /**
   * Returns the php file corresponding to the given violation
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#toResource(org.codehaus.staxmate.in.SMInputCursor)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Resource toResource(SMInputCursor resourcesCursor) throws XMLStreamException {
    String fileName = resourcesCursor.getAttrValue(FILE_NAME_ATTRIBUTE_NAME);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loading " + fileName + " to be associated with rule violation");
    }
    return PhpFile.fromIOFile(new File(fileName), project.getFileSystem().getSourceDirs(), false);
  }
}
