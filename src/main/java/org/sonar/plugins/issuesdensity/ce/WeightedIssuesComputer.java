/*
 * Issues Density Plugin
 * Copyright (C) 2014 SonarSource
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.issuesdensity.ce;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.config.Settings;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.sonar.api.measures.CoreMetrics.BLOCKER_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.CRITICAL_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.INFO_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.MAJOR_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.MINOR_VIOLATIONS_KEY;
import static org.sonar.api.rule.Severity.ALL;
import static org.sonar.api.rule.Severity.BLOCKER;
import static org.sonar.api.rule.Severity.CRITICAL;
import static org.sonar.api.rule.Severity.INFO;
import static org.sonar.api.rule.Severity.MAJOR;
import static org.sonar.api.rule.Severity.MINOR;
import static org.sonar.api.utils.KeyValueFormat.newIntegerConverter;
import static org.sonar.api.utils.KeyValueFormat.newStringConverter;
import static org.sonar.api.utils.KeyValueFormat.parse;
import static org.sonar.plugins.issuesdensity.IssuesDensityMetrics.WEIGHTED_ISSUES_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityPlugin.WEIGHTED_ISSUES_PROPERTY;

public class WeightedIssuesComputer implements MeasureComputer {

  private static final Map<String, String> METRIC_KEYS_BY_SEVERITY = ImmutableMap.of(
    BLOCKER, BLOCKER_VIOLATIONS_KEY,
    CRITICAL, CRITICAL_VIOLATIONS_KEY,
    MAJOR, MAJOR_VIOLATIONS_KEY,
    MINOR, MINOR_VIOLATIONS_KEY,
    INFO, INFO_VIOLATIONS_KEY
  );

  private final Settings settings;

  @CheckForNull
  private Map<String, Integer> weights;

  public WeightedIssuesComputer(Settings settings) {
    this.settings = settings;
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder()
      .setInputMetrics(BLOCKER_VIOLATIONS_KEY, CRITICAL_VIOLATIONS_KEY, MAJOR_VIOLATIONS_KEY, MINOR_VIOLATIONS_KEY, INFO_VIOLATIONS_KEY)
      .setOutputMetrics(WEIGHTED_ISSUES_KEY)
      .build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    int weight = 0;
    Map<String, Integer> weightsBySeverity = getOrInitWeightsBySeverity();

    for (String severity : ALL) {
      Measure measure = context.getMeasure(severityToIssueMetric(severity));
      if (measure != null) {
        int add = weightsBySeverity.get(severity) * measure.getIntValue();
        weight += add;
      }
    }

    context.addMeasure(WEIGHTED_ISSUES_KEY, weight);
  }

  private Map<String, Integer> getOrInitWeightsBySeverity() {
    if (weights == null) {
      weights = getWeights(settings);
    }
    return weights;
  }

  private static Map<String, Integer> getWeights(final Settings settings) {
    String value = settings.getString(WEIGHTED_ISSUES_PROPERTY);
    Map<String, Integer> weights = parse(value, newStringConverter(), newIntegerConverter());
    initWeightsWithValueOne(weights);
    return weights;
  }

  private static void initWeightsWithValueOne(Map<String, Integer> weights) {
    for (String severity : ALL) {
      if (!weights.containsKey(severity)) {
        weights.put(severity, 1);
      }
    }
  }

  private static String severityToIssueMetric(String severity) {
    String metricKey = METRIC_KEYS_BY_SEVERITY.get(severity);
    checkArgument(metricKey != null, "Unsupported severity: %s", severity);
    return metricKey;
  }
}
