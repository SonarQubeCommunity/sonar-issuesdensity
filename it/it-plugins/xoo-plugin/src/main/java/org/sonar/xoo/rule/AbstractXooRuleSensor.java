/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo.rule;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.xoo.Xoo;

public abstract class AbstractXooRuleSensor implements Sensor {

  private final FileSystem fs;
  private final ActiveRules activeRules;

  public AbstractXooRuleSensor(FileSystem fs, ActiveRules activeRules) {
    this.fs = fs;
    this.activeRules = activeRules;
  }

  protected abstract String getRuleKey();

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(fs.predicates().hasLanguages(Xoo.KEY))
      && (activeRules.find(RuleKey.of(XooRulesDefinition.XOO_REPOSITORY, getRuleKey())) != null);
  }

  @Override
  public final void analyse(Project project, SensorContext context) {
    doAnalyse(project, context, Xoo.KEY);
  }

  private void doAnalyse(Project project, SensorContext context, String languageKey) {
    RuleKey ruleKey = getRuleKey(languageKey);
    if (activeRules.find(ruleKey) == null) {
      return;
    }
    for (InputFile inputFile : fs.inputFiles(fs.predicates().hasLanguage(languageKey))) {
      File sonarFile = File.create(inputFile.relativePath());
      sonarFile = context.getResource(sonarFile);
      processFile(inputFile, sonarFile, context, ruleKey, languageKey);
    }
  }

  private RuleKey getRuleKey(String languageKey) {
    return RuleKey.of(XooRulesDefinition.XOO_REPOSITORY, getRuleKey());
  }

  protected abstract void processFile(InputFile inputFile, File sonarFile, SensorContext context, RuleKey ruleKey, String languageKey);
}
