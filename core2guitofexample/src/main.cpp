#include <Arduino.h>
#include "view.h"
#include "networking.h"
#include "sideled.h"
#include <M5Core2.h>


void event_handler_num(struct _lv_obj_t * obj, lv_event_t event);
void event_handler_ok(struct _lv_obj_t * obj, lv_event_t event);
void event_handler_box(struct _lv_obj_t * obj, lv_event_t event);
void init_gui_elements();
void mqtt_callback(char* topic, byte* payload, unsigned int length);

unsigned long next_lv_task = 0;

lv_obj_t * led;

void init_gui_elements() {
  int c = 1;
  for(int y = 0; y < 3; y++) {
    for(int x = 0; x < 3; x++) {
      add_button(String(c).c_str(), event_handler_num, 5 + x*80, 5 + y*80, 70, 70);
      c++;
    }
  }
  add_button("OK", event_handler_ok, 245, 5, 70, 70);
  add_button("0", event_handler_num, 245, 165, 70, 70);
  
  led = add_led(260, 100, 30, 30);
}


// ----------------------------------------------------------------------------
// MQTT callback
// ----------------------------------------------------------------------------

void mqtt_callback(char* topic, byte* payload, unsigned int length) {
  if(String(topic) == "alarmanlage/screenled") {
    char * buf = (char *)malloc((sizeof(char)*(length+1)));
    memcpy(buf, payload, length);
    buf[length] = '\0';
    String payloadS = String(buf);
    payloadS.trim();
    Serial.println(payloadS);
    if(payloadS == "on") {
      lv_led_on(led);
    }
    if(payloadS == "off") {
      lv_led_off(led);
    }
  }

  if(String(topic) == "lvgl/sideled" && length == 1) {
    set_sideled_state(((uint8_t)payload[0]) - '0');
  }
}


// ----------------------------------------------------------------------------
// UI event handlers
// ----------------------------------------------------------------------------

String buffer = "";

void event_handler_num(struct _lv_obj_t * obj, lv_event_t event) {
  if(event == LV_EVENT_CLICKED) {
    lv_obj_t * child = lv_obj_get_child(obj, NULL);
    String num = String(lv_label_get_text(child));
    num.trim();
    buffer += num;
  }
}

lv_obj_t * mbox;

void event_handler_box(struct _lv_obj_t * obj, lv_event_t event) {
  String textBtn = String(lv_msgbox_get_active_btn_text(obj));
  if(event == LV_EVENT_VALUE_CHANGED) {
    if(textBtn == "Send") {
      mqtt_publish("alarmanlage/numpad", buffer.c_str());
    }
    buffer = "";
    close_message_box(mbox);
  }
}

void event_handler_ok(struct _lv_obj_t * obj, lv_event_t event) {
  if(event == LV_EVENT_CLICKED) {
    Serial.println(buffer);
    mbox = show_message_box(buffer.c_str(), "Send", "Cancel", event_handler_box);
  }
}

void loop() {
  if(next_lv_task < millis()) {
    if (digitalRead(36) ==1) {  
       mqtt_publish("alarmanlage/status", "1");
  } else if (digitalRead(36) ==0){
    mqtt_publish("alarmanlage/status", "0");
  }
  delay(500);
  
    lv_task_handler();
    next_lv_task = millis() + 5;
  }
  
  mqtt_loop();
}

void setup() {

  M5.begin();             //Init M5Core2.  初始化 M5Core2
 
  pinMode(36, INPUT); 

  init_m5();
  init_display();
  Serial.begin(115200);
  lv_obj_t * wifiConnectingBox = show_message_box_no_buttons("Connecting to WiFi...");
  lv_task_handler();
  delay(5);
  setup_wifi();
  mqtt_init(mqtt_callback);
  close_message_box(wifiConnectingBox);
  init_gui_elements();
  init_sideled();
}