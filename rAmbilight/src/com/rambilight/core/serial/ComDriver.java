package com.rambilight.core.serial;

import com.rambilight.core.preferences.Global;

import java.util.LinkedList;
import java.util.Queue;

public class ComDriver {

    private SerialControllerJSSC serial;
    private LightHandlerCore     lightHandler;
    private Queue<Byte>          serialBuffer;

    private long                 lastPing               = 0;
    private long                 lastRecived            = 0;
    private long                 ticksSinceLastRecieved = 0;    // 1 tick > ~10ms.

    private boolean              writtenPrefs           = false;

    public ComDriver() {
        lightHandler = new LightHandlerCore(Global.numLights);
        serial = new SerialControllerJSSC();
        serialBuffer = new LinkedList<Byte>();
    }

    public LightHandlerCore getLightHandler() {
        return lightHandler;
    }

    public void initialize() throws Exception {
        if (!serial.initialize(Global.serialPort))
            throw new Exception("Unable to connect to device");

        System.out.println("Serial baud rate: " + serial.getDataRate());
        serial.setEventListener((data) -> recivedPacket(data));
        lastRecived = System.currentTimeMillis();
    }

    public void ping() {
        lastPing = System.currentTimeMillis();
        serialGateway(Gateway.ping);
    }

    public void update() {
        long now = System.currentTimeMillis();
        ticksSinceLastRecieved++;
        if (now - lastRecived > 3000 && ticksSinceLastRecieved > 250) {
            boolean cache = Global.isActive;
            try {
                Global.isActive = false;
                System.err.println("The system seems to have halted.");
                serial.close();
                System.out.print("Reopening port...");
                if (!serial.initialize(Global.serialPort))
                    throw new Exception("Unable to open port");
                System.out.println(" Opened!");
                lastRecived = now;
                writtenPrefs = false;

            } catch (Exception e) {
                e.printStackTrace();
            }
            Global.isActive = cache;
        } else if (now - lastPing > 750)
            ping();
    }

    public void close() {
        serial.close();
    }

    private void recivedPacket(int data) {
        lastRecived = System.currentTimeMillis();
        ticksSinceLastRecieved = 0;
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
            System.out.println("Recived:" + data);
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
        write(ArduinoComunication.BEGIN_SEND);
        for (int i = 0; i < 12; i++) {
            while (lightHandler.requiresUpdate() && (light = lightHandler.next()).requiresUpdate == false) {}
            if (light == null)
                break;
            light.requiresUpdate = false;
            write(new byte[] { (byte) light.id, (byte) light.r, (byte) light.g, (byte) light.b });
        }
        write(ArduinoComunication.END_SEND);
    }

    private void flushPreferences() {
        writePreference(ArduinoComunication.CLEAR_BUFFER, ArduinoComunication.NULL);
        writePreference(ArduinoComunication.NUMBER_OF_LEDS, Global.numLights);
        writePreference(ArduinoComunication.FRAME_DELAY, Global.lightFrameDelay);
        writePreference(ArduinoComunication.SMOOTH_STEP, Global.lightStepSize);
    }

    private void writePreference(byte preference, int value) {
        writeToBuffer(ArduinoComunication.BEGIN_SEND_PREFS);

        writeToBuffer(preference);
        writeToBuffer((byte) value);

        flushBuffer();
    }

    private void pingLights() {
        write(ArduinoComunication.END_SEND);
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

    static class ArduinoComunication {

        public static final byte NULL             = 0;

        public static final byte NUMBER_OF_LEDS   = 1;
        public static final byte SMOOTH_STEP      = 2;
        public static final byte FRAME_DELAY      = 3;
        public static final byte CLEAR_BUFFER     = 4;

        public static final byte END_SEND         = (byte) 254;
        public static final byte BEGIN_SEND       = (byte) 255;
        public static final byte BEGIN_SEND_PREFS = (byte) 253;
    }

}
