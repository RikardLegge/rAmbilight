package com.rambilight.core.clientInterface;

/**
 * The SerialController interface which gives a common interface for both the host->client as well as
 * the debug communication.
 */
public abstract class SerialController {

    // Declares a couple of variables for the serial communication
    protected SerialEventListener serialEvent           = (data) -> {
    };
    protected SerialEventListener serialConnectEvent    = (data) -> {
    };
    protected SerialEventListener serialDisconnectEvent = (data) -> {
    };
    protected int                 dataRate              = 115200;//512000;//256000;//115200;      // Default bits per second for COM port.

    public SerialController() {
    }

    /**
     * Sets the dataRate of the com controller
     *
     * @param dataRate The rate in bytes that the comunication is handled. default = 115200
     */
    public SerialController(int dataRate) {
        this.dataRate = dataRate;
    }

    /**
     * Tries to initialize the COM. In case of fail, return false
     *
     * @param serialName Name of the serialPort
     * @return Indicator of sucess
     */
    public abstract int initialize(String serialName);

    /**
     * @return The current serial data-rate
     */
    public int getDataRate() {
        return dataRate;
    }

    /**
     * @return A string array containing the names of the available ports
     */
    public abstract String[] getAvailablePorts();

    public abstract void update();

    /**
     * Is the current serial port open?
     */
    public abstract boolean isOpen();

    /**
     * Safely close the COM port
     */
    public abstract boolean close();

    /**
     * Dispose the COM port listener
     */
    public abstract boolean removeRootEventListener();


    /**
     * Write BYTE through the serialPort
     *
     * @param o Byte to write
     */
    public abstract void write(byte o) throws Exception;

    /**
     * Write BYTEs through the serialPort
     *
     * @param o Byte[] to write
     */
    public abstract void write(byte[] o) throws Exception;

    /**
     * Write INT through the serialPort (Will probably be split into 4 bytes)
     *
     * @param o Int to write
     */
    public abstract void write(int o) throws Exception;


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
     * Callback handle for when a byte is recieved from the external mictrocontroller
     */
    public interface SerialEventListener {
        public void Recived(int data);
    }

}
