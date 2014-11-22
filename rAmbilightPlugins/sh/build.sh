#!/bin/sh

PLUGINS="~/Library/Application Support/rAmbilight/plugins"
CLASSROOT="../bin/classes/"
CLASS="com/rambilight/plugins"
if [ ! -d "$PLUGINS" ]; then
  mkdir "$PLUGINS"
fi

for part in $(echo $1 | tr "." "\n"); do
    NAME="$part"
    jar cvf "${PLUGINS}/${NAME}.jar" -C "${CLASSROOT}" "${CLASS}/${NAME}"
    break;
done