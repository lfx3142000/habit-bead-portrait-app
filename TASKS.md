# Habit Beads TASKS

## Current status

Habit Beads is now a working Android/Compose prototype on the `ci-validate-build` PR branch. The app builds through GitHub Actions and produces a downloadable debug APK artifact.

## Completed

- [x] Basic Android project structure
- [x] Kotlin/Compose setup
- [x] Landscape-only MainActivity
- [x] GitHub Actions debug APK workflow
- [x] Confirm GitHub Actions build passes
- [x] 14-day bead tracker grid
- [x] Square bead cells
- [x] Tap bead cell to increment count, up to 9
- [x] Long-press bead cell to decrement count
- [x] Local persistence prototype using SharedPreferences
- [x] Add habit flow
- [x] Edit habit by tapping habit title row
- [x] Add optional subtitle field for habits
- [x] Add longer default sample habit title/subtitle for layout testing
- [x] Delete habit from the edit dialog with confirmation
- [x] Reorder habits with long-press drag on grip handle
- [x] Remove visible daily counts/targets from main grid
- [x] Reset test data action
- [x] Move tracker UI out of MainActivity.kt into HabitBeadsApp.kt
- [x] Reduce MainActivity.kt to app entry point only
- [x] Split models/constants into Models.kt
- [x] Split date helper into DateHelpers.kt
- [x] Add basic accessibility descriptions for habit rows, drag grips, day headers, bead cells, and color choices
- [x] Add empty state when all habits are deleted
- [x] Move reset action into Options dialog to reduce main tracker clutter
- [x] Improve top bar balance by keeping primary action focused on Add habit
- [x] Add simple Habit Beads launcher icon resources
- [x] Add branded Compose color scheme
- [x] Compact grid layout based on phone screenshot feedback
- [x] Add status bar safe top padding so the title is not cut off
- [x] Add selectable color themes in Options: Warm, Ocean, Forest, and Grape
- [x] Add Room dependencies and KSP
- [x] Add Room entities, DAOs, database, provider, and repository
- [x] Wire tracker UI to Room-backed repository for habits and bead counts
- [x] Remove legacy SharedPreferences habit/count storage helper after Room wiring
- [x] Persist selected theme choice through AppPreferences
- [x] Add explicit loading state while Room initializes
- [x] Split app theme into AppTheme.kt
- [x] Split tracker screen and major Compose components into separate files
- [x] Clean up Room count persistence so decrementing to zero removes the entry instead of storing a zero row
- [x] Add build/test information to Options for manual APK testing

## Current implementation notes

- Room now owns habit and bead count persistence through HabitRepository.
- Existing installs from pre-Room builds may start fresh because SharedPreferences data is not migrated into Room yet.
- Theme choice persists through AppPreferences.
- The app shell/theme, tracker screen, state cards, habit rows, bead cells, and editor dialog are split into smaller Compose files.
- Zero-count entries are now removed from Room through HabitEntryDao.clearEntry().
- Options now displays the debug build label, local storage note, and restart/persistence test reminder.

## Next tasks

- [ ] Check latest GitHub Actions build after Phase 10B manual-test info
- [ ] Download and install latest APK for manual phone testing
- [ ] Verify Room persistence after close/reopen
- [ ] Verify selected theme persists after close/reopen
- [ ] Verify decrement-to-zero persists as empty after close/reopen
- [ ] Consider migration from old SharedPreferences test builds into Room
- [ ] Add home-screen widget support after core app is stable

## Do not add yet

- Login
- Backend
- Cloud sync
- Analytics
- Billing
