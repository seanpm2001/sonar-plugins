/*
 * Sonar JIRA Reviews Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.reviews.jira.soap;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.core.review.workflow.review.Review;
import org.sonar.plugins.reviews.jira.JiraLinkReviewsConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * SOAP client class that is used for creating issues on a JIRA server
 */
@Properties({
  @Property(
    key = JiraLinkReviewsConstants.SERVER_URL_PROPERTY,
    defaultValue = "",
    name = "Server URL",
    description = "Example : http://jira.codehaus.org",
    global = true,
    project = true
  ),
  @Property(
    key = JiraLinkReviewsConstants.USERNAME_PROPERTY,
    defaultValue = "",
    name = "Username",
    global = true,
    project = true
  ),
  @Property(
    key = JiraLinkReviewsConstants.PASSWORD_PROPERTY,
    defaultValue = "",
    name = "Password",
    global = true,
    project = true
  ),
  @Property(
    key = JiraLinkReviewsConstants.SOAP_BASE_URL_PROPERTY,
    defaultValue = JiraLinkReviewsConstants.SOAP_BASE_URL_DEF_VALUE,
    name = "SOAP base URL",
    description = "Base URL for the SOAP API of the JIRA server",
    global = true,
    project = true
  ),
  @Property(
    key = JiraLinkReviewsConstants.JIRA_PROJECT_KEY_PROPERTY,
    defaultValue = "",
    name = "JIRA project key",
    description = "Key of the JIRA project on which the issues should be created.",
    global = true,
    project = true
  )
})
public class JiraSOAPClient implements ServerExtension {

  private static final Logger LOG = LoggerFactory.getLogger(JiraSOAPClient.class);
  private static final String TASK_ISSUE_TYPE = "3";
  private static final Map<String, String> sonarSeverityToJiraPriority = Maps.newHashMap();

  static {
    sonarSeverityToJiraPriority.put("BLOCKER", "1");
    sonarSeverityToJiraPriority.put("CRITICAL", "2");
    sonarSeverityToJiraPriority.put("MAJOR", "3");
    sonarSeverityToJiraPriority.put("MINOR", "4");
    sonarSeverityToJiraPriority.put("INFO", "5");
  }

  public JiraSOAPClient() {
  }

  @SuppressWarnings("rawtypes")
  public RemoteIssue createIssue(Review review, Settings settings, String commentText) throws RemoteException {
    SOAPSession soapSession = createSoapSession(settings);

    return doCreateIssue(review, soapSession, settings, commentText);
  }

  protected SOAPSession createSoapSession(Settings settings) {
    String jiraUrl = settings.getString(JiraLinkReviewsConstants.SERVER_URL_PROPERTY);
    String baseUrl = settings.getString(JiraLinkReviewsConstants.SOAP_BASE_URL_PROPERTY);

    // get handle to the JIRA SOAP Service from a client point of view
    SOAPSession soapSession = null;
    try {
      soapSession = new SOAPSession(new URL(jiraUrl + baseUrl));
    } catch (MalformedURLException e) {
      throw new IllegalStateException("The JIRA server URL is not a valid one: " + baseUrl, e);
    }
    return soapSession;
  }

  protected RemoteIssue doCreateIssue(Review review, SOAPSession soapSession, Settings settings, String commentText) throws RemoteException {
    // Connect to JIRA
    String userName = settings.getString(JiraLinkReviewsConstants.USERNAME_PROPERTY);
    String password = settings.getString(JiraLinkReviewsConstants.PASSWORD_PROPERTY);
    soapSession.connect(userName, password);

    // The JIRA SOAP Service and authentication token are used to make authentication calls
    JiraSoapService jiraSoapService = soapSession.getJiraSoapService();
    String authToken = soapSession.getAuthenticationToken();

    // And create the issue
    RemoteIssue issue = initRemoteIssue(review, settings, commentText);
    RemoteIssue returnedIssue = jiraSoapService.createIssue(authToken, issue);
    String issueKey = returnedIssue.getKey();
    LOG.debug("Successfully created issue {}", issueKey);

    return returnedIssue;
  }

  protected RemoteIssue initRemoteIssue(Review review, Settings settings, String commentText) {
    RemoteIssue issue = new RemoteIssue();
    issue.setProject(settings.getString(JiraLinkReviewsConstants.JIRA_PROJECT_KEY_PROPERTY));
    issue.setType(TASK_ISSUE_TYPE);
    issue.setPriority(sonarSeverityToJiraPriority(review.getSeverity()));

    issue.setSummary("Sonar Review #" + review.getReviewId());

    StringBuilder description = new StringBuilder("Violation detail:");
    description.append("\n{quote}\n");
    description.append(review.getMessage());
    description.append("\n{quote}\n");
    if (StringUtils.isNotBlank(commentText)) {
      description.append("\nMessage from reviewer:");
      description.append("\n{quote}\n");
      description.append(commentText);
      description.append("\n{quote}\n");
    }
    description.append("\n\nCheck it on Sonar: ");
    description.append(settings.getString("sonar.core.serverBaseURL"));
    description.append("/project_reviews/view/");
    description.append(review.getReviewId());
    issue.setDescription(description.toString());

    return issue;
  }

  protected String sonarSeverityToJiraPriority(String reviewSeverity) {
    String priority = sonarSeverityToJiraPriority.get(reviewSeverity);
    if (priority == null) {
      // default to MAJOR
      priority = "3";
    }
    return priority;
  }

}
