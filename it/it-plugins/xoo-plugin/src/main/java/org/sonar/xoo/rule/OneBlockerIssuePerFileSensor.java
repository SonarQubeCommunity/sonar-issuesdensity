/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo.rule;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.Severity;

public class OneBlockerIssuePerFileSensor extends AbstractXooRuleSensor {

  public static final String RULE_KEY = "OneBlockerIssuePerFile";

  private final ResourcePerspectives perspectives;

  public OneBlockerIssuePerFileSensor(ResourcePerspectives perspectives, FileSystem fs, ActiveRules activeRules) {
    super(fs, activeRules);
    this.perspectives = perspectives;
  }

  @Override
  protected String getRuleKey() {
    return RULE_KEY;
  }

  @Override
  protected void processFile(InputFile inputFile, org.sonar.api.resources.File sonarFile, SensorContext context, RuleKey ruleKey, String languageKey) {
    Issuable issuable = perspectives.as(Issuable.class, sonarFile);
    issuable.addIssue(issuable.newIssueBuilder()
      .ruleKey(ruleKey)
      .severity(Severity.BLOCKER)
      .message("This issue is generated on each file. Severity is blocker, whatever quality profile")
      .build());
  }

}
