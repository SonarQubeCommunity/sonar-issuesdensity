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
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.rule.RuleKey;

public class OneIssuePerFileSensor extends AbstractXooRuleSensor {

  public static final String RULE_KEY = "OneIssuePerFile";

  private static final String EFFORT_TO_FIX_PROPERTY = "sonar.oneIssuePerFile.effortToFix";

  private final ResourcePerspectives perspectives;
  private final Settings settings;

  public OneIssuePerFileSensor(ResourcePerspectives perspectives, Settings settings, FileSystem fs, ActiveRules activeRules) {
    super(fs, activeRules);
    this.perspectives = perspectives;
    this.settings = settings;
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
      .effortToFix(settings.getDouble(EFFORT_TO_FIX_PROPERTY))
      .message("This issue is generated on each file")
      .build());
  }
}
