/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo.rule;

import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.xoo.Xoo;

/**
 * Define all the coding rules that are supported on the repositories named "xoo"
 */
public class XooRulesDefinition implements RulesDefinition {

  public static final String XOO_REPOSITORY = "xoo";

  private static void defineRulesXoo(Context context) {
    NewRepository repo = context.createRepository(XOO_REPOSITORY, Xoo.KEY).setName("Xoo");

    NewRule hasTag = repo.createRule(HasTagSensor.RULE_KEY).setName("Has Tag")
      .setHtmlDescription("Search for a given tag in Xoo files");
    hasTag.setDebtSubCharacteristic(SubCharacteristics.READABILITY)
      .setDebtRemediationFunction(hasTag.debtRemediationFunctions().constantPerIssue("2min"));
    hasTag.createParam("tag")
      .setDefaultValue("xoo")
      .setDescription("The tag to search for");

    NewRule ruleWithParameters = repo.createRule("RuleWithParameters").setName("Rule with parameters")
      .setHtmlDescription("Rule containing parameter of different types : boolean, integer, etc. For information, no issue will be linked to this rule.");
    ruleWithParameters.createParam("string").setType(RuleParamType.STRING);
    ruleWithParameters.createParam("text").setType(RuleParamType.TEXT);
    ruleWithParameters.createParam("boolean").setType(RuleParamType.BOOLEAN);
    ruleWithParameters.createParam("integer").setType(RuleParamType.INTEGER);
    ruleWithParameters.createParam("float").setType(RuleParamType.FLOAT);

    NewRule oneIssuePerLine = repo.createRule(OneIssuePerLineSensor.RULE_KEY).setName("One Issue Per Line")
      .setHtmlDescription("Generate an issue on each line of a file. It requires the metric \"lines\".");
    oneIssuePerLine.setDebtSubCharacteristic(SubCharacteristics.MEMORY_EFFICIENCY)
      .setDebtRemediationFunction(hasTag.debtRemediationFunctions().linear("1min"))
      .setEffortToFixDescription("It takes about 1 minute to an experienced software craftsman to remove a line of code");

    NewRule oneIssuePerFile = repo.createRule(OneIssuePerFileSensor.RULE_KEY).setName("One Issue Per File")
      .setHtmlDescription("Generate an issue on each file");
    oneIssuePerFile.setDebtSubCharacteristic(SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
      .setDebtRemediationFunction(hasTag.debtRemediationFunctions().linear("10min"));

    NewRule oneIssuePerModule = repo.createRule(OneIssuePerModuleSensor.RULE_KEY).setName("One Issue Per Module")
      .setHtmlDescription("Generate an issue on each module");
    oneIssuePerModule.setDebtSubCharacteristic(SubCharacteristics.API_ABUSE)
      .setDebtRemediationFunction(hasTag.debtRemediationFunctions().linearWithOffset("25min", "1h"))
      .setEffortToFixDescription("A certified architect will need roughly half an hour to start working on removal of modules, " +
        "then it's about one hour per module.");

    repo.createRule(OneBlockerIssuePerFileSensor.RULE_KEY).setName("One Blocker Issue Per File")
      .setHtmlDescription("Generate a blocker issue on each file, whatever the severity declared in the Quality profile");

    repo.done();

  }

  @Override
  public void define(Context context) {
    defineRulesXoo(context);
  }

}
