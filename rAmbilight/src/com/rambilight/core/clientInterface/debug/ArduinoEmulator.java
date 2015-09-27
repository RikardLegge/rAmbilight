/* JAVA */
package com.rambilight.core.clientInterface.debug;
/* /Java */

/**
 * A class which is compiled to a subset of C which is able to run on the arduino.
 * This allows the debug environment to behave close to the same as when run on the device.
 * <p>
 * Problem with serial library.
 * https://plus.google.com/102282558639672545743/posts/j1Xrt9iwgrZ?cfem=1
 * <p>
 * TODO:Add changes from arduino.ino.
 */

//  /* JAVA */ THIS WILL ONLY RUN IN THE TEST ENVIRONMENT /* /JAVA */
//  /* ARDUINO /THIS WILL ONLY RUN ON THE ARDUINO/*  /ARDUINO */
//  int/**/ WILL BECOME A BYTE WHEN COMPILED, SINCE BYTES ARE ANNOYING TO WORK WITH IN JAVA.
//  final int BECOMES #define
//  long BECOMES unsigned long

/* JAVA */
public class ArduinoEmulator {
/* /Java */
    /* ARDUINO /
    //#include <PololuLedStrip.h>
    #include <FastLED.h>
    /* /Arduino */      // The LED library

	// Hardware preferences.
	final int DATA_PIN   = 6;       // The pin on the Arduino to use for the LED PWM.
	final int DATA_RATE  = 512000;  //512000//256000//115200  // The speed of the transmission in bits / second.
	final int FRAMESLEEP = 1;       // Time to sleep between each frame.
	final int NUM_LEDS   = 180;     // The maximum number of LEDs

	// Preference types
	final int NUMBER_OF_LEDS    = 1;
	final int SMOOTH_STEP       = 2;
	final int COMPRESSION_LEVEL = 3;
	final int CLEAR_BUFFER      = 4;

	//  Control signals
	final int DISCONNECT       = 250;
	final int PING             = 251;
	final int BEGIN_SEND_PREFS = 252;
	final int END_SEND         = 253;
	final int BEGIN_SEND       = 254;

	// States
	final int CONNECTING   = 1;
	final int ACTIVE       = 2;
	final int PASSIVE      = 3;
	final int LOST         = 4;
	final int HALTING      = 5;
	final int DISCONNECTED = 6;

	/* JAVA */
	//LibLEDStrip ledStrip           = new LibLEDStrip();
	LibFastLED FastLED        = new LibFastLED();
	rgb_color  leds_current[] = new rgb_color[NUM_LEDS];
	//rgb_color  leds_angle[]   = new rgb_color[NUM_LEDS];
	rgb_color  leds_target[]  = new rgb_color[NUM_LEDS];
	int        RGBBuffer[]    = new int[4];
	/* /JAVA */
	/* ARDUINO /
	//PololuLedStrip<DATA_PIN> ledStrip;
    //rgb_color leds_current[NUM_LEDS];
    //rgb_color leds_target[NUM_LEDS];

    CRGB leds_current[NUM_LEDS];            // The current LED value
    //CRGB leds_angle[NUM_LEDS];// The current LED value
	CRGB leds_target[NUM_LEDS];// The buffered LED value

    byte RGBBuffer[4];
    /* /Arduino */

	// Variable to be set by host
	float stepLength    = 2;             // Step size for the lights                     - Default: 2
	int   numActiveLeds = NUM_LEDS;      // Number of active LEDs that are handled       - Default: ~60
	int   compression   = 1;             // The amount of compression which is set up    - Default: 1

	// Private variables

	long lastPing     = 0;          // When was i last pinged?
	long lastRealData = 0;          // When did i last get data?

	int  state                      = DISCONNECTED; // Current connection state
	int  stateHandlerDelay          = 1;            // Delay between state related actions
	long lastStateHandlerInvocation = 0;            // Last time the stateHandle was invoked

	int oldTransmitToken = 0;   // The token related to the last transmission
	int transmitToken    = 0;   // The token related to the current transmission

