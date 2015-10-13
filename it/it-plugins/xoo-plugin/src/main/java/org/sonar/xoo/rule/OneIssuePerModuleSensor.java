/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo.rule;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.xoo.Xoo;

public class OneIssuePerModuleSensor implements Sensor {

  public static final String RULE_KEY = "OneIssuePerModule";

  private final ResourcePerspectives perspectives;
  private final FileSystem fs;
  private final ActiveRules activeRules;

  public OneIssuePerModuleSensor(ResourcePerspectives perspectives, FileSystem fs, ActiveRules activeRules) {
    this.perspectives = perspectives;
    this.fs = fs;
    this.activeRules = activeRules;
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    Issuable issuable = perspectives.as(Issuable.class, (Resource) project);
    issuable.addIssue(issuable.newIssueBuilder()
      .ruleKey(RuleKey.of(XooRulesDefinition.XOO_REPOSITORY, RULE_KEY))
      .message("This issue is generated on each module")
      .build());
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(fs.predicates().hasLanguages(Xoo.KEY)) && (activeRules.find(RuleKey.of(XooRulesDefinition.XOO_REPOSITORY, RULE_KEY)) != null);
  }
}
