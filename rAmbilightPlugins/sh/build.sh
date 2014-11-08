#!/bin/sh

JAR="/Users/me/Library/Application Support/rAmbilight/plugins"
CLASSROOT="../build/classes/"
CLASS="com/rambilight/plugins"
if [ ! -d "$JAR" ]; then
  mkdir "$JAR"
  ls
fi

for part in $(echo $1 | tr "." "\n"); do
    NAME="$part"
    jar cvf "${JAR}/${NAME}.jar" -C "${CLASSROOT}" "${CLASS}/${NAME}"
break;done