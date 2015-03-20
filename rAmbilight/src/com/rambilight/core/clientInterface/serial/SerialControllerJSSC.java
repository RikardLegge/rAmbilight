package com.rambilight.core.clientInterface.serial;

import com.rambilight.core.clientInterface.SerialController;
import jssc.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/* A modified version of a class i found online that handles the serial communication
 * 
 * I don't remember the source, but that person deserves credit for their effort!
 * 
 */

/**
 * The communication with the arduino is done through this class.
 */
public class SerialControllerJSSC extends SerialController implements SerialPortEventListener {

	SerialPort serialPort;

	int initializeReturn = 0;

	public int initialize(String serialName) {

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

				if (isOpen())
					close();

				serialPort = tmpSerialPort;
				initializeReturn = 0;
				System.out.println("Open!");
			} catch (SerialPortException ex) {
				System.err.println("Failed to open!");
				//ex.printStackTrace();
				initializeReturn = 1;
			}
			latch.countDown();
		});
		thread.start();

		try {
			long timeBefore = System.currentTimeMillis();
			latch.await(3, TimeUnit.SECONDS);
			if (System.currentTimeMillis() - timeBefore > 10000) {
				thread.stop();
				System.err.println("Opening of port timed out...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (initializeReturn == 0) {
			System.out.print("Waiting for port to get ready... ");
			try {
				Thread.sleep(4000); // Milliseconds to block while waiting for port open
			} catch (Exception e) {
			}
			try {
				serialPort.addEventListener(this); // Add SerialPortEventListener
			} catch (Exception e) {
				System.err.print("Unable to add event listener. Weird...");
			}
			System.out.println("Ready!");
		}

		return initializeReturn;
	}

	public String[] getAvailablePorts() {
		return SerialPortList.getPortNames(Pattern.compile("tty.(serial|usbserial|usbmodem|wchusbserial).*"));
	}

	public void update() {

	}

	public boolean isOpen() {
		return serialPort != null && serialPort.isOpened();
	}

	public boolean close() {
		CountDownLatch latch = new CountDownLatch(1);
		Thread thread = new Thread(() -> {
			if (serialPort != null) {
				System.out.print("Closing port... ");
				try {
					serialPort.removeEventListener();
				} catch (SerialPortException e) {
					// Nothing has to be done if this fails
					//e.printStackTrace();
				}
				try {
					serialPort.closePort();
					serialPort = null;
					System.out.println("Closed!");
				} catch (SerialPortException e) {
					System.err.println("Failed to close!");
					e.printStackTrace();
				}
			}
			latch.countDown();
		});
		if (serialPort != null)
			thread.start();

		try {
			long timeBefore = System.currentTimeMillis();
			System.out.println("Awaiting closing of port...");
			latch.await(2, TimeUnit.SECONDS);
			if (System.currentTimeMillis() - timeBefore > 4000) {
				System.err.println("Closing of port timed out");
				thread.stop();
				return false;
			}
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean removeRootEventListener() {
		try {
			serialPort.removeEventListener();
			return true;
		} catch (SerialPortException e) {
			e.printStackTrace();
			return false;
		}
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
		} else if (event.isCTS()) {// If CTS line has changed state
			if (event.getEventValue() == 1) {// If line is ON
				System.out.println("CTS - ON");
				serialConnectEvent.Recived(-1);
			} else {
				System.out.println("CTS - OFF");
				serialDisconnectEvent.Recived(-1);
			}
		} else if (event.isDSR()) {// /If DSR line has changed state
			if (event.getEventValue() == 1) {// If line is ON
				System.out.println("DSR - ON");
				serialConnectEvent.Recived(-1);
			} else {
				System.out.println("DSR - OFF");
				serialDisconnectEvent.Recived(-1);
			}
		}
	}
}
