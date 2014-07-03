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
package org.sonar.plugins.issuesdensity;

import com.google.common.collect.Lists;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.List;

public final class IssuesDensityMetrics implements Metrics {

  public static final String ISSUES_DENSITY_KEY = "violations_density";

  public static final Metric<Double> ISSUES_DENSITY = new Metric.Builder(ISSUES_DENSITY_KEY, "Rules compliance", Metric.ValueType.PERCENT)
    .setDescription("Rules compliance")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(CoreMetrics.DOMAIN_ISSUES)
    .create();

  public static final String WEIGHTED_ISSUES_KEY = "weighted_violations";

  public static final Metric<Integer> WEIGHTED_ISSUES = new Metric.Builder(WEIGHTED_ISSUES_KEY, "Weighted issues", Metric.ValueType.INT)
    .setDescription("Weighted Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(true)
    .setDomain(CoreMetrics.DOMAIN_ISSUES)
    .setBestValue(0.0)
    .setOptimizedBestValue(true)
    .create();

  public List<Metric> getMetrics() {
    return Lists.<Metric>newArrayList(ISSUES_DENSITY, WEIGHTED_ISSUES);
  }
}
