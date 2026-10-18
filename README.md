# Atlas

A gym progress tracker — native **iOS (SwiftUI)** and **Android (Kotlin + Jetpack Compose)** rewrites of the
[original Java Android app](https://github.com/dhairyab0069/Atlas) built for COSC 341 (Human Computer Interaction) at UBC Okanagan.

| Feature | iOS | Android |
|---|---|---|
| Gym check-in / check-out and "See who's where" | `HomeView` | `HomeScreen` |
| Lift journal (bench, squat, deadlift, pull-ups): sets, reps, 1-rep max, history, personal best | `JournalView` | `JournalScreen` |
| Calorie diary: goal − food + exercise = remaining, per-meal items | `DiaryView` | `DiaryScreen` |
| Leaderboard | `LeaderboardView` | `LeaderboardScreen` |
| Add friends | `FriendsView` | `FriendsScreen` |

Friends, gym check-ins by other people and leaderboard scores are still the placeholder data from the
prototype (`SampleData`); there is no backend yet. Your own journal, diary, name and check-in are saved
on the device as JSON.

## Layout

```
spec/fixtures.json   shared behaviour spec — both test suites run every case in it
ios/
  Atlas.xcodeproj    the app (iOS 18+)
  Atlas/             SwiftUI views + assets
  AtlasCore/         Swift package: models, store, persistence, tests
android/
  core/              pure Kotlin/JVM module: models, store, persistence, tests
  app/               Compose UI (minSdk 26)
```

The two `core` modules are line-for-line equivalents. Business logic lives there, not in the UI, so it is
unit-tested on both platforms without a simulator or emulator.

## Build and test

**iOS** (Xcode 16+):

```sh
cd ios/AtlasCore && swift test          # logic tests, runs on macOS
open ios/Atlas.xcodeproj                # run the app on a simulator / device
```

**Android** (JDK 17+, Android SDK 35):

```sh
cd android
./gradlew :core:test                    # logic tests
./gradlew :app:installDebug             # run on a device / emulator
```

CI (`.github/workflows/ci.yml`) runs both test suites, builds the iOS app for the simulator and builds the
Android debug APK on every push.

## Adding behaviour

Add a case to `spec/fixtures.json` first, then make both `SharedSpecTests.swift` and `SharedSpecTest.kt` pass.

## Fixed from the Java version

- Journals skipped the first saved entry (an extra `readLine()` before the loop) and crashed when empty.
- Check-out trimmed the wrong number of characters from a shared string, mangling other people's names.
- The four near-identical journal/add-entry activity pairs are now one screen parameterised by exercise.
- The calorie diary layout existed but had no logic; it now works.
