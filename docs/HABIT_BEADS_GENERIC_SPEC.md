# Habit Beads Generic Spec

## 1. Product vision

Habit Beads is a mobile habit-tracking app focused on fast, visual daily logging. The experience emphasizes low-friction input, glanceable history, and satisfying progress feedback.

The app should feel more like moving beads on a tracker than filling out a form. Users should be able to open the app, understand their recent patterns immediately, log progress with one tap, and return later without worrying that data will be lost.

Habit Beads is intentionally local-first. The core product should not depend on login, cloud sync, server features, or internet access. The app should first become excellent as a reliable personal tracker, then support future extensions such as widgets, backup/export, premium limits, and richer customization.

## 2. Product goals

- Make habit logging fast enough to use multiple times per day.
- Let users track more than simple done/not-done outcomes.
- Keep the main tracking experience compact, visual, and easy to scan.
- Support personalization of habits and overall appearance.
- Preserve data reliably and make the app feel dependable.
- Create a foundation for widgets and future expansion.
- Keep the MVP small enough to build, test, and iterate quickly.
- Avoid adding network, account, billing, or analytics complexity before the core experience is validated.

## 3. Primary users

- People tracking several recurring habits at once.
- People who want a grid-style view instead of a diary-style view.
- People who value quick updates and visible streak/progress patterns.
- People who want flexible habit definitions and lightweight customization.
- People who dislike habit apps that require too much navigation, typing, or reflection before logging.
- People who want repeated-count tracking, such as glasses of water, posture checks, short exercises, study blocks, medication reminders, quail chores, or recurring daily routines.

## 4. Core product principles

- Fast entry over deep navigation.
- Visual clarity over decorative complexity.
- Flexible tracking over rigid templates.
- Persistent local reliability over unnecessary connected features.
- Extensible architecture for future widgets and premium options.
- One-screen usefulness before multi-screen complexity.
- Comfortable daily repetition over novelty.
- Simple recovery from mistakes, including decrementing a bead count and confirming destructive actions.

## 5. Platform and technology assumptions

### 5.1 Initial platform

The initial app should target Android.

Recommended implementation:

- Language: Kotlin.
- UI framework: Jetpack Compose.
- Minimum SDK: 24.
- Orientation: landscape-first or landscape-only for the initial MVP, because the bead grid benefits from horizontal space.
- Local persistence: start with SharedPreferences or DataStore for early prototype validation; migrate to Room once the UI behavior is stable.
- Build/CI: GitHub Actions should build a debug APK artifact on each push or pull request.

### 5.2 Future platform direction

The app should be designed in a way that can later be ported to iOS or rebuilt with a cross-platform framework if needed, but the MVP should not be delayed by cross-platform architecture.

### 5.3 Explicit non-goals for MVP

Do not add in the MVP:

- Login.
- Backend server.
- Cloud sync.
- Billing.
- Analytics.
- Social sharing.
- AI features.
- Complex reporting dashboards.
- Multi-device sync.

These may be considered later only after the core habit logging experience is stable and useful.

## 6. Main experience

The app provides a central tracker view where habits and days intersect in interactive cells. Users can review recent history, log progress directly from the main view, and manage their habit list without excessive screen switching.

The main screen should show:

- A fixed habit title column on the left.
- A horizontally scrollable sequence of recent days on the right.
- One row per habit.
- One square cell per habit/day intersection.
- Bead clusters inside each cell representing repeated progress counts.
- Clear visual indication of today.
- Direct tap/long-press interactions for logging.

The user should not need to open a detail screen just to log progress.

## 7. Functional requirements

### 7.1 Habit tracking

Users can log progress for each habit on each day.

Requirements:

- Each habit/day cell stores an integer count.
- Count range for MVP: 0 to 9.
- Tap a cell to increment the count by 1.
- Long-press a cell to decrement the count by 1.
- Counts cannot go below 0.
- Counts cannot exceed 9 in the MVP.
- A count of 0 should show an empty or faint bead marker.
- Counts 1 through 9 should show bead clusters.
- The UI should make recent patterns visible without requiring charts.

