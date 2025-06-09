# 🎙️ SoundRecorder (Jetpack Compose Refactored from AOSP)

This project is a complete Jetpack Compose refactoring of the **built-in SoundRecorder app** from **Android 8 AOSP**.  
It replicates the original app’s behavior while adopting a modern architecture with ViewModel, StateFlow, and Compose UI.

---

## ✨ Features

- ✅ Based on the default SoundRecorder in Android 8.1 (AOSP)
- 🧱 Migrated from Java + XML to Kotlin + Jetpack Compose
- 🎧 Start / Pause / Stop recording
- 💾 Saves files directly to `/storage/emulated/0/SoundRecorder`
- 📊 Displays real-time recording time and amplitude
- 🛠 No runtime permission checks (intended for pre-installed system app)

---

## 🛠 Project Environment

| Item                  | Details                                    |
|-----------------------|---------------------------------------------|
| Target Platform       | Android 8.1 (API 27) AOSP base              |
| Verified Runtime      | ✅ **Only tested on Android 15**            |
| Frameworks Used       | Kotlin, Jetpack Compose, Hilt, MediaRecorder |
| State Management      | ViewModel + StateFlow                       |
| App Type              | System-level prebuilt (built-in) app        |

> ⚠️ This app has only been **verified on Android 15**.  
> We **cannot guarantee functionality on other Android versions**, due to differences in `MediaRecorder`, storage policies, and runtime behavior.

---

## 🔐 Required Permissions

The app declares the following permissions in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
```

> This is a **prebuilt system app**, so runtime permission requests are **not implemented**.  
> It assumes all declared permissions are already granted by the system.

---

## 📂 Project Structure

```
com.android.soundrecorder
├── data/
├── presentation/
│   ├── main/         # ViewModel, RecordUiState
│   └── components/   # Composable UI components
├── util/
└── SoundRecorderApp.kt
```

---

## ▶️ How It Works

- The main screen is rendered using Jetpack Compose.
- On pressing record:
  - `MediaRecorder` is initialized.
  - Output is saved as `.m4a` file inside the app's private storage.
- On pause:
  - Elapsed time is tracked.
- On stop:
  - File is moved to `/storage/emulated/0/SoundRecorder`
  - File name format: `record_yyyy-MM-dd_HH-mm-ss.m4a`

---

## 📌 Notes

- Can be deployed as a **prebuilt app** under `/system/priv-app` in AOSP build systems.
- `MANAGE_EXTERNAL_STORAGE` is a no-op on Android 8, but included for compatibility with Android 11+.
- Runtime behavior has only been tested on Android 15 — other versions may vary or break due to platform-level changes.
- Even though Compose is used, the UX flow mirrors the original SoundRecorder.

---
