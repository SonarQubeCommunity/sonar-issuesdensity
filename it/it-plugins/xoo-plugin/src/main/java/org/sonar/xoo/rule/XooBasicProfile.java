/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo.rule;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.xoo.Xoo;

public class XooBasicProfile extends ProfileDefinition {

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    final RulesProfile profile = RulesProfile.create("Basic", Xoo.KEY);

    profile.activateRule(Rule.create(XooRulesDefinition.XOO_REPOSITORY, HasTagSensor.RULE_KEY), RulePriority.MAJOR);

    return profile;
  }
}
