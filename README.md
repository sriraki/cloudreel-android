# CloudReel Android Application

An enterprise-grade, native Android application for **CloudReel** featuring cellular bonding, live video contribution, and multi-network relay capabilities.

[![Live Interactive Preview](https://img.shields.io/badge/Live%20Simulator-GitHub%20Pages-7928CA?style=for-the-badge&logo=github)](https://sriraki.github.io/cloudreel-android/)
[![Download APK](https://img.shields.io/badge/Download%20APK-v1.0.0--debug-10B981?style=for-the-badge&logo=android)](apks/cloudreel-debug.apk)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)](https://github.com/sriraki/cloudreel-android)

> 🌐 **Interactive Browser Simulator:** Experience the full screen flow in your browser at **[https://sriraki.github.io/cloudreel-android/](https://sriraki.github.io/cloudreel-android/)**

---

## 📱 Application Flow & Architectural Overview

The application entry flow is architected following Google's modern Android conventions, Jetpack Compose, Material 3, and CloudReel's enterprise brand identity.

```
                       SPLASH SCREEN
                             │
                             ▼ (Unconditional navigation)
                        HOME SCREEN
                             │
       ┌─────────────────────┼─────────────────────┬─────────────────────┐
       ▼                     ▼                     ▼                     ▼
1. Start as Host     2. Join as Participant   3. Streamer           4. Relay
       │                     │                     │                     │
       ▼                     ▼                     ▼                     ▼
 URL / Code Entry      URL / Code Entry       Check Session         Existing Relay
   (Host Mode)       (Participant Mode)      Active / Token             Screen
       │                     │                     │
       ▼                     ▼              ┌──────┴──────┐
 Existing Host      Existing Participant    ▼             ▼
   Workflow               Workflow     Unauthenticated Authenticated
                                            │             │
                                            ▼             │
                                       Login / Sign Up    │
                                            │             │
                                            ▼ (Direct)    ▼
                                      Existing Streamer Screen
```

---

### 1. Splash Screen (`com.cloudreel.mobile.ui.splash`)
- Integrated with Android 12+ `androidx.core:core-splashscreen` API for instantaneous system launch.
- Subsequent post-splash branded reveal screen:
  - Centers official CloudReel gradient infinity emblem (`#7C3AED` → `#EC4899`).
  - Smooth spring-based scale & fade transition.
  - **Unconditional Navigation:** Transition directly to the Home screen on app launch. Never forces login before entering Home.

### 2. Home Screen — Four Primary Options (`com.cloudreel.mobile.ui.home`)
- **Header:**
  - Official CloudReel logo and wordmark unified at top-left.
  - Dynamic Theme Toggle (light/dark mode) on top-right.
  - Sign Out action button when an active session exists.
- **Main Heading:** `"Welcome to CloudReel"`
- **Supporting Text:** `"Choose how you want to connect."`
- **Four Primary Options:**
  1. **Start as Host** — `"Create or host a live session."` (Icon: Broadcast Tower / Podcasts). Navigates to the shared session entry screen in **Host Mode**.
  2. **Join as Participant** — `"Join an existing session."` (Icon: Group / Users). Navigates to the shared session entry screen in **Participant Mode**.
  3. **Streamer** — `"Broadcast live using your camera and bonded connections."` (Icon: Video Camera). Direct to Streamer if authenticated; prompts Login / Sign Up if unauthenticated, then auto-forwards immediately on success.
  4. **Relay** — `"Contribute your cellular connection to support live broadcasting."` (Icon: Satellite Relay). Launches the Relay workflow immediately with **no login required**.
- **Bonded Mesh Network Telemetry Card:**
  - Real-time network telemetry card showcasing active 5G cellular connection, Wi-Fi 6 bonding status, latency meter, and bonded throughput.

### 3. Reusable Shared Session Entry Screen (`com.cloudreel.mobile.ui.session`)
- Single composable component (`SessionEntryScreen`) dynamically adapting based on `SessionEntryMode`:
  - **Host Mode:** Title `"Start as Host"`, Subtitle `"Enter the session URL or code to continue."`, Action button `"Continue as Host"`.
  - **Participant Mode:** Title `"Join a Session"`, Subtitle `"Enter the session URL or code to join."`, Action button `"Join Session"`.
- Validates session URLs (e.g. `https://live.cloudreel.com/room/...`) or 4–24 character alphanumeric room codes.
- In-flight loading state disables submission buttons to prevent duplicate requests.
- Provides quick-fill test chips for rapid verification.

### 4. Destination Workflows
- **Host Workflow (`HostWorkflowScreen`)**:
  - Live session badge with one-tap room code copy (`CR-8492-HOST`).
  - Real-time telemetry grid (1080p60, 4 participants, 4.2 Mbps bonded uplink, 22ms latency).
  - Broadcast controls (Mute, Camera Pause, End Broadcast).
- **Participant Workflow (`ParticipantWorkflowScreen`)**:
  - Low-latency viewport with 1080p60 stream and 18ms latency indicator.
  - Stream controls: Audio toggle, fullscreen toggle, and leave session action.
- **Streamer Workflow (`com.cloudreel.mobile.ui.streamer`)**:
  - Full viewfinder preview with crosshair, resolution telemetry, front/rear camera switcher, and On-Air/Standby indicator.
  - Bonded Network Interfaces panel displaying individual cellular and Wi-Fi uplink contributions.
- **Relay Workflow (`com.cloudreel.mobile.ui.relay`)**:
  - Active cellular relay node with live bandwidth contributed and total data donated counters.
  - Carrier SIM telemetry (5G SA, -82 dBm signal).

---

## 🎨 Design System Tokens

The application strictly implements CloudReel's design language:

| Token | Hex Value | Role |
| :--- | :--- | :--- |
| `CRMagenta` | `#EC4899` / `#D300EA` | Primary Brand Magenta Accent |
| `CRPurple` | `#7C3AED` / `#3031CB` | Deep Royal Indigo / Brand Gradient End |
| `CRBrandGradient` | `#7C3AED` → `#EC4899` | Signature Linear Action Gradient |
| `BackgroundLight` | `#FFFFFF` | Clean White Surface |
| `BackgroundDark` | `#0E0E13` | Sleek Dark Mode Background |
| `TextPrimaryLight` | `#111111` | Near-Black High-Contrast Headings |
| `TextSecondaryLight` | `#666666` | Neutral Supporting Copy |
| `BorderLight` | `#E5E7EB` | Subtle Structural Divider |

---

## 📦 Binaries & Recordings

- **Debug APKs:**
  - [`apks/cloudreel-debug.apk`](apks/cloudreel-debug.apk) (18.4 MB)
  - [`apks/app-debug.apk`](apks/app-debug.apk) (18.4 MB)
- **High-Definition Screen Flow Recordings:**
  - [`videos/cloudreel_screen_flow.mp4`](videos/cloudreel_screen_flow.mp4) (H.264 / 30fps)
  - [`videos/cloudreel_screen_flow.webm`](videos/cloudreel_screen_flow.webm) (VP9 / 30fps)
  - [`videos/cloudreel_screen_flow.webp`](videos/cloudreel_screen_flow.webp) (Animated WebP)

---

## 🧪 Testing & Validation

```bash
# Run unit test suite
./gradlew test

# Build debug APK
./gradlew assembleDebug
```
