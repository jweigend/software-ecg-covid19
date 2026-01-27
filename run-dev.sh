#!/usr/bin/env bash

# Development start script for Software-EKG
# Run this after building with: mvn clean install -DskipTests

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Find Java - prefer JAVA_HOME, fall back to system java
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# JavaFX module path from Maven repository
M2_REPO="${HOME}/.m2/repository"
JAVAFX_VERSION="11.0.2"
JAVAFX_PATH="${M2_REPO}/org/openjfx"

MODULE_PATH="${JAVAFX_PATH}/javafx-base/${JAVAFX_VERSION}/javafx-base-${JAVAFX_VERSION}-linux.jar"
MODULE_PATH="${MODULE_PATH}:${JAVAFX_PATH}/javafx-controls/${JAVAFX_VERSION}/javafx-controls-${JAVAFX_VERSION}-linux.jar"
MODULE_PATH="${MODULE_PATH}:${JAVAFX_PATH}/javafx-graphics/${JAVAFX_VERSION}/javafx-graphics-${JAVAFX_VERSION}-linux.jar"
MODULE_PATH="${MODULE_PATH}:${JAVAFX_PATH}/javafx-fxml/${JAVAFX_VERSION}/javafx-fxml-${JAVAFX_VERSION}-linux.jar"
MODULE_PATH="${MODULE_PATH}:${JAVAFX_PATH}/javafx-swing/${JAVAFX_VERSION}/javafx-swing-${JAVAFX_VERSION}-linux.jar"

# Build classpath from Maven dependency plugin
echo "Building classpath..."
CLASSPATH=$(mvn -q dependency:build-classpath -pl ekg-awb-application/awb-application-base -Dmdep.outputFile=/dev/stdout 2>/dev/null)

# Add the project's own compiled classes
CLASSPATH="${SCRIPT_DIR}/ekg-awb-application/awb-application-base/target/classes:${CLASSPATH}"

# Solr configuration path
SOLR_BASE_DIR="${SCRIPT_DIR}/ekg-awb-da/ekg-awb-da-solr/src/main/solr"

echo "Starting Software-EKG..."
exec "$JAVA_CMD" \
    --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
    --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
    --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --module-path "$MODULE_PATH" \
    --add-modules=javafx.controls,javafx.fxml,javafx.swing \
    -Xmx4G \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
    -Dekg.solr.baseDir="$SOLR_BASE_DIR" \
    -classpath "$CLASSPATH" \
    de.qaware.ekg.awb.application.base.EkgCdiApplication "$@"
