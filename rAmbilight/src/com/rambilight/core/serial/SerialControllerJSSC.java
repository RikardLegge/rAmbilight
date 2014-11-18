package com.rambilight.core.serial;

import jssc.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/* A modified version of a class i found online that handles the serial communication
 * 
 * I don't remember the source, but that person deserves credit for their effort!
 * 
 */

public class SerialControllerJSSC extends SerialController implements SerialPortEventListener {

    SerialPort serialPort;

    int initializeReturn = 0;

    public synchronized int initialize(String serialName) {

        initializeReturn = 2;
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            SerialPort tmpSerialPort = new SerialPort(serialName);
            try {
                System.out.print("Opening port... ");
                tmpSerialPort.openPort(); // Open port
                tmpSerialPort.setParams(dataRate, 8, 1, SerialPort.PARITY_NONE); // Set params
                int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR; // Prepare mask
                tmpSerialPort.setEventsMask(mask); // Set mask

                close();
                serialPort = tmpSerialPort;
                initializeReturn = 0;
                System.out.println("OPEN!");
            } catch (SerialPortException ex) {
                System.out.println("FAILED!");
                //ex.printStackTrace();
                initializeReturn = 1;
            }
            latch.countDown();
        });
        thread.run();

        try {
            long timeBefore = System.currentTimeMillis();
            latch.await(3, TimeUnit.SECONDS);
            if (System.currentTimeMillis() - timeBefore > 3000) {
                thread.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (initializeReturn == 0) {
            System.out.print("Waiting for port to get ready...");
            try {
                Thread.sleep(4000); // Milliseconds to block while waiting for port open
            } catch (Exception e) {
            }
            try {
                serialPort.addEventListener(this); // Add SerialPortEventListener
            } catch (Exception e) {
                System.out.print(" Unable to add event listener. Weird...");
            }
            System.out.println(" COMPLETE!");
        }

        return initializeReturn;
    }

    public String[] getAvailablePorts() {
        return SerialPortList.getPortNames(Pattern.compile("tty.(serial|usbserial|usbmodem|wchusbserial).*"));
    }

    public boolean isOpen() {
        return serialPort != null && serialPort.isOpened();
    }

    public synchronized boolean close() {
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            if (serialPort != null) {
                System.out.print("Closing port... ");
                try {
                    serialPort.removeEventListener();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
                try {
                    serialPort.closePort();
                    System.out.println("Closed!");
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
            latch.countDown();
        });
        thread.run();

        try {
            long timeBefore = System.currentTimeMillis();
            latch.await(2, TimeUnit.SECONDS);
            if (System.currentTimeMillis() - timeBefore > 1500) {
                System.out.println("Unable to close port.");
                thread.stop();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public void write(byte o) throws Exception {
        if (serialPort != null)
            serialPort.writeByte(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte " + o);
    }

    public void write(byte[] o) throws Exception {
        if (serialPort != null)
            serialPort.writeBytes(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte[] with length " + o.length);
    }

    public void write(int o) throws Exception {
        if (serialPort != null)
            serialPort.writeInt(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending int " + o);
    }


    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) {// If data is available
            if (event.getEventValue() > 0) {
                byte buffer[];
                try {
                    buffer = serialPort.readBytes();
                    for (byte read : buffer)
                        try {
                            serialEvent.Recived(read & 0xff);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (event.isCTS()) {// If CTS line has changed state
            if (event.getEventValue() == 1) {// If line is ON
                System.out.println("CTS - ON");
                serialConnectEvent.Recived(-1);
            }
            else {
                System.out.println("CTS - OFF");
                serialDisconnectEvent.Recived(-1);
            }
        }
        else if (event.isDSR()) {// /If DSR line has changed state
            if (event.getEventValue() == 1) {// If line is ON
                System.out.println("DSR - ON");
                serialConnectEvent.Recived(-1);
            }
            else {
                System.out.println("DSR - OFF");
                serialDisconnectEvent.Recived(-1);
            }
        }
    }
}
