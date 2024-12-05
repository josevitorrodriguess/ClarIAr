# ClarIAr Project
Project for TRILHA Hackathon

![Project Image](https://github.com/user-attachments/assets/815a6932-89bd-42b0-a0a5-32ef5da0cd48)

### Members:
- [Miguel Queiroz](https://github.com/Miguelqfs)
- [Emyle Santos](https://github.com/Emysntts)
- [José Rodrigues](https://github.com/josevitorrodriguess)
- [Luís Aranha](https://github.com/lharanhamg)

## What is it about?
ClarIAr is a feature created to upgrade the functionality of TalkBack, improving the experience for visually impaired users in Android smartphones. It implements Artificial Intelligence to describe images on the screen with more accuracy and details. In other words, ClarIAr is an accessibility resource created to enable blind people to use their phones in a more efficient way!

![Project Workflow](https://github.com/user-attachments/assets/63b0e9eb-703d-4319-9b22-488badbfef3a)

## Technical Setup

### Prerequisites
- Android Studio
- Groq API Key

### API Key configuration
1. Create a file `secrets.xml` in `app/src/main/res/values/`
2. Add your Groq API key:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="groq_api_key">YOUR_GROQ_API_KEY_HERE</string>
</resources>
```

### Creating an Android Emulator
1. Open Android Studio
2. Click on "Tools" > "Device Manager"
3. Click "Create Device"
4. Select a device definition (e.g., Pixel 4)
5. Choose a system image (recommended: latest stable Android version)
6. Name your virtual device and finish setup

### Running the application
#### Using Emulator
1. Launch the created virtual device
2. Open the project in Android Studio
3. Click "Run" (green play button)

#### Using physical device
1. Enable Developer Options on your Android phone
2. Enable USB Debugging
3. Connect phone via USB
4. Authorize the connection on your device
5. Select your device in Android Studio's device dropdown
6. Click "Run"
