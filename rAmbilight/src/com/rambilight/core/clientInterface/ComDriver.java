package com.rambilight.core.clientInterface;

import com.rambilight.core.rAmbilight;
import com.rambilight.core.api.Global;
import com.rambilight.core.api.Light.Light;
import com.rambilight.core.api.ui.MessageBox;
import com.rambilight.core.preferences.i18n;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The communication handled which gives an abstraction between the core and the serial controller.
 */
public class ComDriver {

    private SerialController serial;
    private LightHandlerCore lightHandler;
    private Queue<Byte>      serialBuffer;

    private long lastPing               = 0;
    private long lastReceived           = 0;
    private long ticksSinceLastReceived = 0;    // 1 tick ~10ms.

    private boolean writtenPrefs         = false;
    private boolean displayedBusyMessage = false;
    private boolean allowSerialUpdate    = false;

    public ComDriver(SerialController serialController) {
        lightHandler = new LightHandlerCore(Global.numLights);
        serial = serialController;
        serialBuffer = new LinkedList<>();

        serial.setEventListener((data) -> receivedPacket(data));
        serial.setDisconnectedListener((data) -> close());
    }

    public LightHandlerCore getLightHandler() {
        return lightHandler;
    }

    public boolean initialize() throws Exception {

        String[] ports = serial.getAvailablePorts();
        //if (ports.length == lastNumPorts)
        //    return false;
        //lastNumPorts = ports.length;
        writtenPrefs = false;

        boolean foundPort = false;
        if (Global.serialPort.length() > 0)
            for (String port : ports)
                if (Global.serialPort.equals(port)) {
                    foundPort = true;
                    break;
                }

        if (!foundPort && ports.length > 0) {
            System.out.println(i18n.noSerialPortSpecified);
            if (ports.length == 1) {
                System.out.println(String.format(i18n.onePortFound, ports[0]));

                String result = MessageBox.Input(i18n.portFound, String.format(i18n.usePortQuestion, ports[0]));
                if (result == null || (!result.toLowerCase().equals("y") && !result.toLowerCase().equals("yes")))
                    return false;

                System.out.println(i18n.activating);
                Global.serialPort = ports[0];
            }
            else {
                String portList = "";
                int i = 0;
                for (String port : ports) {
                    portList += i++ + ": " + port + "\n";
                }

                String extraMessage = "";
                while (true) {
                    String result = MessageBox.Input(i18n.portSelect, String.format(i18n.selectPortActivation, extraMessage, portList));
                    if (result != null && result.toLowerCase().equals("e"))
                        throw new Exception(i18n.noPortSelectShutDown);
                    try {
                        Global.serialPort = ports[Integer.parseInt(result)];
                        break;
                    } catch (Exception e) {
                        extraMessage = String.format(i18n.notValidInput, result);
                    }
                }
            }
        }

        rAmbilight.trayControllerSetMessage(i18n.connectingToDevice, true);
        int errorCode = serial.initialize(Global.serialPort);

        lastReceived = System.currentTimeMillis();
        ticksSinceLastReceived = 0;

        if (errorCode == 1)
            if (displayedBusyMessage) {
                System.out.println(i18n.portBusy);
                return false;
            }
            else {
                displayedBusyMessage = true;
                System.out.println(i18n.portBusyLong);
                return false;
            }
        else if (errorCode == 2)
            return false;

        displayedBusyMessage = false;
        System.out.println(String.format(i18n.baudRate, serial.getDataRate()));
        lightHandler.reset();
        onConnect();
        return true;
    }

    public boolean close() {
        onDisconnect();
        return !serial.isOpen() || serial.close();
    }

