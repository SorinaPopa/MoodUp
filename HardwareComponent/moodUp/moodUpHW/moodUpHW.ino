#include <Arduino.h>
#if defined(ESP32)
#include <WiFi.h>
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>
#include <DHT.h>
#include <Wire.h>
#include <BH1750.h>

// provide the token generation process info and the RTDB payload printing info and other helper functions
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// insert the network credentials: house
// #define WIFI_SSID ""
// #define WIFI_PASSWORD ""

// insert the network credentials: hotspot
#define WIFI_SSID ""
#define WIFI_PASSWORD ""

// insert Firebase project API Key and the RTDB URL
#define API_KEY ""
#define DATABASE_URL ""

// define sensors pin numbers
#define SCL_PIN 22
#define SDA_PIN 21
#define LIGHT_SENSOR_ADDR 0x23
#define PIR_PIN 23
#define DHT_PIN 33
#define DHT_TYPE DHT11

// define rgb led pin numbers
#define RED_PIN 13
#define GREEN_PIN 12
#define BLUE_PIN 14

// define Firebase Data object
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// variables initialisation
unsigned long sendDataPrevMillis = 0;
unsigned long lastMotionRead = 0;
float floatTemp = 0;
float floatHumid = 0;
int intLight = 0;
int intMotion = 0;
int intRed = 0;
int intGreen = 0;
int intBlue = 0;
int motionDetected = 0;
bool signupOK = false;

// dht and light sensor initialisation
DHT dht(DHT_PIN, DHT_TYPE);
BH1750 lightMeter;

void wifiConnection(){
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();
}

void firebaseSignUp(){
  // assign the api key and the RTDB URL 
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  // anonymous sign up
  if (Firebase.signUp(&config, &auth, "", ""))
  {
    Serial.println("ok");
    signupOK = true;
  }
  else
  {
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  // assign the callback function for the long running token generation task 
  config.token_status_callback = tokenStatusCallback; 

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

void setup(){
  Serial.begin(115200);
  wifiConnection();
  firebaseSignUp();

  // sensors and light initialisations
  Wire.begin(SDA_PIN, SCL_PIN);
  lightMeter.begin(BH1750::ONE_TIME_HIGH_RES_MODE, LIGHT_SENSOR_ADDR);
  pinMode(PIR_PIN, INPUT);

  pinMode(RED_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(BLUE_PIN, OUTPUT);
}

void colourDummy(){
  // send a dummy node for manual colour change
  if (Firebase.RTDB.setInt(&fbdo, "devices/esp32wroomDA9826/colour/R", 123))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }

  if (Firebase.RTDB.setInt(&fbdo, "devices/esp32wroomDA9826/colour/G", 44))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }

  if (Firebase.RTDB.setInt(&fbdo, "devices/esp32wroomDA9826/colour/B", 250))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }
}

void readDHTSensor(){
  floatTemp = dht.readTemperature();
  floatHumid = dht.readHumidity();

  if (isnan(floatTemp) || isnan(floatHumid))
  {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }
  Serial.print("Temperature: ");
  Serial.print(floatTemp);
  Serial.print(" °C\t");
  Serial.print("Humidity: ");
  Serial.print(floatHumid);
  Serial.println(" %");
}

void readLightSensor(){
  lightMeter.begin(BH1750::ONE_TIME_HIGH_RES_MODE, LIGHT_SENSOR_ADDR);
  intLight = lightMeter.readLightLevel();
  Serial.print("Light Level: ");
  Serial.print(intLight);
  Serial.println(" lux");
}

void readPIRSensor(){
  int motion = digitalRead(PIR_PIN);
  if (motion == HIGH)
  {
    intMotion = 1;
    Serial.println("Motion detected!");
  }
  else
  {
    intMotion = 0;
    Serial.println("No motion!");
  }
}

void sendSensorData(){
  // write temperature data
  if (Firebase.RTDB.setFloat(&fbdo, "devices/esp32wroomDA9826/sensors/temperature", floatTemp))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }

  // write humidity data
  if (Firebase.RTDB.setFloat(&fbdo, "devices/esp32wroomDA9826/sensors/humidity", floatHumid))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }

  // write light data
  if (Firebase.RTDB.setInt(&fbdo, "devices/esp32wroomDA9826/sensors/light", intLight))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }
}

void sendMotionData(){
  // write motion data
  if (Firebase.RTDB.setInt(&fbdo, "devices/esp32wroomDA9826/sensors/motion", intMotion))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }
}

void readDbColourData(){
  // read colour data
  if (Firebase.RTDB.getInt(&fbdo, "/devices/esp32wroomDA9826/colour/R"))
  {
    if (fbdo.dataType() == "int")
    {
      intRed = fbdo.intData();
      Serial.println(intRed);
    }
  }
  else
  {
    Serial.println(fbdo.errorReason());
  }

  if (Firebase.RTDB.getInt(&fbdo, "/devices/esp32wroomDA9826/colour/G"))
  {
    if (fbdo.dataType() == "int")
    {
      intGreen = fbdo.intData();
      Serial.println(intGreen);
    }
  }
  else
  {
    Serial.println(fbdo.errorReason());
  }

  if (Firebase.RTDB.getInt(&fbdo, "/devices/esp32wroomDA9826/colour/B"))
  {
    if (fbdo.dataType() == "int")
    {
      intBlue = fbdo.intData();
      Serial.println(intBlue);
    }
  }
  else
  {
    Serial.println(fbdo.errorReason());
  }
}

void setColourToLED(){
  delay(100);
  int pwmRed = map(intRed, 0, 255, 0, 1023);
  int pwmGreen = map(intGreen, 0, 255, 0, 1023);
  int pwmBlue = map(intBlue, 0, 255, 0, 1023);

  analogWrite(RED_PIN, pwmRed);
  analogWrite(GREEN_PIN, pwmGreen);
  analogWrite(BLUE_PIN, pwmBlue);

  Serial.print("Red: ");
  Serial.print(intRed);
  Serial.print(" | Green: ");
  Serial.print(intGreen);
  Serial.print(" | Blue: ");
  Serial.println(intBlue);
}

void loop(){
  if (Firebase.ready() && signupOK)
  {
    delay(1000);
    readDbColourData();
    //sensor keeps reading motion until it detects it
    if(motionDetected==0)
    {
      readPIRSensor();
      sendMotionData();
      delay(200);
      motionDetected = intMotion;
      if(motionDetected == 1)
      {
        // remember the first time the sensor detected motion
        lastMotionRead = millis();
      }
    }

    if((motionDetected == 1) && (millis() - sendDataPrevMillis > 2000 || sendDataPrevMillis == 0))
    {
      // once the motion is detected, the other sensors start reading data
      sendDataPrevMillis = millis();
      readLightSensor();
      delay(100);
      readDHTSensor();
      sendSensorData();
      delay(100);
      // and then the light turns on
      readDbColourData();
      setColourToLED();
      
      // and it keeps reading motion data
      readPIRSensor();
      sendMotionData();
      if(intMotion == 1)
      {
        // if there is motion, the time of the motion is reinitialised
        lastMotionRead = millis();
      }
      delay(200);

      // if there has not been any motion for a certain amount of time
      if(millis() - lastMotionRead > 20000)
      {
        readPIRSensor();
        sendMotionData();
        delay(200);
        if(intMotion == 0)
        {
          // the led turns off
          intRed = 0;
          intGreen = 0;
          intBlue = 0;
          setColourToLED();
          // a delay so the motion sensor doesn't misread the LED changing as movement
          delay(5000);
          // set the motion flag to false
          motionDetected = 0;
        }
      }
    }
  }
}


