## rAmbilight

rAmbilight is a home brew project to allow anyone to get a hold of  an easy to use, efficient, affordable Ambient lighting system. It started out as a project for school, but has since then developed into a framework which allows easy manipulation of addressable light strips, using an Arduino micro controller.

## Parts

The repositry is divided into 4 parts which have their own important role.

#### Distribution (dist)

The directory which contains prebuild files. Currently only for OSX, but the configuration necesarry for a windows build is avaiable. 

#### Device (rAmbilight)

The source code for the core part of the project, which includes a module handler, serial connection handler, screen capturer, and API interface to access the loaded modules. This also includes the source code for the arduino microcontroller.

#### API (rAmbilightAPI) : DISCONTINUED

Important: The API is about to become merged into the rAmbilight core. Only the docs are left to be ported.
The interface which any plugin / module are required to use to be supported by the module handler. This also includes a onscreen debugger for testing an application before connecting it to the microcontroller. This should help debugging since the microcontroller doesn't support debugging.

#### Plugins (rAmbilightPlugins)

A directory currently containing the following plugins:
+ Hello World
+ Built in effects
+ Ambilight
+ Push bullet

There are currently no pre-built versions of the plugins, but to build a plugin all that has to be done is change the PATH variable in `sh/build.sh` file to reflect the platform to run on. Currently the `build.sh` file path points to the plugin directory on OSX. For more information about plugins se [this github page](https://github.com/RikardLegge/rAmbilght-Framework).

## Contributors

Rikard Legge  - rikard.legge@gmail.com

## License

Copyright Rikard Legge, All rights reserved.