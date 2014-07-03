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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;
import org.sonar.api.rule.Severity;
import org.sonar.plugins.issuesdensity.IssuesDensityMetrics;
import org.sonar.plugins.issuesdensity.IssuesDensityPlugin;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WeightedIssuesDecoratorTest {

  @Captor
  ArgumentCaptor<Measure> measureCaptor;

  Settings settings;
  Resource resource;

  WeightedIssuesDecorator decorator;

  @Before
  public void before() {
    resource = mock(Resource.class);
    when(resource.getScope()).thenReturn(Scopes.PROJECT);

    settings = new Settings();
    decorator = new WeightedIssuesDecorator(settings);
  }

  @Test
  public void should_execute_on_project() throws Exception {
    assertThat(decorator.shouldExecuteOnProject(mock(Project.class))).isTrue();
  }

  @Test
  public void depends_upon_issues() throws Exception {
    assertThat(decorator.dependsUponIssues()).containsOnly(CoreMetrics.BLOCKER_VIOLATIONS, CoreMetrics.CRITICAL_VIOLATIONS,
      CoreMetrics.MAJOR_VIOLATIONS, CoreMetrics.MINOR_VIOLATIONS, CoreMetrics.INFO_VIOLATIONS);
  }

  @Test
  public void generates_weighted_issues() throws Exception {
    assertThat(decorator.generatesWeightedIssues()).isEqualTo(IssuesDensityMetrics.WEIGHTED_ISSUES);
  }

  @Test
  public void add_weighted_issues() {
    settings.setProperty(IssuesDensityPlugin.WEIGHTED_ISSUES_PROPERTY, "BLOCKER=10;CRITICAL=5;MAJOR=2;MINOR=1;INFO=0");
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.INFO_VIOLATIONS)).thenReturn(new Measure(CoreMetrics.INFO_VIOLATIONS, 50.0));
    when(context.getMeasure(CoreMetrics.CRITICAL_VIOLATIONS)).thenReturn(new Measure(CoreMetrics.CRITICAL_VIOLATIONS, 80.0));
    when(context.getMeasure(CoreMetrics.BLOCKER_VIOLATIONS)).thenReturn(new Measure(CoreMetrics.BLOCKER_VIOLATIONS, 100.0));

    decorator.start();
    decorator.decorate(resource, context);

    verify(context).saveMeasure(measureCaptor.capture());
    assertThat(measureCaptor.getValue().getMetric()).isEqualTo(IssuesDensityMetrics.WEIGHTED_ISSUES);
    assertThat(measureCaptor.getValue().getValue()).isEqualTo(100 * 10 + 80 * 5 + 50 * 0d);
    assertThat(measureCaptor.getValue().getData()).isEqualTo("INFO=50;CRITICAL=80;BLOCKER=100");

    // just for fun :)
    decorator.stop();
  }

  // SONAR-3092
  @Test
  public void do_save_zero() {
    settings.setProperty(IssuesDensityPlugin.WEIGHTED_ISSUES_PROPERTY, "BLOCKER=10;CRITICAL=5;MAJOR=2;MINOR=1;INFO=0");
    DecoratorContext context = mock(DecoratorContext.class);

    decorator.start();
    decorator.decorate(resource, context);

    verify(context).saveMeasure(measureCaptor.capture());
    assertThat(measureCaptor.getValue().getMetric()).isEqualTo(IssuesDensityMetrics.WEIGHTED_ISSUES);
    assertThat(measureCaptor.getValue().getValue()).isEqualTo(0d);
    assertThat(measureCaptor.getValue().getData()).isNull();
  }

  @Test
  public void load_severity_weights_at_startup() {
    settings.setProperty(IssuesDensityPlugin.WEIGHTED_ISSUES_PROPERTY, "BLOCKER=2;CRITICAL=1;MAJOR=0;MINOR=0;INFO=0");
    decorator.start();

    assertThat(decorator.getWeightsBySeverity().get(Severity.BLOCKER)).isEqualTo(2);
    assertThat(decorator.getWeightsBySeverity().get(Severity.CRITICAL)).isEqualTo(1);
    assertThat(decorator.getWeightsBySeverity().get(Severity.MAJOR)).isEqualTo(0);
  }

  @Test
  public void weights_setting_should_be_optional() {
    settings.setProperty(IssuesDensityPlugin.WEIGHTED_ISSUES_PROPERTY, "BLOCKER=2");
    decorator.start();

    assertThat(decorator.getWeightsBySeverity().get(Severity.MAJOR)).isEqualTo(1);
  }

  @Test
  public void severity_to_metric() throws Exception {
    assertThat(WeightedIssuesDecorator.severityToIssueMetric(Severity.INFO)).isEqualTo(CoreMetrics.INFO_VIOLATIONS);
    assertThat(WeightedIssuesDecorator.severityToIssueMetric(Severity.MINOR)).isEqualTo(CoreMetrics.MINOR_VIOLATIONS);
    assertThat(WeightedIssuesDecorator.severityToIssueMetric(Severity.MAJOR)).isEqualTo(CoreMetrics.MAJOR_VIOLATIONS);
    assertThat(WeightedIssuesDecorator.severityToIssueMetric(Severity.BLOCKER)).isEqualTo(CoreMetrics.BLOCKER_VIOLATIONS);
    assertThat(WeightedIssuesDecorator.severityToIssueMetric(Severity.CRITICAL)).isEqualTo(CoreMetrics.CRITICAL_VIOLATIONS);
  }

  @Test
  public void fail_on_unknown_severity() throws Exception {
    try {
      assertThat(WeightedIssuesDecorator.severityToIssueMetric("Unknown"));
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class).hasMessage("Unsupported severity: Unknown");
    }
  }
}
