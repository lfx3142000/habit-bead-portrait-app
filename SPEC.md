# Habit Beads MVP Spec

Habit Beads is a landscape Android habit tracker where users log habit progress as small bead counts in a compact 14-day grid.

## MVP decisions

- Android first
- Kotlin
- Jetpack Compose
- Material 3
- Landscape orientation
- Local-first storage
- Room for habits and entries
- DataStore for preferences
- Tap cell to increment
- Long-press cell to decrement
- Count range 0–9
- Last 14 days including today
- No login, backend, cloud sync, or analytics in MVP

## Current implementation checkpoint

The repository currently contains a lean Compose foundation used to validate GitHub Actions before layering in the full app.
