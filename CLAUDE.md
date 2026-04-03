# CLAUDE.md — WordBook Development Guide

## Development Process
- Work **step by step**; never implement everything at once
- Always **clarify requirements** before writing code
- Define **MVP** (Minimum Viable Product) for new features and **Minimum Viable Change** for bug fixes
- Do **NOT refactor** code unless explicitly instructed
- At the end of each step:
  1. Summarize what was done
  2. Propose next steps
  3. Update `progress.md`
- Ask if anything is unclear before starting
- Keep `progress.md`, `requirements.md`, `tasks.md` up to date

---

## Git Conventions

### Commit Format — Conventional Commits
```
<type>: <short summary>

<optional body>

Generated-by: Claude (claude.ai/code)   ← required when Claude wrote/modified the code
```
Types: `feat` · `fix` · `chore` · `docs` · `refactor` (only when instructed) · `test`

### Branch Naming
- `feature/<short-description>`
- `fix/<short-description>`
- `chore/<short-description>`

### Claude Authorship Rule
Every commit that contains code **created or modified by Claude** MUST include the
following line in the commit body:

```
Generated-by: Claude (claude.ai/code)
```

---

## Architecture

### Three-Layer Clean Architecture (strict — no cross-layer imports)
```
presentation/  →  domain/  only  (never imports from data/)
domain/        →  nothing  (pure Kotlin, zero Android dependencies)
data/          →  domain/  only
```

### Unidirectional Data Flow
```
Screen → ViewModel → UseCase → RepositoryImpl → DAO → DB
                                                      ↓
Screen ← ViewModel ←────────────────── StateFlow/Flow ┘
```

### Package Structure
```
com.wordbook
├── presentation/
│   ├── screens/{feature}/   # {Feature}Screen.kt + {Feature}ViewModel.kt
│   ├── components/          # shared Composables
│   ├── navigation/          # Screen.kt, NavGraph.kt
│   └── theme/               # Color.kt, Theme.kt, Type.kt
├── domain/
│   ├── model/               # pure data classes & enums
│   ├── repository/          # interfaces only
│   └── usecase/{feature}/   # {Action}{Entity}UseCase.kt
└── data/
    ├── database/
    │   ├── entities/        # {Entity}Entity.kt
    │   └── daos/            # {Entity}Dao.kt
    ├── repository/          # {Entity}RepositoryImpl.kt + Mappers.kt
    ├── preferences/         # DataStore
    └── importexport/        # DeckSerializer.kt
```

---

## Naming Conventions

| What | Convention | Example |
|---|---|---|
| Screen file | `{Feature}Screen.kt` | `HomeScreen.kt` |
| ViewModel | `{Feature}ViewModel.kt` | `HomeViewModel.kt` |
| UI state | `{Feature}UiState` data class | `HomeUiState` |
| Use case | `{Action}{Entity}UseCase.kt` | `GetCardsForDeckUseCase.kt` |
| DAO | `{Entity}Dao.kt` | `CardDao.kt` |
| Entity | `{Entity}Entity.kt` | `CardEntity.kt` |
| Repository impl | `{Entity}RepositoryImpl.kt` | `CardRepositoryImpl.kt` |
| Private MutableStateFlow | `_fieldName` | `_query`, `_errorMessage` |
| Public StateFlow | `fieldName` | `uiState`, `importText` |
| Composable callbacks | `on{Action}` | `onDeckClick`, `onQueryChange` |

---

## Kotlin / Coroutines Rules
- `SharingStarted.WhileSubscribed(5_000)` for all `stateIn` calls
- Use `.update { }` for all StateFlow mutations — never assign directly
- Use `collectAsStateWithLifecycle()` in Composables — never `.collect {}`
- Async ops in ViewModels: `viewModelScope.launch { }`
- **try-catch in ViewModels only** — errors propagate up from DAO → Repository → UseCase

---

## Error Handling Pattern
```kotlin
// ViewModel
private val _errorMessage = MutableStateFlow<String?>(null)

viewModelScope.launch {
    try { ... }
    catch (e: Exception) { _errorMessage.update { e.message ?: "Failed to <action>" } }
}

fun clearError() { _errorMessage.update { null } }

// Screen
LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { msg ->
        snackbarHostState.showSnackbar(msg)
        viewModel.clearError()
    }
}
```

---

## Compose UI Rules
- Wrap every screen in `WordBookTheme { }`
- Use `Scaffold` with `topBar`, `snackbarHost`, `floatingActionButton` as needed
- Content loading pattern:
  ```kotlin
  when {
      uiState.isLoading -> CircularProgressIndicator(...)
      uiState.items.isEmpty() -> EmptyState(title = "...")
      else -> LazyColumn(...) { items(list, key = { it.id }) { ... } }
  }
  ```