### 7.2 Habit progress representation

Habit progress should support repeated completion levels, not only binary completion.

Examples:

- Water: count glasses or bottles.
- Stretch: count sessions.
- Reading: count sessions or reading blocks.
- Medication or supplements: count doses if applicable.
- Exercise: count sets or short activity blocks.
- Household chores: count repeated tasks.

The MVP does not need habit-specific units, but the data model should allow this later.

### 7.3 Habit management

Users can create, edit, organize, and remove habits.

Each habit should include:

- Unique local ID.
- Title/name.
- Optional subtitle/description.
- Color/accent.
- Display order.
- Optional target or goal field for future use, even if not shown in the MVP grid.
- Active/archived status in a future version.

MVP habit management:

- Add habit.
- Edit habit title.
- Edit optional subtitle.
- Edit color.
- Delete habit with confirmation.
- Reorder habits.

Recommended interaction:

- Tap habit title row to edit.
- Include delete inside the edit dialog/menu rather than as a visible row-level delete button.
- Use a grip/dot handle for reorder instead of up/down arrows.
- Long-press and drag the grip to reorder.

### 7.4 Persistence

User data is saved locally and restored reliably.

Requirements:

- Habit list persists after app restart.
- Habit order persists after app restart.
- Habit title/subtitle/color persists after app restart.
- Bead counts persist after app restart.
- Reset/test-data clear action may be available during prototype builds.
- Storage should handle simple schema evolution, such as adding subtitle to previously saved habits.

MVP storage may use SharedPreferences or DataStore for speed. Production-ready storage should use Room.

### 7.5 Settings and personalization

Users can adjust appearance and behavior settings.

MVP settings can be minimal. Future settings should include:

- Theme mode: system, light, dark.
- Haptics on/off.
- Sound feedback on/off.
- Compact/comfortable layout density.
- First day of week preference.
- Number of visible days.
- Export/import options.
- Backup options.
- Widget preferences.

### 7.6 Widgets and extensions

The product should be designed to support home-screen widgets.

Future widget concepts:

- Today-only habit bead widget.
- Single habit quick-log widget.
- Multi-habit compact widget.
- Weekly streak glance widget.

Widget support should build on the same local data model as the app, not a separate data store.

## 8. MVP definition

The MVP is successful when users can:

- Open the app and understand the grid immediately.
- Define habits.
- Log progress quickly.
- Increment and decrement counts without friction.
- Trust that data survives closing and reopening the app.
- Edit, delete, and reorder habits.
- Use the tracker comfortably as a daily tool.

The MVP does not need:

- Account creation.
- Cloud sync.
- Notifications.
- Widgets.
- Billing.
- Reports.
- Charts.
- Multi-device backup.

## 9. Core screens

### 9.1 Tracker screen

The tracker screen is the primary screen.

Elements:

- App title.
- Brief helper text.
- Add habit button.
- Optional reset button in debug/prototype builds.
- Habit title column.
- Day header row.
- Bead grid.

Behavior:

- Horizontal scrolling for day columns.
- Habit title column remains visible.
- Each habit row aligns with bead cells.
- Cells remain square.
- Today is visually highlighted.
- Habit rows support title and optional subtitle.
- Long habit titles are truncated gracefully with ellipsis.
- Optional subtitle is shown in smaller text under the title.

### 9.2 Add/edit habit dialog

Fields:

- Habit title.
- Optional subtitle.
- Color selector.
- Save/Add button.
- Cancel button.
- Delete habit button only when editing an existing habit.

Behavior:

- Title is required.
- Subtitle is optional.
- Title input should have a practical limit, such as 60 characters.
- Subtitle input should have a practical limit, such as 70 characters.
- Delete should require confirmation.

### 9.3 Delete confirmation dialog

