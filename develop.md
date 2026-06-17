# HiPenter — Developer Documentation

Welcome to the HiPenter project! This document outlines the repository structure, architecture, and current state of development to help anyone onboarding to the project.

## Project Overview

HiPenter is a decentralized social posting app where users own their data in their personal Google Sheet. 
- **Read:** Everyone can read the public global feed (managed via Vercel + Apps Script).
- **Write:** Only the user can edit their posts, because their posts live in their personal Google Drive.

## Repository Structure

The project is structured into three main environments (App, Backend, Web). Currently, we are actively developing the **Android App**.

```text
HiPenter/
├── app/                        # Main Android Application Module
│   ├── src/main/java/com/example/hipenter/
│   │   ├── auth/               # Google Sign-In & Session Management (Credential Manager)
│   │   │   ├── GoogleAuthManager.kt    # Triggers native Google Sign-In sheet
│   │   │   └── UserSession.kt          # Stores token and user profile
│   │   │
│   │   ├── data/               # Repositories & Local Storage
│   │   │   ├── DataRepository.kt       # Coordinates network + cache + sheets
│   │   │   └── LocalCache.kt           # SharedPreferences local feed cache
│   │   │
│   │   ├── network/            # API Communication Layer
│   │   │   ├── HiPenterApi.kt          # Client talking to Vercel Proxy
│   │   │   ├── AuthInterceptor.kt      # OkHttp interceptor attaching OAuth tokens
│   │   │   └── models/Post.kt          # Core Data Model
│   │   │
│   │   ├── sheets/             # Google Sheets Integration
│   │   │   └── UserSheetManager.kt     # Creates and writes to user's Google Sheet
│   │   │
│   │   ├── theme/              # Design System (Jetpack Compose)
│   │   │   ├── Color.kt                # Teal, Coral, Navy branded palette
│   │   │   ├── Theme.kt                # Light/Dark scheme configurations
│   │   │   └── Type.kt                 # Inter and Outfit typography
│   │   │
│   │   ├── ui/                 # UI Screens & Components
│   │   │   ├── auth/LoginScreen.kt           # Beautiful gradient login UI
│   │   │   ├── compose/ComposePostScreen.kt  # Post creation UI with word limits
│   │   │   ├── main/MainScreen.kt            # Global Feed showing LazyColumn of posts
│   │   │   ├── profile/ProfileScreen.kt      # User dashboard
│   │   │   └── components/                   # Reusable widgets
│   │   │       ├── PostCard.kt               # The main feed item card
│   │   │       └── WordCounter.kt            # Animated circular limit counter
│   │   │
│   │   ├── MainActivity.kt     # App entry point
│   │   ├── Navigation.kt       # Jetpack Navigation routing logic
│   │   └── NavigationKeys.kt   # Strongly typed navigation targets
│   │
│   └── build.gradle.kts        # Android build configurations and dependencies
│
├── .gitignore                  # Prevents sensitive files (local.properties, keys) from leaking
├── implementation_plan.md      # The master architectural blueprint
└── task.md                     # Current checklist tracking our build progress
```

## How It Works

1. **Authentication:** Uses Android's modern `CredentialManager` API. We request scopes for Google Sheets and Drive.
2. **Posting Data:** When a user posts, it is written to a private Google Sheet they own (`UserSheetManager.kt`), and a copy is sent to the Vercel Proxy to appear in the global feed.
3. **Reading Data:** The feed calls a Vercel proxy, which caches responses from a central Apps Script to save quota and load instantly. The app also caches locally (`LocalCache.kt`) so the UI loads at 60fps immediately on launch.

## Security Practices
- **Never commit `local.properties`**: It contains your machine's local Android SDK paths.
- **Never commit keystores (`*.jks`)**: These are your private signing keys.
- **Never commit API Keys directly**: Use build environment variables.
*(These are all safely ignored by `.gitignore`)*
