# Java Arcade Game

My first programming project — a 2D arcade-style survival game originally built with Processing and later converted into Java using Swing and AWT.

The player controls a futuristic drone and must collect elemental orbs while avoiding corrupted dark energy projectiles. The game includes collision detection, score tracking, health mechanics, keyboard/mouse controls, and a custom-rendered HUD.

---

## Features

- Real-time game loop running at 60 FPS
- Keyboard movement using WASD
- Mouse drag movement support
- Collision detection system
- Score and health system
- Game over and win conditions
- Custom HUD with timer, score, and lives
- Procedurally positioned collectibles and enemies
- Java executable (`Game.jar`)

---

## Gameplay

- Collect elemental orbs to increase your score
- Avoid dark energy projectiles that reduce health
- Reach the target score before time runs out
- Survive with your remaining lives

---

## Controls

| Action | Input |
|---|---|
| Move | W A S D |
| Mouse Movement | Drag with Left Click |

---

## How to Run

1. Make sure Java is installed
2. Double-click `Game.jar`

---

## Rebuilding the Game

If you modify the Java source files:

1. Double-click `build.command`
2. The script will automatically compile the project and rebuild `Game.jar`

---

## Project Structure

```text
src/App.java
```
Creates the game window and launches the application.

```text
src/game.java
```
Contains:
- Main game loop
- Rendering and drawing
- Collision detection
- Character movement
- Enemy/orb systems
- HUD and game state logic

```text
build.command
```
Compiles the Java source files and creates the executable `.jar` file.

---

## Technologies Used

- Java
- Java Swing
- Java AWT
- Processing concepts

---

## Author

Mohammad Rahman
