package com.rambilight.core.serial;

import com.rambilight.core.api.Global;
import com.rambilight.core.api.Light.Light;
import com.rambilight.core.api.ui.MessageBox;

import java.util.LinkedList;
import java.util.Queue;

public class ComDriver {

    private SerialController serial;
    private LightHandlerCore lightHandler;
    private Queue<Byte>      serialBuffer;

    private long lastPing               = 0;
    private long lastReceived           = 0;
    private long ticksSinceLastReceived = 0;    // 1 tick ~10ms.

    private boolean writtenPrefs         = false;
    private boolean displayedBusyMessage = false;

    public ComDriver() {
        lightHandler = new LightHandlerCore(Global.numLights);
        serial = new SerialControllerJSSC();
        serialBuffer = new LinkedList<>();

        serial.setEventListener((data) -> receivedPacket(data));
        serial.setDisconnectedListener((data) -> {
            if (Global.isSerialConnectionActive) {
                System.out.println("Lost connection to the USB device.");
                Global.isSerialConnectionActive = false;
            }
        });
    }


    public LightHandlerCore getLightHandler() {
        return lightHandler;
    }

    public boolean initialize() throws Exception {

        String[] ports = serial.getAvailablePorts();
        boolean foundPort = false;
        if (Global.serialPort.length() > 0)
            for (String port : ports)
                if (Global.serialPort.equals(port)) {
                    foundPort = true;
                    break;
                }

        if (!foundPort && ports.length > 0) {
            System.out.println("No serial port specified. Finding most appropriate port...");
            if (ports.length == 1) {
                System.out.println("One port found: " + ports[0]);

                String result = MessageBox.Input("Port found!", "Are you sure you want to use this port as an rAmbilight device? (Type 'Yes' or 'No')'\n" + ports[0] + "\n ");
                if (result == null || (!result.toLowerCase().equals("y") && !result.toLowerCase().equals("yes")))
                    throw new Exception("Some thing other than YES as entered. Shutting down...");

                System.out.println("Activating...");
                Global.serialPort = ports[0];
            }
            else {
                System.out.println("Available ports:");
                String portList = "";
                int i = 0;
                for (String s : ports) {
                    System.out.println(s);
                    portList += i++ + ": " + s + "\n";
                }
                System.out.println("");

                String extraMessage = "";
                while (true) {
                    String result = MessageBox.Input("Port select", extraMessage + "Which port should be activated? (Type the line number in the box below. 'E' to exit.)\n" + portList);
                    if (result != null && result.toLowerCase().equals("e"))
                        throw new Exception("No port was chosen. Shutting down...");
                    try {
                        Global.serialPort = ports[Integer.parseInt(result)];
                        break;
                    } catch (Exception e) {
                        extraMessage = result + " isn't a valid input!\n";
                    }
                }
            }
        }

        int errorCode = serial.initialize(Global.serialPort);

        lastReceived = System.currentTimeMillis();
        ticksSinceLastReceived = 0;

        if (errorCode == 1)
            if (displayedBusyMessage) {
                System.out.println("The port seems to be used by another application");
                return false;
            }
            else {
                displayedBusyMessage = true;
                throw new Exception("The port seems to be used by another application, please close any other application which might communicate with the device.\nIf you don't know of any application which might be running and be connected to the USB device, try removing and reinserting the USB cable it into the computer.");
            }
        else if (errorCode == 2)
            return false;

        displayedBusyMessage = false;
        System.out.println("Serial baud rate: " + serial.getDataRate());
        Global.isSerialConnectionActive = true;
        lightHandler.reset();
        return true;
    }

    public boolean close() {
        return !serial.isOpen() || serial.close();
    }

