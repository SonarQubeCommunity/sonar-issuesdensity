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

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.api.measures.CoreMetrics.NCLOC_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityMetrics.ISSUES_DENSITY_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityMetrics.WEIGHTED_ISSUES_KEY;

public class IssuesDensityComputerTest {

  static final Component PROJECT = new TestComponent("Project", Component.Type.PROJECT, null);

  MeasureComputer underTest = new IssuesDensityComputer();

  MeasureComputer.MeasureComputerDefinition definition = underTest.define(new TestMeasureComputerDefinitionContext());
  TestMeasureComputerContext projectContext = new TestMeasureComputerContext(PROJECT, new TestSettings(), definition);

  @Test
  public void test_definition() throws Exception {
    assertThat(definition.getInputMetrics()).containsOnly(WEIGHTED_ISSUES_KEY, NCLOC_KEY);
    assertThat(definition.getOutputMetrics()).containsOnly(ISSUES_DENSITY_KEY);
  }

  @Test
  public void compute_density_measure() {
    projectContext.addInputMeasure(NCLOC_KEY, 200);
    projectContext.addInputMeasure(WEIGHTED_ISSUES_KEY, 50);

    underTest.compute(projectContext);

    assertMeasure(ISSUES_DENSITY_KEY, 75d);
  }

  @Test
  public void does_not_compute_density_measure_when_no_ncloc() {
    projectContext.addInputMeasure(WEIGHTED_ISSUES_KEY, 50);

    underTest.compute(projectContext);

    assertNoMeasure(ISSUES_DENSITY_KEY);
  }

  @Test
  public void does_not_compute_density_measure_when_ncloc_is_zero() {
    projectContext.addInputMeasure(NCLOC_KEY, 0);
    projectContext.addInputMeasure(WEIGHTED_ISSUES_KEY, 50);

    underTest.compute(projectContext);

    assertNoMeasure(ISSUES_DENSITY_KEY);
  }

  @Test
  public void compute_one_hundred_density_measure_when_no_weighted_issue_measure() {
    projectContext.addInputMeasure(NCLOC_KEY, 200);

    underTest.compute(projectContext);

    assertMeasure(ISSUES_DENSITY_KEY, 100d);
  }

  @Test
  public void compute_one_hundred_density_measure_when_weighted_issue_is_zero() {
    projectContext.addInputMeasure(NCLOC_KEY, 200);
    projectContext.addInputMeasure(WEIGHTED_ISSUES_KEY, 0);

    underTest.compute(projectContext);

    assertMeasure(ISSUES_DENSITY_KEY, 100d);
  }

  @Test
  public void compute_zero_density_measure_when_weighted_issue_is_huge() {
    projectContext.addInputMeasure(NCLOC_KEY, 200);
    projectContext.addInputMeasure(WEIGHTED_ISSUES_KEY, 5000);

    underTest.compute(projectContext);

    assertMeasure(ISSUES_DENSITY_KEY, 0d);
  }

  private void assertMeasure(String metric, double expectedValue) {
    Measure measure = projectContext.getMeasure(metric);
    assertThat(measure).isNotNull();
    assertThat(measure.getDoubleValue()).isEqualTo(expectedValue);
  }

  private void assertNoMeasure(String metric) {
    Measure measure = projectContext.getMeasure(metric);
    assertThat(measure).isNull();
  }

}
