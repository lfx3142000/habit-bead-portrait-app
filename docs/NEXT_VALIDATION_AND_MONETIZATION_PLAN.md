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

Chosen monetization path: free core app plus one-time premium unlock. Avoid ads; they clash with the calm, personal utility feel. Avoid subscriptions and cloud backup; the app's privacy position is local-only habit tracking.

## Premium Feature Candidates

1. Unlimited habits.
2. Custom bead colors.
3. Custom background colors.
4. Optional future extras that do not require an account or cloud sync.

Free plan: up to 5 habits.

Premium plan: one-time Google Play in-app product unlocks unlimited habits and custom color pickers for bead and background elements.

## Security And Privacy Checks

1. Confirm all habit data stays local unless a future backup/sync feature is explicitly added.
2. Keep Android backup disabled so local habit data does not silently move through device cloud backup.
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
13. Confirm free users cannot create more than 5 habits.
14. Confirm premium-gated custom color affordances point to the one-time unlock.

## Suggested Next Update

1. Wire Google Play Billing Library to a one-time product named `premium_unlock`.
2. Add an in-app privacy/about screen.
3. Add debug-only validation tooling: sample data generator and large-dataset stress mode.
4. Add release QA notes to the GitHub workflow artifact summary.
5. Keep widgets hidden until a full visual redesign can be tested on real launchers.