    public boolean update() {
        long now = System.currentTimeMillis();
        ticksSinceLastReceived++;
        if (ticksSinceLastReceived > 100 && Global.isSerialConnectionActive) {
            if (now - lastReceived > 8000) {
                Global.isSerialConnectionActive = false;
                System.err.println("The system seems to have halted.");
                MessageBox.Error("If you recently unplugged the USB device and haven't reinserted it, you can ignore this message!\n\nUnable to connect to the device, please unplug and reinsert the USB device.\nPress OK when this has been done.\n\nWARNING: DON'T QUIT the application while the serial port is in this state, since it will lock it. To fix this, just force close all other instances of the application.\n\nNOTE: There is a known bug which causes this problem, which hopefully will be fixed in on of the upcoming releases.\nUntil then, when this window pops up, please just reinsert the USB device \n\nRegards\nThe rAmbilight development team");
                return false;

            /*if (serial.getAvailablePorts().length == 0) {
                Global.requestExit = true;
                throw new Exception("The serial device can no longer be found.\n Please reinsert it and restart the application. \n\n Exiting...");
            }

            boolean cache = Global.isActive;
            try {
                Global.isActive = false;
                System.err.println("The system seems to have halted.");
                if (serial.isOpen())
                    serial.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                System.out.print("Reopening port...");
                if (!serial.initialize(Global.serialPort))
                    throw new Exception("Unable to open port");
                System.out.println(" Opened!");
                lastReceived = now;
                writtenPrefs = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Global.isActive = cache;*/
            }
            else if (now - lastReceived > 2000) {
                System.out.println("Lost connection to the USB device.");
                Global.isSerialConnectionActive = false;
                return false;
            }
            else if (now - lastPing > 750)
                ping();
        }
        else if (now - lastPing > 750)
            ping();
        return true;
    }

    public boolean serialPortsAvailable() {
        return serial.getAvailablePorts().length > 0;
    }


    private void write(byte[] b) {
        try {
            serial.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write(byte b) {
        try {
            serial.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToBuffer(byte b) {
        serialBuffer.add(b);
    }

    private void writeToBuffer(byte b[]) {
        for (byte bi : b) {
            serialBuffer.add(bi);
        }
    }

    private void writePreference(byte preference, int value) {
        writeToBuffer(ArduinoCommunication.BEGIN_SEND_PREFS);

        writeToBuffer(preference);
        writeToBuffer((byte) value);
    }


    private void flushBuffer() {
        if (serialBuffer.size() > 0) {
            byte[] toWrite = new byte[serialBuffer.size()];

            int i = 0;
            while (serialBuffer.size() > 0)
                toWrite[i++] = serialBuffer.poll();

            write(toWrite);
        }
    }

    private void flushLights() {
        if (!lightHandler.requiresUpdate())
            return;
        lastPing = System.currentTimeMillis();

        Light light;

        flushBuffer();
        writeToBuffer(ArduinoCommunication.BEGIN_SEND); // Should be more efficient than an ordinary write
        for (int i = 0; i < 13; i++) {
            if ((light = lightHandler.next()) == null)
                break;
            writeToBuffer(new byte[]{(byte) light.id, (byte) light.r, (byte) light.g, (byte) light.b});

        }
        writeToBuffer(ArduinoCommunication.END_SEND);
        flushBuffer();

    }

    private void flushPreferences() {
        writePreference(ArduinoCommunication.CLEAR_BUFFER, ArduinoCommunication.NULL);
        writePreference(ArduinoCommunication.NUMBER_OF_LEDS, Global.numLights);
        writePreference(ArduinoCommunication.SMOOTH_STEP, Global.lightStepSize);
        flushBuffer();
    }


    private void receivedPacket(int data) {
        lastReceived = System.currentTimeMillis();
        ticksSinceLastReceived = 0;

        switch (data) {
            case 1: // Ready
                if (!writtenPrefs) {
                    writtenPrefs = true;
                    serialGateway(Gateway.preferences);
                }
                serialGateway(Gateway.data);
                break;
            case 2: // Sleeping
                break;
            default:
                System.out.println("Received:" + data);
                break;
        }
    }

    private synchronized void serialGateway(Gateway gate) {
        switch (gate) {
            case ping:
                pingLights();
                break;
            case data:
                flushLights();
                break;
            case preferences:
                flushPreferences();
                break;
        }
    }

    public void ping() {
        lastPing = System.currentTimeMillis();
        serialGateway(Gateway.ping);
    }

    private void pingLights() {
        write(ArduinoCommunication.END_SEND);
    }


    private enum Gateway {
        ping, data, preferences
    }

    private static class ArduinoCommunication {

        public static final byte NULL = 0;

        public static final byte NUMBER_OF_LEDS    = 1;
        public static final byte SMOOTH_STEP       = 2;
        public static final byte COMPRESSION_LEVEL = 3;
        public static final byte CLEAR_BUFFER      = 4;

        public static final byte END_SEND         = (byte) 254;
        public static final byte BEGIN_SEND       = (byte) 255;
        public static final byte BEGIN_SEND_PREFS = (byte) 253;
    }

}
