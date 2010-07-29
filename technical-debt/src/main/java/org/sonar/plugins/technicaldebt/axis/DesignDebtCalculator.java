/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
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

package org.sonar.plugins.technicaldebt.axis;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.batch.DecoratorContext;

import org.sonar.api.measures.Measure;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.technicaldebt.TechnicalDebtPlugin;

import java.util.List;
import java.util.Arrays;

public final class DesignDebtCalculator extends AxisDebtCalculator {

  public DesignDebtCalculator(Configuration configuration) {
    super(configuration);
  }

  @Override
  public double calculateAbsoluteDebt(DecoratorContext context) {
    Measure measure = context.getMeasure(CoreMetrics.PACKAGE_TANGLES);
    if (!MeasureUtils.hasValue(measure)) {
      return 0.0;
    }
    // technicaldebt is calculated in man days
    return measure.getValue() * getWeight(TechnicalDebtPlugin.TD_COST_CYCLE, TechnicalDebtPlugin.TD_COST_CYCLE_DEFAULT) / HOURS_PER_DAY;
  }

  @Override
  public double calculateTotalPossibleDebt(DecoratorContext context) {
    Measure measure = context.getMeasure(CoreMetrics.PACKAGE_EDGES_WEIGHT);
    if (!MeasureUtils.hasValue(measure)) {
      return 0.0;
    }
    return measure.getValue() / 2 * getWeight(TechnicalDebtPlugin.TD_COST_CYCLE, TechnicalDebtPlugin.TD_COST_CYCLE_DEFAULT) / HOURS_PER_DAY;
  }

  @Override
  public List<Metric> dependsOn() {
    return Arrays.asList(CoreMetrics.PACKAGE_TANGLES, CoreMetrics.PACKAGE_EDGES_WEIGHT);
  }

  @Override
  public String getName() {
    return "Design";

  }
}