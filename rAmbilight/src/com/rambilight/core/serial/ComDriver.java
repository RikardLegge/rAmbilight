package com.rambilight.core.serial;

import com.rambilight.core.Global;

import java.util.LinkedList;
import java.util.Queue;

public class ComDriver {

    private SerialControllerJSSC serial;
    private LightHandlerCore     lightHandler;
    private Queue<Byte>          serialBuffer;

    private long lastPing               = 0;
    private long lastReceived           = 0;
    private long ticksSinceLastReceived = 0;    // 1 tick > ~10ms.

    private boolean writtenPrefs = false;

    public ComDriver() {
        lightHandler = new LightHandlerCore(Global.numLights);
        serial = new SerialControllerJSSC();
        serialBuffer = new LinkedList<>();
    }

    public LightHandlerCore getLightHandler() {
        return lightHandler;
    }

    public void initialize() throws Exception {
        if (!serial.initialize(Global.serialPort))
            throw new Exception("Unable to connect to device");

        System.out.println("Serial baud rate: " + serial.getDataRate());
        serial.setEventListener((data) -> receivedPacket(data));
        lastReceived = System.currentTimeMillis();
    }

    public void ping() {
        lastPing = System.currentTimeMillis();
        serialGateway(Gateway.ping);
    }

    public void update() {
        long now = System.currentTimeMillis();
        ticksSinceLastReceived++;
        if (now - lastReceived > 3000 && ticksSinceLastReceived > 250) {
            boolean cache = Global.isActive;
            try {
                Global.isActive = false;
                System.err.println("The system seems to have halted.");
                System.out.println("Closing port.");
                serial.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
                System.out.print("Reopening port...");
                if (!serial.initialize(Global.serialPort))
                    throw new Exception("Unable to open port");
                System.out.println(" Opened!");
                lastReceived = now;
                writtenPrefs = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Global.isActive = cache;
        }
        else if (now - lastPing > 750)
            ping();
    }

    public void close() {
        serial.close();
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

    private void flushBuffer() {
        byte[] toWrite = new byte[serialBuffer.size()];

        int i = 0;
        while (serialBuffer.size() > 0)
            toWrite[i++] = serialBuffer.poll();

        write(toWrite);
    }

    private void flushLights() {
        if (!lightHandler.requiresUpdate())
            return;
        lastPing = System.currentTimeMillis();

        Light light = null;
        write(ArduinoCommunication.BEGIN_SEND);
        for (int i = 0; i < 12; i++) {
            while (lightHandler.requiresUpdate() && !(light = lightHandler.next()).requiresUpdate) ;
            if (light == null)
                break;
            light.requiresUpdate = false;
            write(new byte[]{(byte) light.id, (byte) light.r, (byte) light.g, (byte) light.b});
        }
        write(ArduinoCommunication.END_SEND);
    }

    private void flushPreferences() {
        writePreference(ArduinoCommunication.CLEAR_BUFFER, ArduinoCommunication.NULL);
        writePreference(ArduinoCommunication.NUMBER_OF_LEDS, Global.numLights);
        writePreference(ArduinoCommunication.FRAME_DELAY, Global.lightFrameDelay);
        writePreference(ArduinoCommunication.SMOOTH_STEP, Global.lightStepSize);
    }

    private void writePreference(byte preference, int value) {
        writeToBuffer(ArduinoCommunication.BEGIN_SEND_PREFS);

        writeToBuffer(preference);
        writeToBuffer((byte) value);

        flushBuffer();
    }

    private void pingLights() {
        write(ArduinoCommunication.END_SEND);
    }

    private enum Gateway {
        ping, data, preferences
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

    static class ArduinoCommunication {

        public static final byte NULL = 0;

        public static final byte NUMBER_OF_LEDS = 1;
        public static final byte SMOOTH_STEP    = 2;
        public static final byte FRAME_DELAY    = 3;
        public static final byte CLEAR_BUFFER   = 4;

        public static final byte END_SEND         = (byte) 254;
        public static final byte BEGIN_SEND       = (byte) 255;
        public static final byte BEGIN_SEND_PREFS = (byte) 253;
    }

}
