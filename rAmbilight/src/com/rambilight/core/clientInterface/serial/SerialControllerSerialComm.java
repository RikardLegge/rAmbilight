/*
package com.rambilight.core.clientInterface.serial;

import com.rambilight.core.clientInterface.SerialController;
import j.extensions.comm.SerialComm;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class SerialControllerSerialComm extends SerialController {

    byte[]                      serialCommInputByteBuffer = new byte[2000];
    HashMap<String, SerialComm> portCache                 = new HashMap<>();

    InputStream  serialCommInputStream;
    OutputStream serialCommOutputStream;
    SerialComm   serialCommPort;

    public int initialize(String serialName) {
        if (true)
            throw new NotImplementedException();
        if (!portCache.containsKey(serialName))
            return 2;

        SerialComm serialCommPort = portCache.get(serialName);

        System.out.println("Opening " + serialCommPort.getDescriptivePortName() + ": " + serialCommPort.openPort());
        serialCommPort.setComPortTimeouts(SerialComm.TIMEOUT_READ_BLOCKING, 1000, 0);
        serialCommPort.setBaudRate(512000);

        try {
            Thread.sleep(4000); // Milliseconds to block while waiting for port open
        } catch (Exception e) {
        }

        serialCommInputStream = serialCommPort.getInputStream();
        serialCommOutputStream = serialCommPort.getOutputStream();
        return 0;
    }

    public String[] getAvailablePorts() {
        portCache.clear();
        SerialComm[] ports = SerialComm.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            SerialComm port = ports[i];
            portCache.put(port.getSystemPortName(), port);
            portNames[i] = port.getSystemPortName();
        }
        return portNames;
    }

    public boolean isOpen() {
        return serialCommPort != null;
    }

    public boolean close() {
        try {
            serialCommInputStream.close();
            serialCommOutputStream.close();
            serialCommOutputStream = null;
            serialCommInputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serialCommPort.closePort();
    }

    public boolean removeRootEventListener() {
        return true;
    }

    public void write(byte o) throws Exception {
        if (serialCommOutputStream != null)
            serialCommOutputStream.write(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte " + o);
    }

    public void write(byte[] o) throws Exception {
        if (serialCommOutputStream != null)
            serialCommOutputStream.write(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte[] with length " + o.length);
    }

    public void write(int o) throws Exception {
        throw new NotImplementedException();
    }

    public void update() {
        if (isOpen() && serialCommPort.bytesAvailable() > 0) {
            try {
                int numReadBytes = serialCommInputStream.read(serialCommInputByteBuffer);
                for (int i = 0; i < numReadBytes; i++)
                    serialEvent.Recived(serialCommInputByteBuffer[i] & 0xff);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
 */