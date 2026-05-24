# Habit Beads Android

Habit Beads is an Android-first habit tracker focused on fast, visual daily logging.

## Current status

This repository currently contains a lean Android/Compose foundation that is intended to validate the GitHub Actions build pipeline before adding the full Habit Beads feature set.

Current app behavior:

- Launches a landscape Android app.
- Shows a simple Habit Beads placeholder screen.
- Uses Kotlin, Jetpack Compose, and Material 3.
- Builds a debug APK through GitHub Actions.

## Build with GitHub Actions

1. Open the repository on GitHub.
2. Go to **Actions**.
3. Run or wait for **Android Build**.
4. Open the run.
5. Download the `habit-beads-debug-apk` artifact.

## Local build

If you have Gradle installed:

```bash
gradle :app:assembleDebug
```

A Gradle wrapper should be added later for normal local development.

## Next development task

After GitHub Actions builds successfully, continue adding the Habit Beads MVP in small commits:

1. Room database and models
2. Repository/business logic
3. DataStore preferences
4. Tracker ViewModel
5. Bead grid UI
6. Add/edit habits
7. Manage/settings screens
