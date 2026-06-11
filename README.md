# QR & Barcode Scanner — Android App

A native Android implementation of the provided `qr-scanner-ui.html` design, built
with **Kotlin + Jetpack Compose**. It reproduces the three screens (Scan, Create,
History) and the floating bottom navigation pixel-for-pixel, and adds real,
working functionality:

- **Live scanning** of QR codes and 1D/2D barcodes via **CameraX + ML Kit** (on-device, offline).
- **Result bottom sheet** that slides up over the camera with real **Open / Copy / Share** actions.
- **Scan from gallery** — pick an image and decode any code inside it.
- **Torch, camera flip, and zoom** controls.
- **Type filter chips** (QR code / Barcode / All types).
- **Create** screen that generates a real QR code (ZXing) live as you type and saves it to the gallery.
- **History** screen with grouped entries and live search; new scans are added automatically.

## Design fidelity

The palette, the red laser-sweep viewfinder, the two-mood (dark camera / light
content) layout, the type chips, the result sheet, and the floating dark nav all
mirror the original mock. Colours are taken directly from the HTML `:root`
variables (see [Color.kt](app/src/main/java/com/example/qrscanner/ui/theme/Color.kt)).

> **Fonts:** the mock uses *Inter* and *JetBrains Mono*. To keep the app fully
> offline the project uses the system sans-serif / monospace substitutes. For an
> exact match, drop `inter_*.ttf` and `jetbrains_mono_*.ttf` into `app/src/main/res/font`
> and update [Type.kt](app/src/main/java/com/example/qrscanner/ui/theme/Type.kt) — it is a two-line change.

## Requirements

- **Android Studio** (Ladybug / 2024.2 or newer) with **JDK 17+**.
- **Android SDK 35** (compileSdk 35), build-tools 35.
- A device or emulator running **Android 7.0 (API 24)** or newer with a camera.

## Build & run

### Option A — Android Studio (easiest)
1. `File ▸ Open…` and select this folder.
2. Let Gradle sync (it downloads dependencies on first run).
3. Pick a device/emulator and press **Run ▶**.

### Option B — command line
```bash
# from the project root
./gradlew assembleDebug          # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # installs onto a connected device/emulator
```
If `gradlew` is missing, generate it once with `gradle wrapper` (or just open the
project in Android Studio, which creates it automatically).

## Permissions

- **Camera** — requested at runtime the first time you open the Scan tab.
- **Storage** (only on Android 9 and below) — requested when you save a generated QR.

## Project layout

```
app/src/main/java/com/example/qrscanner/
├─ MainActivity.kt            # hosts the three screens + floating bottom nav
├─ AppViewModel.kt            # camera state, scan history, active result
├─ model/ScanModels.kt        # ScanKind / ClassifiedScan / ScanEntry + Wi-Fi parser
├─ util/
│  ├─ ContentClassifier.kt    # raw text  ->  type + heading + primary action
│  └─ ImageSaver.kt           # save generated QR into the gallery
├─ scanner/
│  ├─ BarcodeAnalyzer.kt      # ML Kit analyzer + gallery decoder + format names
│  └─ CameraPreview.kt        # CameraX PreviewView binding (torch/lens/zoom)
├─ generator/QrGenerator.kt   # ZXing QR bitmap generation
└─ ui/
   ├─ theme/                  # Color / Type / Theme
   ├─ components/             # Viewfinder, ResultSheet, BottomNav
   └─ screens/               # ScanScreen, CreateScreen, HistoryScreen
```

## Tech stack

| Concern | Library |
|---|---|
| UI | Jetpack Compose (Material 3) |
| Camera | CameraX 1.4 |
| Scanning | ML Kit Barcode Scanning 17.3 |
| QR generation | ZXing core 3.5.3 |
| Language | Kotlin 2.0.21 |
