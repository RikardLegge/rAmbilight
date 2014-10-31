#!/bin/sh

JAR="/Users/me/Library/Application Support/rAmbilight/plugins"
#"../../dist/plugins"
CLASS="../build/classes/com/rambilight/plugins"

if [ ! -d "$JAR" ]; then
  mkdir "$JAR"
  ls
fi

for part in $(echo $1 | tr "." "\n"); do
    NAME="$part"
    jar cvf "${JAR}/${NAME}.jar" -C "${CLASS}/${NAME}" .
break;done