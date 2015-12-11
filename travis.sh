#!/bin/bash

set -euo pipefail

function installTravisTools {
  mkdir ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/v21 | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}

installTravisTools

case "$TESTS" in

CI)
  mvn verify -B -e -V
  ;;

IT-DEV)
  start_xvfb

  build_snapshot "SonarSource/sonarqube"

  mvn install -Dsource.skip=true -Denforcer.skip=true -Danimal.sniffer.skip=true -Dmaven.test.skip=true

  cd it
  mvn -Dsonar.runtimeVersion="DEV" -Dmaven.test.redirectTestOutputToFile=false install
  ;;

IT-LTS_OR_OLDEST_COMPATIBLE)
  start_xvfb

  mvn install -Dsource.skip=true -Denforcer.skip=true -Danimal.sniffer.skip=true -Dmaven.test.skip=true

  cd it
  mvn -Dsonar.runtimeVersion="5.2-RC2" -Dmaven.test.redirectTestOutputToFile=false install
  ;;

esac
