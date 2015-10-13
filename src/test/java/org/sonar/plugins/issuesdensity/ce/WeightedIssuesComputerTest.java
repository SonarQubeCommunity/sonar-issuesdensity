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

import org.junit.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.test.TestComponent;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinitionContext;
import org.sonar.api.ce.measure.test.TestSettings;
import org.sonar.api.config.Settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.api.measures.CoreMetrics.BLOCKER_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.CRITICAL_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.INFO_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.MAJOR_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.MINOR_VIOLATIONS_KEY;
import static org.sonar.api.measures.CoreMetrics.NCLOC_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityMetrics.ISSUES_DENSITY_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityMetrics.WEIGHTED_ISSUES_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityPlugin.WEIGHTED_ISSUES_PROPERTY;

public class WeightedIssuesComputerTest {

  static final Component PROJECT = new TestComponent("Project", Component.Type.PROJECT, null);

  Settings settings = new Settings();

  MeasureComputer underTest = new WeightedIssuesComputer(settings);

  MeasureComputer.MeasureComputerDefinition definition = underTest.define(new TestMeasureComputerDefinitionContext());
  TestMeasureComputerContext projectContext = new TestMeasureComputerContext(PROJECT, new TestSettings(), definition);

  @Test
  public void test_definition() throws Exception {
    assertThat(definition.getInputMetrics()).containsOnly(BLOCKER_VIOLATIONS_KEY, CRITICAL_VIOLATIONS_KEY, MAJOR_VIOLATIONS_KEY, MINOR_VIOLATIONS_KEY, INFO_VIOLATIONS_KEY);
    assertThat(definition.getOutputMetrics()).containsOnly(WEIGHTED_ISSUES_KEY);
  }

  @Test
  public void compute_weighted_measure() throws Exception {
    settings.setProperty(WEIGHTED_ISSUES_PROPERTY, "BLOCKER=10;CRITICAL=5;MAJOR=2;MINOR=1;INFO=0");

    projectContext.addInputMeasure(INFO_VIOLATIONS_KEY, 50);
    projectContext.addInputMeasure(MINOR_VIOLATIONS_KEY, 60);
    projectContext.addInputMeasure(MAJOR_VIOLATIONS_KEY, 70);
    projectContext.addInputMeasure(CRITICAL_VIOLATIONS_KEY, 80);
    projectContext.addInputMeasure(BLOCKER_VIOLATIONS_KEY, 100);

    underTest.compute(projectContext);

    assertMeasure(WEIGHTED_ISSUES_KEY, 100 * 10 + 80 * 5 + 70 * 2 + 60 * 1 + 50 * 0);
  }

  @Test
  public void compute_zero_weighted_measure_when_no_input_measures() {
    settings.setProperty(WEIGHTED_ISSUES_PROPERTY, "BLOCKER=10;CRITICAL=5;MAJOR=2;MINOR=1;INFO=0");
    underTest.compute(projectContext);

    assertMeasure(WEIGHTED_ISSUES_KEY, 0);
  }

  @Test
  public void compute_weighted_measure_even_if_issues_severity_measures_are_missing() throws Exception {
    settings.setProperty(WEIGHTED_ISSUES_PROPERTY, "BLOCKER=10;CRITICAL=5;MAJOR=2;MINOR=1;INFO=0");

    projectContext.addInputMeasure(INFO_VIOLATIONS_KEY, 50);
    projectContext.addInputMeasure(CRITICAL_VIOLATIONS_KEY, 80);
    projectContext.addInputMeasure(BLOCKER_VIOLATIONS_KEY, 100);

    underTest.compute(projectContext);

    assertMeasure(WEIGHTED_ISSUES_KEY, 100 * 10 + 80 * 5 + 50 * 0);
  }

  @Test
  public void default_weight_is_one() throws Exception {
    projectContext.addInputMeasure(INFO_VIOLATIONS_KEY, 50);
    projectContext.addInputMeasure(MINOR_VIOLATIONS_KEY, 60);
    projectContext.addInputMeasure(MAJOR_VIOLATIONS_KEY, 70);
    projectContext.addInputMeasure(CRITICAL_VIOLATIONS_KEY, 80);
    projectContext.addInputMeasure(BLOCKER_VIOLATIONS_KEY, 100);

    underTest.compute(projectContext);

    assertMeasure(WEIGHTED_ISSUES_KEY, 100 * 1 + 80 * 1 + 70 * 1 + 60 * 1 + 50 * 1);
  }

  private void assertMeasure(String metric, int expectedValue) {
    Measure measure = projectContext.getMeasure(metric);
    assertThat(measure).isNotNull();
    assertThat(measure.getIntValue()).isEqualTo(expectedValue);
  }

}
