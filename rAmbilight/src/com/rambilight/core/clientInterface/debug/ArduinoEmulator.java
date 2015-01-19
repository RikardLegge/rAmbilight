/* JAVA */

package com.rambilight.core.clientInterface.debug;

public class ArduinoEmulator {
/* /Java */
    /* ARDUINO
    #include <PololuLedStrip.h>      // The LED library
    /* /Arduino */
    /* STATIC VARIABLES */

    final int DATA_PIN  = 6;        // The pin on the Arduino to use for the LED PWM.
    final int DATA_RATE = 512000;   //512000//256000//115200  // The speed of the transmission

    final int NUMBER_OF_LEDS    = 1;
    final int SMOOTH_STEP       = 2;
    final int COMPRESSION_LEVEL = 3;
    final int CLEAR_BUFFER      = 4;

    final int END_SEND         = 254;
    final int BEGIN_SEND       = 255;
    final int BEGIN_SEND_PREFS = 253;
    final int NUM_LEDS         = 250;     // The maximum number of LEDs
    final int FRAMESLEEP       = 7;

    /* JAVA */
    LEDStrip  ledStrip           = new LEDStrip();
    rgb_color leds[]             = new rgb_color[NUM_LEDS];
    rgb_color leds_TargetValue[] = new rgb_color[NUM_LEDS];
    int       RGBBuffer[]        = new int[4];
    /* /JAVA */

    /* ARDUINO
    PololuLedStrip<DATA_PIN> ledStrip;
    rgb_color leds[NUM_LEDS];
    rgb_color leds_TargetValue[NUM_LEDS];
    byte RGBBuffer[4];
    /* /Arduino */

    // Variable to be set by host
    float smoothStep    = 0;             // Step size for the lights                     - Default: 0
    int   numActiveLeds = NUM_LEDS;      // Number of active LEDs that are handled       - Default: ~60
    int   compression   = 1;             // The amount of compression which is set up    - Default: 1

    // Private variables
    double smoothStepConstant = 0;
    long   lastPing           = 0;          // When was i last pinged?
    long   lastRealData       = 0;          // When did i last get data?
    int    state              = 0;          // Am i sleeping?

    void setup() {
        /* ARDUINO
        PololuLedStripBase::interruptFriendly = true;
        /* /Arduino */

        delay(500);                       // sanity check delay - allows reprogramming if accidentally blowing power w/leds
        clearLEDS();
        delay(500);

        Serial.begin(DATA_RATE);   // Starts the serial communication port
    }

    void loop() {
        serialHandle();
        stateHandle();
    }

    void stateHandle() {
        long delta = difference(millis(), lastPing);

        if (delta < 3000) {
            state = 0;

            if (ColorSmoothing())    // If something has changed
                writeLEDS();

            Serial.write(1);       // I'm ready for more
            Serial.flush();
            if (difference(millis(), lastRealData) > 5000)
                delay(FRAMESLEEP * 10); // Wait for a longer while to spare system resources on the host device
            delay(FRAMESLEEP);    // Wait for a very short while
        }
        else if (state == 0) {
            clearLightColors();
            state = 1;
        }
        else if (state == 1 && ColorSmoothing()) {
            clearLEDS();
            delay(FRAMESLEEP);     // Sleep for a while
        }
        else {
            state = 2;
            delay(1000);
        }
    }

    void serialHandle() {
        int i;
        int buffered;
        if (Serial.available() > 0) {
            buffered = Serial.read();
            lastPing = millis();
            if (buffered == BEGIN_SEND) { // Is normal send START
                lastRealData = lastPing;
                i = 0;
                while (true) {
                    if (Serial.available() > 0) {
                        lastPing = millis();
                        RGBBuffer[i] = Serial.read();
                        if (RGBBuffer[i] == END_SEND) {
                            break;
                        }
                        if (i == 3) {
                            setLightColor(RGBBuffer[0], RGBBuffer[1], RGBBuffer[2], RGBBuffer[3]);
                            i = 0;
                        }
                        else
                            i++;
                        continue;
                    }
                    else if (difference(millis(), lastPing) > 2) // Break if timed out.
                        break;
                    delay(1);
                }
            }
            else if (buffered == END_SEND) { // Is PING or END
                //Ping...
            }
            else if (buffered == BEGIN_SEND_PREFS) { // Is preferences. Read the coming 2 bytes. Key, Value
                if (Serial.available() >= 2) {
                    switch (Serial.read()) {
                        case NUMBER_OF_LEDS:
                            numActiveLeds = Serial.read();
                            if (numActiveLeds > NUM_LEDS)
                                numActiveLeds = NUM_LEDS;
                            else if (numActiveLeds < 0)
                                numActiveLeds = 0;
                            break;
                        case SMOOTH_STEP:
                            smoothStep = Serial.read();
                            if (smoothStep > 255)
                                smoothStep = 255;
                            else if (smoothStep < 1)
                                smoothStep = 1;
                            smoothStepConstant = (54.0 + smoothStep) / 10.0;
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
                            Serial.read();
                            break;
                    }
                }
            }
            else { // Other. Print out what might have gone wrong
                while (Serial.available() > 0 && buffered > 0) {
                    switch (buffered) {
                        case BEGIN_SEND:
                            break;
                        case END_SEND:
                            Serial.read();
                            break;
                        default:
                            Serial.read();
                            break;
                    }
                    buffered = Serial.peek();
                }
            }
        }
    }

