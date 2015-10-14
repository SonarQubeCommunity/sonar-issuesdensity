/*
 * Issues Density Plugin :: Xoo
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
package org.sonar.xoo;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.SonarPlugin;
import org.sonar.xoo.rule.HasTagSensor;
import org.sonar.xoo.rule.OneBlockerIssuePerFileSensor;
import org.sonar.xoo.rule.OneIssuePerFileSensor;
import org.sonar.xoo.rule.OneIssuePerLineSensor;
import org.sonar.xoo.rule.OneIssuePerModuleSensor;
import org.sonar.xoo.rule.XooBasicProfile;
import org.sonar.xoo.rule.XooEmptyProfile;
import org.sonar.xoo.rule.XooRulesDefinition;

/**
 * Plugin entry-point, as declared in pom.xml.
 */
public class XooPlugin extends SonarPlugin {

  /**
   * Declares all the extensions implemented in the plugin
   */
  @Override
  public List getExtensions() {
    return Arrays.asList(
      Xoo.class,
      XooRulesDefinition.class,
      XooBasicProfile.class,
      XooEmptyProfile.class,

      // sensors
      MeasureSensor.class,
      HasTagSensor.class,
      OneBlockerIssuePerFileSensor.class,
      OneIssuePerLineSensor.class,
      OneIssuePerFileSensor.class,
      OneIssuePerModuleSensor.class);
  }

}
