# Plato Chess Assistant

Android chess analysis/training prototype.

Implemented: Android/Gradle CI base, draggable floating overlay, screen-capture permission, foreground MediaProjection frame stream, screenshot-calibrated Plato board crop, 64-square feature extraction, automatic bottom-side color inference, orientation/algebraic mapping, FEN and UCI protocol models, board stabilization helpers, and an initial-position offline suggestion.

The remaining blocker for arbitrary live positions is reliable classification of all 12 piece types from Plato frames plus a bundled UCI engine binary. The current code must not be represented as a complete arbitrary-position engine yet. It never automates taps/moves. Use assistance only where allowed.

Build trigger: 2026-09-16T22:42+03:00
