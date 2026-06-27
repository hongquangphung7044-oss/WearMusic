#!/bin/sh

# Gradle wrapper script for Linux/macOS
set -e

APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Add default JVM options here
DEFAULT_JVM_OPTS=""

# Use the maximum available, or set MAX_FD != -1 to use that value
MAX_FD="maximum"

case "$(uname)" in
  CYGWIN*)
    CYGWIN=1
    ;;
  Darwin*)
    DARWIN=1
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVACMD="$JAVA_HOME/bin/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    echo "Please set the JAVA_HOME variable to match your Java installation."
    exit 1
fi

# Collect arguments for the java command
set -- "-jar" "$CLASSPATH" "$@"

"$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
