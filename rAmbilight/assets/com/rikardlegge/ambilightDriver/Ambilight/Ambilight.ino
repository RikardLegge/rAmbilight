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

#define  NUM_LEDS 120     // The maximum number of LEDs

CRGB leds[NUM_LEDS];      // The current LED value
CRGB leds_TargetValue[NUM_LEDS];  // The buffered LED value

void setup() {

  delay(2000);  // sanity check delay - allows reprogramming if accidently blowing power w/leds

  Serial.begin(DATA_RATE);   // Starts the serial comunications port

  LEDS.addLeds<WS2812B, DATA_PIN, RGB>(leds, NUM_LEDS);  // Sets WS2812B as the selected LED microcontroller
  LEDS.clear();              // Clear all the LED values
}
/*
 1: ready.
 2: sleeping.
 
 254: End send / Ping
 255: Start send
 */

// Editable
int smoothStep = 6;       // Stepsize for the lights
int numActiveLeds = 59;    // Number of active LEDs that are handled
int normalSleep = 6;      // The ordinary sleep time

// Static
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
        } 
        else if(differance(millis(), lastPing) > 10) // Break if timed out.
          break;
      }
    } 
    else if(buffi == END_SEND){ // Is PING or END
      //Ping...
    }
    else if(buffi == BEGIN_SEND_PREFS){ // Is preferences. Read the comming 2 bytes. Key, Value
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
          normalSleep = Serial.read();
          if(normalSleep > 2000) 
            normalSleep = 2000;
          else if(normalSleep < 0) 
            normalSleep = 0;
          break;
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
      Serial.write(buffi);
      Serial.flush();
    }
  }

  delta = differance(millis(), lastPing);

  if(delta < 3000) // If i was pinged recently
    sleeping = 0;
  else if (sleeping == 0){ // Else if i havn''t prepared for sleep mode
    for(int i = 0; i < numActiveLeds; i++){
      //leds_TargetValue[i] = CRGB(0,0,0);
      leds_TargetValue[i].r = 0;
      leds_TargetValue[i].g = 0;
      leds_TargetValue[i].b = 0;
    }
    sleeping = 2;
  } 
  else if (delta > 4000){ // Else sleep
    sleeping = 1;
  }
  if(sleeping == 1){ // If i'm sleeping.
    delay(1000);     // Sleep for a while
  } 
  else {
    if(ColorSmoothening()) // If sometghing has changed
      FastLED.show();      // Update the LEDs
    Serial.write(1);       // I'm ready for more
    Serial.flush();
    //delay(normalSleep);    // Wait for a very short while
  } 
}


// Variables for ColorSmoothening
boolean requiresUpdate;
int i, j;
int l, lt; 

// Collor smothening of the lights. Returns true if something has changed.
boolean ColorSmoothening(){
  requiresUpdate = false;
  for(i = 0; i < numActiveLeds; i++){
    for(j = 0; j < 3; j++){
      l = leds[i][j];
      lt = leds_TargetValue[i][j];
      if(l != lt){
        if(differance(l,lt) <= smoothStep)
          l = lt;
        else if(l < lt)
          l += smoothStep;
        else if(l > lt)
          l -= smoothStep;

        if(l != leds[i][j]){
          requiresUpdate = true;
          leds[i][j] = l;
        }
      }
    }
  }
  return requiresUpdate;
}

// Simple differance calculation
int differance(int v1, int v2){
  return abs(v1-v2);
}










