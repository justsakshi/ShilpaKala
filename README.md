# Shilpa-Kala · ಶಿಲ್ಪ-ಕಲಾ

A digital portfolio assistant for Karnataka's wood artisans.

---

## The Problem

Wood carvers and *Gombe* makers in places like Channapatna and Kinnala produce world-class handcrafted work. But their product photos, shot casually and shared on WhatsApp, look unprofessional. This makes high-quality heritage craft look cheap to city buyers, directly hurting the artisan's income.

Shilpa-Kala fixes that.

---

## What It Does

Shilpa-Kala is a guided camera app that helps artisans take professional product photos and brand them instantly. No design skills needed. No internet required.

### Capture

A full-screen camera view with a guided framing overlay. Corner bracket guides turn green when the phone is level. A real-time bubble level bar gives tilt feedback in both portrait and landscape orientations. A rule-of-thirds grid helps with composition.

### Brand

After capture, the artisan enters their name, wood type, and price. The app generates a catalog-style branded photo with a warm cream footer, a *Handmade in Karnataka* heritage label in gold, the artisan's name in serif typography, and the wood type and price on either side.

### Save and Share

Photos are saved automatically to `Pictures/ShilpaKala/` and appear in the phone gallery. From there the artisan can share directly to WhatsApp, Instagram, or any other app in one tap.

### Kannada Language Support

The full UI is available in Kannada for artisans who don't read English. A single button toggles between the two languages instantly.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Camera | CameraX (Preview + ImageCapture) |
| Overlay | Custom View with Canvas drawing |
| Image Branding | Bitmap manipulation and Canvas |
| Storage | MediaStore API |
| Tilt Detection | Accelerometer via SensorManager |
| UI | XML Layouts with ConstraintLayout |
| Permissions | Runtime camera permission handling |

---

## Features

- Guided camera interface with framing overlay
- Real-time level and tilt guidance for portrait and landscape
- Artisan name input for personalised branding
- Wood type selection with a custom free-text option for unlisted varieties
- Price tag on the branded photo
- Heritage label: Handmade in Karnataka
- Catalog-style professional footer background
- Gallery save via MediaStore API
- Direct share to WhatsApp and Instagram
- Kannada language UI toggle

---

## Impact

This project was built around three goals. First, to enhance the perceived value of Indian handicrafts by giving artisans a tool to present their work professionally. Second, to introduce artisan communities to the basics of visual branding and digital literacy. Third, to create a direct path to higher income by helping artisans charge a premium for quality-branded work.

---

## Installation

Download `app-debug.apk` from the Releases section of this repo. On your Android phone, go to Settings, allow installation from unknown sources, then open the APK to install. No internet connection is required. The app works fully offline.

---

## Building from Source

```bash
git clone https://github.com/justsakshi/ShilpaKala.git
```

Open in Android Studio, let Gradle sync, connect an Android device or start an emulator, and run the project. Requires Android SDK 26 or above.

---

## Project Context

Built as part of the Android App Development using GenAI module under the Shilpa-Kala self-employment track. Designed specifically for artisan communities in Karnataka, India.

---

## License

MIT License. Free to use, modify, and distribute.
