# Acoustic Driver Android — Implementation Plan

## 1. Project Overview

### Goal

Build an Android application that recreates the spirit of the classic Sony Ericsson acoustic-driver ecosystem:

- Import legacy acoustic-driver files.
- Parse and normalize their tuning parameters.
- Translate supported parameters into modern Android audio processing.
- Provide the maximum practical functionality without root.
- Unlock deeper system/vendor audio functionality when root is available.
- Provide a large community-driven acoustic-driver catalog.
- Allow users to preview, install/apply, compare, rate, and manage acoustic profiles.

The application should **not claim that an Android DSP profile is identical to the original Sony Ericsson hardware driver**. Legacy acoustic parameters can be device-, codec-, amplifier-, and DSP-specific. The translator therefore needs explicit compatibility levels and approximations.

---

# 2. Product Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│                    Acoustic Driver App                       │
├──────────────────────────────────────────────────────────────┤
│ UI / Profile Browser / Device Detection / Settings           │
├──────────────────────────────────────────────────────────────┤
│ Acoustic Driver Manager                                      │
│  ├─ Import                                                   │
│  ├─ Parser                                                   │
│  ├─ Normalizer                                               │
│  ├─ Translator                                               │
│  ├─ Compatibility Analyzer                                   │
│  └─ Profile Validator                                        │
├──────────────────────────────────────────────────────────────┤
│ Audio Backend                                                │
│  ├─ No-Root Backend                                          │
│  │   ├─ Android AudioEffect                                 │
│  │   ├─ Equalizer                                            │
│  │   ├─ Dynamics Processing                                  │
│  │   ├─ Bass Boost                                           │
│  │   ├─ Virtualizer                                          │
│  │   └─ App/device-specific available effects               │
│  │                                                           │
│  └─ Root Backend                                             │
│      ├─ Root capability detection                            │
│      ├─ Vendor configuration                                 │
│      ├─ Audio effects configuration                           │
│      ├─ DSP/vendor controls                                  │
│      ├─ HAL integration                                      │
│      └─ Backup/restore                                       │
├──────────────────────────────────────────────────────────────┤
│ Driver Catalog                                               │
│  ├─ Bundled catalog                                          │
│  ├─ Community repository                                     │
│  ├─ Metadata                                                 │
│  ├─ Compatibility                                            │
│  ├─ Versioning                                               │
│  └─ Integrity/signature verification                         │
└──────────────────────────────────────────────────────────────┘
```

Android's architecture places AudioFlinger above the Audio HAL, with the HAL connecting Android audio services to device-specific audio hardware. The Effects HAL provides the system-level effect layer. Modern Android 14+ uses AIDL for the Audio HAL, while older implementations may use HIDL. [AOSP Audio HAL](https://source.android.com/docs/core/audio/implement)

---

# 3. Core Design Principle

## Acoustic Driver ≠ EQ Preset

The application must internally represent a driver as a structured set of parameters rather than immediately converting everything into EQ.

Example:

```json
{
  "driver": {
    "name": "Example Acoustic",
    "author": "Community",
    "source_device": "Sony Ericsson W810i",
    "version": "1.0"
  },

  "parameters": {
    "preamp": null,
    "eq": {},
    "bass_boost": null,
    "compressor": {},
    "limiter": {},
    "speaker_gain": null,
    "drc": {},
    "stereo": {},
    "spatial": {},
    "mic": {},
    "unknown": {}
  }
}
```

Each parameter receives a compatibility classification:

```text
NATIVE
APPROXIMATED
UNSUPPORTED
UNKNOWN
```

This allows the UI to tell the user exactly what will happen.

Example:

```text
Original Driver
-------------------------
EQ                  ✓ Native
Bass enhancement    ✓ Approximation
Compressor          ✓ Native
Hardware gain       ⚠ Approximation
DSP calibration     ✕ Unsupported
Speaker protection  ✕ Unsupported
```

---

# 4. Mode 1 — No Root

## Objective

Provide the **maximum possible acoustic-driver functionality using normal Android application permissions and public/available audio APIs**, without modifying `/system`, `/vendor`, the Audio HAL, kernel, or vendor DSP configuration.

Android exposes audio effect types including Equalizer, Bass Boost, Dynamics Processing, Virtualizer, AGC, AEC, NS and others through its audio-effect framework where supported by the device. Actual availability is device/vendor dependent. [Android AudioEffect API](https://developer.android.com/reference/kotlin/classes)

## No-Root Pipeline

```text
Legacy Acoustic Driver
        ↓
