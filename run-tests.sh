#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export PATH="/Users/spervala/homebrew/bin:/opt/homebrew/bin:$PATH"
JAVA_HOME_CANDIDATE="/Users/spervala/jdk/jdk-11.0.25+9/Contents/Home"
if [[ -d "$JAVA_HOME_CANDIDATE" ]]; then
  export JAVA_HOME="$JAVA_HOME_CANDIDATE"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
# Local runs are headed unless HEADLESS=true is set
export HEADLESS="${HEADLESS:-false}"
echo "Java: $(java -version 2>&1 | head -1)"
echo "Maven: $(mvn -v | head -1)"
echo "HEADLESS=$HEADLESS"
mvn test -B -Dmaven.compiler.release=11 -Dsurefire.suiteXmlFiles=master.xml "$@"
echo "Surefire: target/surefire-reports/"
ls -lt reports 2>/dev/null | head -5 || true
