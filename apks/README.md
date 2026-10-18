# CloudReel Android APK Artifacts

This folder contains the compiled Android Application Package (APK) binaries for CloudReel Mobile.

## Available Binaries

| Binary | Size | Build Variant | Target SDK | Min SDK | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`cloudreel-debug.apk`** | 18 MB | `debug` | 35 (Android 15) | 26 (Android 8.0) | Full debug build with Jetpack Compose & live telemetry |
| **`app-debug.apk`** | 18 MB | `debug` | 35 (Android 15) | 26 (Android 8.0) | Standard Gradle debug output artifact |

---

## Package Information
- **Application ID:** `com.cloudreel.mobile`
- **Permissions:**
  - `android.permission.INTERNET`
  - `android.permission.ACCESS_NETWORK_STATE`
  - `android.permission.CHANGE_NETWORK_STATE`
  - `android.permission.ACCESS_WIFI_STATE`
  - `android.permission.CAMERA`
  - `android.permission.RECORD_AUDIO`

---

## Installation Instructions

### Option 1: Via ADB (Android Debug Bridge)
If your Android phone or emulator is connected via USB:
```bash
adb install -r apks/cloudreel-debug.apk
```

### Option 2: Direct Device Sideloading
1. Transfer `cloudreel-debug.apk` to your Android device via USB, AirDrop/Nearby Share, Google Drive, or Slack.
2. Tap the APK file in your device's File Manager / Downloads.
3. Allow "Install unknown apps" if prompted, then tap **Install**.
