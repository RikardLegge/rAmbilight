#!/bin/sh

PLUGINS=../dist/rAmbilight.app/Contents/Resources/plugins           #"$HOME/Library/Application Support/rAmbilight/plugins";     # Build output path
CLASSROOT=../rAmbilightPlugins/bin;                                 # Root path to the compiled plugin classes
CLASSPREFIX=com/rambilight/plugins;                                 # The required plugin path prefix
PLUGINNAME="$1";                                                    # The filename of the plugin to build
EXTENSIONNAME="$2";                                                 # The filename of the plugin to build
DEPENDENCIES="";

# If no plugin name is available, exit
if [ -z "$PLUGINNAME" ] | [ -z "$EXTENSIONNAME" ]; then
    echo "No input path specified. Plese both specify the plugin name and the extension name.";
    echo "";
    echo "Usage: `basename $0` plugin_name extension_name"
    exit 1;
fi

# If the output path doesn't exist, create it
if [ ! -d "$PLUGINS" ]; then
    echo "The plugin target directory was not found, creating it at '$PLUGINS'";
    mkdir "$PLUGINS";
fi

# If the extension file isn't available
if [ ! -e "$CLASSROOT/$CLASSPREFIX/$PLUGINNAME/extensions/$EXTENSIONNAME.class" ]; then
    echo "The extension file wasn't found, are you sure it has been built?";
    exit 2;
fi

# If the extension has dependencies.
if [ -d "$CLASSROOT/$CLASSPREFIX/$PLUGINNAME/extensions/$EXTENSIONNAME" ]; then
    echo "Including extension dependencies";
    DEPENDENCIES="-C \"$CLASSROOT\" \"$CLASSPREFIX/$PLUGINNAME/extensions/$EXTENSIONNAME\"";
    echo "Packaging files...";
else
    echo "Packaging file...";
fi

# Package the files into a JAR and place it in the plugins directory
jar cvf "${PLUGINS}/${PLUGINNAME}.${EXTENSIONNAME}.jar" -C "$CLASSROOT" "$CLASSPREFIX/$PLUGINNAME/extensions/$EXTENSIONNAME.class" ${DEPENDENCIES};

echo " ";
echo "Finished exporting \"$PLUGINNAME.$EXTENSIONNAME\" to \"$PLUGINS\"";