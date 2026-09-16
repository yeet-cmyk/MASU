# Plato Chess Assistant

Android prototype for permitted chess analysis/training with Plato-style boards.

Current implementation includes the Android project/build pipeline plus board orientation/algebraic-square mapping and normalized board calibration based on the supplied White/Black screenshots. The local player's pieces are treated as the bottom side, so mapping automatically supports both orientations.

Next integration points are MediaProjection screen capture, on-device piece recognition, engine analysis, and the floating overlay. External assistance should only be used in games/modes where it is permitted.
