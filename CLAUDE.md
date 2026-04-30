# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Candy Crush-style puzzle game built with Kotlin Multiplatform (KMP), targeting Android, iOS, and a Ktor server. Compose Multiplatform is used for shared UI across Android and iOS.

## Build Commands

```bash
./gradlew :composeApp:assembleDebug       # Android APK
./gradlew :composeApp:installDebug        # build + install on device
./gradlew :server:run                     # start Ktor server (port 8080)
./gradlew :composeApp:testDebugUnitTest   # run unit tests
./gradlew test --tests "com.example.ClassName.methodName"  # single test
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode, or use the IDE run configuration.

## Module Structure

- **`composeApp`** — All game UI and logic (shared Android + iOS via Compose Multiplatform)
- **`shared`** — Thin KMP utilities (`Greeting`, `Platform`, `Constants`) used by all targets
- **`server`** — Ktor/Netty JVM server; depends on `shared`
- **`iosApp`** — Swift entry point that loads the Compose `UIViewController`

## Architecture

Clean Architecture in `composeApp/src/commonMain/kotlin/candycrush/`:

```
core/
  common/       — AppResult, AppDispatchers
  designsystem/ — AppTheme, AppColors, AppSpacing, AppTypography, reusable components
feature/
  game/
    data/         — GameDataSource, GameRepositoryImpl
    domain/       — GameState/Board models, 8 focused use cases
    presentation/ — GameViewModel, GameUiState, GameUiEvent, screen + components
```

**Data flow**: UI event → `GameViewModel` → `GameRepository` → use cases → immutable `GameState` copy → `StateFlow` → recompose.

**DI**: Manual service locator — `GameViewModel.buildRepository()` wires dependencies; no DI framework.

## Game Use Cases (domain layer)

Each game rule is its own use case:

| Use Case | Responsibility |
|---|---|
| `CreateNewGameUseCase` | Generates 8×8 board with no initial matches |
| `SelectCandyUseCase` | Handles cell selection |
| `SwapCandiesUseCase` | Swaps two adjacent candies |
| `DetectMatchesUseCase` | Finds 3+ consecutive horizontal/vertical matches |
| `ApplyGravityUseCase` | Drops candies into empty cells |
| `RefillBoardUseCase` | Fills empty cells with random candies |
| `ResolveBoardUseCase` | Loops gravity+refill+detect until no matches remain |
| `CheckGameStatusUseCase` | Win if score ≥ 1000, lose if moves reach 0 |

Default config: 8×8 board, 30 moves, target score 1000, 10 pts/candy.

## Key Tech Versions

- Kotlin 2.3.20 (multiplatform)
- Compose Multiplatform 1.10.3
- Jetpack Lifecycle/ViewModel 2.10.0
- Ktor 3.4.1 (server)
- Android minSdk 24 / targetSdk 36
