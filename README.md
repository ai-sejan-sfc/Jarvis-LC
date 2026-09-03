# J.A.R.V.I.S. Android Assistant

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7C4DFF.svg)](https://m3.material.io)
[![Gemini Live](https://img.shields.io/badge/AI-Gemini%20Live%20Engine-4285F4.svg?logo=google)](https://ai.google.dev)
[![API](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-informational.svg)](https://developer.android.com)

**J.A.R.V.I.S.** is an intelligent conversational AI assistant app for Android designed with Jetpack Compose and Material 3, powered by Google's **Gemini Live Cloud Engine**. Featuring real-time acoustic voice transcription, dynamic Google Gemini API model negotiation, and a polite, friendly Bengali persona, J.A.R.V.I.S. provides an interface for interacting with voice and text.

---

## Key Features

- **Gemini Live Cloud Engine**: Direct integration with Google's Generative Language API, featuring server-side and client-side conversational AI.
- **Dynamic Model Discovery & Selection**: Automatically queries `https://generativelanguage.googleapis.com/v1beta/models` to discover and employ the optimal available model (e.g., Gemini 2.5 Flash, 1.5 Flash/Pro) for every query.
- **Friendly Bengali AI Persona**: System instructions guide J.A.R.V.I.S. to converse in warm, helpful, and natural Bengali.
- **Real-Time Voice Transcription**: Instant speech-to-text input with live acoustic waveform visualization, decibel metering (RMS & SNR), and acoustic noise floor filtering.
- **Controlled Manual Dispatch**: Voice transcriptions seamlessly fill the query composer, allowing the user full review and edit control before tapping **Send**.
- **Modern Jetpack Compose UI**: Clean, responsive layout adhering to Material Design 3 guidelines with smooth state transitions, visual audio feedback, and customizable voice engine settings.
- **Text-to-Speech (TTS) Synthesizer**: Expressive voice responses with multi-dialect support and customizable personas.

---

## Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Architecture**: Clean MVVM (Model-View-ViewModel) with Kotlin Coroutines and `StateFlow`
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) with Moshi serialization
- **Audio Processing**: Custom Android AudioRecord capture service with real-time FFT waveform and RMS calculation
- **Speech Synthesis**: Android Text-to-Speech (`TextToSpeech`) engine
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP
- **Target SDK**: Android 16 (API 36) | **Min SDK**: Android 7.0 (API 24)

---

## Project Structure

```
.
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/
│   │   │   ├── local/              # Room database & cryptomanager
│   │   │   ├── model/              # Domain and UI state models
│   │   │   └── voice/              # GeminiLiveVoiceEngine & MicrophoneCaptureService
│   │   ├── ui/
│   │   │   ├── components/         # Reusable UI components & Voice Settings Sheet
│   │   │   ├── screens/            # AssistantScreen & Primary views
│   │   │   ├── theme/              # Material 3 ColorScheme, Typography & Shapes
│   │   │   └── navigation/         # Navigation routing
│   │   └── viewmodel/              # JarvisViewModel (central reactive state engine)
│   └── build.gradle.kts            # App-level dependencies and plugins
├── Jarvis.apk                      # Ready-to-install debug APK
├── settings.gradle.kts             # Gradle project configuration
└── README.md                       # Project documentation
```

---

## Getting Started

### Prerequisites

- **Android Studio Ladybug (2024.2+)** or later
- **JDK 17** or **JDK 21**
- Android SDK with Platform 36 installed
- A **Gemini API Key** from [Google AI Studio](https://aistudio.google.com/)

### Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/<your-username>/jarvis-android.git
   cd jarvis-android
   ```

2. **Configure Gemini API Key**:
   - Add your Gemini API key in a `.env` file at the root of the project:
     ```properties
     GEMINI_API_KEY=your_actual_gemini_api_key_here
     ```
   - *Alternatively*, launch the app and tap the **Voice Settings** icon in the header to enter your API key directly into the secure settings sheet.

3. **Build the Debug APK**:
   ```bash
   gradle :app:assembleDebug
   ```
   The resulting APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

4. **Run on Device or Emulator**:
   ```bash
   gradle :app:installDebug
   ```

---

## Direct APK Download

For quick testing without compiling from source:
- Download the pre-built APK directly from the root repository: **[`Jarvis.apk`](./Jarvis.apk)**
- Enable *"Install unknown apps"* on your Android device and open the APK file to install.

---

## Permissions

The app requests the following Android permissions:
- `android.permission.INTERNET`: Connects to Google Generative Language APIs.
- `android.permission.RECORD_AUDIO`: Captures microphone input for live speech transcription and waveform metering.
- `android.permission.ACCESS_NETWORK_STATE`: Monitors active network connectivity.

---

## License

This project is distributed under the [Apache License 2.0](LICENSE).
