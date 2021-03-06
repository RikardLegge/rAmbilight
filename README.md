## rAmbilight

rAmbilight is a home brew project to allow anyone to get a hold of  an easy to use, efficient, affordable Ambient lighting system. It started out as a project for school, but has since then developed into a framework which allows easy manipulation of addressable light strips, using an Arduino micro controller and a host computer.

## Showcase

Since I've been the only contributor to the project, I havn't focused on creating a website to showcase the ambilight.

Photos can on the other hand be found [here on Google+](https://plus.google.com/photos/104773716095315761126/albums/6078003639491029425?authkey=CK3J6PTm6f3rCw).

## Repository layout

The repository is divided into 3 parts which have their own important role.

#### Distribution (dist)

The directory which contains pre-build files. Currently only for Mac OSX, but the configuration necessary for a windows build is available. 

#### Core (rAmbilight)

The source code for the core part of the project, which includes a module handler, serial connection handler, and API interface to access the loaded modules. This also includes the source code for the Arduino microcomputer.

#### Plugins (rAmbilightPlugins)

A directory currently containing the following plugins:
+ Hello World
+ Built in effects
+ Ambilight
+ Push bullet

There are currently no pre-built versions of the plugins, but to build a plugin all that has to be done is change the PATH variable in `sh/build.sh` file to reflect the platform to run on. Currently the `build.sh` file path points to the plugin directory on OSX.

## The API

In the core library there is a built in API interface for development of plugins. 
It lies under the path `com.rambilight.core.api`.

## Hello World

Below is a simple class which which shows basic plugin / module. For example, the snippet bellow changes light with position 1 to red when the class is loaded.

```java

// Create a class which extends the Module class included in the API
public class HelloWorld extends Module {
    // Function which is called when the module is loaded
    public void loaded() {
        // Sets light with position <1> to red.
        lightHandler.addToUpdateBuffer(1, 255, 0, 0);
    }
```

## Javadoc
The javadoc for the project can be found at the following url. This url will how ever change in the future when the whole project is merged into one repository. 

[See the javadoc](http://rikardlegge.github.io/rAmbilight/)

## Installation

Installation is as easy as adding the Application.jar file to the projects dependency paths. 
To run the application using the API, create a new instance of the Debug class, with the module as an input parameter.

Currently the file can be found in `dist/rAmbilight.app/Contents/Resources/Java/Application.jar`.
This will change when a better build script is created.

```java

// Create a class with a meaningfull class name.
public class ModuleDebugger {
    // The default java method of starting an application
    public static void main(String[] args) throws Exception {
        // Create a new instance of the rAmbilight core and load the application in debug mode. 
        // Set the HelloWorld class as the input parameter, to allow code hotswap and speed up 
        // development and debugging.
        new rAmbilight().loadDebugger(HelloWorld.class);
    }
}

```


It's recommended that the Example project Hello_World is used as a starting point, since it includes all the most important functions of the framework, and also being documented.

## Export

To use the module which you've created, just export it as a jar or class file. When rAmbilight starts, it searches through the directory located at
> OS X: /User/%USERNAME%/Library/Application Support/rAmbilight/plugins

> Windows: C:/Users/%USERNAME%/AppData/Local/rAmbilight/plugins

> Linux: /home/%USERNAME%/.rAmbilight/plugins

## Contributors
Rikard Legge  - rikard.legge@gmail.com

## License
GNU General Public License v3.0
Copyright (C) 2015 Rikard Legge

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

http://www.gnu.org/licenses/
