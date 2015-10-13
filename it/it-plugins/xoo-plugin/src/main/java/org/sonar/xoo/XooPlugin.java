/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
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
