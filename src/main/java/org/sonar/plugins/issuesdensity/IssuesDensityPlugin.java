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

import com.google.common.collect.ImmutableList;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.issuesdensity.batch.IssuesDensityDecorator;
import org.sonar.plugins.issuesdensity.batch.WeightedIssuesDecorator;
import org.sonar.plugins.issuesdensity.ui.HotspotMostViolatedComponentsWidget;

import java.util.List;

public class IssuesDensityPlugin extends SonarPlugin {

  public static final String WEIGHTED_ISSUES_PROPERTY = "sonar.issuesdensity.weight";
  private static final String WEIGHTED_ISSUES_DEFAULT_VALUE = "INFO=0;MINOR=1;MAJOR=3;CRITICAL=5;BLOCKER=10";

  public List getExtensions() {
    return ImmutableList.of(
      IssuesDensityMetrics.class,
      HotspotMostViolatedComponentsWidget.class,
      WeightedIssuesDecorator.class,
      IssuesDensityDecorator.class,
      PropertyDefinition.builder(WEIGHTED_ISSUES_PROPERTY)
        .name("Rules weight")
        .description("A weight is associated to each severity to emphasize the most critical issues.")
        .defaultValue(WEIGHTED_ISSUES_DEFAULT_VALUE)
        .deprecatedKey("sonar.core.rule.weight")
        .build()
      );
  }

}
