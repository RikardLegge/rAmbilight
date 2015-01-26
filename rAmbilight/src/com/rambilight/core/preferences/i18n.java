package com.rambilight.core.preferences;

import java.util.Hashtable;

/**
 * Beelining of an implementation of a version of i18n, Localization.
 * Currently only for cleaning the code from strings.
 */
public class i18n {

    private static Hashtable<String, String> translations = new Hashtable<>();

    public static String noSerialPortSpecified = "No serial port specified. Finding most appropriate port...";
    public static String onePortFound          = "One port found: %s";
    public static String portFound             = "Port found!";
    public static String usePortQuestion       = "Are you sure you want to use this port as an rAmbilight device? (Type 'Yes' or 'No')'\n" +
            "%s\n";
    public static String activating            = "Activating...";
    public static String portSelect            = "Port select";
    public static String selectPortActivation  = "%sWhich port should be activated? (Type the line number in the box below. 'E' to exit.)\n%s";
    public static String noPortSelectShutDown  = "No port was chosen. Shutting down...";

    public static String notValidInput      = "%s isn't a valid input!\n";
    public static String connectingToDevice = "Connecting to device";
    public static String portBusy           = "The port seems to be used by another application";
    public static String portBusyLong       = "The port seems to be used by another application, please close any other application which might communicate with the device.\nIf you don't know of any application which might be running and be connected to the USB device, try removing and reinserting the USB cable it into the computer.";

    public static String baudRate       = "Serial baud rate: %d";
    public static String reinsertCable  = "Please reinsert the USB cable";
    public static String hasHalted      = "The system seems to have halted.";
    public static String connectionLost = "Lost connection to the USB device.";
    public static String key            = "val";

}
