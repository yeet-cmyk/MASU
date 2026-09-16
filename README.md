# Plato Chess Assistant

Minimal native Android starter written in Kotlin.

Package: `com.masu.platochess`.

## Build

Use JDK 17 and Android SDK 35, then run:

```sh
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions builds and verifies the debug APK on every push. Download `plato-chess-assistant-debug` from the run's Artifacts section and unzip it before installing on Android 6.0 or newer.

This starter contains only a launcher screen. No chess engine, screen capture, overlay, or AI integration is implemented.
