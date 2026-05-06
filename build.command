#!/bin/sh
set -eu

cd "$(dirname "$0")"
mkdir -p bin
javac -d bin src/App.java src/game.java
jar cfm Game.jar MANIFEST.MF -C bin .

echo "Built Game.jar"
