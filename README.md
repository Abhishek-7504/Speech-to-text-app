# Android Application for Speech-To-Text using [Vosk](https://alphacephei.com/vosk/models)

This is an android application for transcription built using [Java](https://www.java.com/en/). The app takes input of an audio file of [WAV](https://en.wikipedia.org/wiki/WAV) format and performs transcription.
---
## Folder Structure
```
Folder Structure
├── app/ # Main Android source code (Java, assets, res)
│ └── src/
│ └── main/
│ ├── java/com/example/voskapplication/MainActivity.java
│ ├── assets/model/ # Vosk model files go here
│ └── res/layout/activity_main.xml
├── libs/jna-temp/ # Temporary or unpacked JNA .so files
├── jni/ # (Optional) Native code if extended
├── Copy-JNADispatch.ps1 # Script to help copy JNA native libs
├── jna.zip # Downloaded JNA release archive
├── classes.jar 
├── AndroidManifest.xml 
├── build.gradle
├── settings.gradle
└── gradlew / gradlew.bat # Gradle wrapper scripts
```
---
## Working
1. On launch, the app copies the Vosk model from assets/ to internal storage.

2. It requests permissions to access external files.

3. It looks for output.wav inside the Downloads folder.

4. It uses Vosk to transcribe the audio and displays the result in a TextView.
