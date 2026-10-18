# CloudReel Android Application

An enterprise-grade, native Android application for **CloudReel** featuring cellular bonding, live video contribution, and multi-network relay capabilities.

---

## 📱 Application Flow & Architectural Overview

The application entry flow is architected following Google's modern Android conventions, Material 3, and CloudReel's enterprise brand identity.

```
                              ┌────────────────────────┐
                              │  App Launch (Splash)   │
                              │ AndroidX SplashScreen  │
                              └───────────┬────────────┘
                                          │
                                          ▼
                            ┌────────────────────────────┐
                            │ Check Session Persistence  │
                            │  EncryptedSharedPreferences│
                            └──────┬──────────────┬──────┘
                                   │              │
                    Active Session │              │ No Active Session / Expired
                                   ▼              ▼
                    ┌─────────────────┐   ┌────────────────────────┐
                    │  Go Live Home   │   │     Authentication     │
                    │ (Role Selection)│   │    (Login / Sign Up)   │
                    └────────┬────────┘   └───────────┬────────────┘
                             │                        │
                             │                        ▼ Success
                             │               ┌─────────────────┐
                             │               │  Go Live Home   │
                             │               └────────┬────────┘
                             │                        │
                ┌────────────┴───────────┐            │
                ▼                        ▼            │
     ┌──────────────────────┐ ┌──────────────────────┐│
     │   Streamer Workflow  │ │    Relay Workflow    ││
     │  (Camera + Bonding)  │ │ (Cellular Donation)  ││
     └──────────────────────┘ └──────────────────────┘│
```

### 1. Splash Screen (`com.cloudreel.mobile.ui.splash`)
- Integrated with Android 12+ `androidx.core:core-splashscreen` API for instantaneous system launch.
- Subsequent post-splash branded reveal screen:
  - Centers official CloudReel gradient infinity emblem (`#D300EA` → `#3031CB`).
  - Smooth 1.4-second scale & fade transition (`FastOutSlowInEasing`).
  - Asynchronously verifies the encrypted session token in background.
  - Automatically routes authenticated users directly to **Go Live Home**, bypassing login.
  - Routes unauthenticated or expired users to **Login**.

### 2. Authentication Screen (`com.cloudreel.mobile.ui.auth`)
- **Login Screen**:
  - Work Email validation (`android.util.Patterns.EMAIL_ADDRESS`).
  - Password field with interactive visibility toggle.
  - "Forgot password?" recovery dialog with enterprise email delivery feedback.
  - Primary "Sign In" button with signature CloudReel purple/magenta gradient.
  - "Don't have an account? Create Account" transition.
  - Connects to CloudReel's `.NET` production backend (`v1/login`), with built-in development fallback.
- **Sign Up Screen**:
  - Full Name, Work Email, Password, and Password Confirmation fields.
  - Comprehensive client-side validation and clear, understandable error banners.
  - Re-routes upon successful registration directly to Go Live Home with auto-session creation.

### 3. Go Live Home Screen (`com.cloudreel.mobile.ui.home`)
- **Header**:
  - CloudReel official vector logo on top-left.
  - Dynamic Theme Toggle (light/dark mode) on top-right.
  - Sign Out action button with session invalidation.
- **Enterprise Platform Badge**:
  - `✦ ENTERPRISE BROADCAST PLATFORM` pill badge with subtle purple border.
- **Two Exclusive Role Options**:
  1. **Streamer**:
     - *Title*: "Streamer"
     - *Subtitle*: "Camera + Bonding"
     - *Description*: "Broadcast live using your camera and bonded network connections."
     - *Visual*: Signature CloudReel purple/magenta gradient card with video camera icon.
     - *Action*: Tapping immediately launches the **Streamer Workflow**.
  2. **Relay**:
     - *Title*: "Relay"
     - *Subtitle*: "Donate This Cellular"
     - *Description*: "Contribute your cellular connection to support live broadcasting."
     - *Visual*: Light neutral surface, subtle 1dp border, cellular tower icon.
     - *Action*: Tapping immediately launches the **Relay Workflow**.
- **Bonded Network Telemetry Card**:
  - Real-time network telemetry card showcasing active 5G cellular connection, Wi-Fi 6 bonding status, latency meter, and total bonded throughput.

