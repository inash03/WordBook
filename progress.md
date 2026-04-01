# Progress

## v0.5 (current — merged to master)
- All 13 screens implemented: Home, DeckDetail, DeckEdit, CardEdit, Study,
  TestSetup, Test, TestResult, History, HistoryDetail, Search, Stats, Settings
- Room DB with 7 entities + junction tables (CardLabel, DeckLabel)
- Hilt dependency injection
- DataStore preferences (theme settings)
- Jetpack Compose Navigation (type-safe routes)
- SM-2 SRS algorithm (UpdateSrsUseCase)
- CSV / JSON import & export (DeckSerializer)
- Light/Dark + 6 accent colors + Material You theme (Android 12+)
- Builds and runs on Pixel 9a via Android IDE

### Build fixes applied for Android IDE (ARM)
- gradle-wrapper.jar generated via `gradle wrapper` task
- JDK 17 set via `org.gradle.java.home` in gradle.properties
- ARM-compatible aapt2 set via `android.aapt2FromMavenOverride=/usr/bin/aapt2`
- Launcher icons added (mipmap-anydpi-v26 + vector drawables)

## Next
- Review and fix v0.5 issues (TBD)