Parser
        ↓
Normalized Acoustic Model
        ↓
Compatibility Analyzer
        ↓
No-Root Translator
        ↓
Android Audio Effects
        ↓
Playback
```

## Features

### 4.1 Equalizer Translation

Translate:

- frequency bands
- gain
- Q where possible
- preamp
- low/high shelf approximations
- bass/treble parameters

into the best available Android equalizer implementation.

Fallback:

```text
Complex legacy EQ
        ↓
FIR/IIR approximation
        ↓
Available Android processing path
```

### 4.2 Bass Enhancement

Map legacy bass parameters to:

- Bass Boost
- low-frequency EQ
- low-shelf filter
- custom DSP implementation where permitted

Avoid simply increasing low frequencies when doing so would clip the signal.

### 4.3 Dynamics

Translate:

- compressor threshold
- ratio
- attack
- release
- makeup gain
- limiter threshold
- output ceiling

into Dynamics Processing where supported.

### 4.4 Loudness / Gain

Implement software gain with:

- preamp
- output gain
- headroom
- clipping prevention

Important:

**No-root mode must never promise actual amplifier gain.**

Software gain cannot increase the physical maximum output power of the amplifier.

### 4.5 Stereo / Spatial Processing

Where supported:

- stereo width
- balance
- virtualizer
- channel matrix
- mono compatibility

### 4.6 Device-Specific Effect Discovery

At startup:

```text
Scan Android AudioEffect implementations
        ↓
List supported effect types
        ↓
List implementation UUIDs
        ↓
Test capabilities
        ↓
Build device capability matrix
```

This is important because two Android phones may expose different effect implementations.

### 4.7 Output Device Profiles

Maintain independent profiles for:

```text
Built-in speaker
Wired headset
USB DAC
Bluetooth A2DP
Bluetooth LE Audio
HDMI / external
```

Not every backend can control every output.

### 4.8 No-Root Compatibility Score

Example:

```text
Sony Ericsson Driver: Mega Acoustic

No-Root Compatibility
██████████████░░ 82%

EQ                 100%
Bass                95%
Dynamics             80%
Hardware Gain        20%
DSP Calibration       0%
Speaker Protection    0%
```

---

# 5. No-Root Limitations

The application must clearly communicate these limitations.

It generally cannot:

- replace the actual kernel audio driver
- replace the vendor Audio HAL
- modify proprietary DSP firmware
- directly change amplifier hardware gain
- directly modify codec calibration registers
- access vendor-only DSP controls unless the device exposes them
- guarantee system-wide processing for every audio path
- guarantee that protected/offloaded paths pass through the application's processing

The Audio HAL is the boundary between Android's higher-level audio framework and device-specific audio hardware. [AOSP Audio HAL](https://source.android.com/docs/core/audio/implement)

## UX Rule

Never show:

> "Driver installed"

when only a software profile has been applied.

Show:

> "Acoustic profile applied"

and indicate:

> `Software compatibility: 78%`

---

# 6. Mode 2 — Root

## Objective

When root is available, unlock unsupported functionality that cannot be reached through normal application APIs.

Root mode should be designed as a **modular backend**, because Android audio implementations vary heavily between manufacturers and SoCs.

```text
Root App
   ↓
Root Capability Detector
   ↓
Device / SoC Detector
   ↓
Audio Stack Detector
   ↓
Backend Selector
   ├─ AIDL HAL
   ├─ HIDL HAL
   ├─ Vendor Audio HAL
   ├─ Vendor DSP
   ├─ Audio Effects
   └─ Device-specific implementation