Content:

- Clear title, such as “Delete habit?”
- Message explaining that saved bead history for that habit will be deleted.
- Delete confirmation button.
- Cancel button.

Behavior:

- Deleting a habit removes its associated bead entries.
- The action should not be triggered accidentally from the main grid.

## 10. Interaction design

### 10.1 Bead cell interactions

- Tap: increment count.
- Long press: decrement count.
- Empty state: faint dot or empty bead marker.
- Nonzero state: bead cluster.
- Max count: 9.

### 10.2 Habit row interactions

- Tap habit row/title: open edit dialog.
- Long-press and drag grip: reorder habit.
- Dragging should provide visual feedback.
- Reordering should persist.
- Avoid visible up/down arrows in the polished UI.
- Avoid visible row-level delete buttons unless user testing shows they are needed.

### 10.3 Date/day interactions

MVP does not require tapping date headers.

Future options:

- Jump to today.
- Scroll back to historical dates.
- Calendar picker.
- Monthly overview.

## 11. Visual design requirements

### 11.1 Overall feel

The interface should feel:

- Compact.
- Tactile.
- Calm.
- Satisfying.
- Clear.
- Reusable every day.

### 11.2 Grid aesthetic

- Cells should always be square.
- Cell size should be consistent across all habit rows and day columns.
- Habit rows should align with cell rows.
- Beads should be visible but not noisy.
- Empty cells should not dominate the screen.
- Today should be highlighted without overwhelming the grid.

### 11.3 Habit title layout

- Habit title column should support longer titles.
- Habit title should use ellipsis if needed.
- Optional subtitle should appear below the title in smaller text.
- Long titles should not break the square-cell grid.
- If the title/subtitle column grows, bead cells should remain square and scrollable.

### 11.4 Color

Each habit should have a color used for:

- Habit identity dot.
- Bead fill color.
- Optional subtle completed-cell background.
- Optional drag feedback.

Colors should be attractive but not overly saturated.

## 12. Data model

### 12.1 Habit

Recommended fields:

```kotlin
data class Habit(
    val id: Int,
    val name: String,
    val subtitle: String = "",
    val color: Color,
    val target: Int = 1,
    val displayOrder: Int = 0,
    val isArchived: Boolean = false
)
```

For the prototype, `displayOrder` may be represented by list order rather than a field.

### 12.2 Habit entry

Recommended production model:

```kotlin
data class HabitEntry(
    val habitId: Int,
    val dateKey: String,
    val count: Int
)
```

Where `dateKey` is stored as `yyyy-MM-dd` in local date terms.

### 12.3 Preferences

Recommended future fields:

```kotlin
data class UserPreferences(
    val themeMode: ThemeMode,
    val hapticsEnabled: Boolean,
    val soundEnabled: Boolean,
    val visibleDayCount: Int,
    val layoutDensity: LayoutDensity
)
```

## 13. Persistence plan

### 13.1 Prototype persistence

Use SharedPreferences or DataStore to validate behavior quickly.

Prototype can store:

- Habit list as serialized text or JSON.
- Counts as key-value pairs such as `habitId:yyyy-MM-dd=count`.
- Next habit ID.

Prototype storage should handle old saved formats gracefully.

### 13.2 Production persistence

Move to Room once UI behavior is validated.

Recommended Room tables:

#### HabitEntity

Fields:

- id: Int primary key.
- name: String.
- subtitle: String.
- colorArgb: Int.
- target: Int.
- displayOrder: Int.
- isArchived: Boolean.
- createdAt: Long.
- updatedAt: Long.

#### HabitEntryEntity

Fields:

- habitId: Int.
- dateKey: String.
- count: Int.
- updatedAt: Long.

Primary key:

- habitId + dateKey.

### 13.3 Repository layer

The repository should provide:

- Load active habits.
- Save habit.
- Delete/archive habit.
- Reorder habits.
- Load entries for visible date range.
- Increment entry.
- Decrement entry.
- Reset or clear test data in debug builds.

