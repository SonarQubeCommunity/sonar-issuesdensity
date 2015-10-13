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
import com.sonar.orchestrator.selenium.Selenese;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static org.sonar.it.issuesdensity.ItUtils.createRunner;

public class WidgetTest {

  @ClassRule
  public static Orchestrator orchestrator = IssuesDensityTestSuite.ORCHESTRATOR;

  @BeforeClass
  public static void setup() throws Exception {
    orchestrator.resetData();

    orchestrator.getServer().restoreProfile(FileLocation.ofClasspath("/com/sonar/it/issuesdensity/profile-with-many-rules.xml"));
    orchestrator.executeBuild(createRunner(orchestrator, "xoo-multi-modules-sample", "com.sonarsource.it.samples:multi-modules-sample", "with-many-rules"));
  }

  /**
   * SONARRCI-3
   */
  @Test
  public void test_most_violated_components_widget() throws Exception {
    Selenese selenese = Selenese
      .builder()
      .setHtmlTestsInClasspath("most-violated-components-widget",
        "/selenium/most-violated-components-widget.html",
        "/selenium/most-violated-components-popup-on-violations.html"
      ).build();
    orchestrator.executeSelenese(selenese);
  }

}
