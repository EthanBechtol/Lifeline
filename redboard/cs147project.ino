// ADDITIONAL LIBRARIES INCLUDED ON PARTICLE: Grove_LCD_RGB_Backlight (1.0.1) and HttpClient (0.0.5).
#include <HttpClient.h>
#include "application.h"
#include "SparkFun_MMA8452Q.h"
#include <Grove_LCD_RGB_Backlight.h>
#include "SparkFun_Bio_Sensor_Hub_Library.h"
#include <Wire.h>

#define NOTE_C4  262
#define NOTE_D4  294
#define NOTE_E4  330
#define NOTE_E5  659

// HR/O2 MONITOR ---
#define DEF_ADDR 0x55
const int resPin = 4;
const int mfioPin = 5;
SparkFun_Bio_Sensor_Hub bioHub(resPin, mfioPin); 
bioData body; 

// RGB LCD ---
rgb_lcd lcd;
char lcdbuf[20];
 
const int colorR = 255;
const int colorG = 255;
const int colorB = 255;

// ACCEL
MMA8452Q accel; 

// Reminder lights
#define MEDICATION_TIMER 10000
long nextMedication = millis() + MEDICATION_TIMER;
int medLight = 7;
int reminderButton = 2;

#define TURN_TIMER 15000
long nextTurn = millis() + TURN_TIMER;
int turnLight = 6;
String lastOrientation = "left";
String orientation = "left";


// Vital Update Data
#define MIN_VITAL_POST_TIMER 20000
long nextVitalPost = millis() + MIN_VITAL_POST_TIMER;

#define MIN_CRITICAL_POST_TIMER 5000
long nextCriticalPost = millis() + MIN_CRITICAL_POST_TIMER;

// Speaker
const int speaker = 3;

// HTTP CLIENT
HttpClient http;

http_header_t headers[] = {
    { "Content-Type", "application/json" },
    { "Accept" , "*/*"},
    { NULL, NULL } 
};

http_request_t request;
http_response_t response;

void sanitizePath(String& path) {
    path.replace(" ", "+");
    path.replace("%", "%25");
    path.replace(":", "%3A");
}

void doPost(const String& path, const String& name) {
    request.hostname = "REDACTED";
    request.port = 80;
    request.path = path.c_str();
	
    http.post(request, response, headers);
    Serial.print(name + " POST> Response status: ");
    Serial.println(response.status);
    Serial.print(name + " POST> HTTP Response Body: ");
    Serial.println(response.body);
}

void postVitals(int hr, int o2) {
    String path("/new-vital-reading-handler?orientation=");
    path.concat(orientation.c_str());
    path.concat("&hr=");
    path.concat(hr);
    path.concat("&o2=");
    path.concat(o2);
    doPost(path, "VITAL");
}

void postEvent(const String& type, String& description) {
    String path("/new-event-handler?type=");
    path.concat(type);
    path.concat("&description=");
    sanitizePath(description);
    path.concat(description);
    doPost(path, "EVENT");
}

String getTimeDiff(long startMs, long endMs) {
    long ms = endMs - startMs;
    long minutes = (ms / 1000)  / 60;
    int seconds = (int)((ms / 1000) % 60);
    String result(minutes);
    if(minutes == 1)
        result.concat(" minute ");
    else
        result.concat(" minutes ");
        
    result.concat(seconds);
    if(seconds == 1)
        result.concat(" second");
    else
        result.concat(" seconds");
        
    return result;
}


void setup(){
    Serial.begin(115200);
    
    Wire.begin();
    int result = bioHub.begin();
    if (!result)
        Serial.println("Bio Sensor started!");
    else
        Serial.println("ERROR: Could not communicate with bio sensor!");
    
    Serial.println("Configuring Sensor...."); 
    int error = bioHub.configBpm(MODE_TWO);
    if(!error){
        Serial.println("Bio sensor configured.");
    }
    else {
        Serial.println("ERROR: Error configuring bio sensor.");
        Serial.print("Error: "); 
        Serial.println(error); 
    }
    lcd.begin(16, 2);
    lcd.setRGB(colorR, colorG, colorB);
    
    if (accel.begin() == false) {
        Serial.println("ERROR: MMA8452Q not connected. Recheck connections.");
    }
    
    pinMode(reminderButton, INPUT);
    pinMode(medLight, OUTPUT);
    pinMode(turnLight, OUTPUT);
    pinMode(speaker, OUTPUT);
    noTone(speaker);
    tone(speaker, NOTE_C4, 1);
    
    delay(4000); 
}


void lcdReminder(const String& msg, int r, int g, int b) {
    lcd.setRGB(r, g, b);
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("New Task:");
    lcd.setCursor(0, 1);
    lcd.print(msg.c_str());
    delay(2500);
    lcd.setRGB(20, 20, 20);
}

void playReminderTone() {
    tone(speaker, NOTE_C4, 300);
    delay(300);
    tone(speaker, NOTE_D4, 300);
    delay(300);
    tone(speaker, NOTE_E4, 300);
}

