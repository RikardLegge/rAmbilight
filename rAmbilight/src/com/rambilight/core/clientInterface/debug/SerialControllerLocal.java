package com.rambilight.core.clientInterface.debug;

import com.rambilight.core.clientInterface.SerialController;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SerialControllerLocal extends SerialController {

    private final static int BUFFERSIZE = 64;   // Arduino serial buffer length
    private SynchronizedArray outBuffer;
    private SynchronizedArray inBuffer;
    private ArduinoEmulator   arduinoSerialHandler;

    private SerialRuntime serialRuntime;

    public synchronized int initialize(String serialName) {
        outBuffer = new SynchronizedArray(BUFFERSIZE);
        inBuffer = new SynchronizedArray(BUFFERSIZE);
        arduinoSerialHandler = new ArduinoEmulator(outBuffer, inBuffer);
        serialRuntime = new SerialRuntime();
        serialRuntime.start();
        return 0;
    }

    public String[] getAvailablePorts() {
        return new String[]{"local"};
    }

    public boolean isOpen() {
        return serialRuntime != null;
    }

    public synchronized boolean close() {
        serialRuntime.exit();
        serialRuntime = null;
        return true;
    }

    public boolean removeRootEventListener() {
        serialRuntime.exit();
        serialRuntime = null;
        return true;
    }

    public void write(byte o) throws Exception {
        if (serialRuntime != null)
            outBuffer.write(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte " + o);
    }

    public void write(byte[] o) throws Exception {
        if (serialRuntime != null)
            outBuffer.write(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte[] with length " + o.length);
    }

    public void write(int o) throws Exception {
        throw new NotImplementedException();
    }


    class SerialRuntime extends Thread {
        private boolean isActive = false;

        public void run() {
            setName("Serial Runtime");
            isActive = true;

            while (isActive) {
                arduinoSerialHandler.update();
                try {
                    int read;
                    while ((read = (int) inBuffer.read()) > -1)
                        serialEvent.Recived(read);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void exit() {
            arduinoSerialHandler.dispose();
            isActive = false;
        }
    }
}
