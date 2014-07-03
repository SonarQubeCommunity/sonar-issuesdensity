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

import org.junit.Test;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.Severity;

import static org.fest.assertions.Assertions.assertThat;

public class SeverityUtilsTest {

  @Test
  public void severity_to_metric() throws Exception {
    assertThat(SeverityUtils.severityToIssueMetric(Severity.INFO)).isEqualTo(CoreMetrics.INFO_VIOLATIONS);
    assertThat(SeverityUtils.severityToIssueMetric(Severity.MINOR)).isEqualTo(CoreMetrics.MINOR_VIOLATIONS);
    assertThat(SeverityUtils.severityToIssueMetric(Severity.MAJOR)).isEqualTo(CoreMetrics.MAJOR_VIOLATIONS);
    assertThat(SeverityUtils.severityToIssueMetric(Severity.BLOCKER)).isEqualTo(CoreMetrics.BLOCKER_VIOLATIONS);
    assertThat(SeverityUtils.severityToIssueMetric(Severity.CRITICAL)).isEqualTo(CoreMetrics.CRITICAL_VIOLATIONS);
  }

  @Test
  public void fail_on_unknown_severity() throws Exception {
    try {
      assertThat(SeverityUtils.severityToIssueMetric("Unknown"));
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class).hasMessage("Unsupported severity: Unknown");
    }
  }
}
