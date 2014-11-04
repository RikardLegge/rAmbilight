#include <FastLED.h>      // The LED library

#define DATA_PIN 6        // The pin on the Arduino to use for the LED PWM.
#define DATA_RATE 256000//115200  // The speed of the transmission

#define NULL 0
#define NUMBER_OF_LEDS 1
#define SMOOTH_STEP 2
#define FRAME_DELAY 3
#define CLEAR_BUFFER 4

#define END_SEND 254
#define BEGIN_SEND 255
#define BEGIN_SEND_PREFS 253

#define NUM_LEDS 120     // The maximum number of LEDs

CRGB leds[NUM_LEDS];      // The current LED value
CRGB leds_TargetValue[NUM_LEDS];  // The buffered LED value

void setup() {

  delay(2000);  // sanity check delay - allows reprogramming if accidentally blowing power w/leds

  Serial.begin(DATA_RATE);   // Starts the serial communication port

  LEDS.addLeds<WS2812B, DATA_PIN, RGB>(leds, NUM_LEDS);  // Sets WS2812B as the selected LED microcontroller
  LEDS.clear();              // Clear all the LED values
  FastLED.show();
}
/*
 1: ready.
 2: sleeping.
 
 254: End send / Ping
 255: Start send
 */

// Editable
float smoothStep = 0;       // Step size for the lights
int numActiveLeds = 60;    // Number of active LEDs that are handled

// Static
int frameSleep = 6;
int buff[4];               // LED read buffer
int buffi;                 // Variable that is used for many things. Mostly as itt
unsigned long lastPing = 0;// When was i last pinged?
int sleeping = 0;          // Am i sleeping?
unsigned long delta;       // TMP value for delta calculations

void loop() {
  if(Serial.available() > 0){
    buffi = Serial.read();
    lastPing = millis();
    if(buffi == BEGIN_SEND){ // Is normal send START
      buffi = 0;
      while(true){
        if(Serial.available() > 0){
          lastPing = millis();
          buff[buffi] = Serial.read();
          if(buff[buffi] == END_SEND){
            break;
          }
          if(buffi == 3){
            leds_TargetValue[buff[0]].r = buff[2];
            leds_TargetValue[buff[0]].g = buff[1];
            leds_TargetValue[buff[0]].b = buff[3];
            //leds_TargetValue[buff[0]] = CRGB(buff[2],buff[1],buff[3]); // Swap r & g to compensate for the unusial format of my lights 
            buffi = 0;
          } 
          else
            buffi++;
          continue;
        }else if(difference(millis(), lastPing) > 5) // Break if timed out.
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
        case FRAME_DELAY:
          Serial.read();
        case CLEAR_BUFFER:
          Serial.read();
          for(int i = 0; i < NUM_LEDS; i++){
            leds_TargetValue[i].r = 0;
            leds_TargetValue[i].g = 0;
            leds_TargetValue[i].b = 0;
          }
          for(int i = 0; i < NUM_LEDS; i++){
            leds[i].r = 0;
            leds[i].g = 0;
            leds[i].b = 0;
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

  delta = difference(millis(), lastPing);

  if(delta < 3000){
    sleeping = 0;
    Serial.write(1);       // I'm ready for more
    Serial.flush();

    delay(frameSleep);    // Wait for a very short while

    if(ColorSmoothing())    // If something has changed
        FastLED.show();      // Update the LEDs
  } else if(sleeping == 0) {
    for(int i = 0; i < numActiveLeds; i++){
      leds_TargetValue[i].r = 0;
      leds_TargetValue[i].g = 0;
      leds_TargetValue[i].b = 0;
    }
    sleeping = 1;
  } else if(sleeping == 1 && ColorSmoothing()){
    delay(frameSleep);     // Sleep for a while
    FastLED.show();
  } else {
     sleeping = 2;
     delay(1000);
  }
}


// Variables for Color Smoothing
boolean requiresUpdate;
int i, j;
int l, lt;
int stepSize;
float stepDecimal;

// Color smothering of the lights. Returns true if something has changed.
boolean ColorSmoothing(){
  requiresUpdate = false;
  stepDecimal = (51 - smoothStep) / 10;
  for(i = 0; i < numActiveLeds; i++){
    for(j = 0; j < 3; j++){
      l = leds[i][j];
      lt = leds_TargetValue[i][j];
      if(l != lt){
        stepSize = 51-smoothStep + (difference(l,lt) - 255)/stepDecimal;
        if(difference(l,lt) <= stepSize)
          l = lt;
        else if(l < lt)
          l += stepSize;
        else if(l > lt)
          l -= stepSize;

        if(l != leds[i][j]){
          requiresUpdate = true;
          leds[i][j] = l;
        }
      }
    }
  }
  return requiresUpdate;
}

// Simple difference calculation
int difference(int v1, int v2){
  return abs(v1-v2);
}