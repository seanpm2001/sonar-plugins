/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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
package org.sonar.plugins.debt;

import java.util.ArrayList;
import java.util.List;

import org.sonar.commons.Language;
import org.sonar.commons.Languages;
import org.sonar.commons.Metric;
import org.sonar.commons.resources.Resource;
import org.sonar.commons.resources.Measure;
import org.sonar.plugins.api.Java;
import org.sonar.plugins.api.measures.PropertiesBuilder;
import org.sonar.plugins.api.jobs.AbstractJob;
import org.sonar.plugins.api.jobs.JobContext;
import org.sonar.plugins.api.metrics.CoreMetrics;
import org.apache.commons.configuration.Configuration;

public class TechnicalDebtJob extends AbstractJob {

  private final Configuration configuration;

  public TechnicalDebtJob(Languages languages, Configuration configuration) {
    super(languages);
    this.configuration = configuration;
  }

  public java.util.List<Metric> dependsOnMetrics() {
    List<Metric> metrics = new ArrayList<Metric>();
    metrics.add(CoreMetrics.DUPLICATED_BLOCKS);
    metrics.add(CoreMetrics.MANDATORY_VIOLATIONS);
    metrics.add(CoreMetrics.PUBLIC_UNDOCUMENTED_API);
    metrics.add(CoreMetrics.UNCOVERED_COMPLEXITY_BY_TESTS);
    return metrics;
  }

  public java.util.List<Metric> generatesMetrics() {
    List<Metric> metrics = new ArrayList<Metric>();
    metrics.add(TechnicalDebtMetrics.TOTAL_TECHNICAL_DEBT);
    metrics.add(TechnicalDebtMetrics.EXTRA_TECHNICAL_DEBT);
    metrics.add(TechnicalDebtMetrics.SONAR_TECHNICAL_DEBT);
    metrics.add(TechnicalDebtMetrics.TOTAL_TECHNICAL_DEBT_DAYS);
    metrics.add(TechnicalDebtMetrics.TECHNICAL_DEBT_REPARTITION);
    return metrics;
  }

  @Override
  protected boolean shouldExecuteOnLanguage(Language language) {
    return language.equals(new Java());
  }

  public boolean shouldExecuteOnResource(Resource resource) {
    return true;
  }

  public void execute(JobContext jobContext) {
    double duplicationDebt = this.calculateMetricDebt(jobContext, CoreMetrics.DUPLICATED_BLOCKS, TechnicalDebtPlugin.COST_DUPLI_BLOCK, TechnicalDebtPlugin.COST_DUPLI_BLOCK_DEFAULT);
    double violationsDebt = this.calculateMetricDebt(jobContext, CoreMetrics.MANDATORY_VIOLATIONS, TechnicalDebtPlugin.COST_VIOLATION, TechnicalDebtPlugin.COST_VIOLATION_DEFAULT);
    double undocumApiDebt = this.calculateMetricDebt(jobContext, CoreMetrics.PUBLIC_UNDOCUMENTED_API, TechnicalDebtPlugin.COST_UNDOCUMENTED_API, TechnicalDebtPlugin.COST_UNDOCUMENTED_API_DEFAULT);
    double uncovComplexDebt = this.calculateMetricDebt(jobContext, CoreMetrics.UNCOVERED_COMPLEXITY_BY_TESTS, TechnicalDebtPlugin.COST_UNCOVERED_COMPLEXITY, TechnicalDebtPlugin.COST_UNCOVERED_COMPLEXITY_DEFAULT);
    // compute the debt by day and calculate the cost
    double sonarDebt = (duplicationDebt + violationsDebt + undocumApiDebt + uncovComplexDebt) / 8.0;
    //compute extra debt (input manually) by day
    double extraDebt = calculateExtraDebt(jobContext);

    PropertiesBuilder techDebtRepartition = new PropertiesBuilder(TechnicalDebtMetrics.TECHNICAL_DEBT_REPARTITION);
    techDebtRepartition.add("Violations", violationsDebt);
    techDebtRepartition.add("Duplication", duplicationDebt);
    techDebtRepartition.add("Comments", undocumApiDebt);
    techDebtRepartition.add("Complexity", uncovComplexDebt);
    techDebtRepartition.add("Other", extraDebt);

    double dailyRate = getWeight(TechnicalDebtPlugin.DAILY_RATE, TechnicalDebtPlugin.DAILY_RATE_DEFAULT);
    double daysDebt = sonarDebt + extraDebt;
    jobContext.addMeasure(TechnicalDebtMetrics.TOTAL_TECHNICAL_DEBT, daysDebt * dailyRate);
    jobContext.addMeasure(TechnicalDebtMetrics.EXTRA_TECHNICAL_DEBT, extraDebt * dailyRate);
    jobContext.addMeasure(TechnicalDebtMetrics.SONAR_TECHNICAL_DEBT, sonarDebt * dailyRate);
    jobContext.addMeasure(TechnicalDebtMetrics.TOTAL_TECHNICAL_DEBT_DAYS, daysDebt);
    jobContext.addMeasure(techDebtRepartition.build());
  }

  // Browse manual metric to add up manual measure linked
  private double calculateExtraDebt(JobContext jobContext) {
    //double extraDebt = 0.0;

    // Right now, the API does not enable to to retrieve manual measures...
    /*List<Measure> list = jobContext.getMeasures();
    for (Measure measure : list) {
        String metricName = measure.getMetric().getName();
        if (TechnicalDebtMetrics.MANUAL_MEASURE_DEBT_NAME.equals(metricName)) {
            extraDebt += measure.getValue();
        }
    }*/
    return 0.0;
  }


  private double calculateMetricDebt(JobContext jobContext, Metric metric, String keyWeight, String defaultWeight) {
    Measure measure = jobContext.getMeasure(metric);

    if (measure == null || !measure.hasValue()) {
      return 0.0;
    }
    double weight = getWeight(keyWeight, defaultWeight);
    return measure.getValue() * weight;
  }


  private double getWeight(String keyWeight, String defaultWeight) {
    Object property = configuration.getProperty(keyWeight);
    if (property != null) {
      return Double.parseDouble((String) property);
    }
    return Double.parseDouble(defaultWeight);
  }
}
