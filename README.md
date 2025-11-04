# Rhetorix - AI-Powered Debate Gaming Platform

A competitive debate platform where players sharpen their critical thinking skills through
AI-powered and peer-to-peer debates.

## Project Overview

**Hackathon Project**: Bringing back the art of debate through gamification and AI

### The Problem

- Debates are declining in modern society
- People are losing critical thinking and argumentation skills
- Lack of platforms to practice structured debates

### Our Solution

Rhetorix - A mobile debate gaming platform that:

- Gamifies debates to make them engaging
- Uses AI for realistic practice opponents and fair judging
- Provides detailed feedback to improve skills
- Offers competitive matchmaking against real players

---

## Features

### Authentication System

- Login / Sign Up with beautiful dark theme UI
- User profiles with player stats

### Game Modes

#### AI Mode (3 Difficulty Levels)

- **Beginner**: Easy debates for newcomers
- **Intermediate**: Moderate challenge
- **Advanced**: Intense debates for masters

#### P2P Mode

- Real-time debates against other players
- Competitive matchmaking
- Global leaderboards

### Debate Mechanics

- Random topic assignment
- Random side assignment (For/Against)
- 30-second prep time
- 10 minutes per player (20 min total)
- Turn-based chat interface
- Real-time AI judging

### Scoring & Feedback

AI judges debates on:

- Logic & Reasoning (1-10)
- Evidence Quality (1-10)
- Tone & Respect (1-10)
- Counter-Arguments (1-10)
- Factual Accuracy (1-10)

Post-match detailed feedback on:

- Strengths and weaknesses
- Behavior analysis
- Improvement suggestions
- Historical performance tracking

---

## Tech Stack

- **Platform**: Android (Kotlin)
- **UI Framework**: Jetpack Compose
- **AI Integration**: RunAnywhere SDK (On-Device LLMs)
- **Architecture**: MVVM
- **Real-time**: Firebase (for P2P mode)

---

## Quick Start

### Prerequisites

- Android Studio (Latest version)
- JDK 17 or higher
- Android device with ARM64 architecture
- Minimum Android 7.0 (API 24)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/rhetorix-debate.git
cd rhetorix-debate
```

2. **Open in Android Studio**
    - File → Open → Select project folder

3. **Build the project**

```bash
./gradlew assembleDebug
```

4. **Run on physical device**
    - Connect Android phone via USB
    - Enable USB Debugging
    - Click Run ▶️ in Android Studio

### First Time Setup

1. Launch app
2. Sign up / Login
3. Download AI model (recommended: SmolLM2 360M - 119MB)
4. Load the model
5. Choose game mode and start debating!

---

## Screens

### Completed

- [x] Authentication Screen (Login/Signup)
- [x] Main Menu (Mode Selection)
- [x] Model Management

### In Progress

- [ ] Debate Screen (Turn-based chat)
- [ ] Timer System
- [ ] AI Opponent Integration
- [ ] AI Judge System
- [ ] Results & Feedback Screen
- [ ] P2P Matchmaking

---

## Project Structure

```
app/src/main/java/com/runanywhere/startup_hackathon20/
├── AuthScreen.kt           # Login/Signup UI
├── MainMenuScreen.kt       # Game mode selection
├── DebateScreen.kt         # Main debate interface (WIP)
├── DebateViewModel.kt      # Debate logic & AI integration (WIP)
├── DebateModels.kt         # Data classes and topics
├── MainActivity.kt         # Navigation & app entry
├── ChatViewModel.kt        # Original chat (to be refactored)
└── MyApplication.kt        # SDK initialization
```

---

## Team

- **Developer 1**: [Your Name]
- **Developer 2**: [Teammate Name]

---

## Design

- **Primary Color**: Cyan (#00d9ff)
- **Background**: Dark gradient (#1a1a2e → #0f0f1e)
- **Theme**: Dark mode
- **Font**: System default (Roboto)

---

## Debate Topics

### Beginner (5 topics)

- Social media does more harm than good
- Students should wear school uniforms
- Video games cause violence
- Fast food should be banned
- Homework should be abolished

### Intermediate (5 topics)

- AI will replace human jobs
- Climate change is biggest threat
- Universal basic income
- Genetic engineering in humans
- Democracy is the best government

### Advanced (5 topics)

- Cryptocurrency will replace traditional currency
- Space exploration vs Earth problems
- Consciousness and philosophical zombies
- Quantum computing threatens cybersecurity
- Transhumanism and human evolution

---

## Development

### Running Locally

```bash
./gradlew assembleDebug
```

### Testing on Device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Viewing Logs

```bash
adb logcat | grep "Rhetorix"
```

---

## Known Issues

- Emulator requires ARM64 architecture (use physical device)
- First model download requires internet
- Large models (1GB+) may cause memory issues on low-end devices

---

## License

This project uses the RunAnywhere SDK. See SDK license for details.

---

## Acknowledgments

- RunAnywhere SDK for on-device AI capabilities
- HuggingFace for pre-trained models
- Material Design for UI components

---

## Contact

For questions or feedback:

- GitHub Issues: [Create an issue](https://github.com/YOUR_USERNAME/rhetorix-debate/issues)
- Email: your.email@example.com

---

**Made with for [Hackathon Name]**

*Bringing back the art of debate, one conversation at a time.*