```

Modern Android 14+ uses Stable AIDL for the Audio HAL; older Android releases may use HIDL. [AOSP AIDL Audio HAL](https://source.android.com/docs/core/audio/aidl-implement)

---

# 7. Root Features

## 7.1 System Audio Configuration

Where supported by the specific device:

- vendor audio effect configuration
- audio effect registration
- processing chains
- device effects
- audio policy configuration
- vendor tuning parameters

AOSP supports device-specific effects through the audio effects framework, although implementation requires the effect to exist in the appropriate vendor/system audio stack. [AOSP Audio Effects](https://source.android.com/docs/core/audio/audio-effects)

## 7.2 Vendor DSP Integration

Create vendor adapters:

```text
Qualcomm Adapter
MediaTek Adapter
Samsung Adapter
Exynos Adapter
Unisoc Adapter
Generic Adapter
```

Each adapter exposes a common interface:

```text
getCapabilities()
readProfile()
writeProfile()
applyProfile()
restoreProfile()
validateProfile()
```

Do not assume that a Qualcomm implementation can be copied to another Qualcomm phone. The exact codec, amplifier, DSP firmware, vendor HAL and configuration can still differ.

## 7.3 Vendor Configuration Discovery

Root mode can inspect known audio locations and identify:

- audio effect configuration
- vendor effect libraries
- audio policy configuration
- mixer configuration
- codec configuration
- vendor DSP configuration
- device-specific tuning files

The app should **detect rather than assume paths**.

Example conceptual scan:

```text
/system/etc/
/system_ext/etc/
/vendor/etc/
/odm/etc/
/vendor/lib/
/vendor/lib64/
```

Actual paths and permissions vary by Android version and device.

## 7.4 Backup Before Modification

Before applying any root-level change:

```text
Create backup
      ↓
Validate backup
      ↓
Apply modification
      ↓
Restart affected audio service
      ↓
Test
      ↓
Rollback automatically if failure
```

Backup metadata:

```json
{
  "device": "...",
  "android_version": "...",
  "build": "...",
  "backup_time": "...",
  "files": [],
  "hashes": {}
}
```

## 7.5 Safe Mode / Recovery

Provide:

```text
Last Known Good Profile
Emergency Disable
Restore Original
Restore Previous Profile
```

A bad audio configuration must never leave the user with an unusable audio stack.

---

# 8. Root Compatibility Levels

Root mode should still report capability instead of pretending that root means unlimited control.

```text
ROOT LEVEL 0
No root

ROOT LEVEL 1
Root detected

ROOT LEVEL 2
System/vendor configuration accessible

ROOT LEVEL 3
Audio effect configuration accessible

ROOT LEVEL 4
Vendor DSP controls detected

ROOT LEVEL 5
Device-specific deep integration
```

Example:

```text
Device:
Pixel / Snapdragon

Root capability:

✓ Vendor partition
✓ Audio effect configuration
✓ DSP interface
⚠ Codec controls
✕ Amplifier register access

Driver translation:
94%
```

---

# 9. Acoustic Driver Selector

## Objective

Create a large acoustic-driver catalog inspired by the old Sony Ericsson community ecosystem.

Main screen:

```text
┌───────────────────────────────────────────┐
│ Acoustic Drivers                          │
│                                           │
│ 🔍 Search drivers...                      │
│                                           │
│ [All] [Sony Ericsson] [Community]         │
│ [Speaker] [Headphone] [Bass] [Hi-Fi]      │
│                                           │
│ ★ Mega Acoustic                           │
│ Sony Ericsson W810i                       │
│ Community • 2010                          │
│ Compatibility: 82%                        │
│                                           │
│ ★ Clear Acoustic                          │
│ Sony Ericsson K750i                       │
│ Community • 2009                          │
│ Compatibility: 76%                        │
└───────────────────────────────────────────┘
```

---

# 10. Driver Catalog Architecture

Use a remote catalog plus bundled fallback.

```text
                ┌─────────────────┐
                │ Community Repo  │
                └────────┬────────┘
                         ↓
                  Catalog API
                         ↓
                ┌─────────────────┐
                │ Android App     │
                └────────┬────────┘
                         ↓
                Local Driver DB
                         ↓
                Cached Driver Files
```

## Bundled Catalog

The APK should contain a curated baseline catalog so the app works offline.

Include:

- driver name
- original device
- author
- source
- version
- description
- tags
- supported outputs
- translated parameters
- compatibility metadata
- SHA-256 hash
- license/permission information

Do not blindly bundle community files without checking redistribution rights.

---

# 11. Community Driver Repository

Recommended repository structure:

```text
repository/
├── index.json
├── drivers/
│   ├── sony-ericsson/
│   │   ├── k750/
│   │   ├── w800/
│   │   └── w810/
│   └── generic/
├── metadata/
│   └── devices.json
└── signatures/
```

Example driver metadata:

```json
{
  "id": "se-w810-mega-acoustic",
  "name": "Mega Acoustic",
  "manufacturer": "Sony Ericsson",
  "source_device": "W810i",
  "author": "Community",
  "version": "1.2",
  "tags": [
    "bass",
    "speaker",
    "loud",
    "classic"
  ],
  "source_format": "sony_acoustic",
  "license": "unknown",
  "sha256": "...",
  "files": [
    "acoustic.bin"
  ]
}
```

---

# 12. Catalog Synchronization

On startup:

```text
Bundled Catalog
      ↓
