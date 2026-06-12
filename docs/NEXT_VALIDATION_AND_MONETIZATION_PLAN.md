# Next Validation And Monetization Plan

## Current Decision

Widgets are paused from the user-facing app surface. The widget implementation files remain in the codebase, but the app no longer registers widget providers in `AndroidManifest.xml`, and the in-app widget options button is hidden. This lets the app move forward without exposing unfinished widgets.

## Monetization Options

1. One-time paid unlock
   - Keep the base app free with a limited number of habits.
   - Unlock unlimited habits, color themes, import/export, and advanced reminders with a single purchase.
   - Lowest support burden and easiest fit for a personal habit tracker.

2. Gentle subscription
   - Free app includes core tracking.
   - Subscription adds cloud backup, cross-device sync, premium themes, lock-screen/widget features when redesigned, and deeper history views.
   - Only worth doing if backup/sync becomes part of the roadmap.

3. Paid app
   - Simple monetization, no in-app purchase plumbing.
   - Harder to grow because users cannot try the habit loop first.

Recommended first monetization path: free core app plus one-time premium unlock. Avoid ads; they clash with the calm, personal utility feel.

## Premium Feature Candidates

1. Unlimited habits.
2. Extra color palettes and custom habit colors.
3. Export/import backup.
4. Optional bead numbers and advanced count limits.
5. Longer history ranges beyond two weeks.
6. Reminders and routine presets.
7. Redesigned widgets later, only after they look native and reliable.

## Security And Privacy Checks

1. Confirm all habit data stays local unless a future backup/sync feature is explicitly added.
2. Disable Android backup or define backup rules if local habit data should not silently move through device cloud backup.
3. Review exported Android components before release. Only `MainActivity` should be exported unless a specific integration requires otherwise.
4. Keep widget providers unregistered until redesigned.
5. Check Room database migrations before any schema change.
6. Avoid logging habit names, counts, or dates.
7. Add a plain privacy note in the app and store listing: no account, no server, local-only storage.

## Memory Leak And Performance Checks

1. Review coroutine usage in Compose screens and widget/config code for activity leaks.
2. Move long-lived repository work into lifecycle-aware scopes where possible.
3. Check that bitmap creation is not triggered unnecessarily during normal app rendering.
4. Test large datasets: 30, 100, and 300 habits with two weeks of entries.
5. Profile scrolling in portrait and landscape with Android Studio Profiler.
6. Run LeakCanary in a debug-only build, especially around dialogs, rotation, and activity recreation.
7. Validate that rotation does not duplicate in-memory state or lose pending edits.

## Release Validation Checklist

1. Install over the prior portrait APK without package conflict.
2. Fresh install, launch, and verify sample data appears.
3. Add, edit, reorder, and delete habits.
4. Tap to increment and hold to decrement beads.
5. Toggle bead numbers.
6. Switch every color theme and confirm backgrounds update consistently.
7. Rotate portrait to landscape and back.
8. Verify two weeks of data scrolls smoothly with frozen headers.
9. Test with many habits and confirm table scrolling remains smooth.
10. Confirm no widgets appear in the Android widget picker.
11. Confirm app remains usable after force stop and relaunch.
12. Confirm no sensitive data appears in logs during normal use.

## Suggested Next Update

1. Add an in-app privacy/about screen.
2. Add debug-only validation tooling: sample data generator and large-dataset stress mode.
3. Add release QA notes to the GitHub workflow artifact summary.
4. Decide monetization scope for version 1: one-time premium unlock is the current recommended path.
5. Keep widgets hidden until a full visual redesign can be tested on real launchers.
