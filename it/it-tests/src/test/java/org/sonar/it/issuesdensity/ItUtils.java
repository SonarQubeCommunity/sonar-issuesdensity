/*
 * it-issues-report
 * Copyright (C) 2014 ${owner}
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.it.issuesdensity;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import com.sonar.orchestrator.locator.FileLocation;
import org.apache.commons.io.FileUtils;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.io.File;

public final class ItUtils {

  private static String DEV_VERSION = "1.1-SNAPSHOT";

  private ItUtils() {
  }

  private static final File home;

  static {
    File testResources = FileUtils.toFile(ItUtils.class.getResource("/ItUtilsLocator.txt"));
    home = testResources // home/tests/src/tests/resources
      .getParentFile() // home/tests/src/tests
      .getParentFile() // home/tests/src
      .getParentFile() // home/tests
      .getParentFile(); // home
  }

  public static FileLocation issuesDensityPlugin() {
    return FileLocation.of("../../target/sonar-issues-density-plugin-" + DEV_VERSION + ".jar");
  }

  public static FileLocation xooPlugin() {
    return locateTestPlugin("xoo-plugin");
  }

  public static FileLocation locateTestPlugin(String artifactId) {
    return locateTestPlugin(locateTestPluginDir(artifactId), artifactId);
  }

  private static FileLocation locateTestPlugin(File pluginDir, String artifactId) {
    return locateTestPlugin(pluginDir, artifactId, "1.0-SNAPSHOT");
  }

  private static FileLocation locateTestPlugin(File pluginDir, String artifactId, String version) {
    File pluginJar = new File(pluginDir, "target/" + artifactId + "-" + version + ".jar");
    if (!pluginJar.exists()) {
      throw new IllegalArgumentException("Jar file of test plugin does not exist: " + pluginJar);
    }
    return FileLocation.of(pluginJar);
  }

  private static File locateTestPluginDir(String artifactId) {
    File pluginDir = locatePluginDir(artifactId);
    if (!pluginDir.exists()) {
      throw new IllegalArgumentException("Directory of test plugin does not exist: " + pluginDir);
    }
    return pluginDir;
  }

  public static File locatePluginDir(String pluginDirname) {
    return new File(locateHome(), "it-plugins/" + pluginDirname);
  }

  public static File locateHome() {
    return home;
  }

  public static File locateProjectDir(String projectName) {
    return new File(locateHome(), "it-projects/" + projectName);
  }

  public static Measure getMeasure(Orchestrator orchestrator, String resourceKey, String metricKey) {
    Resource resource = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics(resourceKey, metricKey));
    return resource != null ? resource.getMeasure(metricKey) : null;
  }

  public static SonarRunner createRunner(String project, String profile, String... properties) {
    return SonarRunner.create(ItUtils.locateProjectDir(project))
      .setProfile(profile)
      .setProperties("sonar.dynamicAnalysis", "false")
      .setProperties(properties);
  }

}
