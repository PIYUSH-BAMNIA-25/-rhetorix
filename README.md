# 🎯 Rhetorix - AI-Powered Competitive Debate Platform

> **Bringing back the art of structured debate through AI and gamification**

A revolutionary mobile debate gaming platform where players sharpen critical thinking,
argumentation, and persuasion skills through AI-powered debates and real-time peer-to-peer
competitions.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)

---

## 📖 Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [How It Works](#how-it-works)
- [Game Modes](#game-modes)
- [Scoring System](#scoring-system)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Screenshots](#screenshots)
- [Roadmap](#roadmap)
- [Team](#team)
- [License](#license)

---

## 🎮 About the Project

### **The Problem**

In today's digital age, we're witnessing a decline in structured debates and critical thinking:

- 📉 Debates are becoming rare in modern society
- 🧠 People are losing argumentation and reasoning skills
- 💬 Social media encourages shallow discussions over deep debates
- 🎓 Limited platforms to practice structured argumentation

### **Our Solution**

**Rhetorix** is a mobile debate gaming platform that:

- 🎮 **Gamifies debates** - Makes structured debates engaging and competitive
- 🤖 **AI-Powered** - Uses on-device LLMs for realistic opponents and fair judging
- 📊 **Real-time Feedback** - Provides detailed analysis to improve skills
- 🏆 **Competitive** - Offers P2P matchmaking and global leaderboards
- 📱 **Accessible** - Runs entirely on-device, no cloud dependency

---

## ✨ Features

### 🔐 **Authentication System**

- Email/Password authentication with Supabase
- User profiles with persistent stats
- Player rankings and history tracking
- Beautiful dark-themed UI with gradient effects

### 🤖 **AI Mode - Practice Against AI**

**3 Difficulty Levels:**

- **🟢 Beginner**: Easy debates for newcomers (simple arguments)
- **🟡 Intermediate**: Moderate challenge with structured reasoning
- **🔴 Advanced**: Intense debates with complex counter-arguments

**Features:**

- On-device AI opponent (Llama 3.2 1B)
- Real-time AI judging with detailed feedback
- Turn-based debate system
- 30-second prep time + 15-minute debate time
- Random topic & side assignment

### 👥 **P2P Mode - Compete Against Real Players**

**Matchmaking System:**

- Real-time matchmaking queue
- Automatic opponent pairing
- Session-based debate management
- Forfeit detection and handling

**Debate Features:**

- Turn-based message exchange
- Live score tracking for both players
- AI judges both players' arguments
- Real-time status updates (Your turn / Opponent's turn)
- 15-minute countdown timer with visual progress bar

### 📊 **Scoring & Judging System**

**AI Judge evaluates on:**

- 🧠 Logic & Reasoning (0-10 points)
- 📚 Evidence Quality (0-10 points)
- 🗣️ Tone & Respect (0-10 points)
- ⚔️ Counter-Arguments (0-10 points)
- ✅ Factual Accuracy (0-10 points)

**Post-Debate Feedback:**

- Strengths and weaknesses analysis
- Turn-by-turn score breakdown
- Behavioral insights
- Historical performance trends
- Win/Loss statistics

### 🏆 **Leaderboard & Stats**

- Global player rankings
- Win/loss ratios
- Average debate scores
- Total debates completed
- Performance analytics

### ⏱️ **Timer System**

- 30-second preparation phase
- 15-minute debate countdown
- Visual progress bar with color coding (green → yellow → red)
- Auto-end when time expires

### 📜 **Dynamic Topics**

**60+ debate topics across 6 categories:**

- 🎓 Education & Learning
- 💼 Technology & Innovation
- 🌍 Social Issues & Ethics
- 💰 Economics & Inequality
- 🏛️ Politics & Governance
- 🌱 Environment & Sustainability

**3 difficulty tiers:**

- Beginner (20 topics)
- Intermediate (20 topics)
- Advanced (20 topics)

---

## 🛠️ Tech Stack

### **Frontend**

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Compose Navigation
- **State Management**: Kotlin Flows & StateFlow

### **Backend & Database**

- **Backend**: Supabase (PostgreSQL)
- **Authentication**: Supabase Auth
- **Real-time**: Supabase Realtime (for P2P updates)
- **Database**: PostgreSQL with row-level security

### **AI Integration**

- **SDK**: RunAnywhere SDK
- **Engine**: llama.cpp (ARM optimized)
- **Models**:
    - Llama 3.2 1B Instruct (Q6_K quantized)
    - SmolLM2 360M/1.7B
- **Inference**: 100% on-device, no cloud

### **Dependencies**

```kotlin
// Core Android
androidx.core:core-ktx:1.12.0
androidx.activity:activity-compose:1.8.2

// Jetpack Compose
androidx.compose.ui:ui:1.6.0
androidx.compose.material3:material3:1.2.0

// Supabase
io.github.jan-tennert.supabase:postgrest-kt:2.0.0
io.github.jan-tennert.supabase:realtime-kt:2.0.0
io.github.jan-tennert.supabase:gotrue-kt:2.0.0

// RunAnywhere SDK
com.runanywhere:sdk-android:1.0.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
```

---

## 🏗️ Architecture

### **MVVM Pattern**

```
┌─────────────────┐
│   Composables   │  ← UI Layer (Screens)
└────────┬────────┘
         │
┌────────▼────────┐
│   ViewModels    │  ← Presentation Layer (State Management)
└────────┬────────┘
         │
┌────────▼────────┐
│   Repositories  │  ← Data Layer (Business Logic)
└────────┬────────┘
         │
┌────────▼────────────────────┐
│  Services (Supabase, AI)    │  ← Data Sources
└─────────────────────────────┘
```

### **Key Components**

**ViewModels:**

- `DebateViewModel` - AI mode debate logic
- `P2PDebateViewModel` - P2P mode debate logic
- `AuthViewModel` - Authentication state management

**Services:**

- `SupabaseClient` - Database operations
- `MatchmakingService` - P2P matchmaking logic
- `P2PDebateService` - P2P session management
- `ServerRepository` - Stats and history tracking

**Data Models:**

- `DebateMessage` - Message structure
- `TurnScore` - Individual turn scoring
- `AccumulatedScores` - Overall match scores
- `P2PSessionResponse` - P2P session data

---

## 🚀 Getting Started

### **Prerequisites**

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 or higher
- **Android Device**:
    - ARM64 architecture required
    - Minimum: Android 7.0 (API 24)
    - Recommended: Android 10+ (API 29+)
    - RAM: 4GB+ recommended
- **Internet**: Required for initial setup and P2P mode

### **Installation**

1. **Clone the repository**
```bash
git clone https://github.com/PIYUSH-BAMNIA-25/rhetorix.git
cd rhetorix
```

2. **Open in Android Studio**

```
File → Open → Select project folder
```

3. **Configure Supabase**
    - Create a Supabase project at https://supabase.com
    - Copy your project URL and API key
    - Add to `SupabaseConfig.kt`:

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    const val SUPABASE_KEY = "YOUR_SUPABASE_KEY"
}
```

4. **Run SQL Schema**
    - Go to Supabase SQL Editor
    - Run the schema files in order:
        - `users.sql`
        - `debate_history.sql`
        - `debate_sessions_p2p.sql`
        - `debate_messages_p2p.sql`
        - `matchmaking_queue.sql`

5. **Build and Run**
```bash
./gradlew assembleDebug
```

- Connect Android device via USB
- Enable USB Debugging
- Click Run ▶️ in Android Studio

### **First Time Setup**

1. **Launch App** and create an account
2. **Download AI Model**:
    - Go to Model Management
    - Recommended: Llama 3.2 1B Instruct Q6_K (~1GB)
    - Alternative: SmolLM2 360M (~119MB) for lower-end devices
3. **Load Model** - Wait for model to initialize
4. **Start Debating!** - Choose AI Mode or P2P Mode

---

## 🎯 How It Works

### **AI Mode Flow**

```
1. Select Difficulty → 
2. Random Topic Assignment → 
3. Random Side Assignment (For/Against) →
4. 30-second Prep Time →
5. Turn-based Debate (You → AI → You → AI...) →
6. AI Judge evaluates each turn →
7. Real-time score updates →
8. Time expires or forfeit →
9. Final results & detailed feedback
```

### **P2P Mode Flow**

```
1. Join Matchmaking Queue →
2. Automatic Opponent Pairing →
3. Session Creation (random topic + sides) →
4. 30-second Prep Phase →
5. Turn-based Debate (Player 1 → Player 2 → ...) →
6. AI Judge evaluates both players →
7. Real-time score tracking →
8. Timer countdown (15 minutes) →
9. Match ends → Results & Rankings
```

### **Judging System**

```
┌──────────────────────┐
│  Player sends message │
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│ AI Judge analyzes:   │
│ - Logic & reasoning  │
│ - Evidence quality   │
│ - Tone & respect     │
│ - Counter-arguments  │
│ - Factual accuracy   │
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│ Score (0-10) + Brief │
│ Feedback Generated   │
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│ Score popup displayed│
│ Total score updated  │
└──────────────────────┘
```

---

## 🎮 Game Modes

### **🤖 AI Mode**

**Beginner Difficulty:**

- Simple topics (e.g., "Homework should be abolished")
- AI uses basic arguments
- Forgiving judging criteria
- Perfect for learning debate structure

**Intermediate Difficulty:**

- Complex topics (e.g., "AI will replace human jobs")
- AI uses structured reasoning
- Balanced judging
- Tests logical consistency

**Advanced Difficulty:**

- Philosophical topics (e.g., "Consciousness and free will")
- AI uses advanced counter-arguments
- Strict judging with high standards
- Requires evidence-based arguments

### **👥 P2P Mode**

**Matchmaking:**

- Join queue → Wait for opponent (max 60 seconds)
- Automatic pairing with available players
- Session created with random topic
- Random side assignment (fair competition)

**Turn System:**

- Random first player selection
- Alternating turns between players
- 30-second prep before first turn
- Input locked when not your turn

**Live Features:**

- Real-time message polling (2-second intervals)
- Turn status updates (1-second intervals)
- Live score comparison (Your Score vs Opponent Score)
- Visual turn indicators

**Forfeit Detection:**

- Opponent inactivity tracked
- Auto-forfeit after 60 seconds of no response
- Back button shows confirmation dialog
- Results saved for both players

---

## 📊 Scoring System

### **Per-Turn Scoring**

Each message is scored 0-10 points based on:

| Criteria          | Weight | Description                       |
|-------------------|--------|-----------------------------------|
| Logic & Reasoning | 20%    | Clear logical flow, no fallacies  |
| Evidence Quality  | 20%    | Facts, examples, citations        |
| Tone & Respect    | 20%    | Professional, respectful language |
| Counter-Arguments | 20%    | Addresses opponent's points       |
| Factual Accuracy  | 20%    | No misinformation or false claims |

### **Final Score Calculation**
```
Total Score = Sum of all turn scores
Average Score = Total Score / Number of turns

Winner = Player with higher total score
```

### **Performance Metrics**

- **Win/Loss Ratio**: Total wins / Total games
- **Average Score**: Mean score across all debates
- **Best Performance**: Highest single debate score
- **Consistency**: Standard deviation of scores

---

## 🗄️ Database Schema

### **Tables**

**`users`**

- User authentication and profile data
- Fields: `user_id`, `email`, `player_id`, `player_name`, `created_at`

**`debate_history`**

- Historical debate records for all modes
- Fields: `id`, `user_id`, `topic`, `user_side`, `opponent_type`, `user_score`, `opponent_score`,
  `feedback`, `created_at`

**`debate_sessions_p2p`**

- Active P2P debate sessions
- Fields: `session_id`, `topic_id`, `topic_title`, `player1_id`, `player1_name`, `player1_side`,
  `player2_id`, `player2_name`, `player2_side`, `status`, `current_turn`, `turn_number`,
  `prep_time_remaining`, `debate_time_remaining`, `start_time`, `end_time`

**`debate_messages_p2p`**

- Messages exchanged in P2P debates
- Fields: `id`, `session_id`, `player_id`, `player_name`, `message`, `turn_number`, `timestamp`,
  `word_count`

**`matchmaking_queue`**

- Real-time matchmaking queue
- Fields: `id`, `player_id`, `player_name`, `joined_at`, `status`, `session_id`

---

## 📁 Project Structure

```
app/src/main/java/com/runanywhere/startup_hackathon20/
│
├── 📱 UI Layer (Compose Screens)
│   ├── AuthScreen.kt                    # Login/Signup UI
│   ├── MainMenuScreen.kt                # Mode selection
│   ├── DebatePreparationScreen.kt       # AI mode prep
│   ├── DebateActiveScreen.kt            # AI mode debate UI
│   ├── DebateResultsScreen.kt           # AI mode results
│   ├── P2PMatchmakingScreen.kt          # P2P queue screen
│   ├── P2PDebatePreparationScreen.kt    # P2P prep
│   ├── P2PDebateActiveScreen.kt         # P2P debate UI
│   ├── P2PDebateResultsScreen.kt        # P2P results
│   ├── LeaderboardScreen.kt             # Global rankings
│   └── TestP2PScreen.kt                 # Testing utilities
│
├── 🧠 Presentation Layer (ViewModels)
│   ├── DebateViewModel.kt               # AI mode logic
│   ├── P2PDebateViewModel.kt            # P2P mode logic
│   └── AuthViewModel.kt                 # Auth state
│
├── 🗄️ Data Layer
│   ├── network/
│   │   ├── SupabaseConfig.kt            # DB configuration
│   │   ├── SupabaseClient.kt            # DB operations
│   │   ├── ServerRepository.kt          # Stats & history
│   │   ├── MatchmakingService.kt        # Queue management
│   │   └── P2PDebateService.kt          # Session management
│   │
│   └── models/
│       ├── DebateModels.kt              # Data classes
│       └── TopicGenerator.kt            # Topic selection
│
├── 🎮 Core
│   ├── MainActivity.kt                  # Navigation root
│   └── MyApplication.kt                 # SDK initialization
│
└── 📊 Resources
    ├── res/
    │   ├── values/
    │   │   ├── strings.xml
    │   │   ├── colors.xml
    │   │   └── themes.xml
    │   └── drawable/
    │       └── app_icon.xml
    └── AndroidManifest.xml
```

---

## 📸 Screenshots

### Authentication & Main Menu

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Login Screen  │  │  Signup Screen  │  │   Main Menu     │
│   - Email       │  │  - Email        │  │  - AI Mode      │
│   - Password    │  │  - Password     │  │  - P2P Mode     │
│   - Remember me │  │  - Confirm Pass │  │  - Leaderboard  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### AI Mode

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Difficulty     │  │  Prep Screen    │  │  Active Debate  │
│  - Beginner     │  │  - Topic        │  │  - Messages     │
│  - Intermediate │  │  - Your Side    │  │  - Your turn    │
│  - Advanced     │  │  - 30s timer    │  │  - AI response  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### P2P Mode

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Matchmaking    │  │  P2P Debate     │  │  Results        │
│  - Searching... │  │  - Turn-based   │  │  - Final score  │
│  - Queue status │  │  - Live scores  │  │  - Strengths    │
│  - Cancel       │  │  - Timer: 14:32 │  │  - Weaknesses   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## 🗺️ Roadmap

### ✅ **Completed**

- [x] Authentication system
- [x] AI Mode (3 difficulties)
- [x] P2P Mode with real-time matchmaking
- [x] Turn-based debate system
- [x] AI judging with detailed feedback
- [x] Timer system (prep + debate countdown)
- [x] Score tracking and history
- [x] Leaderboard
- [x] 60+ debate topics across 6 categories
- [x] Forfeit detection and handling
- [x] Database integration (Supabase)
- [x] On-device AI (Llama 3.2 1B)

### 🚧 **In Progress**

- [ ] Voice input for debates
- [ ] Push notifications for P2P matches
- [ ] Achievement system
- [ ] Tournament mode
- [ ] Spectator mode (watch live debates)

### 📅 **Future Enhancements**

- [ ] iOS version (Swift UI)
- [ ] Web version (React)
- [ ] Video debates with webcam
- [ ] Team debates (2v2, 3v3)
- [ ] Custom topics and private matches
- [ ] Debate replays and analysis
- [ ] Social features (friends, chat)
- [ ] Premium subscription with advanced analytics

---

## 👥 Team

### **Developers**

**Piyush Bamnia**

- Role: Team Leader and AI integration
- GitHub: [@PIYUSH-BAMNIA-25](https://github.com/PIYUSH-BAMNIA-25)
- LinkedIn : [@Piyush Bamnia](https://www.linkedin.com/in/piyush-bamnia-ab09ab255/)
- Email: piyushbamnia25@gmail.com
- Contributions:
    - AI Mode Implementation
    - AI judging Implementation
    - Real-time sync logic

**M. Pihu Mohanty**

- Role: Frontend & Database management
- GitHub: [@Pihu-Mohanty](https://github.com/kumarimanjusrimohantycse2024-art)
- LinkedIn : [@M. Pihu Mohanty](https://www.linkedin.com/in/m-pihu-mohanty-5753212b0/)
- Email: kumarimanjusrimohantycse2024@gmail.com
- Contributions:
    - DataBase Implementation
    - UI Development
    - P2P Mode Implementation

### **Project Links**

- **Repository
  **: [https://github.com/PIYUSH-BAMNIA-25/rhetorix](https://github.com/PIYUSH-BAMNIA-25/rhetorix)
- **Demo Video**: [Coming Soon]
- **Documentation**: [GitHub Wiki](https://github.com/PIYUSH-BAMNIA-25/rhetorix/wiki)

---

## 🎨 Design System

### **Color Palette**

```kotlin
// Primary Colors
CyanPrimary     = #00D9FF    // Buttons, highlights
CyanLight       = #5FEDFF    // Hover states
PurpleAccent    = #9D4EDD    // Secondary actions

// Background
DarkBackground  = #0A0A0F    // Base background
DarkSurface     = #1A1A2E    // Cards, surfaces
DarkCard        = #16213E    // Elevated cards

// Semantic Colors
GreenWin        = #4ADE80    // Success, wins
RedLoss         = #FF6B6B    // Errors, losses
YellowWarning   = #FBBF24    // Warnings
GoldStar        = #FFD700    // Achievements

// Text
TextWhite       = #FFFFFF    // Primary text
TextGray        = #B0B0B0    // Secondary text
```

### **Typography**

- **Headings**: Bold, 24-48sp
- **Body**: Regular, 14-16sp
- **Captions**: Light, 11-13sp
- **Font Family**: System Default (Roboto)

### **Design Principles**

- 🌑 **Dark First**: Optimized for night-time use
- ⚡ **Fast & Responsive**: Smooth 60fps animations
- 🎯 **Clear Hierarchy**: Important info stands out
- 🔄 **Consistent**: Reusable components
- ♿ **Accessible**: High contrast, readable fonts

---

## 🧪 Testing

### **Testing P2P Mode Locally**

Since P2P requires two players, you can simulate opponents using SQL:

```sql
-- 1. Clean up old sessions
DELETE FROM debate_sessions_p2p WHERE player1_id = 'YOUR_PLAYER_ID';
DELETE FROM matchmaking_queue WHERE player_id IN ('YOUR_PLAYER_ID', 'TEST_BOT');

-- 2. Create test opponent in queue
INSERT INTO matchmaking_queue (player_id, player_name, status)
VALUES ('TEST_BOT', 'Test Opponent', 'WAITING');

-- 3. After match starts, simulate opponent's message
INSERT INTO debate_messages_p2p (session_id, player_id, player_name, message, turn_number)
VALUES ('[SESSION_ID]', 'TEST_BOT', 'Test Opponent', 'I disagree with your point.', 2);

-- 4. Switch turn back to you
UPDATE debate_sessions_p2p
SET current_turn = 'YOUR_PLAYER_ID', turn_number = 2
WHERE session_id = '[SESSION_ID]';
```

### **Unit Tests**
```bash
./gradlew test
```

### **UI Tests**
```bash
./gradlew connectedAndroidTest
```

---

### **Third-Party Licenses**

- **RunAnywhere SDK**: Proprietary (see SDK documentation)
- **Llama Models**: Meta AI Research License
- **Supabase**: Apache 2.0
- **Jetpack Compose**: Apache 2.0

---

## 🙏 Acknowledgments

### **Technology Partners**

- [RunAnywhere](https://runanywhere.ai) - On-device AI SDK
- [Supabase](https://supabase.com) - Backend infrastructure
- [Meta AI](https://ai.meta.com) - Llama models

### **Inspiration**

- Traditional debate clubs and tournaments
- Competitive gaming (League of Legends, Chess.com)
- AI assistants (ChatGPT, Claude)

### **Resources**

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Material Design 3](https://m3.material.io/)

---

## 📞 Contact & Support

### **Get Help**

- 📧 Email: piyushbamnia25@gmail.com
- 🐛 [Report a Bug](https://github.com/PIYUSH-BAMNIA-25/rhetorix/issues)
- 💡 [Request a Feature](https://github.com/PIYUSH-BAMNIA-25/rhetorix/issues/new)
- 📚 [Documentation](https://github.com/PIYUSH-BAMNIA-25/rhetorix/wiki)

### **Contributing**

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 🌟 Show Your Support

If you found this project helpful or interesting:

- ⭐ Star the repository
- 🐛 Report issues
- 💡 Suggest new features
- 📢 Share with others

---

<div align="center">

### **Made with ❤️ for competitive debaters worldwide**

**Rhetorix** - *Where words become weapons, and arguments become art.*

[⬆ Back to Top](#-rhetorix---ai-powered-competitive-debate-platform)

</div>

---

**Last Updated**: January 2025  
**Version**: 1.0.0  
**Status**: ✅ Production Ready

