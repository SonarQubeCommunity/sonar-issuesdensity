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

import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;

import static java.lang.Math.max;
import static org.sonar.api.measures.CoreMetrics.NCLOC_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityMetrics.ISSUES_DENSITY_KEY;
import static org.sonar.plugins.issuesdensity.IssuesDensityMetrics.WEIGHTED_ISSUES_KEY;

public class IssuesDensityComputer implements MeasureComputer {

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder()
      .setInputMetrics(WEIGHTED_ISSUES_KEY, NCLOC_KEY)
      .setOutputMetrics(ISSUES_DENSITY_KEY)
      .build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    Measure ncloc = context.getMeasure(NCLOC_KEY);
    Measure weightedIssues = context.getMeasure(WEIGHTED_ISSUES_KEY);
    if (ncloc != null && ncloc.getIntValue() > 0) {
      int weightedIssuesValue = weightedIssues == null ? 0 : weightedIssues.getIntValue();
      double density = calculateDensity(weightedIssuesValue, ncloc.getIntValue());
      context.addMeasure(ISSUES_DENSITY_KEY, density);
    }
  }

  private static double calculateDensity(double weight, double ncloc) {
    double density = (1.0 - (weight / ncloc)) * 100.0;
    return max(density, 0.0);
  }

}
