# Azkari Wasalati Android

This repo now contains a native Android version of your azkar and prayer app that can be built on GitHub without Android Studio.

## What was added

- A full Android project in `app/`
- A native Android UI built with Jetpack Compose
- Extracted local JSON assets for azkar categories, sunnah data, and city lists
- Native Android background reminders for:
  - prayer times
  - morning azkar
  - evening azkar
  - sleep azkar
  - Friday Surah Al-Kahf reminder
- Notification and exact-alarm permission support
- Automatic rescheduling after reboot, time changes, and app updates
- A GitHub Actions workflow that builds a debug APK

## Build the APK on GitHub

1. Upload this whole folder to a GitHub repository.
2. Open the repository on GitHub.
3. Go to the `Actions` tab.
4. Run the `Build Android APK` workflow, or push to `main` and let it run automatically.
5. Download the generated APK artifact from the workflow run.

## Important notes

- The generated workflow builds a `debug` APK, which is the easiest option for GitHub builds.
- Prayer reminders are most accurate when the app is opened once, the city is selected, notifications are allowed, and exact alarms are enabled from Android settings.
- The original HTML files were used as the source to extract the app data, but the Android interface itself is now native.