	void setup() {
	    /* ARDUINO /
	    //PololuLedStripBase::interruptFriendly = true;
	    LEDS.addLeds<WS2812B, DATA_PIN, RGB>(leds_current, NUM_LEDS);  // Sets WS2812B as the selected LED microcontroller
	    /* /Arduino */

		delay(500);                       // sanity check delay - allows reprogramming if accidentally blowing power w/leds
		clearLightColors();
		writeAllLeds();
		delay(500);

		Serial.begin(DATA_RATE);   // Starts the serial communication port
	}

	void loop() {
		serialHandle();
		//if (difference(lastStateHandlerInvocation, millis()) > stateHandlerDelay)
		delay(20);
		stateHandle();
	}

	void stateHandle() {
		lastStateHandlerInvocation = millis();

		if (difference(millis(), lastRealData) < 2000)  // Has any real data come by?
			setState(ACTIVE);                           // High frame rate
		if (difference(millis(), lastPing) < 3000)      // Has something come by?
			setState(PASSIVE);                          // Low frame rate
		else
			setState(LOST);                             // Disconnect and shut down

		switch (state) {
			case CONNECTING:
				writeSingle(BEGIN_SEND_PREFS);
				break;
			case PASSIVE:
			case ACTIVE:
				stateHandlerDelay = FRAMESLEEP;

				if (ColorSmoothing())    // If something has changed
					writeLEDS();
				if (oldTransmitToken != transmitToken) {
					writeSingle(PING);       // I'm ready for more
					oldTransmitToken = transmitToken;
				}
				break;
			case LOST:
				clearLightColors();
				setState(HALTING);
			case HALTING:
				if (ColorSmoothing())
					writeLEDS();
				else
					setState(DISCONNECTED);

				stateHandlerDelay = FRAMESLEEP;
				break;
			case DISCONNECTED:
				stateHandlerDelay = 1000;
				break;
		}
	}

	void serialHandle() {
		if (Serial.available() > 0) {
			incrementTransmitToken();
			lastPing = millis();
			int buffered = Serial.read();
			switch (buffered) {
				case PING:
					break;
				case BEGIN_SEND:
					serialHandleData();
					break;
				case BEGIN_SEND_PREFS:
					serialHandlePreferences();
					break;
				case DISCONNECT:
					setState(LOST);
					break;
				default:
					serialHandleOther(buffered);
			}
		}
	}

	void setState(int newState) {
		state = newState;
	}

	void writeSingle(int data) {
		Serial.write(data);
		Serial.flush();
	}

	boolean validateData(int data) {
		return (data < 250);
	}


	void serialHandleData() {
		lastRealData = lastPing;
		int i = 0;
		while (true) {
			if (Serial.available() > 0) {
				lastPing = millis();
				RGBBuffer[i] = Serial.read();
				if (RGBBuffer[i] == END_SEND || !validateData(RGBBuffer[i])) {
					break;
				}
				if (i == 3) {
					setLightColor(RGBBuffer[0], RGBBuffer[1], RGBBuffer[2], RGBBuffer[3]);
					i = 0;
				} else
					i++;
				continue;
			} else if (difference(millis(), lastPing) > 2) // Break if timed out.
				break;
			delay(1);
		}
	}

	void serialHandlePreferences() {
		while (Serial.available() < 2) {
			if (difference(millis(), lastPing) > 10) // Break if timed out.
				return;
			delay(1);
		}
		lastRealData = lastPing;
		switch (Serial.read()) {
			case NUMBER_OF_LEDS:
				numActiveLeds = Serial.read();
				if (numActiveLeds > NUM_LEDS)
					numActiveLeds = NUM_LEDS;
				else if (numActiveLeds < 0)
					numActiveLeds = 0;
				break;
			case SMOOTH_STEP:
				stepLength = Serial.read();
				if (stepLength > 255)
					stepLength = 255;
				else if (stepLength < 1)
					stepLength = 1;
				break;
			case COMPRESSION_LEVEL:
				compression = Serial.read();
				if (compression > NUM_LEDS)
					compression = NUM_LEDS;
				else if (compression < 1)
					compression = 1;
				break;
			case CLEAR_BUFFER:
				Serial.read();
				clearLightColors();
				break;
			default:
				if (Serial.peek() != 254)
					Serial.read();
				break;
		}
	}