### 13.4 ViewModel layer

The ViewModel should own:

- Visible date range.
- Habit list state.
- Entry count state.
- Add/edit/delete actions.
- Reorder actions.
- UI state for dialogs.

Compose UI should eventually become mostly stateless.

## 14. Architecture plan

### 14.1 Prototype architecture

Acceptable early architecture:

- `MainActivity.kt` launches app.
- `HabitBeadsApp.kt` contains tracker UI.
- `Models.kt` contains models/constants.
- `DateHelpers.kt` contains date utilities.
- `HabitStorage.kt` contains SharedPreferences persistence.

### 14.2 Production architecture

Recommended later structure:

```text
app/src/main/java/com/habitbeads/app/
  MainActivity.kt
  HabitBeadsApp.kt
  model/
    Habit.kt
    HabitEntry.kt
    UserPreferences.kt
  data/
    HabitEntity.kt
    HabitEntryEntity.kt
    HabitDao.kt
    HabitEntryDao.kt
    HabitBeadsDatabase.kt
    HabitRepository.kt
  ui/
    tracker/
      HabitTrackerScreen.kt
      HabitNameCell.kt
      BeadCell.kt
      DayHeader.kt
      HabitEditorDialog.kt
    theme/
      Theme.kt
  util/
    DateHelpers.kt
```

## 15. Build and CI requirements

Use GitHub Actions to build a debug APK.

Minimum CI should:

- Check out repo.
- Set up JDK.
- Set up Gradle.
- Build debug APK.
- Upload APK artifact.

Recommended workflow output:

- Artifact name: `habit-beads-debug-apk`.

A successful build should produce a debug APK users can manually install on Android.

## 16. Manual testing checklist

Before considering the MVP stable, test:

- App opens successfully.
- Tracker grid displays.
- Cells are square.
- Today column is highlighted.
- Tap bead increments.
- Long-press bead decrements.
- Count never goes below 0.
- Count never exceeds 9.
- Add habit works.
- Edit title works.
- Edit subtitle works.
- Edit color works.
- Delete habit works with confirmation.
- Reorder habit works with grip drag.
- Habit order persists after app restart.
- Bead counts persist after app restart.
- Long habit titles display acceptably.
- Subtitles display acceptably.
- Reset data works in debug builds.
- Existing older saved data still loads.

## 17. Future enhancements

### 17.1 Widgets

- Today quick-log widget.
- Single-habit widget.
- Multi-habit widget.

### 17.2 Export/import

- CSV export.
- JSON backup.
- Local file import.

### 17.3 Premium options

Possible premium features:

- Unlimited habits if free version is limited.
- Widgets.
- Advanced themes.
- Export/import.
- Backup.

Premium should not be added until the core UX is validated.

### 17.4 Advanced views

- Month view.
- Habit detail view.
- Streak summary.
- Heatmap.
- Calendar history.

## 18. Success criteria

Habit Beads is successful when:

- Users can log habits in under a few seconds.
- The grid is readable at a glance.
- Users understand bead counts without explanation.
- Habit management is simple.
- The app feels reliable.
- The app is pleasant enough to use repeatedly every day.
- The architecture can support widgets and Room persistence later without a full rewrite.

## 19. Current recommended development sequence

1. Build Android/Compose project foundation.
2. Add GitHub Actions debug APK workflow.
3. Build interactive tracker grid.
4. Add local prototype persistence.
5. Add habit add/edit/delete/reorder.
6. Polish square-cell grid and habit title interactions.
7. Add title/subtitle support for longer habit labels.
8. Split code into smaller model, UI, date, and storage files.
9. Add accessibility descriptions and polish.
10. Add app icon and theme polish.
11. Migrate persistence to Room.
12. Add ViewModel/repository structure.
13. Add widget support.
14. Consider premium/export/backups only after core app validation.
