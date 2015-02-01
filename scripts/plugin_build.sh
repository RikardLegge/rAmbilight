#!/bin/sh

PLUGINS="$HOME/Library/Application Support/rAmbilight/plugins";     # Build output path
CLASSROOT=../rAmbilightPlugins/bin;                              # Root path to the compiled plugin classes
CLASSPREFIX=com/rambilight/plugins;                               # The required plugin path prefix
PLUGINNAME="$1";                                                    # The filename of the plugin to build

# If no plugin name is available, exit
if [ -z "$PLUGINNAME" ]; then
    echo "No input path specified.";
    exit 1;
fi

# If the output path doesn't exist, create it
if [ ! -d "$PLUGINS" ]; then
    echo "The plugin target directory was not found, creating it at '$PLUGINS'";
    mkdir "$PLUGINS";
fi

# Package the files into a JAR and place it in the plugins directory
echo "Packaging files...";
echo " ";

jar cvf "${PLUGINS}/${PLUGINNAME}.jar" -C "$CLASSROOT" "$CLASSPREFIX/$PLUGINNAME";

echo " ";
echo "Finished exporting $PLUGINNAME to $PLUGINS";