Check remote index
      ↓
Compare catalog version
      ↓
Download metadata
      ↓
Download only requested drivers
      ↓
Verify SHA-256
      ↓
Store locally
```

Do not download every driver automatically.

Use:

```text
Catalog metadata: small
Driver file: downloaded on demand
```

This keeps the application lightweight.

---

# 13. Community Contributions

Provide a contribution format:

```text
Upload Driver
      ↓
Metadata
      ↓
Original Device
      ↓
Author
      ↓
License
      ↓
Original File
      ↓
Automatic Parser
      ↓
Compatibility Analysis
      ↓
Community Review
      ↓
Publish
```

Potential statuses:

```text
Official
Community Verified
Community
Experimental
Unparsed
Unsafe / Rejected
```

---

# 14. Security

This is critical because the application will eventually support root-level modification.

## Driver Files

Never execute downloaded driver files.

Treat them as data.

```text
Download
  ↓
Hash verification
  ↓
Format validation
  ↓
Parser sandbox
  ↓
Parameter normalization
  ↓
Compatibility analysis
```

## Root Modifications

Never execute arbitrary shell commands supplied by a remote driver.

Remote drivers should contain **declarative parameters**, not arbitrary commands.

Bad:

```json
{
  "command": "rm -rf ..."
}
```

Good:

```json
{
  "parameters": {
    "eq": [...],
    "compressor": {...}
  }
}
```

Device-specific root adapters should be shipped as trusted application code, not downloaded executable payloads.

---

# 15. Driver Translation Engine

Create a common intermediate representation:

```text
Legacy Driver
      ↓
Legacy Parser
      ↓
Acoustic Intermediate Representation (AIR)
      ↓
Target Backend Translator
      ├─ Android No-Root
      ├─ Android Root
      ├─ Qualcomm
      ├─ MediaTek
      └─ Future backends
```

## AIR Example

```json
{
  "sample_rate": 48000,

  "gain": {
    "preamp_db": -2.0,
    "output_db": 1.0
  },

  "eq": [
    {"frequency": 60, "gain_db": 3.5, "q": 0.7},
    {"frequency": 120, "gain_db": 2.0, "q": 0.8},
    {"frequency": 1000, "gain_db": -1.0, "q": 1.0},
    {"frequency": 8000, "gain_db": 2.5, "q": 0.8}
  ],

  "compressor": {
    "threshold_db": -12,
    "ratio": 2.0,
    "attack_ms": 10,
    "release_ms": 100
  },

  "limiter": {
    "ceiling_db": -1
  }
}
```

---

# 16. Translation Rules

Every legacy parameter should have a translator:

```text
translate(parameter, target_backend)
```

Result:

```json
{
  "status": "APPROXIMATED",
  "confidence": 0.84,
  "target": "DynamicsProcessing",
  "loss": "Hardware-specific gain unavailable"
}
```

Possible statuses:

```text
EXACT
NATIVE
APPROXIMATED
PARTIAL
UNSUPPORTED
UNKNOWN
```

This becomes one of the application's core technologies.

---

# 17. Profile Preview

Before applying:

```text
Driver:
Mega Acoustic

Translation:

✓ 12 EQ parameters
✓ Bass enhancement
✓ Compressor
✓ Limiter

⚠ Speaker hardware gain converted to software gain
✕ Proprietary DSP calibration unavailable

Estimated compatibility:
82%

[Preview] [Apply]
```

---

# 18. A/B Testing

Provide:

```text
A = Stock
B = Acoustic Driver

[ A ] [ B ]

Volume Match: ON
```

Volume matching is important because a louder profile can appear subjectively "better" even if its frequency response is worse.

---

# 19. Measurement Mode

Future feature:

Use the phone microphone to measure the output.

```text
Play test signal
       ↓
Phone microphone
       ↓
Frequency response
       ↓
Compare:
Original
vs
Translated driver
```

Possible visual:

```text
Frequency Response

dB
 ↑
 │       Original
 │      ╱──────╲
 │ ────╱        ╲────
 │
 │       Translated
 │     ╱──────────╲
 └────────────────────→ Hz