    // Color smothering of the lights. Returns true if something has changed.
    boolean ColorSmoothing() {
        boolean requiresUpdate = false;
        int i, j;
        int l, lt;
        for (i = 0; i < numActiveLeds; i += compression) {
            l = leds[i].red;
            lt = leds_TargetValue[i].red;
            if (l != lt) {
                l = colorStep(l, lt);
                if (l != leds[i].red) {
                    requiresUpdate = true;
                    for (j = i; j < i + compression; j++)
                        leds[j].red = l;
                }
            }

            l = leds[i].green;
            lt = leds_TargetValue[i].green;
            if (l != lt) {
                l = colorStep(l, lt);
                if (l != leds[i].green) {
                    requiresUpdate = true;
                    for (j = i; j < i + compression; j++)
                        leds[j].green = l;
                }
            }

            l = leds[i].blue;
            lt = leds_TargetValue[i].blue;
            if (l != lt) {
                l = colorStep(l, lt);
                if (l != leds[i].blue) {
                    requiresUpdate = true;
                    for (j = i; j < i + compression; j++)
                        leds[j].blue = l;
                }
            }
        }
        return requiresUpdate;
    }

    int colorStep(int l, int lt) {
        //int stepSize = /* JAVA */ (int) /* /JAVA */ (30.0 - smoothStep + (difference(l, lt) - 255.0) / smoothStepConstant);
        int stepSize = 10;
        if (difference(l, lt) <= stepSize)
            l = lt;
        else if (l < lt)
            l += stepSize;
        else if (l > lt)
            l -= stepSize;
        return l;
    }

    long difference(long num1, long num2) {
        long result = num1 - num2;
        return result >= 0 ? result : result * -1;
    }

    void clearLEDS() {
        ledStrip.write(leds, NUM_LEDS);
    }

    void writeLEDS() {
        ledStrip.write(leds, numActiveLeds);
    }

    /* ARDUINO
    void setLightColor(int l, byte r, byte g, byte b) {
    /* /ARDUINO */
    /* JAVA */
    void setLightColor(int l, int r, int g, int b) {
    /* /JAVA */
        leds_TargetValue[l].red = r;
        leds_TargetValue[l].green = g;
        leds_TargetValue[l].blue = b;
    }

    void clearLightColors() {
        for (int i = 0; i < NUM_LEDS; i++) {
            leds_TargetValue[i].red = 0;
            leds_TargetValue[i].green = 0;
            leds_TargetValue[i].blue = 0;
        }
        for (int i = 0; i < NUM_LEDS; i++) {
            leds[i].red = 0;
            leds[i].green = 0;
            leds[i].blue = 0;
        }
    }

    /* JAVA COMPATIBLE VARIABLES */

    ArduinoSerial     Serial;
    SynchronizedArray inBuffer, outBuffer;
    Visualizer visualizer;

    public ArduinoEmulator(SynchronizedArray inBuffer, SynchronizedArray outBuffer) {
        this.inBuffer = inBuffer;
        this.outBuffer = outBuffer;
        Serial = new ArduinoSerial();
        visualizer = new Visualizer(leds);

        for (int i = 0; i < leds_TargetValue.length; i++) {
            leds_TargetValue[i] = new rgb_color();
            leds[i] = new rgb_color();
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

        }

        void flush() {

        }

        void write(int b) {
            outBuffer.write(b);
        }

        int read() {
            return inBuffer.read() & 0xFF;
        }

        int peek() {
            return inBuffer.peek() & 0xFF;
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

    class LEDStrip {
        public void write(rgb_color leds[], int count) {
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
