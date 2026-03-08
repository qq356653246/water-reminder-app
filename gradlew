#!/bin/sh
# Gradle wrapper script

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Download gradle if not exists
GRADLE_VERSION="8.2"
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"

if [ ! -d "$GRADLE_HOME" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "$GRADLE_HOME"
    curl -L "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d "$GRADLE_HOME"
    rm /tmp/gradle.zip
fi

GRADLE_BIN=$(find "$GRADLE_HOME" -name "gradle" -type f | head -1)

exec "$GRADLE_BIN" "$@"
