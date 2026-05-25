# MyApplication (Hot News)

## Requirements

- Android Studio (latest stable)
- JDK 17
- Android SDK (via Android Studio)

## Setup

1. Clone the repo.
2. Open the project in Android Studio.
3. Ensure `local.properties` points to your Android SDK:

   ```
   sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
   ```

4. Let Gradle sync finish.

## Run (Android Studio)

- Select an emulator or a connected device.
- Click **Run**.

## Run (Command Line)

Windows:

```
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

macOS/Linux:

```
./gradlew assembleDebug
./gradlew installDebug
```

## Tests

Windows:

```
.\gradlew.bat testDebugUnitTest
```

macOS/Linux:

```
./gradlew testDebugUnitTest
```

## Notes

- `google-services.json` is already included under `app/` for Firebase.
- If you see a "SDK not found" error, update `local.properties` to your SDK path and re-sync.