```

This allows the translator to automatically optimize its approximation.

Important limitation: the phone microphone itself has a frequency response and should not be treated as laboratory measurement equipment.

---

# 20. Device Profiles

Store device information:

```json
{
  "manufacturer": "...",
  "model": "...",
  "soc": "...",
  "android_version": "...",
  "audio_hal": "...",
  "effects": [],
  "outputs": []
}
```

The translator can then choose:

```text
Driver
   +
Device Profile
   ↓
Best Translation
```

This is much better than using one universal translation.

---

# 21. App Screens

## Home

```text
Current Driver
──────────────
Mega Acoustic

Compatibility
82%

[Disable]
[Change Driver]
```

## Driver Browser

```text
Search
Filters
Categories
Community rating
Compatibility
Original device
```

## Driver Detail

```text
Mega Acoustic

Sony Ericsson W810i
Community

Bass       █████████
Loudness   ████████
Clarity    ███████

No-root compatibility: 82%
Root compatibility:    97%

[Preview]
[Apply]
```

## Translation Report

```text
Supported             17
Approximated            6
Unsupported             3

Compatibility          82%
```

## Root Dashboard

```text
Root detected ✓

Audio HAL
AIDL ✓

Vendor DSP
Detected ✓

Backup
[Create Backup]

Current profile
Mega Acoustic

[Apply Root Profile]
[Restore]
```

---

# 22. Recommended Technology Stack

## Android

Prefer:

- Kotlin
- Jetpack Compose
- Android Media APIs
- NDK/C++ only where required
- Room/SQLite for local driver database
- WorkManager for catalog synchronization

## Parser

Use:

```text
Kotlin / Java
```

for normal formats.

Use:

```text
C++ / Rust
```

for complex binary reverse-engineering/parsing if needed.

## Driver Format

Internally normalize everything into:

```text
AIR — Acoustic Intermediate Representation
```

Use JSON for debugging/export and a compact binary/database representation for production if needed.

---

# 23. Development Phases

## Phase 1 — Audio Engine

- Detect Android audio effects.
- Build capability scanner.
- Implement EQ.
- Implement gain/headroom.
- Implement bass enhancement.
- Implement dynamics.
- Implement limiter.
- Build profile system.

**Deliverable:** standalone Android acoustic profile engine.

---

## Phase 2 — Acoustic Parser

- Research legacy Sony Ericsson formats.
- Implement parser.
- Create AIR.
- Build parameter mapping.
- Implement translation report.
- Add unit tests against known driver files.

**Deliverable:** legacy acoustic → AIR → Android profile.

---

## Phase 3 — No-Root Product

- Compose UI.
- Driver browser.
- Profile selector.
- Apply/remove.
- A/B testing.
- Device capability detection.
- Compatibility score.
- Offline bundled drivers.

**Deliverable:** fully usable no-root application.

---

## Phase 4 — Community Catalog

- Repository format.
- Catalog API.
- Metadata synchronization.
- On-demand downloads.
- Hash verification.
- Search/filter.
- Community metadata.
- Contribution workflow.

**Deliverable:** large acoustic-driver ecosystem.

---

## Phase 5 — Root Engine

- Root detection.
- Device/vendor detection.
- Backup/restore.
- Vendor configuration discovery.
- Audio effect configuration.
- Device-specific adapters.
- Rollback mechanism.

**Deliverable:** root-enhanced acoustic engine.

---

## Phase 6 — Deep Vendor Integration

Prioritize devices/platforms based on community demand:

```text
1. Qualcomm
2. MediaTek
3. Samsung/Exynos
4. Other SoCs
```

For each platform:

```text
Detection
↓
Audio stack discovery
↓
Capability model
↓
Safe read-only inspection
↓
Backup
↓
Write support
↓
Rollback
```

---

# 24. Testing Strategy

## Automated

Test:

- parser correctness
- AIR conversion
- parameter mapping
- clipping
- gain/headroom
- malformed drivers
- catalog hashes
- compatibility calculations

## Device Testing

Minimum matrix:

```text
Android 11
Android 12
Android 13
Android 14
Android 15+
```

and multiple:

```text
Qualcomm
MediaTek
Samsung
```

devices.

## Audio Testing

Test:

- speaker
- wired headphone
- USB DAC
- Bluetooth
- Bluetooth LE where available

---

# 25. Important Technical Constraints

The project must never assume:

> "Root = full hardware control."

Root only gives access to areas that the particular device exposes and permits modifying.

Likewise:

> "No root = only EQ."

No-root mode should aggressively use every supported Android audio-effect capability available on the device, while clearly distinguishing native support from approximation.

AOSP's audio-effects architecture supports effect implementations through the Effects HAL, and Android can expose device-specific effects when the vendor implementation provides them. [AOSP Audio Effects](https://source.android.com/docs/core/audio/audio-effects)

---

# 26. Long-Term Goal

The final product should feel like a modern version of the old Sony Ericsson acoustic-driver culture:

```text
OLD

