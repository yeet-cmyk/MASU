#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Stockfish 11: small classical evaluator, no NNUE download, GPLv3.
REV=c3483fa9a7d7c0ffa9fcc32b467ca844cfb63790
NDK="${ANDROID_NDK_HOME:-${ANDROID_HOME:?Android SDK required}/ndk/27.2.12479018}"
SRC="$PWD/app/build/stockfish-source"
OUT="$PWD/app/build/stockfish-libs"
ASSETS="$PWD/app/build/stockfish-assets"
mkdir -p "$SRC" "$OUT" "$ASSETS"
if [ ! -f "$SRC/src/main.cpp" ]; then
  git -C "$SRC" init -q
  git -C "$SRC" fetch --depth 1 https://github.com/official-stockfish/Stockfish.git "$REV"
  git -C "$SRC" checkout -q --detach FETCH_HEAD
fi
test "$(git -C "$SRC" rev-parse HEAD)" = "$REV"
git -C "$SRC" archive --format=zip --output="$ASSETS/stockfish-source.zip" HEAD
cp "$SRC/Copying.txt" "$ASSETS/Stockfish-COPYING.txt"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin"
for entry in 'arm64-v8a:aarch64-linux-android:64' 'armeabi-v7a:armv7a-linux-androideabi:32' 'x86_64:x86_64-linux-android:64'; do
  IFS=: read -r abi target bits <<< "$entry"
  mkdir -p "$OUT/$abi"
  defs=(-DNO_PREFETCH)
  if [ "$bits" = 64 ]; then defs+=(-DIS_64BIT); fi
  "$TOOLCHAIN/${target}23-clang++" -std=c++11 -O3 -DNDEBUG "${defs[@]}" \
    -fPIE -pie -static-libstdc++ -pthread -Wl,-z,max-page-size=16384 \
    "$SRC"/src/*.cpp "$SRC"/src/syzygy/tbprobe.cpp -latomic -o "$OUT/$abi/libstockfish.so"
  "$TOOLCHAIN/llvm-strip" "$OUT/$abi/libstockfish.so"
done
