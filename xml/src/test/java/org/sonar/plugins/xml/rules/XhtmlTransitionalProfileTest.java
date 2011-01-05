/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonar.plugins.xml.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.AbstractXmlPluginTester;


public class XhtmlTransitionalProfileTest extends AbstractXmlPluginTester {

  @Test
  public void testCreateProfile() {
    XhtmlTransitionalProfile profile = new XhtmlTransitionalProfile(getProfileDefinition());
    ValidationMessages messages = ValidationMessages.create();
    profile.createProfile(messages);
    assertEquals(0, messages.getErrors().size());
    assertEquals(0, messages.getWarnings().size());
    assertEquals(0, messages.getInfos().size());
  }
}
