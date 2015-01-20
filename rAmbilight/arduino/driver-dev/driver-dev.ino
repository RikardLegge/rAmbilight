
#include <PololuLedStrip.h>      // The LED library

// Hardware preferences.
#define DATA_PIN 6       // The pin on the Arduino to use for the LED PWM.
#define DATA_RATE 512000  //512000//256000//115200  // The speed of the transmission in bits / second.
#define FRAMESLEEP 7       // Time to sleep between each frame.
#define NUM_LEDS 200     // The maximum number of LEDs

// Preference types
#define NUMBER_OF_LEDS 1
#define SMOOTH_STEP 2
#define COMPRESSION_LEVEL 3
#define CLEAR_BUFFER 4

//  Control signals
#define BEGIN_SEND_PREFS 253
#define END_SEND 254
#define BEGIN_SEND 255

// States
#define CONNECTING 1
#define ACTIVE 2
#define PASSIVE 3
#define LOST 4
#define HALTING 5
#define DISCONNECTED 6



PololuLedStrip<DATA_PIN> ledStrip;
rgb_color leds[NUM_LEDS];
rgb_color leds_TargetValue[NUM_LEDS];
byte RGBBuffer[4];


// Variable to be set by host
float smoothStep    = 0;             // Step size for the lights                     - Default: 2
int   numActiveLeds = NUM_LEDS;      // Number of active LEDs that are handled       - Default: ~60
int   compression   = 1;             // The amount of compression which is set up    - Default: 1

// Private variables
float smoothStepConstant = 0;
unsigned long   lastPing           = 0;          // When was i last pinged?
unsigned long   lastRealData       = 0;          // When did i last get data?
int    state              = DISCONNECTED;         // Am i sleeping?

void setup() {
    PololuLedStripBase::interruptFriendly = true;

    delay(500);                       // sanity check delay - allows reprogramming if accidentally blowing power w/leds
    clearLightColors();
    writeAllLeds();
    delay(500);

    Serial.begin(DATA_RATE);   // Starts the serial communication port
}

void loop() {
    serialHandle();
    stateHandle();
}

void stateHandle() {

    if (difference(millis(), lastRealData) > 5000) {
        state = PASSIVE;
    }
    else if (difference(millis(), lastPing) < 3000) {
        state = ACTIVE;
    }
    else {
        state = LOST;
    }

    switch (state) {
        case CONNECTING:
            writeSingle(BEGIN_SEND_PREFS);
            break;
        case PASSIVE:
            delay(FRAMESLEEP * 10);          // Wait for a very short while
        case ACTIVE:
            if (ColorSmoothing())    // If something has changed
                writeLEDS();

            writeSingle(BEGIN_SEND);       // I'm ready for more

            delay(FRAMESLEEP);          // Wait for a very short while
            break;
        case LOST:
            clearLightColors();
            state = HALTING;
            break;
        case HALTING:
            if (ColorSmoothing())
                writeLEDS();
            else
                state = DISCONNECTED;

            delay(FRAMESLEEP);
            break;
        case DISCONNECTED:
            delay(1000);
            break;
    }
}

void serialHandle() {
    if (Serial.available() > 0) {
        int buffered = Serial.read();
        lastPing = millis();
        switch (buffered) {
            case BEGIN_SEND:
                serialHandleData();
                break;
            case BEGIN_SEND_PREFS:
                serialHandlePreferences();
                break;
            default:
                serialHandleOther(buffered);
        }
    }
}

void writeSingle(int data) {
    Serial.write(data);
    Serial.flush();
}

boolean validateData(int data) {
    return (data < 252);
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

void serialHandlePreferences() {
    while (Serial.available() < 2) {
        if (difference(millis(), lastPing) > 10) // Break if timed out.
            return;
        delay(1);
    }
    state = ACTIVE;
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
            if (Serial.peek() != 255)
                Serial.read();
            break;
    }
}

void serialHandleOther(int buffered) {
    while (Serial.available() > 0 && buffered >= 0) {
        switch (buffered) {
            case BEGIN_SEND:
                return;
            case END_SEND:
            default:
                Serial.read();
                break;
        }
        buffered = Serial.peek();
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
            requiresUpdate = true;
            for (j = i; j < i + compression; j++)
                leds[j].red = l;
        }

        l = leds[i].green;
        lt = leds_TargetValue[i].green;
        if (l != lt) {
            l = colorStep(l, lt);
            requiresUpdate = true;
            for (j = i; j < i + compression; j++)
                leds[j].green = l;
        }

        l = leds[i].blue;
        lt = leds_TargetValue[i].blue;
        if (l != lt) {
            l = colorStep(l, lt);
            requiresUpdate = true;
            for (j = i; j < i + compression; j++)
                leds[j].blue = l;
        }
    }
    return requiresUpdate;
}

int colorStep(int l, int lt) {
    int stepSize =  (3.0 + difference(l, lt) / 8.0);
    //int stepSize = 10;
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
    ledStrip.write(leds, NUM_LEDS);
}

void writeLEDS() {
    ledStrip.write(leds, numActiveLeds);
}

void setLightColor(int l, byte r, byte g, byte b) {
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
}


