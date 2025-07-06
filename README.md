# Kalaha OpenGL - Android Mancala Game

An Android implementation of the classic Mancala board game family, featuring AI opponents with neural network evaluation and OpenGL ES 2.0 rendering.
For fun developed a version where dice is used.

## Features

### Game Variants
- **Kalaha** - Traditional variant 4-6 stones per pit
- **Oware** - Variant with special capture rules
- **Jackpot Kalaha** - Dice-based experimental variant

### AI Engine
- **Alpha-Beta Pruning** Using multithreading and neural networks

### Graphics & UI
- **OpenGL ES 2.0** Used OpenGl to render the game
- **Texture Atlas** System for sprite management

## Architecture

```
├── boardgame/           # Core game logic and rules
│   ├── algoritm/        # AI algorithms (Alpha-Beta, game trees)
│   ├── kalaha/          # Kalaha game implementation
│   ├── oware/           # Oware game implementation
│   └── jackpotkalaha/   # Kalaha with dice variant
├── androidmancala/      # Android-specific implementation
│   ├── opengl/          # OpenGL rendering engine
│   ├── animation/       # Game animations
│   └── menu/            # UI and navigation
├── mlfn/                # Neural network implementation
└── util/                # Utility classes
```

## Technical Stack

- **Language**: Java
- **Platform**: Android (API 18+)
- **Graphics**: OpenGL ES 2.0
- **AI**: Custom Alpha-Beta with neural networks
- **Build System**: Gradle

## Installation

1. Clone the repository
2. Open in Android Studio
3. Build and run on Android device/emulator
