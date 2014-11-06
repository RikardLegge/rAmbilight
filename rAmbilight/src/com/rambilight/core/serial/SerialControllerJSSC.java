package com.rambilight.core.serial;

import jssc.*;

/* A modified version of a class i found online that handles the serial communication
 * 
 * I don't remember the source, but that person deserves credit for their effort!
 * 
 */

public class SerialControllerJSSC implements SerialPortEventListener {

    // Declares a couple of variables for the serial communication
    public SerialPort serialPort;
    private SerialEventListener serialEvent           = (data) -> {
    };
    private SerialEventListener serialConnectEvent    = (data) -> {
    };
    private SerialEventListener serialDisconnectEvent = (data) -> {
    };
    private int                 dataRate              = 512000;//256000;//115200;      // Default bits per second for COM port.

    public SerialControllerJSSC() {
    }

    /**
     * Sets the dataRate of the com controller
     *
     * @param dataRate The rate in bytes that the comunication is handled. default = 115200
     */
    public SerialControllerJSSC(int dataRate) {
        this.dataRate = dataRate;
    }

    /**
     * @return The current serial data-rate
     */
    public int getDataRate() {
        return dataRate;
    }

    /**
     * Tries to initialize the COM. In case of fail, return false
     *
     * @param serialName Name of the serialPort
     * @return Indicator of sucess
     */
    public boolean initialize(String serialName) {
        serialPort = new SerialPort(serialName);
        try {
            serialPort.openPort(); // Open port
            serialPort.setParams(dataRate, 8, 1, SerialPort.PARITY_MARK); // Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR; // Prepare mask
            serialPort.setEventsMask(mask); // Set mask
            serialPort.addEventListener(this); // Add SerialPortEventListener
            Thread.sleep(2000); // Milliseconds to block while waiting for port open
            return true;
        } catch (SerialPortException | InterruptedException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String[] getAvailablePorts() {
        return SerialPortList.getPortNames();
    }

    /**
     * Is the current serial port open?
     */
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpened();
    }

    /**
     * When a byte is recieved, handle it and call the callback
     */
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

    /**
     * Set the callback event listener
     */
    public void setEventListener(SerialEventListener serialEvent) {
        this.serialEvent = serialEvent;
    }

    public void setConnectedListener(SerialEventListener serialEvent) {
        this.serialConnectEvent = serialEvent;
    }

    public void setDisconnectedListener(SerialEventListener serialEvent) {
        this.serialDisconnectEvent = serialEvent;
    }

    /**
     * Safely close the COM port
     */
    public synchronized void close() {
        if (serialPort != null) {
            System.out.print("Closing port...");
            try {
                serialPort.removeEventListener();
                serialPort.closePort();
                System.out.println(" Closed!");
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write BYTE through the serialPort
     *
     * @param o Byte to write
     */
    public void write(byte o) throws Exception {
        if (serialPort != null)
            serialPort.writeByte(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte " + o);
    }

    /**
     * Write BYTEs through the serialPort
     *
     * @param o Byte[] to write
     */
    public void write(byte[] o) throws Exception {
        if (serialPort != null)
            serialPort.writeBytes(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending byte[] with length " + o.length);
    }

    /**
     * Write INT through the serialPort (Will probably be split into 4 bytes)
     *
     * @param o Int to write
     */
    public void write(int o) throws Exception {
        if (serialPort != null)
            serialPort.writeInt(o);
        else
            System.err.println("WARNING: Output device not initiated. Not sending int " + o);
    }

    /**
     * Callback handle for when a byte is recieved from the external mictrocontroller
     */
    public interface SerialEventListener {

        public void Recived(int data);
    }

}