bool medicationLightOn = false;
void updateMedicationReminder(){
    if(!medicationLightOn && millis() >= nextMedication) {
        medicationLightOn = true;
        Serial.println("REMINDER> Medication reminder light turned on.");
        playReminderTone();
        lcdReminder("Medication", 20, 20, 0);
    }
    else {
        if(medicationLightOn && digitalRead(reminderButton) == HIGH){
            String desc("Patient was given their medication. (");
            desc.concat(getTimeDiff(nextMedication, millis()));
            desc.concat(" after a reminder was sent)");
            postEvent("REMINDER", desc);
            
            medicationLightOn = false;
            nextMedication = millis() + MEDICATION_TIMER;
            Serial.println("REMINDER> Medication was administered.");
        }
    }
}

bool turnLightOn = false;
void updateTurnReminder() {
    if(!turnLightOn && millis() >= nextTurn) {
        turnLightOn = true;
        Serial.println("REMINDER> Turn reminder light turned on.");
        playReminderTone();
        lcdReminder("Turn patient", 20, 20, 0);
    }
    else {
        if(turnLightOn && orientation != lastOrientation){
            turnLightOn = false;
            String desc("Patient was turned from their ");
            desc.concat(lastOrientation);
            desc.concat(" side to their ");
            desc.concat(orientation);
            desc.concat(" side. (");
            desc.concat(getTimeDiff(nextTurn, millis()));
            desc.concat(" after a reminder was sent)");
            postEvent("REMINDER", desc);
            
            lastOrientation = orientation;
            nextTurn = millis() + TURN_TIMER;
            Serial.println("REMINDER> Patient was turned.");
        }
    }
}

void updateReminders() {
    updateMedicationReminder();
    updateTurnReminder();
}

void updateReminderLights() {
    if(medicationLightOn){
        digitalWrite(medLight, HIGH);
    } else {
        digitalWrite(medLight, LOW);
    }
    
    if(turnLightOn){
        digitalWrite(turnLight, HIGH);
    } else {
        digitalWrite(turnLight, LOW);
    }
}


void playCriticalTone() {
    tone(speaker, NOTE_E5, 250);
    delay(250);
    tone(speaker, NOTE_D4, 250);
    delay(750);
}

bool isValidVitalReading(uint8_t hr, uint8_t o2, uint8_t confidence, uint8_t status) {
    if(confidence >= 80 && hr != 0 && o2 != 0 && status == 3)
        return true;
    else
        return false;
}

bool isCriticalVitalReading(uint8_t hr, uint8_t o2, uint8_t confidence, uint8_t status) {
    const int HRMin = 60;
    const int HRMax = 105;
    const int O2Min = 95;
    const int O2Max = 100;
    
    if(!isValidVitalReading(hr, o2, confidence, status))
        return false;
    
    if(hr < HRMin || hr > HRMax || o2 < O2Min || o2 > O2Max)
        return true;
    else
        return false;
    
}

bool validVitals = false;
void checkVitals() {
    body = bioHub.readBpm();
    uint8_t hr = body.heartRate;
    uint8_t o2 = body.oxygen;
    uint8_t confidence = body.confidence;
    uint8_t status = body.status;
    int8_t extStatus = body.extStatus;
    bool validReading = isValidVitalReading(hr, o2, confidence, status);
    bool critialReading = isCriticalVitalReading(hr, o2, confidence, status);
    validVitals = validReading;
    
    if(validReading && millis() >= nextVitalPost){
        Serial.println("VITAL POST> Posting vital data to server.");
        postVitals(hr, o2);
        nextVitalPost = millis() + MIN_VITAL_POST_TIMER;
    }
    
    if(critialReading) {
        lcd.setRGB(20, 0, 0);
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("CRITICAL VITALS");
        lcd.setCursor(0, 1);
        sprintf(lcdbuf, "HR %dbpm O2 %d%%", body.heartRate, body.oxygen);
        lcd.print(lcdbuf);
        playCriticalTone();
        
        if(millis() >= nextCriticalPost) {
            String desc("Critial vital readings: ");
            desc.concat(hr);
            desc.concat("bpm, ");
            desc.concat(o2);
            desc.concat("% O2.");
            postEvent("VITAL", desc);
            nextCriticalPost = millis() + MIN_CRITICAL_POST_TIMER;
        }
    }
    else {
        lcd.setRGB(20, 20, 20);
        lcd.clear();
        lcd.setCursor(0, 0);
        sprintf(lcdbuf, "HR %dbpm O2 %d%%", body.heartRate, body.oxygen);
        lcd.print(lcdbuf);
        lcd.setCursor(0, 1);
        sprintf(lcdbuf, "Position: %s", orientation.c_str());
        lcd.print(lcdbuf);
    }
}



void loop(){
    if (accel.isRight() == true) {
        orientation = "right";
    }
    else if (accel.isLeft() == true) {
        orientation = "left";
    }
    else if (accel.isUp() == true) {
        orientation = "up";
    }
    else if (accel.isDown() == true) {
        orientation = "down";
    }
    else if (accel.isFlat() == true) {
        orientation = "flat";
    }
    else {
        orientation = "?";
    }

    checkVitals();
    updateReminderLights();
    updateReminders();

    delay(250);
}
