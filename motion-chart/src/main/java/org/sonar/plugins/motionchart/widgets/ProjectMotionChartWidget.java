/*
 * Sonar Motion Chart plugin
 * Copyright (C) 2009 SonarSource
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

package org.sonar.plugins.motionchart.widgets;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.WidgetCategory;
import org.sonar.api.web.WidgetProperties;
import org.sonar.api.web.WidgetProperty;
import org.sonar.api.web.WidgetPropertyType;

import static org.sonar.plugins.motionchart.widgets.WidgetConstants.CHART_HEIGHT_PROP_DEF_VALUE;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.CHART_HEIGHT_PROP_KEY;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_COLOR_PROP_DEF_VALUE;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_COLOR_PROP_KEY;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_SIZE_PROP_DEF_VALUE;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_SIZE_PROP_KEY;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_X_PROP_DEF_VALUE;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_X_PROP_KEY;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_Y_PROP_DEF_VALUE;
import static org.sonar.plugins.motionchart.widgets.WidgetConstants.METRIC_Y_PROP_KEY;

@WidgetCategory({"History"})
@WidgetProperties(
{
  @WidgetProperty(key = "title", type = WidgetPropertyType.STRING),
  @WidgetProperty(key = "showComponentsByDefault", type = WidgetPropertyType.BOOLEAN, defaultValue = "true"),
  @WidgetProperty(key = METRIC_X_PROP_KEY, type = WidgetPropertyType.METRIC, defaultValue = METRIC_X_PROP_DEF_VALUE),
  @WidgetProperty(key = METRIC_Y_PROP_KEY, type = WidgetPropertyType.METRIC, defaultValue = METRIC_Y_PROP_DEF_VALUE),
  @WidgetProperty(key = METRIC_COLOR_PROP_KEY, type = WidgetPropertyType.METRIC, defaultValue = METRIC_COLOR_PROP_DEF_VALUE),
  @WidgetProperty(key = METRIC_SIZE_PROP_KEY, type = WidgetPropertyType.METRIC, defaultValue = METRIC_SIZE_PROP_DEF_VALUE),
  @WidgetProperty(key = CHART_HEIGHT_PROP_KEY, type = WidgetPropertyType.INTEGER, defaultValue = CHART_HEIGHT_PROP_DEF_VALUE)
})
public class ProjectMotionChartWidget extends AbstractRubyTemplate implements RubyRailsWidget {
  public String getId() {
    return "project_motion_chart";
  }

  public String getTitle() {
    return "Project Motion Chart";
  }

  @Override
  protected String getTemplatePath() {
    return "/org/sonar/plugins/motionchart/widgets/project_motion_chart.html.erb";
    // return
    // "/Users/fbellingard/Documents/Sonar/workspace/sonar-plugins/motion-chart/src/main/resources/org/sonar/plugins/motionchart/widgets/project_motion_chart.html.erb";
  }
}
