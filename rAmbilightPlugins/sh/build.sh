#!/bin/sh

JAR="/Users/me/Library/Application Support/rAmbilight/plugins"
#"../../dist/plugins"
CLASSROOT="../build/classes/"
CLASS="com/rambilight/plugins"
if [ ! -d "$JAR" ]; then
  mkdir "$JAR"
  ls
fi

for part in $(echo $1 | tr "." "\n"); do
    NAME="$part"
    jar cvfm "${JAR}/${NAME}.jar" mainfest.mf -C "${CLASSROOT}" "${CLASS}/${NAME}"
break;done