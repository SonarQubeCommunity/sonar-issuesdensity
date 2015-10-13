/*
 * Issues Density Plugin :: Integration Tests
 * Copyright (C) 2014 ${owner}
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
package org.sonar.it.issuesdensity.suite;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.locator.FileLocation;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.it.issuesdensity.ItUtils.createRunner;

public class MeasuresTest {

  @ClassRule
  public static Orchestrator orchestrator = IssuesDensityTestSuite.ORCHESTRATOR;

  @Before
  public void resetData() {
    orchestrator.resetData();
  }

  /**
   * SONARRCI-2
   */
  @Test
  public void weighted_violations_measures() throws Exception {
    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/issuesdensity/profile-with-many-rules.xml"));
    orchestrator.executeBuild(createRunner(orchestrator, "xoo-multi-modules-sample", "com.sonarsource.it.samples:multi-modules-sample", "with-many-rules"));

    Resource project = orchestrator.getServer().getWsClient()
      .find(ResourceQuery.createForMetrics("com.sonarsource.it.samples:multi-modules-sample", "weighted_violations"));
    assertThat(project.getMeasure("weighted_violations").getValue()).isEqualTo(84);
  }

  /**
   * SONARRCI-1
   */
  @Test
  public void violations_density_measures() throws Exception {
    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/issuesdensity/MeasuresTest/violations-density-measures-profile.xml"));
    orchestrator.executeBuild(createRunner(orchestrator, "xoo-multi-modules-sample", "com.sonarsource.it.samples:multi-modules-sample", "violations-density-measures-profile"));

    Resource project = orchestrator.getServer().getWsClient()
      .find(ResourceQuery.createForMetrics("com.sonarsource.it.samples:multi-modules-sample", "violations_density"));
    assertThat(project.getMeasureIntValue("violations_density")).isEqualTo(33);
  }

}
