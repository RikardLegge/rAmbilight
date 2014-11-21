//#include <PololuLedStrip.h>
#include <FastLED.h>

#define DATA_PIN 6        // The pin on the Arduino to use for the LED PWM.
#define DATA_RATE 512000//512000//256000//115200  // The speed of the transmission

#define NULL 0
#define NUMBER_OF_LEDS 1
#define SMOOTH_STEP 2
#define COMPRESSION_LEVEL 3
#define CLEAR_BUFFER 4

#define END_SEND 254
#define BEGIN_SEND 255
#define BEGIN_SEND_PREFS 253

#define NUM_LEDS 250     // The maximum number of LEDs
/*
PololuLedStrip<DATA_PIN> ledStrip;

rgb_color leds[NUM_LEDS];
rgb_color leds_TargetValue[NUM_LEDS];
*/

CRGB leds[NUM_LEDS];
CRGB leds_TargetValue[NUM_LEDS];

/*
 1: ready.
 2: sleeping.
 
 254: End send / Ping
 255: Start send
 */

// Editable
float smoothStep = 0;       // Step size for the lights                     - Default: 0
int numActiveLeds = NUM_LEDS;      // Number of active LEDs that are handled       - Default: ~60
int compression = 1;        // The amount of compression which is set up    - Default: 1

// Static
int frameSleep = 7;
int buff[4];               // LED read buffer
int buffi;                 // Variable that is used for many things. Mostly as itt
unsigned long lastPing = 0;// When was i last pinged?
unsigned long lastRealData = 0;
int sleeping = 0;          // Am i sleeping?
unsigned long delta;       // TMP value for delta calculations

void setup() {
  //PololuLedStripBase::interruptFriendly = true;
  delay(500);                       // sanity check delay - allows reprogramming if accidentally blowing power w/leds
  LEDS.addLeds<WS2812B, DATA_PIN, RGB>(leds, NUM_LEDS);
  clearLEDS();
  delay(500);

  Serial.begin(DATA_RATE);   // Starts the serial communication port
}

void clearLEDS(){
    //ledStrip.write(leds, NUM_LEDS);
    LEDS.clear();
    FastLED.show();
}

void writeLEDS(){
    //ledStrip.write(leds, numActiveLeds);
    FastLED.show();
}

void loop() {
  serialHandle();
  stateHandle();
}

void stateHandle(){
  delta = difference(millis(), lastPing);

  if(delta < 3000){
    sleeping = 0;

    if(ColorSmoothing())    // If something has changed
      writeLEDS();

    Serial.write(1);       // I'm ready for more
    Serial.flush();

    delta = difference(millis(), lastRealData);
    if(delta > 5000)
      delay(frameSleep*10); // Wait for a longer while to spare system resources on the host device
    else
      delay(frameSleep);    // Wait for a very short while
  } 
  else if(sleeping == 0) {
    for(int i = 0; i < numActiveLeds; i++){
      leds_TargetValue[i].red = 0;
      leds_TargetValue[i].green = 0;
      leds_TargetValue[i].blue = 0;
    }
    sleeping = 1;
  } 
  else if(sleeping == 1 && ColorSmoothing()){
    clearLEDS();
    delay(frameSleep);     // Sleep for a while
  } 
  else {
    sleeping = 2;
    delay(1000);
  }
}

void serialHandle(){
  if(Serial.available() > 0){
    buffi = Serial.read();
    lastPing = millis();
    if(buffi == BEGIN_SEND){ // Is normal send START
      lastRealData = lastPing;
      buffi = 0;
      while(true){
        if(Serial.available() > 0){
          lastPing = millis();
          buff[buffi] = Serial.read();
          if(buff[buffi] == END_SEND){
            break;
          }
          if(buffi == 3){
            leds_TargetValue[buff[0]].red = buff[2];
            leds_TargetValue[buff[0]].green = buff[1];
            leds_TargetValue[buff[0]].blue = buff[3];
            buffi = 0;
          }
          else
            buffi++;
          continue;
        }
        else if(difference(millis(), lastPing) > 2) // Break if timed out.
          break;
        delay(1);
      }
    }
    else if(buffi == END_SEND){ // Is PING or END
      //Ping...
    }
    else if(buffi == BEGIN_SEND_PREFS){ // Is preferences. Read the coming 2 bytes. Key, Value
      if(Serial.available() >= 2){
        switch(Serial.read()){
        case NUMBER_OF_LEDS:
          numActiveLeds = Serial.read();
          if(numActiveLeds > NUM_LEDS)
            numActiveLeds = NUM_LEDS;
          else if(numActiveLeds < 0)
            numActiveLeds = 0;
          break;
        case SMOOTH_STEP:
          smoothStep = Serial.read();
          if(smoothStep > 255)
            smoothStep = 255;
          else if(smoothStep < 1)
            smoothStep = 1;
          break;
        case COMPRESSION_LEVEL:
          compression = Serial.read();
          if(compression > NUM_LEDS)
            compression = NUM_LEDS;
          else if(compression < 1)
            compression = 1;
          break;
        case CLEAR_BUFFER:
          Serial.read();
          for(int i = 0; i < NUM_LEDS; i++){
            leds_TargetValue[i].red = 0;
            leds_TargetValue[i].green = 0;
            leds_TargetValue[i].blue = 0;
          }
          for(int i = 0; i < NUM_LEDS; i++){
            leds[i].red = 0;
            leds[i].green = 0;
            leds[i].blue = 0;
          }
          break;
        default:
          Serial.read();
          break;
        }
      }
    }
    else { // Other. Print out what might have gone wrong
      while(Serial.available() > 0 && buffi > 0){
        switch(buffi){
        case BEGIN_SEND:
          buffi = -1;
          break;
        case END_SEND:
          Serial.read();
          buffi = -1;
          break;
        default:
          Serial.read();
          break;
        }
        buffi = Serial.peek();
      }
    }
  }
}

// Variables for Color Smoothing
boolean requiresUpdate;
int i, j;
int l, lt;
int stepSize, compressedItts;
float stepDecimal;

// Color smothering of the lights. Returns true if something has changed.
boolean ColorSmoothing(){
  requiresUpdate = false;
  stepDecimal = (54.0 + smoothStep) / 10.0;
  for(i = 0; i < numActiveLeds; i += compression){
    l = leds[i].red;
    lt = leds_TargetValue[i].red;
    if(l != lt){
      l = colorStep(l, lt);
      if(l != leds[i].red){
        requiresUpdate = true;
        for(j = i; j < i + compression; j++)
          leds[j].red = l;
      }
    }

    l = leds[i].green;
    lt = leds_TargetValue[i].green;
    if(l != lt){
      l = colorStep(l, lt);
      if(l != leds[i].green){
        requiresUpdate = true;
        for(j = i; j < i + compression; j++)
          leds[j].green = l;
      }
    }

    l = leds[i].blue;
    lt = leds_TargetValue[i].blue;
    if(l != lt){
      l = colorStep(l, lt);
      if(l != leds[i].blue){
        requiresUpdate = true;
        for(j = i; j < i + compression; j++)
          leds[j].blue = l;
      }
    }
  }
  return requiresUpdate;
}

int colorStep(int l, int lt){
  stepSize = 50.0-smoothStep + (difference(l,lt) - 255.0)/stepDecimal;
  if(difference(l,lt) <= stepSize)
    l = lt;
  else if(l < lt)
    l += stepSize;
  else if(l > lt)
    l -= stepSize;
  return l;
}

// Simple difference calculation
int difference(int v1, int v2){
  return abs(v1-v2);
}

