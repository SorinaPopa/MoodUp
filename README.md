# MoodUp -Bachelor Thesis Project (2024)
A Smart Ambient Lighting System Using Sentiment Analysis Controlled by a Mobile Chatting Application

## Overview
MoodUp consists of three main components that work together to enhance the user's mood through smart lighting control.

### Hardware
- Built with an **ESP32** microcontroller programmed in **Arduino C**.
- Integrated **motion**, **temperature/humidity**, and **light sensors**.
- Communicates with the cloud via **Firebase Realtime Database**.

### Backend
- Developed a **Node.js** service deployed on **Google Cloud Platform**.
- Performed **sentiment analysis** using the **AFINN lexicon**.
- Processes user messages and updates lighting parameters accordingly.

### Mobile App
- Android application built in **Kotlin** using **MVVM architecture**.
- Allows users to **chat with an AI assistant** and control the smart light.
- Syncs with **Firebase Firestore** and **Realtime Database** for live updates.

## Tech Stack
**Languages:** Kotlin, JavaScript (Node.js), Arduino C  
**Frameworks & Tools:** Android Studio, Firebase, Google Cloud Platform  
**Hardware:** ESP32, DHT11 (temp/humidity), LDR (light sensor), PIR (motion sensor)

## Repository
More information can be found in the Documentation folder.