	void serialHandleOther(int buffered) {
		while (Serial.available() > 0 || buffered >= 0) {
			switch (buffered) {
				case BEGIN_SEND:
					return;
				case END_SEND:
				default:
					if (Serial.peek() == buffered)
						Serial.read();
					break;
			}
			buffered = Serial.peek();
		}
	}


	void incrementTransmitToken() {
		transmitToken = (++transmitToken) % 249;
	}

	// Color smothering of the lights. Returns true if something has changed.
	boolean ColorSmoothing() {
		boolean requiresUpdate = false;
		int i, j;
		int l, lt;
		for (i = 0; i < numActiveLeds; i += compression) {
			l = leds_current[i].red;
			lt = leds_target[i].red;
			if (l != lt) {
				l = colorStep(l, lt);
				requiresUpdate = true;
				for (j = i; j < i + compression; j++)
					leds_current[j].red = l;
			}
			//leds_angle[i].red = l / lt;

			l = leds_current[i].green;
			lt = leds_target[i].green;
			if (l != lt) {
				l = colorStep(l, lt);
				requiresUpdate = true;
				for (j = i; j < i + compression; j++)
					leds_current[j].green = l;
			}

			//leds_angle[i].green = l / lt;


			l = leds_current[i].blue;
			lt = leds_target[i].blue;
			if (l != lt) {
				l = colorStep(l, lt);
				requiresUpdate = true;
				for (j = i; j < i + compression; j++)
					leds_current[j].blue = l;
			}

			//leds_angle[i].blue = l / lt;

		}
		return requiresUpdate;
	}

	int colorStep(int l, int lt) {
		int stepSize = floor(1 + difference(l, lt) / stepLength);
		if (difference(l, lt) <= stepSize)
			l = lt;
		else if (l < lt)
			l += stepSize;
		else if (l > lt)
			l -= stepSize;
		return l;
	}

	int difference(int num1, int num2) {
		return abs(num1 - num2);
	}

	void writeAllLeds() {
		//ledStrip.write(leds, NUM_LEDS);
		FastLED.show();
	}

	void writeLEDS() {
		//ledStrip.write(leds, numActiveLeds);
		FastLED.show();
	}

	void setLightColor(int l, int/**/ r, int/**/ g, int/**/ b) {
		leds_target[l].red = r;
		leds_target[l].green = g;
		leds_target[l].blue = b;
	}

	void clearLightColors() {
		for (int i = 0; i < NUM_LEDS; i++) {
			leds_target[i].red = 0;
			leds_target[i].green = 0;
			leds_target[i].blue = 0;
		}
	}

    /* JAVA COMPATIBLE VARIABLES */

	long difference(long num1, long num2) {
		return Math.abs(num1 - num2);
	}

	int abs(int num) {
		return Math.abs(num);
	}

	int floor(float num) {
		return (int) Math.floor(num);
	}

	ArduinoSerial     Serial;
	SynchronizedArray inBuffer, outBuffer;
	Visualizer visualizer;

	public ArduinoEmulator(SynchronizedArray inBuffer, SynchronizedArray outBuffer) {
		this.inBuffer = inBuffer;
		this.outBuffer = outBuffer;
		Serial = new ArduinoSerial();
		visualizer = new Visualizer(leds_current);

		for (int i = 0; i < leds_target.length; i++) {
			leds_target[i] = new rgb_color();
			leds_current[i] = new rgb_color();
		}
	}

	void update() {
		loop();
		visualizer.update();
	}

	void dispose() {
		visualizer.removeAll();
		visualizer.dispose();
	}

	class ArduinoSerial {
		void begin(int baudRate) {
			System.out.println("Serial emulation with a baud rate of " + baudRate);
		}

		void flush() {

		}

		void write(int b) {
			outBuffer.write(b);
		}

		int read() {
			return inBuffer.read();
		}

		int peek() {
			return inBuffer.peek();
		}

		int available() {
			return inBuffer.length();
		}
	}

	class rgb_color {
		public int red   = 0;
		public int blue  = 0;
		public int green = 0;
	}

	class LibLEDStrip {
		public void write(rgb_color[] leds, int count) {
			visualizer.update();
		}
	}

	class LibFastLED {
		public void show() {
			visualizer.update();
		}
	}

	long millis() {
		return System.currentTimeMillis();
	}

	void delay(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/* /JAVA */