### 4. Destination Workflows
- **Streamer Workflow (`com.cloudreel.mobile.ui.streamer`)**:
  - Full viewfinder preview with crosshair, resolution telemetry (1080p60 · SRT Bonded · 6.8 Mbps), front/rear camera switcher, and On-Air/Standby indicator.
  - Bonded Network Interfaces panel displaying individual cellular and Wi-Fi uplink contributions.
  - "Start Live Broadcast" / "End Broadcast" toggle.
- **Relay Workflow (`com.cloudreel.mobile.ui.relay`)**:
  - "ACTIVE CELLULAR RELAY NODE" status indicator.
  - Live Bandwidth Contributed and Total Data Donated counters.
  - Carrier SIM telemetry (5G SA, -82 dBm signal).
  - Thermal & battery saver protection toggles.

---

## 🎨 Design System Tokens

The application strictly implements CloudReel's design language:

| Token | Hex Value | Role |
| :--- | :--- | :--- |
| `CRMagenta` | `#D300EA` | Primary Brand Magenta Accent |
| `CRPurple` | `#3031CB` | Deep Royal Indigo / Brand Gradient End |
| `CRViolet` | `#7928CA` | Mid Gradient Tone / Interactive Focus |
| `CRBrandGradient` | `#D300EA` → `#3031CB` | Signature Linear Action Gradient |
| `BackgroundLight` | `#FFFFFF` | Clean White Surface |
| `BackgroundDark` | `#0E0E13` | Sleek Dark Mode Background |
| `TextPrimaryLight` | `#111111` | Near-Black High-Contrast Headings |
| `TextSecondaryLight` | `#666666` | Neutral Supporting Copy |
| `BorderLight` | `#E5E7EB` | Subtle Structural Divider |

---

## 🔒 Security & Session Handling

1. **Encrypted Storage**: Uses `androidx.security.crypto:security-crypto` (`EncryptedSharedPreferences`) backed by Android Keystore (AES256-GCM / AES256-SIV).
2. **Session Lifecycle**: Tokens are automatically validated on startup. Sessions older than 30 days are automatically invalidated, enforcing corporate re-authentication.
3. **No Plaintext Passwords**: Passwords are never logged or stored locally.
4. **Android Backup Protection**: `data_extraction_rules.xml` and `backup_rules.xml` exclude `cloudreel_secure_prefs.xml` from cloud and device-to-device transfers.

---

## 🛠 Project Structure

```
cloudreel_Mapp/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/cloudreel/mobile/
│       │   │   ├── CloudReelApp.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── local/SessionManager.kt
│       │   │   │   ├── model/AuthModels.kt
│       │   │   │   ├── model/User.kt
│       │   │   │   └── remote/NetworkClient.kt
│       │   │   ├── navigation/
│       │   │   │   ├── AppNavigation.kt
│       │   │   │   └── Screen.kt
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Theme.kt
│       │   │   │   └── Type.kt
│       │   │   └── ui/
│       │   │       ├── auth/
│       │   │       │   ├── AuthViewModel.kt
│       │   │       │   ├── LoginScreen.kt
│       │   │       │   └── SignUpScreen.kt
│       │   │       ├── components/
│       │   │       │   ├── Badge.kt
│       │   │       │   ├── CloudReelLogo.kt
│       │   │       │   ├── GradientButton.kt
│       │   │       │   ├── InputField.kt
│       │   │       │   ├── SecondaryButton.kt
│       │   │       │   └── TopBar.kt
│       │   │       ├── home/
│       │   │       │   ├── GoLiveHomeScreen.kt
│       │   │       │   └── HomeViewModel.kt
│       │   │       ├── relay/RelayScreen.kt
│       │   │       └── streamer/StreamerScreen.kt
│       │   └── res/
│       │       ├── drawable/ (Official CloudReel SVG & PNG assets)
│       │       ├── values/ (colors.xml, strings.xml, themes.xml)
│       │       └── values-night/ (themes.xml)
│       └── test/java/com/cloudreel/mobile/ui/ValidationTest.kt
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── README.md
```

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug (2024.2+) or Android Command-line Tools.
- JDK 17 or JDK 21.
- Android SDK 35 (compileSdk: 35, minSdk: 26).

### Build Commands
```bash
# Run unit test suite
./gradlew test

# Assemble Debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```
