# School Bus Tracker
## Project Overview
    The School Bus Tracker is an Android mobile application designed to help university staff track the 
    location of their assigned buses in real time.The app provides a user-friendly interface that allows 
    users to select a route, choose a bus, and view its movement on a Google Map.
## Features
* Select Route: Users can view and choose available bus routes.
* Select Bus: Lists buses assigned to the selected route.
* Real-time Bus Tracking: Displays bus movement along the route.
* User Authentication: Firebase-based login and registration.
* Firebase Integration: Stores user accounts and bus details.
## Tech Stack
* Programming Language: Kotlin
* Android Framework: Android Studio
* Database: Firebase Realtime Database
* Map API: Google Maps API
## Installation & Setup
1.	Clone the Repository: 
2.	git clone https://github.com/kua-University/MuBusGo-Tesfalem.git
3.	Open in Android Studio
4.	Set up Firebase: 
    Create a Firebase project.
    Enable Authentication & Realtime Database.
    Download google-services.json and place it in app.
5.	Enable Google Maps API: 
    Get an API key from Google Cloud Console.
    Add it to AndroidManifest.xml:
  	 
``` android
<meta-data
  android:name="com.google.android.geo.API_KEY"
  android:value="your api key/>
```
6.	Run the App on an emulator or physical device.
Future Improvements
* Implement real GPS tracking.
* Add bus arrival time predictions.
* Improve UI/UX with animations and better map integration.
## Contributors
* Developer Tesfalem Gebreyesus
* email tesfalem2711@gmail.com 
* Mekelle university - Project under Mobile App Development Course
