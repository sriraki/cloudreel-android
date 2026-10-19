# CloudReel Screen Flow Recordings

This directory contains high-definition recordings of the complete navigation flow in the CloudReel Android application, showcasing the post-splash navigation redesign with Category Selection, Live Sessions, Broadcast, and Network Contribution (Cellular Bonding Mesh).

## Available Video Formats

| File | Format | Resolution | Duration | Description |
| :--- | :--- | :--- | :--- | :--- |
| **[`cloudreel_screen_flow.mp4`](file:///Users/rakeshk/Desktop/cloudreel_Mapp/videos/cloudreel_screen_flow.mp4)** | H.264 / MP4 | 1200 × 980 | 34.5s | Universally playable standard video format for macOS, Windows, iOS, Android, and web. |
| **[`cloudreel_screen_flow.webm`](file:///Users/rakeshk/Desktop/cloudreel_Mapp/videos/cloudreel_screen_flow.webm)** | VP9 / WebM | 1200 × 980 | 34.5s | High-efficiency web video format with crisp vector edges. |
| **[`cloudreel_screen_flow.webp`](file:///Users/rakeshk/Desktop/cloudreel_Mapp/videos/cloudreel_screen_flow.webp)** | Animated WebP | 800 × 653 | 34.5s | Compact animated graphic playable directly in image viewers and web browsers without video controls. |

## Screen Flow Sequence

1. **Splash Screen (0.0s – 2.5s)**
   - Centered CloudReel infinity emblem (`#D300EA` to `#3031CB` gradient)
   - "CloudReel" brand wordmark
   - Unconditional navigation to Category Selection

2. **Category Selection Screen (2.5s – 5.5s)**
   - Heading: *"How would you like to use CloudReel?"*
   - Subtitle: *"Choose your live production or participation mode below."*
   - **Two Primary Categories:**
     1. **Live Sessions**: *"Host a session or join an existing one."*
     2. **Broadcast**: *"Stream live video or contribute your mobile connection to strengthen a live stream."*

3. **Live Sessions Screen (5.5s – 8.0s)**
   - Heading: *"Live Sessions"*
   - Subtitle: *"Host or join an interactive live session."*
   - **Option A**: Start as Host (*"Create and host a live session."*)
   - **Option B**: Join as Participant (*"Join an existing live session."*)

4. **Host Entry Screen (8.0s – 10.5s)**
   - Mode: Host Mode
   - Input validation for session URL or room code

5. **Host Hub Workflow (10.5s – 13.0s)**
   - Live Room Hub (`CR-84920`) with room code copy
   - Real-time telemetry: 1080p60, 4 participants, 42.8 Mbps uplink, 18ms latency

6. **Participant Entry Screen (13.0s – 15.5s)**
   - Mode: Participant Mode
   - Input validation and join action

7. **Participant Workflow Screen (15.5s – 18.0s)**
   - Low-latency viewport with 1080p60 stream and 18ms latency badge
   - Audio mute toggle and session controls

8. **Broadcast Screen (18.0s – 21.0s)**
   - Heading: *"Broadcast"*
   - Subtitle: *"Live streaming & network contribution."*
   - **Option A**: Streamer (*"Go live and stream video."*)
   - **Option B**: Relay (*"Contribute your connection to support a live stream."*)
   - **Option C**: Network Contribution (*"Share your mobile connection to strengthen a live stream."*)
   - Real-time bonded telemetry preview card

9. **Streamer Authentication Check (21.0s – 23.5s)**
   - Prompted when unauthenticated user selects Streamer
   - Upon successful sign-in, automatically forwards directly to Streamer

10. **Streamer Screen (23.5s – 26.0s)**
    - Live camera broadcast feed with multi-network bonding telemetry

11. **Relay Screen (26.0s – 28.5s)**
    - Direct access without login requirement
    - Cellular bandwidth contribution telemetry (48.2 Mbps)

12. **Network Contribution / Cellular Bonding Mesh Screen (28.5s – 31.5s)**
    - Direct access without login requirement
    - Bonded uplink channels (5G SA + Wi-Fi 6, 42.5 Mbps)
    - Background contribution & battery saver throttling toggles
    - Contribution pause / resume control

13. **Category Selection in Dark Mode (31.5s – 34.5s)**
    - High-contrast, accessibility-compliant dark theme
