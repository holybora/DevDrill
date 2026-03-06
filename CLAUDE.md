# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DevDrill is a **Kotlin Multiplatform** (KMP) application targeting Android, iOS, and Server (Ktor).
Package: `com.sls.devdrill`. Early-stage project (v1.0) using shared Compose Multiplatform UI.

## Figma MCP server rules

- If the Figma MCP server returns a localhost source for an image or SVG, use that source directly
- DO NOT import/add new icon packages — all assets should come from the Figma payload
- Do NOT use or create placeholders if a localhost source is provided

## Build & Test Commands

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:assembleRelease
./gradlew :composeApp:installDebug            # Install on connected device/emulator

# Server (Ktor)
./gradlew :server:run                          # Starts on http://localhost:8080

# Tests — all modules
./gradlew test

# Module-specific tests
./gradlew :composeApp:testDebugUnitTest
./gradlew :shared:test
./gradlew :server:test

# Single test class
./gradlew :composeApp:testDebugUnitTest --tests "com.sls.devdrill.SomeTest"

# Clean build
./gradlew clean :composeApp:assembleDebug
```

## SDK & Tooling

- **Kotlin:** 2.3.0 with Compose Multiplatform 1.10.0
- **AGP:** 8.11.2
- **Compile/Target SDK:** 36 | **Min SDK:** 24
- **Java compatibility:** 11 (JBR 21 for build)
- **Ktor:** 3.3.3
- **Material3:** 1.10.0-alpha05 (multiplatform)
- **Gradle:** 8.14.3
- **Dependency versions:** `gradle/libs.versions.toml`

## Module Structure

```
DevDrill/
├── composeApp/          # KMP Compose application (Android + iOS entry points)
│   └── src/
│       ├── commonMain/  # Shared Compose UI (App.kt)
│       ├── androidMain/ # MainActivity, Android resources
│       ├── iosMain/     # MainViewController (ComposeUIViewController bridge)
│       └── commonTest/  # Shared tests
├── shared/              # KMP library (Android, iOS, JVM)
│   └── src/
│       ├── commonMain/  # Platform interface, Greeting, Constants
│       ├── androidMain/ # Platform.android.kt (Build.VERSION)
│       ├── iosMain/     # Platform.ios.kt (UIDevice)
│       ├── jvmMain/     # Platform.jvm.kt (System.getProperty)
│       └── commonTest/
├── server/              # Ktor JVM server (port 8080, uses shared module)
└── iosApp/              # Xcode project wrapping ComposeView in SwiftUI
```

## Architecture Patterns

- **Multiplatform expect/actual:** `Platform` interface in `commonMain`, platform implementations in `androidMain`/`iosMain`/`jvmMain`
- **Shared UI:** `App()` composable in `commonMain` used by all platforms
- **Android:** Single Activity with `enableEdgeToEdge()`, calls `App()` via `setContent`
- **iOS:** SwiftUI `ContentView` wraps `ComposeView` → `ComposeUIViewController { App() }`
- **Server:** Ktor `embeddedServer(Netty)` with routing, shares `Greeting` class from `:shared`
- **Constants:** `SERVER_PORT` defined in `shared/Constants.kt`, used by server module

## Dependency Graph

```
:composeApp → :shared (Android + iOS UI)
:server     → :shared (JVM server)
:shared     → (no dependencies, pure KMP library)
```

## Notes

- No DI framework, navigation library, or state management beyond Compose `remember`
- No CI/CD, linting, or code coverage tooling configured
- No build-logic convention plugins — standard KMP Gradle setup