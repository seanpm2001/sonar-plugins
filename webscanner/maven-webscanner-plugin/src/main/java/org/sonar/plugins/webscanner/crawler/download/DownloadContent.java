/*
 * Maven Webscanner Plugin
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

package org.sonar.plugins.webscanner.crawler.download;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sonar.plugins.webscanner.crawler.frontier.CrawlerTask;
import org.sonar.plugins.webscanner.crawler.parser.Page;

/**
 * Compose filename and download the content.
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public class DownloadContent {

  private static final Logger LOG = Logger.getLogger(DownloadContent.class);

  /**
   * Removes jsessionid from string
   *
   * @param value
   * @return
   */
  private static String makeFileName(String str) {
    String fileName;

    // Removing jsessionid
    if ( !StringUtils.isEmpty(str) && StringUtils.contains(str.toLowerCase(), ";jsessionid")) {
      fileName = str.substring(0, StringUtils.indexOf(str, ";jsessionid"));
    } else {
      fileName = str;
    }

    // Remove numeric and long part elements
    String[] parts = fileName.split("/");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part.length() == 0 || !Character.isDigit(part.charAt(0))) {
        if (sb.length() > 0) {
          sb.append('/');
        }
        sb.append(StringUtils.substring(part, 0, 30));
      }
    }

    return sb.toString();
  }

  private File downloadDirectory;

  public DownloadContent() {

  }

  /**
   * This method is called after each crawl attempt. Warning - it does not matter if it was unsuccessfull attempt or response was
   * redirected. So you should check response code before handling it.
   *
   * @param crawlerTask
   * @param page
   */
  public void afterCrawl(CrawlerTask crawlerTask, Page page) {

    if (page == null) {
      LOG.debug(crawlerTask.getUrl() + " violates crawler constraints (content-type or content-length or other)");
    } else if (page.getResponseCode() >= 300 && page.getResponseCode() < 400) {
      // If response is redirected - crawler schedules new task with new url
      LOG.debug("Response was redirected from " + crawlerTask.getUrl());
    } else if (page.getResponseCode() == HttpURLConnection.HTTP_OK) {
      // Printing url crawled
      LOG.debug(crawlerTask.getUrl() + ". Found " + (page.getLinks() != null ? page.getLinks().size() : 0) + " links.");

      saveContent(crawlerTask, page);
    }
  }

  public File getDownloadDirectory() {
    return downloadDirectory;
  }

  private void saveContent(CrawlerTask crawlerTask, Page page) {
    try {
      URL url = new URL(crawlerTask.getUrl());

      String fileName = makeFileName(url.getPath());
      if (StringUtils.isEmpty(fileName)) {
        fileName = "index";
      } else {
        fileName = URLDecoder.decode(fileName, "UTF-8");
      }
      fileName = StringUtils.stripEnd(fileName, "/");

      StringBuilder path = new StringBuilder();
      path.append(downloadDirectory.getAbsolutePath());
      if ( !fileName.startsWith("/")) {
        path.append('/');
      }

      // for html files we force the extension to html
      if ("text/html".equals(page.getContentType())) {
        path.append(StringUtils.substringBeforeLast(fileName, "."));
        path.append(".html");
      } else {
      // other filenames stay as is.
        path.append(fileName);
      }

      // write content
      writeContent(path.toString(), page.getContentString(), page.getCharset());

      // write headers
      path.append(".txt");
      File propertyFile = new File(path.toString());
      Properties properties = new Properties();
      properties.put("url", crawlerTask.getUrl());
      properties.put("content-type", page.getHeader("content-type"));
      OutputStream out = FileUtils.openOutputStream(propertyFile);
      properties.store(out, null);

      IOUtils.closeQuietly(out);
    } catch (IOException e) {
      LOG.warn("Could not download from " + page.getUrl());
    }
  }

  private void writeContent(String fileName, String content, String charset) {
    OutputStream out = null;
    OutputStreamWriter writer = null;
    try {
      File file = new File(fileName);
      boolean equals = file.exists() && IOUtils.contentEquals(new StringReader(content), new FileReader(file));

      if ( !equals) {
        out = FileUtils.openOutputStream(file);
        writer = new OutputStreamWriter(out, charset);
        writer.write(content);
      }
    } catch (IOException e) {
      LOG.warn("Could not write content to " + fileName);
    } finally {
      IOUtils.closeQuietly(writer);
      IOUtils.closeQuietly(out);
    }
  }

  public void setDownloadDirectory(File downloadDirectory) {
    this.downloadDirectory = downloadDirectory;
  }

}
