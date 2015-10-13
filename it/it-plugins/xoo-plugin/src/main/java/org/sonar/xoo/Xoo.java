/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo;

import org.sonar.api.resources.Language;

public class Xoo implements Language {

  public static final String KEY = "xoo";
  public static final String NAME = "Xoo";
  public static final String FILE_SUFFIX = ".xoo";

  private static final String[] XOO_SUFFIXES = {
    FILE_SUFFIX
  };

  @Override
  public String getKey() {
    return KEY;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getFileSuffixes() {
    return XOO_SUFFIXES;
  }
}