    public boolean update() {
        PackageCounter.update();
        long now = System.currentTimeMillis();
        ticksSinceLastReceived++;
        if (allowSerialUpdate && lightHandler.requiresUpdate())
            serialGateway(Gateway.data);    // TODO: Will never trigger, bug fix needed
        else if (ticksSinceLastReceived > 100 && Global.isSerialConnectionActive) {
            // Might have halted
            if (now - lastReceived > 8000) {
                rAmbilight.trayControllerSetMessage(i18n.reinsertCable, false);
                close();
                System.out.println(i18n.hasHalted);
                return false;
            }
            else if (now - lastReceived > 2000) {
                close();
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


    private void onDisconnect() {
        onDisconnect(i18n.connectionLost);
    }

    private void onDisconnect(String message) {
        if (Global.isSerialConnectionActive) {
            write(ArduinoCommunication.DISCONNECT);
            System.out.println(message);
            Global.isSerialConnectionActive = false;
        }
    }

    private void onConnect() {
        Global.isSerialConnectionActive = true;
    }


    private void write(byte[] b) {
        PackageCounter.add(b.length);
        try {
            serial.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write(byte b) {
        PackageCounter.add(1);
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
        if (serialBuffer.size() > 64) {
            System.out.println("Arduino serial buffer overflow: " + serialBuffer.size());
        }
        if (serialBuffer.size() > 0) {
            byte[] toWrite = new byte[serialBuffer.size()];

            int i = 0;
            while (serialBuffer.size() > 0)
                toWrite[i++] = serialBuffer.poll();

            write(toWrite);
        }
    }

    private void flushLights() {
        if (lightHandler.requiresUpdate()) {
            allowSerialUpdate = false;
            lastPing = System.currentTimeMillis();

            writeToBuffer(ArduinoCommunication.BEGIN_SEND); // Should be more efficient than an ordinary write

            Light light;
            flushBuffer();
            for (int i = 0; i < 13; i++) {
                if ((light = lightHandler.next()) == null)
                    break;
                writeToBuffer(new byte[]{(byte) light.id, (byte) light.r, (byte) light.g, (byte) light.b});
            }
            writeToBuffer(ArduinoCommunication.END_SEND);
            flushBuffer();
        }
        else
            ping();
    }

    private void flushPreferences() {
        writtenPrefs = true;
        writePreference(ArduinoCommunication.CLEAR_BUFFER, ArduinoCommunication.NULL);
        writePreference(ArduinoCommunication.NUMBER_OF_LEDS, Global.numLights);
        writePreference(ArduinoCommunication.SMOOTH_STEP, Global.lightStepSize);
        writePreference(ArduinoCommunication.COMPRESSION_LEVEL, Global.compressionLevel);
        flushBuffer();
    }


    public void ping() {
        lastPing = System.currentTimeMillis();
        serialGateway(Gateway.ping);
    }

    private void pingLights() {
        write(ArduinoCommunication.PING);
    }


    private void receivedPacket(int data) {
        lastReceived = System.currentTimeMillis();
        ticksSinceLastReceived = 0;
        switch (data) {
            case 253: // Needs setup
                serialGateway(Gateway.preferences);
                break;
            case 252: // PING
                if (!writtenPrefs)
                    serialGateway(Gateway.preferences);
                else {
                    //allowSerialUpdate = true;
                    serialGateway(Gateway.data);
                }
                break;
            default:
                System.out.println("Relieved unknown data with value: " + data);
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


    private enum Gateway {
        ping, data, preferences
    }

    private static class ArduinoCommunication {

        public static final byte NULL = 0;

        public static final byte NUMBER_OF_LEDS    = 1;
        public static final byte SMOOTH_STEP       = 2;
        public static final byte COMPRESSION_LEVEL = 3;
        public static final byte CLEAR_BUFFER      = 4;

        public static final byte DISCONNECT       = (byte) 251;
        public static final byte PING             = (byte) 252;
        public static final byte BEGIN_SEND_PREFS = (byte) 253;
        public static final byte END_SEND         = (byte) 254;
        public static final byte BEGIN_SEND       = (byte) 255;
    }

    private static class PackageCounter {
        private static int  packageCount        = 0;
        private static int  packageContentCount = 0;
        private static long lastOutput          = 0;

        private static void update() {
            if (System.currentTimeMillis() - lastOutput > 1000) {
                System.out.println(String.format("Packages: %03d st, Contents: %d bytes", packageCount, packageContentCount));
                packageCount = 0;
                packageContentCount = 0;
                lastOutput = System.currentTimeMillis();
            }
        }

        private static void add(int count) {
            packageCount++;
            packageContentCount += count;
        }
    }
}