Acoustic Driver
      ↓
Flash / Replace
      ↓
New Sound


MODERN

Acoustic Driver
      ↓
Universal Parser
      ↓
Device-Aware Translator
      ↓
┌───────────────────────────┐
│ No Root                   │
│ Android DSP approximation │
└───────────────────────────┘
              OR
┌───────────────────────────┐
│ Root                      │
│ Vendor / DSP integration  │
└───────────────────────────┘
      ↓
Device-specific tuning
```

The differentiating technology is **not the EQ itself**.

The key technology is:

> **Legacy Acoustic Driver → Acoustic Intermediate Representation → Device-aware Android translation**

That architecture allows the project to continuously add new Sony Ericsson driver formats, community drivers, Android devices, SoCs, and root backends without rewriting the entire application.

---

# 28. Modern UI / UX Direction

## Objective

The application should have a **modern, clean visual design without sacrificing advanced audio controls**.

The design should avoid the traditional "audio utility app" look where every parameter is exposed simultaneously.

Instead:

> **Simple at first glance, powerful when opened.**

Use progressive disclosure:

```text
Simple overview
      ↓
Tabs
      ↓
Detailed controls
      ↓
Advanced parameters
```

## Visual Principles

- Dark-first modern interface.
- Large typography hierarchy.
- Card-based sections.
- Rounded controls.
- Minimal borders.
- Clear spacing.
- Subtle animations.
- Responsive layout for phones/tablets.
- Avoid excessive gradients and decorative elements.

Advanced features remain accessible through Basic / Advanced / Expert levels rather than being removed.

---

# 29. Main Tab Navigation

Recommended primary tabs:

```text
┌─────────────────────────────────────────────────┐
│  Drivers   Equalizer   Effects   Device   More  │
└─────────────────────────────────────────────────┘
```

For phones, use bottom navigation:

```text
Home | Drivers | EQ | Effects | Device
```

For tablets/foldables, use a navigation rail.

---

# 30. Home Tab

The Home tab provides a quick overview instead of exposing every setting.

```text
┌──────────────────────────────────┐
│ Acoustic                         │
│                                  │
│ Mega Acoustic                    │
│ Sony Ericsson W810i              │
│                                  │
│ Compatibility       82%          │
│                                  │
│        [ Enabled ]               │
└──────────────────────────────────┘

Output
Speaker

Quick Effects
[Bass] [Virtualizer] [Compressor]

[Open Equalizer]
[Change Driver]
```

---

# 31. Drivers Tab

The Drivers tab is the acoustic-driver ecosystem.

Features:

- Search.
- Categories.
- Device filtering.
- Community filtering.
- Favorites.
- Recently used.
- Download status.
- Compatibility score.
- Driver version.
- Author.
- Rating.
- Translation report.

Example:

```text
Drivers

🔍 Search acoustic drivers...

Featured
────────────────────────

★ Mega Acoustic
Sony Ericsson W810i
82% No Root / 97% Root

★ Clear Bass
Sony Ericsson K750i
76% No Root / 94% Root

Categories

Sony Ericsson
Community
Bass
Speaker
Headphone
Hi-Fi
Experimental
```

---

# 32. Equalizer Tab

The application should also function as a **full modern equalizer application**, independently of the acoustic-driver system.

Users can manually tune audio without importing an acoustic driver.

Provide:

- 5-band mode.
- 10-band mode.
- 15/20-band advanced mode where supported.
- Parametric EQ mode for advanced users.
- Preamp.
- Gain per band.
- Frequency.
- Q.
- Filter type where supported.

Example:

```text
Equalizer

Preamp                         -2.0 dB

+12 ┤
    │       ╭──╮
 +6 ┤   ╭───╯  ╰────╮
  0 ┼───┤            ├────
 -6 ┤   ╰────────────╯
