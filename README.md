# Plato Chess Assistant

Native Kotlin Android app, package `com.masu.platochess`, continued from the existing project on `main`.

## Use

1. Install the Debug APK (Android 6+; ARM64, ARMv7 and x86_64).
2. Press **بدء التحليل والفقاعة**, grant overlay permission, then press it again and approve full-screen capture.
3. Open a new chess game **before the first move**, keeping all 64 squares visible. Move the bubble above or below the board.
4. The app learns piece templates from a stable starting layout and infers your colour from the bottom pieces. No colour selector is used.
5. When a stable, legal position is recognized and it is your turn, the bubble displays Stockfish's move. Make the move yourself.
6. Stop and restart capture for a new game, changed theme, resized/rotated screen, or a lost tracking session. Use the Stop button or the capture notification to stop.

Assistance must be allowed in the game being played. No taps, accessibility automation, anti-cheat evasion, account login, ads, paid API, server, or runtime internet permission are included.

## Implementation

- User-approved MediaProjection foreground service, callback registration, explicit teardown, background frame analysis, full-screen sizing and rotation/resize stop.
- Draggable overlay with saved placement and bounded dragging.
- Automatic checkerboard calibration from the four empty middle ranks of an initial position. Three stable frames are required to learn templates.
- Per-session piece silhouettes and luminance templates; both board orientations. Confidence failures hide suggestions instead of fabricating a position.
- Canonical board, legal move validation, king safety, castling rights, en passant, all four promotions, side to move and FEN counters.
- Three-frame stabilization and unique legal one- or two-ply transitions. More than two missed plies cannot be recovered; a new session is required.
- UCI move history saved privately under `files/games`; the latest session is viewable in the app.
- Stockfish 11, compiled locally for three Android ABIs, 1 thread, 16 MiB hash and a 700 ms search. A new frame must confirm the position after search before the suggestion is displayed.

Stockfish 11 deliberately uses the compact classical evaluator without NNUE assets. It is older than current Stockfish. It runs entirely offline from the app's extracted native library directory.

## Build and verification

JDK 17, Android SDK 35/build-tools 35.0.0 and NDK 27.2.12479018 are required on Linux. `./gradlew testDebugUnitTest assembleDebug` builds native Stockfish and the app. The first build fetches the pinned upstream source revision `c3483fa9a7d7c0ffa9fcc32b467ca844cfb63790` from official-stockfish/Stockfish.

GitHub Actions runs unit tests, builds and verifies the APK signature/package, and runs Android instrumentation on an API 35 x86_64 emulator before uploading `plato-chess-assistant-debug`.

Unit tests cover initial-position perft through depth 4 (197281), Kiwipete through depth 3 (97862), castling, en passant including discovered check, underpromotion, orientation and stable one-/two-ply tracking. Android tests cover launching the activity, executing the bundled engine, legal engine responses, both orientations of synthetic board images and rejecting a blank image.

**Real Plato recognition and capture/overlay interaction have not been tested on a physical phone.** No real Plato screenshots were present in the repository. Synthetic silhouettes are only pipeline tests, not a measured recognition accuracy claim. Theme effects, highlights, selected pieces, animations, timers, overlays, capture restrictions and fast moves may prevent recognition. The app requires a visible initial position and cannot load an arbitrary mid-game screen.

## License and source

This project is distributed under GPLv3; see `LICENSE`. Stockfish copyright and GPL notices are preserved in the original source. Each APK contains `assets/Stockfish-COPYING.txt` and `assets/stockfish-source.zip` with the exact upstream source used. This repository contains the Android code and native build script; preserve access to the corresponding source when redistributing an APK.
