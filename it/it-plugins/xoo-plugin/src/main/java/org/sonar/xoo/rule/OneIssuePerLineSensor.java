/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo.rule;

import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.File;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class OneIssuePerLineSensor extends AbstractXooRuleSensor {

  public static final String RULE_KEY = "OneIssuePerLine";
  private static final Logger LOG = Loggers.get(OneIssuePerLineSensor.class);
  private static final String EFFORT_TO_FIX_PROPERTY = "sonar.oneIssuePerLine.effortToFix";

  private final ResourcePerspectives perspectives;
  private final Settings settings;

  public OneIssuePerLineSensor(ResourcePerspectives perspectives, Settings settings, FileSystem fs, ActiveRules activeRules) {
    super(fs, activeRules);
    this.perspectives = perspectives;
    this.settings = settings;
  }

  @Override
  protected String getRuleKey() {
    return RULE_KEY;
  }

  @DependsUpon
  public Metric dependsUponLinesMeasure() {
    return CoreMetrics.LINES;
  }

  @Override
  protected void processFile(InputFile inputFile, File sonarFile, org.sonar.api.batch.SensorContext context, RuleKey ruleKey, String languageKey) {
    Issuable issuable = perspectives.as(Issuable.class, inputFile);
    Measure linesMeasure = context.getMeasure(sonarFile, CoreMetrics.LINES);
    if (linesMeasure == null) {
      LOG.warn("Missing measure " + CoreMetrics.LINES_KEY + " on " + inputFile.relativePath());
    } else {
      for (int line = 1; line <= linesMeasure.getValue().intValue(); line++) {
        issuable.addIssue(issuable.newIssueBuilder()
          .line(line)
          .ruleKey(ruleKey)
          .effortToFix(settings.getDouble(EFFORT_TO_FIX_PROPERTY))
          .message("This issue is generated on each line")
          .build());
      }
    }
  }
}
