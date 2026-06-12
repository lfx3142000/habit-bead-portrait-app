# Google Play Release Prep

## Monetization

- Free tier: up to 5 habits.
- Premium tier: one-time in-app product unlocks unlimited habits and custom bead color pickers.
- Product ID to create in Play Console: `premium_unlock`.
- No subscriptions.
- No ads.
- No cloud backup or account system.

Google Play Billing is required for the one-time unlock because it sells digital app functionality. The app currently has the free-tier gate, premium messaging, and premium-gated custom bead color picker UI in place; the purchase flow should be wired after `premium_unlock` exists in Play Console.

## Release Artifacts

- Debug APK workflow still runs on push.
- Manual workflow option `release_bundle` builds `:app:bundleRelease`.
- Required GitHub secrets for release bundle signing:
  - `HABIT_BEADS_RELEASE_KEYSTORE_BASE64`
  - `HABIT_BEADS_RELEASE_STORE_PASSWORD`
  - `HABIT_BEADS_RELEASE_KEY_ALIAS`
  - `HABIT_BEADS_RELEASE_KEY_PASSWORD`

## Privacy And Data Safety Draft

- Data collected: habit titles, optional habit subtitles, colors, dates, and counts entered by the user.
- Data sharing: none.
- Server transfer: none.
- Account required: no.
- Ads: no.
- Analytics SDKs: none currently.
- Cloud backup: disabled in the manifest.
- Permissions: no runtime permissions currently requested.

Store listing privacy summary:

Habit Beads stores your habit names, bead counts, and display preferences locally on your device. The app does not create an account, upload your habit data to a server, sell your data, show ads, or use cloud backup.

## Security Validation

1. Confirm `AndroidManifest.xml` has no unnecessary exported components.
2. Confirm `android:allowBackup="false"` remains set.
3. Confirm `android:usesCleartextTraffic="false"` remains set.
4. Confirm no runtime permissions are added without a documented reason.
5. Confirm release builds are signed with Play upload credentials, not the debug keystore.
6. Confirm ProGuard/R8 release build succeeds.
7. Confirm no habit names, subtitles, dates, or counts are logged.

## Memory Leak And Performance Validation

1. Rotate portrait to landscape and back 20 times while editing a habit.
2. Open and dismiss Add, Edit, Options, Premium, Delete, and Reset dialogs repeatedly.
3. Run with 5, 30, 100, and 300 habits.
4. Scroll the table in portrait and landscape with two weeks of data.
5. Profile allocations during scrolling and repeated rotation.
6. Run a debug build with LeakCanary before production release.

## Manual QA Before Play Upload

1. Fresh install.
2. Upgrade install over previous portrait debug package.
3. Add 5 habits as a free user.
4. Try adding a 6th habit and confirm the premium prompt appears.
5. Confirm widgets do not appear in Android's widget picker.
6. Confirm all theme choices render correctly.
7. Confirm Android backup does not restore habit data after uninstall/reinstall.
8. Build a release AAB with signing secrets.
9. Upload to Play internal testing.
10. Test Play Billing purchase, restore, refund, and reinstall entitlement behavior.

## Official References

- Google Play Billing supports one-time products and subscriptions through Play's billing system.
- Android App Bundles are the publishing artifact for Google Play.
- Google Play requires accurate Data safety disclosures in Play Console.
