/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.xoo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.MetricFinder;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.FileType;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;

/**
 * Parse files *.xoo.measures
 */
public class MeasureSensor implements Sensor {

  private static final String MEASURES_EXTENSION = ".measures";

  private ModuleFileSystem fileSystem;
  private MetricFinder metricFinder;

  public MeasureSensor(ModuleFileSystem ds, MetricFinder metricFinder) {
    this.fileSystem = ds;
    this.metricFinder = metricFinder;
  }

  @DependedUpon
  public Metric provide() {
    // This sensor will be responsible to provide line metrics
    return CoreMetrics.LINES;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return true;
  }

  public void analyse(Project project, SensorContext context) {
    analyse(project, context, Xoo.KEY);
  }

  public void analyse(Project project, SensorContext context, String languageKey) {
    // Process main sources
    List<File> ioFiles = fileSystem.files(FileQuery.on(FileType.SOURCE).onLanguage(languageKey));
    for (File ioFile : ioFiles) {
      org.sonar.api.resources.File xooFile = org.sonar.api.resources.File.fromIOFile(ioFile, project);
      if (xooFile != null) {
        processFileMeasures(xooFile, ioFile, context);
      }
    }

    // Process test sources
    ioFiles = fileSystem.files(FileQuery.on(FileType.TEST).onLanguage(languageKey));
    for (File ioFile : ioFiles) {
      org.sonar.api.resources.File xooFile = org.sonar.api.resources.File.fromIOFile(ioFile, project);
      if (xooFile == null) {
        // backward-compatibility with SQ 3.7
        xooFile = org.sonar.api.resources.File.fromIOFile(ioFile, project.getFileSystem().getTestDirs());
      }
      processFileMeasures(xooFile, ioFile, context);

    }
  }

  private void processFileMeasures(org.sonar.api.resources.File xooFile, File ioFile, SensorContext context) {
    File measureFile = new File(ioFile.getParentFile(), ioFile.getName() + MEASURES_EXTENSION);
    if (measureFile.exists()) {
      try {
        List<String> lines = FileUtils.readLines(measureFile, fileSystem.sourceCharset().name());
        int lineNumber = 0;
        for (String line : lines) {
          lineNumber++;
          if (StringUtils.isBlank(line)) {
            continue;
          }
          if (line.startsWith("#")) {
            continue;
          }
          try {
            String metricKey = StringUtils.substringBefore(line, ":");
            String value = line.substring(metricKey.length() + 1);
            Metric metric = metricFinder.findByKey(metricKey);
            if (metric == null) {
              throw new SonarException(metricKey + " is not a valid metric key");
            }
            saveMeasure(xooFile, context, metric, value);
          } catch (Exception e) {
            throw new SonarException("Error processing line " + lineNumber + " of file " + measureFile.getAbsolutePath(), e);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void saveMeasure(org.sonar.api.resources.File xooFile, SensorContext context, Metric metric, String value) {
    if (metric.getType() == ValueType.BOOL) {
      context.saveMeasure(xooFile, metric, Boolean.parseBoolean(value) ? 1.0 : 0.0);
    } else if (metric.isNumericType()) {
      context.saveMeasure(xooFile, metric, Double.valueOf(value));
    } else {
      Measure m = new Measure(metric, value);
      context.saveMeasure(xooFile, m);
    }
  }
}
