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
import org.mockito.Matchers;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;
import org.sonar.plugins.issuesdensity.IssuesDensityMetrics;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class IssuesDensityDecoratorTest {

  IssuesDensityDecorator decorator;
  Resource resource;

  @Before
  public void before() {
    resource = mock(Resource.class);
    when(resource.getScope()).thenReturn(Scopes.PROJECT);
    decorator = new IssuesDensityDecorator();
  }

  @Test
  public void should_execute_on_project() throws Exception {
    assertThat(decorator.shouldExecuteOnProject(mock(Project.class))).isTrue();
  }

  @Test
  public void depends_upon_weighted_issues_and_ncloc() throws Exception {
    assertThat(decorator.dependsUponWeightedIssuesAndNcloc()).containsOnly(IssuesDensityMetrics.WEIGHTED_ISSUES, CoreMetrics.NCLOC);
  }

  @Test
  public void generates_issues_density() throws Exception {
    assertThat(decorator.generatesIssuesDensity()).isEqualTo(IssuesDensityMetrics.ISSUES_DENSITY);
  }

  @Test
  public void calculate_density() {
    assertThat(IssuesDensityDecorator.calculate(4000, 200)).isEqualTo(0.0);
    assertThat(IssuesDensityDecorator.calculate(200, 200)).isEqualTo(0.0);
    assertThat(IssuesDensityDecorator.calculate(50, 200)).isEqualTo(75.0);
    assertThat(IssuesDensityDecorator.calculate(0, 200)).isEqualTo(100.0);
  }

  @Test
  public void decorate_density() {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 200.0));
    when(context.getMeasure(IssuesDensityMetrics.WEIGHTED_ISSUES)).thenReturn(new Measure(IssuesDensityMetrics.WEIGHTED_ISSUES, 50.0));

    decorator.decorate(resource, context);

    verify(context).saveMeasure(IssuesDensityMetrics.ISSUES_DENSITY, 75.0);
  }

  @Test
  public void no_density_if_no_ncloc() {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 0.0));
    when(context.getMeasure(IssuesDensityMetrics.WEIGHTED_ISSUES)).thenReturn(new Measure(IssuesDensityMetrics.WEIGHTED_ISSUES, 50.0));

    decorator.decorate(resource, context);

    verify(context, never()).saveMeasure(Matchers.eq(IssuesDensityMetrics.ISSUES_DENSITY), Matchers.anyDouble());
  }

  @Test
  public void save_density_if_value_is_zero() {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 200.0));
    when(context.getMeasure(IssuesDensityMetrics.WEIGHTED_ISSUES)).thenReturn(new Measure(IssuesDensityMetrics.WEIGHTED_ISSUES, 5000.0));

    decorator.decorate(resource, context);

    verify(context).saveMeasure(IssuesDensityMetrics.ISSUES_DENSITY, 0.0);
  }

  @Test
  public void density_is_hundred_when_no_debt() {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 200.0));

    decorator.decorate(resource, context);

    verify(context).saveMeasure(IssuesDensityMetrics.ISSUES_DENSITY, 100.0);
  }

  @Test
  public void density_is_hundred_when_debt_is_zero() {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 200.0));
    when(context.getMeasure(IssuesDensityMetrics.WEIGHTED_ISSUES)).thenReturn(new Measure(IssuesDensityMetrics.WEIGHTED_ISSUES, 0.0));

    decorator.decorate(resource, context);

    verify(context).saveMeasure(IssuesDensityMetrics.ISSUES_DENSITY, 100.0);
  }
}
