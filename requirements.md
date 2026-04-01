# Requirements

## Core Features (v0.5 — implemented)
- **Deck management**: create, edit, delete, duplicate decks; attach labels
- **Card management**: create, edit, delete cards (front/back text); attach labels; multi-select delete
- **Study mode**: flip cards, mark Remembered / Needs Review, sequential order
- **Test mode**: 4 modes — Sequential, Random, Needs-Review-First, SRS (SM-2 algorithm)
- **Test history**: per-session results (correct / incorrect / skipped counts, accuracy)
- **Statistics dashboard**: total cards, remembered %, streak, per-deck breakdown
- **Search**: full-text search across cards, filter by label and study status
- **Import / Export**: CSV and JSON formats, per-deck
- **Color themes**: Light / Dark mode + 6 accent colors + Material You (Android 12+)
- **Local-only storage**: Room / SQLite, no network required
- **Text-only cards**: no image or audio support (by design)

## Constraints
- Android minSdk 26 (Android 8.0+)
- Development environment: Android IDE on Pixel 9a (ARM64, no PC)
- Language: Kotlin + Jetpack Compose (Material Design 3)