- **Material3 only** — never import from `androidx.compose.material` (M2)
- Colors: always `MaterialTheme.colorScheme.*` — no hardcoded hex values in UI code
- Status colors from `Color.kt`: `ColorRemembered`, `ColorNeedsReview`, `ColorNotStudied`

---

## Room / Database Rules
- Many-to-many via junction tables (`CardLabelEntity`, `DeckLabelEntity`)
- Relation queries: `@Transaction` + `@Relation` with `*WithLabels` data class
- All entity ↔ domain conversions in `Mappers.kt`:
  - `Entity.toDomain()` and `Domain.toEntity()`
- Enum storage: `.name` (domain→entity), `enumValueOf()` (entity→domain)
- Timestamps: `createdAt`, `updatedAt` as `Long` (epoch milliseconds)
- To update labels: **delete all then re-insert** — no partial update

---

## Navigation Rules
- Routes defined in `Screen.kt` sealed class with `createRoute()` helpers
- Required params: path segment — `"deck/{deckId}"` (`NavType.LongType`)
- Optional params: query string — `"deck_edit?deckId={deckId}"` with `defaultValue = 0L`
- Convert optional 0L → null: `.takeIf { it != 0L }`
- **Navigation logic lives in `NavGraph.kt` only** — ViewModels never call `navController`

---

## Security Rules
- **No dynamic SQL string concatenation** — Room parameterized queries only
- **Validate and sanitize all user-supplied input** before processing:
  - Import (CSV/JSON): check for null/empty fields, cap string lengths, reject malformed data
  - Never pass raw user input to reflection or `eval`-like constructs
- **No sensitive data logging** — no `Log.d/e` calls with card content or user data
- **No unnecessary permissions** — `READ_EXTERNAL_STORAGE` is already scoped to API ≤ 32; do not broaden it
- **No device ID or hardware identifier collection**
- **Future network features**: HTTPS only; never add `android:usesCleartextTraffic="true"`
- Follow OWASP Mobile Top 10 principles when adding new features

---

## Build Environment (Android IDE / Pixel 9a)
> **Do not change these settings without on-device testing.**

| Setting | Value |
|---|---|
| JDK 17 | `/home/test/jdk-17.0.9+9` (via `org.gradle.java.home`) |
| ARM aapt2 | `/usr/bin/aapt2` (via `android.aapt2FromMavenOverride`) |
| Gradle | 8.9 |
| AGP | 8.3.2 |
| minSdk | 26 |
| targetSdk / compileSdk | 34 |

---

## Issue-Driven Development

### Labels
| Label | Meaning |
|---|---|
| `ready` | Fully specified, ready for Claude to implement |
| `in-progress` | Claude is currently working on it |
| `needs-review` | PR is open, waiting for merge |

### Issue Format
Issues must include:
- **Type**: `bug` or `feature` (use the appropriate issue template)
- **Description**: clear explanation of the problem or desired behavior
- **Acceptance Criteria**: checklist of conditions that define "done"
- **Affected Files / Layers** (optional): hints about scope

### Trigger
User says **"process open issues"** or **"handle issue #N"**.

### Reporting Bugs or Features Directly in Chat
Instead of going to GitHub, you can report issues directly in this chat.
Use either of these prefixes:

```
bug: <title>
<description of what goes wrong>

feature: <title>
<description of what you want>
```

Claude will then:
1. Create a GitHub issue from your description (applying the template format)
2. Immediately proceed with the full issue-handling process (steps 4–11 above)

If acceptance criteria are unclear, Claude will ask before implementing.

### Claude's Issue-Handling Process (strictly in order)
1. List issues labelled `ready` in `inash03/wordbook`
2. Pick the oldest unprocessed one (or the one the user specified)
3. Read the full issue body; if acceptance criteria are missing or ambiguous,
   **comment on the issue asking for clarification — do NOT start implementing**
4. Add label `in-progress`; remove label `ready`
5. Create branch:
   - Bug → `fix/<issue-number>-<short-slug>`
   - Feature → `feature/<issue-number>-<short-slug>`
6. Implement **Minimum Viable Change** only — no scope creep
7. Commit with `Generated-by: Claude (claude.ai/code)` in the commit body
8. Push branch and open PR with `Closes #<issue-number>` in the PR body
9. Comment on the issue: link to the PR
10. Update `tasks.md` — add issue to "In Progress"
11. Summarize what was done and propose next steps
