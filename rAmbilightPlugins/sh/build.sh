#!/bin/sh

PLUGINS="$HOME/Library/Application Support/rAmbilight/plugins";
CLASSROOT="../bin/classes/";
CLASS="com/rambilight/plugins";
if [ ! -d "$PLUGINS" ]; then
  mkdir "$PLUGINS";
fi

for part in $(echo $1 | tr "." "\n"); do
    NAME="$part";
    EXTENSION="$CLASS/extensions/$NAME";
    jar cvf "${PLUGINS}/${NAME}.jar" -C "${CLASSROOT}" "${CLASS}/${NAME}" -C "${CLASSROOT}" "${EXTENSION}";
    break;
done;
echo " ";
echo "Saved to $PLUGINS";