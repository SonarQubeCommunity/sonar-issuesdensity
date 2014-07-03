/*
 * Issues Density Plugin
 * Copyright (C) 2014 SonarSource
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
package org.sonar.plugins.issuesdensity.batch;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import org.picocontainer.Startable;
import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.KeyValueFormat;
import org.sonar.plugins.issuesdensity.IssuesDensityMetrics;
import org.sonar.plugins.issuesdensity.IssuesDensityPlugin;

import java.util.List;
import java.util.Map;

public class WeightedIssuesDecorator implements Decorator, Startable {

  private Settings settings;
  private Map<String, Integer> weightsBySeverity;

  public WeightedIssuesDecorator(Settings settings) {
    this.settings = settings;
  }

  @DependsUpon
  public List<Metric> dependsUponIssues() {
    return Lists.<Metric>newArrayList(CoreMetrics.BLOCKER_VIOLATIONS, CoreMetrics.CRITICAL_VIOLATIONS,
      CoreMetrics.MAJOR_VIOLATIONS, CoreMetrics.MINOR_VIOLATIONS, CoreMetrics.INFO_VIOLATIONS);
  }

  @DependedUpon
  public Metric generatesWeightedIssues() {
    return IssuesDensityMetrics.WEIGHTED_ISSUES;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return true;
  }

  @Override
  public void start() {
    weightsBySeverity = getWeights(settings);
  }

  @Override
  public void stop() {
    // Nothing to do
  }

  Map<String, Integer> getWeightsBySeverity() {
    return weightsBySeverity;
  }

  static Map<String, Integer> getWeights(final Settings settings) {
    String value = settings.getString(IssuesDensityPlugin.WEIGHTED_ISSUES_PROPERTY);

    Map<String, Integer> weights = KeyValueFormat.parse(value, KeyValueFormat.newStringConverter(), KeyValueFormat.newIntegerConverter());

    for (String severity : Severity.ALL) {
      if (!weights.containsKey(severity)) {
        weights.put(severity, 1);
      }
    }
    return weights;
  }

  @Override
  public void decorate(Resource resource, DecoratorContext context) {
    decorate(context);
  }

  void decorate(DecoratorContext context) {
    double value = 0.0;
    Multiset<String> distribution = HashMultiset.create();

    for (String severity : Severity.ALL) {
      Measure measure = context.getMeasure(SeverityUtils.severityToIssueMetric(severity));
      if (measure != null && MeasureUtils.hasValue(measure)) {
        distribution.add(severity, measure.getIntValue());
        double add = weightsBySeverity.get(severity) * measure.getIntValue();
        value += add;
      }
    }

    String distributionFormatted = KeyValueFormat.format(distribution);
    // SONAR-4987 We should store an empty string for the distribution value
    Measure measure = new Measure(IssuesDensityMetrics.WEIGHTED_ISSUES, value, Strings.emptyToNull(distributionFormatted));
    context.saveMeasure(measure);
  }

}
