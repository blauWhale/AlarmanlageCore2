#include "sideled.h"

CRGB leds[SIDELED_NUM_LEDS];



uint8_t led_state = 0;


void LEDtask(void *arg){
  while (1){
    if(led_state == SIDELED_STATE_ALARM) {
      fill_solid(leds, SIDELED_NUM_LEDS, CRGB::Red);
      FastLED.show();
      delay(500);
      fill_solid(leds, SIDELED_NUM_LEDS, CRGB::Blue);
      FastLED.show();
      delay(500);
    }
    else if(led_state == SIDELED_STATE_FUN) {
      for(int a = 0; a < SIDELED_NUM_LEDS; a++) {
        leds[a] = random(LONG_MAX);
      }
      delay(200);
      FastLED.show();
    }
    else if(led_state == SIDELED_STATE_OFF) {
      fill_solid(leds, SIDELED_NUM_LEDS, CRGB::Black);
      FastLED.show();
      delay(1000);
    }else{
      delay(1000);
    }
  }
}

void init_sideled() {
    FastLED.addLeds<NEOPIXEL, SIDELED_DATA_PIN>(leds, SIDELED_NUM_LEDS);
    xTaskCreatePinnedToCore(LEDtask, "LEDTask", 4096, NULL, 2, NULL, 0);
}

void set_sideled_state(uint8_t state) {
    led_state = state;
}