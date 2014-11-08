## rAmbilight

rAmbilight is a home brew project to allow anyone to get a hold of  an easy to use, efficient, affordable Ambient lighting system. It started out as a project for school, but has since then developed into a framework which allows easy manipulation of addressable light strips, using an Arduino micro controller.

## Hello World

Below is a simple class which which shows just how easy the API is to use. Everything which has to be 

```
#!java

// Create a class which extends the Module class included in the API
public class HelloWorld extends Module {
    // Function which is called when the module is loaded
    public void loaded() {
        // Sets light "1" to red.
        lightHandler.addToUpdateBuffer(1, 255, 0, 0);
    }
```

## Installation

Installation is as easy as adding the rAmbilightAPI.jar file to the projects dependency paths. 

## API Reference

Depending on the size of the project, if it is small and simple enough the reference docs can be added to the README. For medium size to larger projects it is important to at least provide a link to where the API reference docs live.

## Tests

Describe and show how to run the tests with code examples.

## Contributors

Rikard Legge  - rikard.legge@gmail.com

## License

Copyright Rikard Legge, All rights reserved.