-12 ┤
    └──────────────────────
      60  120  250  500  1k  2k  4k  8k  16k

[ Flat ] [ Bass ] [ Vocal ] [ Custom ]
```

## Parametric EQ

Advanced users can create filters such as:

```text
Filter 1
Type: Low Shelf
Freq: 80 Hz
Gain: +3 dB
Q: 0.70

Filter 2
Type: Peak
Freq: 1.2 kHz
Gain: -2 dB
Q: 1.20

Filter 3
Type: High Shelf
Freq: 8 kHz
Gain: +2 dB
Q: 0.70
```

---

# 33. Effects Tab

The Effects tab contains audio processing independent of the acoustic-driver system.

Recommended effects:

```text
Bass Boost
Virtualizer
Compressor
Limiter
Loudness
Stereo Width
Balance
Channel Mixer
Preamp
Dynamics
Noise Reduction
Spatial Audio
```

Availability must depend on the Android device/backend.

---

# 34. Bass Boost

Controls can include:

```text
Enabled
Strength
Frequency
Amount
```

The app should distinguish between Android BassBoost, custom DSP bass processing, and vendor DSP bass processing.

---

# 35. Virtualizer / Spatial

Controls can include:

```text
Enabled
Strength
Stereo Width
Room / Spatial amount
```

Where supported, expose speaker virtualization, headphone virtualization, and spatial audio separately.

Do not expose parameters that the active backend cannot actually control.

---

# 36. Compressor

Normal-user UI:

```text
Compressor

Enabled

Threshold       -12 dB
Ratio            2.0 : 1
Attack           10 ms
Release         100 ms
Makeup Gain      +2 dB

[Advanced]
```

Advanced parameters may include knee, lookahead, detector, sidechain, and channel linking when supported.

---

# 37. Limiter

Controls:

```text
Enabled
Ceiling
Threshold
Release
Lookahead
```

Preferred conceptual chain:

```text
Input
 ↓
Preamp
 ↓
EQ
 ↓
Bass
 ↓
Dynamics / Compressor
 ↓
Virtualization / Spatial
 ↓
Limiter
 ↓
Output
```

Actual processing order is backend-dependent and must be reported accurately.

---

# 38. Audio Effect Rack

Provide a visual signal chain:

```text
Audio Chain

┌────────────┐
│   Preamp   │
└─────┬──────┘
      ↓
┌────────────┐
│     EQ     │
└─────┬──────┘
      ↓
┌────────────┐
│ Bass Boost │
└─────┬──────┘
      ↓
┌────────────┐
│ Compressor │
└─────┬──────┘
      ↓
┌────────────┐
│ Virtualizer│
└─────┬──────┘
      ↓
┌────────────┐
│  Limiter   │
└────────────┘
```

Users can enable/disable effects, open parameters, reset effects, reorder effects where supported, and save the chain as a preset.

If the backend fixes processing order, the UI must clearly indicate that the order is fixed.

---

# 39. Device Tab

Show what the current phone can actually do.

```text
Device

Pixel / Snapdragon

Android
15

Audio Backend
AIDL Audio HAL

Available Effects

✓ Equalizer
✓ Bass Boost
✓ Dynamics Processing
✓ Loudness
✓ Virtualizer
⚠ Vendor DSP
✕ Hardware Gain
```

The app must not pretend every phone supports every feature.

---

# 40. Output Selection

Allow separate configurations for:

```text
● Phone Speaker
○ Wired Headphones
○ USB DAC
○ Bluetooth
○ Bluetooth LE
```

Each output can have its own acoustic driver, EQ, effects, and preset.

Example:

```text
Speaker
  Mega Acoustic
  + Bass Boost

Headphones
  Neutral EQ
  No Virtualizer

Bluetooth
  Custom EQ
  Loudness
```

---

# 41. Preset System

Three preset types:

```text
ACOUSTIC DRIVER
Imported/community legacy tuning.

AUDIO PRESET
Manual EQ/effects configuration.

DEVICE PRESET
Device/output-specific complete configuration.
```

Example:

```text
★ W810 Mega Bass
  Acoustic Driver

★ Daily Headphones
  10-band EQ + Compressor

★ Gaming
  EQ + Spatial + Compressor

★ Speaker Loud
  Bass + Loudness + Limiter
