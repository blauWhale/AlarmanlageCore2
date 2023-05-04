#include <Arduino.h>
#include "view.h"
#include "networking.h"
#include "sideled.h"
#include <M5Core2.h>
#include <Speaker.h>
#include "MFRC522_I2C.h"
#include "NfcAdapter.h"


#define SS_PIN 10
#define RST_PIN 9




void event_handler_num(struct _lv_obj_t * obj, lv_event_t event);
void event_handler_ok(struct _lv_obj_t * obj, lv_event_t event);
void event_handler_box(struct _lv_obj_t * obj, lv_event_t event);
void init_gui_elements();
void mqtt_callback(char* topic, byte* payload, unsigned int length);
void triggerAlarm(); 
void play_tone();
void checkAlarm();
void checkAlarmDuration();
void dump_byte_array(byte *buffer, byte bufferSize);
void read_nfcTag();

unsigned long next_lv_task = 0;

lv_obj_t * led;

bool anwesenheit = true;
bool alarmIsActive = false;

MFRC522 mfrc522(0x28); 

unsigned long alarmStartTime = 0;
const unsigned long ALARM_DURATION = 30000;

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
  //Serial.print("Message received on topic: ");
  //Serial.println(topic);
  if(String(topic) == "alarmanlage/screenled") {
    char * buf = (char *)malloc((sizeof(char)*(length+1)));
    memcpy(buf, payload, length);
    buf[length] = '\0';
    String payloadS = String(buf);
    payloadS.trim();
    Serial.println(payloadS);
    if(payloadS == "1") {
      lv_led_on(led);
    }
    if(payloadS == "0") {
      lv_led_off(led);
    }
  }

  if(String(topic) == "alarmanlage/status") {
    char * buf = (char *)malloc((sizeof(char)*(length+1)));
    memcpy(buf, payload, length);
    buf[length] = '\0';
    String payloadS = String(buf);
    payloadS.trim();
    Serial.println(payloadS);
    if(payloadS == "1") {
      anwesenheit=true;
    }
    if(payloadS == "0") {
      anwesenheit=false;
    }
  }

  if(String(topic) == "alarmanlage/alarm") {
    char * buf = (char *)malloc((sizeof(char)*(length+1)));
    memcpy(buf, payload, length);
    buf[length] = '\0';
    String payloadS = String(buf);
    payloadS.trim();
    Serial.println(payloadS);
    if(payloadS == "1") {
      alarmIsActive=true;
    }
    if(payloadS == "0") {
      alarmIsActive=false;
    }
  
  }

  if(String(topic) == "alarmanlage/sideled" && length == 1) {
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
      if(strcmp(buffer.c_str(), "0") == 0){
        mqtt_publish("alarmanlage/status", buffer.c_str());
      }
      else{
         mqtt_publish("alarmanlage/numpad", buffer.c_str());
      }
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





// ----------------------------------------------------------------------------
// Alarm tone
// ----------------------------------------------------------------------------

const uint16_t SAMPLE_RATE = 44100; // Audio sample rate in Hz
const uint16_t AMPLITUDE = 10000; // Audio amplitude
const uint16_t DURATION_MS = 1000; // Audio duration in milliseconds

void play_tone() {
  float frequency = 444.0;
  // Generate a sine wave audio sample
  uint16_t buffer_size = DURATION_MS * SAMPLE_RATE / 1000;
  uint16_t* buffer = new uint16_t[buffer_size];
  for (int i = 0; i < buffer_size; i++) {
    buffer[i] = AMPLITUDE * sin(2 * PI * i / (SAMPLE_RATE / frequency));
  }
  // Play the audio sample through the M5 speaker
  size_t bytes_written = M5.Spk.PlaySound((const unsigned char*)buffer, buffer_size * sizeof(uint16_t));
  delay(DURATION_MS); // Wait for the audio to finish playing

  // Deallocate the buffer
  delete[] buffer;
}



// ----------------------------------------------------------------------------
// Badge Reader
// ----------------------------------------------------------------------------
void dump_byte_array(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}

byte uidBytes[7]; // Global variable to store UID
bool isTagPresent = false; // Global variable to track if a tag is present



void read_tag() {
  // Check if NFC is available and enabled
  mfrc522.PCD_Init();

  // Look for new cards
  if (!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial()) {
    // Reset UID and tag present flag
    memset(uidBytes, 0, sizeof(uidBytes));
    isTagPresent = false;

    mfrc522.PICC_HaltA();
    mfrc522.PCD_StopCrypto1();
    return;
  }

  // Save the UID into the global variable
  memcpy(uidBytes, mfrc522.uid.uidByte, sizeof(uidBytes));

  // Print the card's serial number
  Serial.print("The RFID is reading this: ");
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? "0" : "");
    Serial.print(mfrc522.uid.uidByte[i], HEX);
  }
  Serial.println();

  // Print the card's UID
  Serial.print("Card UID: ");
  dump_byte_array(mfrc522.uid.uidByte, mfrc522.uid.size);
  Serial.println();

  // Print the card's data
  Serial.print("Data: ");
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    Serial.print((char)mfrc522.uid.uidByte[i]);
  }
  Serial.println();

  Serial.println();

  // Set the tag present flag
  isTagPresent = true;

  // Halt the NFC tag and stop crypto1 communication
  mfrc522.PICC_HaltA();
  mfrc522.PCD_StopCrypto1();
}


bool isAlarmDurationExceeded = false; // Flag to track if alarm duration has been exceeded

void checkAlarmDuration() {
  if (millis() - alarmStartTime > ALARM_DURATION) {
    Serial.println("Alarm has been active for 30 seconds");
    isAlarmDurationExceeded = true;
  }
}

void checkAlarm() {
  if (alarmIsActive) {
    if (alarmStartTime == 0) {
      alarmStartTime = millis(); // Start the alarm timer
      play_tone();
    } else {
      checkAlarmDuration();
      if (!isAlarmDurationExceeded) {
        play_tone();
      }
    }
    Serial.println("Alarm is going off");
   
  } else {
    alarmStartTime = 0;
    isAlarmDurationExceeded = false; // Reset the alarm duration exceeded flag
  }
}

void triggerAlarm(){
  mqtt_publish("alarmanlage/screenled", "on");
  mqtt_publish("alarmanlage/sideled", "1");
  mqtt_publish("alarmanlage/alarm", "1");
}

void loop() {
  if(next_lv_task < millis()) {
    checkAlarm();
    read_tag();
    if (digitalRead(36) == 1 ) {  
      mqtt_publish("alarmanlage/motion", "1");
      if(!anwesenheit){
       triggerAlarm();
      }
      else{
        //do nothing
      }
      
  } else if (digitalRead(36) == 0){
    mqtt_publish("alarmanlage/motion", "0");
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

  mfrc522.PCD_Init(); 

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