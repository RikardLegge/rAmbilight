#!/bin/sh

PLUGINS="$HOME/Library/Application Support/rAmbilight/plugins";
CLASSROOT="../bin/classes/";
CLASS="com/rambilight/plugins";
MODULE="Ambilight";
NAME="SingleColor";

if [ ! -d "$PLUGINS" ]; then
  mkdir "$PLUGINS";
fi

cp -f "$CLASSROOT$CLASS/$MODULE/extensions/$NAME.class" "$PLUGINS/$MODULE.$NAME.class";
echo "Exported plugin NAME";