```

---

# 42. Driver + Manual Override

Users should be able to modify a translated acoustic driver without destroying its original data.

```text
Mega Acoustic
        ↓
Android translation
        ↓
[ Edit ]
        ↓
Translated parameters
        ↓
User Overrides
```

Preserve three layers:

```text
Original Driver
+
Translated Parameters
+
User Overrides
```

---

# 43. Translation Inspector

Advanced users can inspect exactly how a legacy acoustic driver was converted.

```text
Translation Inspector

Legacy Parameter
────────────────────────────
Speaker Gain: +4

No Root
→ Software Gain: +4
→ Status: APPROXIMATED

Root
→ Vendor Gain: +4
→ Status: NATIVE

DSP Calibration

No Root
→ UNSUPPORTED

Root
→ Vendor DSP
→ Status: NATIVE
```

This is a core transparency feature.

---

# 44. Quick Controls

Provide a compact panel for common adjustments:

```text
Quick Controls

Bass       ─────●──── +3
Treble     ───────●── +1
Loudness   ─────●────
Spatial    ───●──────
Compressor ─────●────

[Open Full Controls]
```

This lets normal users adjust sound without entering advanced screens.

---

# 45. Advanced / Expert Mode

Expert Mode progressively exposes technical parameters.

```text
Settings → Interface → Expert Mode

OFF
Basic controls only

ON
Full DSP parameters
```

Possible expert controls:

- Q.
- Filter topology.
- Attack/release.
- Lookahead.
- Channel routing.
- DSP sample rate.
- Processing order.
- Vendor-specific parameters.
- Raw translated parameter values.

---

# 46. Modern Theme System

Support:

```text
System
Dark
Light
```

Recommended default: Dark.

Use a restrained visual system with clear surfaces, typography, accent, warning, error, and success states. Avoid making the interface depend on a single flashy color scheme.

---

# 47. Responsive Layout

### Phone

Use bottom navigation:

```text
Home | Drivers | EQ | Effects | Device
```

### Tablet / Foldable

Use a navigation rail with expanded content.

### Desktop / Emulator

Use expanded navigation and multi-panel layout.

---

# 48. UX Rule — Simple Surface, Complete Engine

> **Hide complexity, don't remove capability.**

Normal user:

```text
Driver
EQ
Bass
Spatial
Compressor
```

Advanced user:

```text
Parametric EQ
DSP chain
Dynamics
Limiter
Translation inspector
```

Expert/root user:

```text
HAL
Vendor DSP
Device configuration
Raw parameters
Backup / restore
```

All three should use the same underlying audio-engine architecture.

---

# 49. Updated Application Information Architecture

```text
                    Acoustic Driver App
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
        Drivers             EQ              Effects
          │                 │                 │
 Legacy Drivers       Parametric EQ       Bass Boost
 Community Drivers    Graphic EQ          Virtualizer
 Bundled Drivers      Presets             Compressor
 Downloads             Preamp              Limiter
 Favorites                                  Loudness
                                            Spatial
                            │
                            │
                         Device
                            │
                    Capability Detection
                    Output Selection
                    Audio Backend
                    Root Status
                            │
                            ▼
                    Translation Engine
                            │
                  ┌─────────┴─────────┐
                  │                   │
               No Root              Root
                  │                   │
            Android Effects      Vendor / DSP
                  │                   │
                  └─────────┬─────────┘
                            ▼
                       Audio Output
```

---

# 50. Updated Product Positioning

The application should not be marketed only as a Sony Ericsson Acoustic Driver Emulator.

It should be positioned as:

> **A modern Android audio tuning platform inspired by the classic acoustic-driver modding scene.**

The legacy acoustic-driver system becomes one major feature inside a larger audio platform:

```text
                 MODERN AUDIO PLATFORM

        ┌──────────────────────────────┐
        │     Acoustic Driver Engine   │
        ├──────────────────────────────┤
        │ EQ                           │
        │ Bass Boost                   │
        │ Compressor                   │
        │ Limiter                      │
        │ Virtualizer / Spatial       │
        │ Loudness                     │
        │ Custom DSP                   │
        │ Device Profiles              │
        │ Community Drivers            │
        ├──────────────────────────────┤
        │ No Root       │ Root         │
        └──────────────────────────────┘
```

This makes the application useful even for users who have never heard of Sony Ericsson acoustic drivers, while preserving the original community/modding concept as the unique differentiator.
