package com.rambilight.core.clientInterface.debug;

import com.rambilight.core.clientInterface.SerialController;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Debug serial controller which starts a visualizer and an approximation/emulation of how the code will
 * run on the arduino.
 */
public class SerialControllerLocal extends SerialController {

    private final static int BUFFERSIZE = 64;   // Arduino serial buffer length
    private SynchronizedArray outBuffer;
    private SynchronizedArray inBuffer;
    private ArduinoEmulator   arduinoSerialHandler;

    private SerialRuntime serialRuntime;

    public SerialControllerLocal() {
        outBuffer = new SynchronizedArray(BUFFERSIZE);
        inBuffer = new SynchronizedArray(BUFFERSIZE);
        arduinoSerialHandler = new ArduinoEmulator(outBuffer, inBuffer);
        serialRuntime = new SerialRuntime();
        serialRuntime.start();
    }

    public synchronized int initialize(String serialName) {
        serialRuntime.isOpen = true;
        return 0;
    }

    public String[] getAvailablePorts() {
        return new String[]{"local"};
    }

    public void update() {

    }

    public boolean isOpen() {
        return serialRuntime.isOpen;
    }

    public synchronized boolean close() {
        serialRuntime.isOpen = false;
        return true;
    }

    public boolean removeRootEventListener() {
        serialRuntime.isOpen = false;
        return true;
    }

    public void write(byte o) throws Exception {
        if (serialRuntime.isOpen)
            outBuffer.write(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte " + o);
    }

    public void write(byte[] o) throws Exception {
        if (serialRuntime.isOpen)
            outBuffer.write(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte[] with length " + o.length);
    }

    public void write(int o) throws Exception {
        throw new NotImplementedException();
    }


    class SerialRuntime extends Thread {
        private boolean isClosing = false;
        public  boolean isOpen    = false;

        public void run() {
            setName("Serial Runtime");
            isOpen = true;

            while (!isClosing) {
                arduinoSerialHandler.update();
                if (isOpen)
                    try {
                        int read;
                        while ((read = inBuffer.read()) > -1)
                            serialEvent.Recived(read);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                else
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            isOpen = false;
        }

        public void exit() {
            arduinoSerialHandler.dispose();
            isClosing = true;
        }
    }
}
