#!/usr/bin/env bash

# Get script directory - handle paths with spaces and special characters
SCRIPTPATH="$(cd "$(dirname "$0")" && pwd)"

# Use bundled JDK if available and working, otherwise use system JAVA_HOME or java in PATH
if [ -x "$SCRIPTPATH/jdk/bin/java" ] && "$SCRIPTPATH/jdk/bin/java" -version >/dev/null 2>&1; then
    export JAVA_HOME="$SCRIPTPATH/jdk"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    echo "Using system JAVA_HOME: $JAVA_HOME"
else
    JAVA_CMD=$(which java 2>/dev/null)
    if [ -n "$JAVA_CMD" ]; then
        export JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$JAVA_CMD")")")
        echo "Using java from PATH: $JAVA_HOME"
    else
        echo "ERROR: No Java installation found. Please install Java 17+ or set JAVA_HOME."
        exit 1
    fi
fi

export EKG_CONFIG="$SCRIPTPATH/configs"
export JRE_HOME="$JAVA_HOME"

# Build JavaFX module path from libs directory (only javafx jars)
# Use find to handle paths with spaces correctly
JAVAFX_MODULES=""
while IFS= read -r -d '' jar; do
    if [ -z "$JAVAFX_MODULES" ]; then
        JAVAFX_MODULES="$jar"
    else
        JAVAFX_MODULES="$JAVAFX_MODULES:$jar"
    fi
done < <(find "$SCRIPTPATH/libs" -maxdepth 1 -name 'javafx-*.jar' -print0 2>/dev/null)

# JavaFX modules loaded via module-path, application via classpath
"$JAVA_HOME/bin/java" \
    --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
    --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
    --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --module-path "$JAVAFX_MODULES" \
    --add-modules=javafx.controls,javafx.fxml,javafx.swing \
    -Dekg.solr.baseDir="$SCRIPTPATH/solr" \
    -Xmx8G \
    -classpath "$SCRIPTPATH/libs/*" \
    de.qaware.ekg.awb.application.base.EkgCdiApplication