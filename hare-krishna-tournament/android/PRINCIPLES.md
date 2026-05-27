# Coding Principles

The rules every Kotlin file in this project follows. Optimized for an app that starts small (two screens) but is built so the third, fifth, and tenth screen drop in cleanly without restructuring.

If a guideline below conflicts with a rule the user has set in a specific PR or comment, the user wins — these are defaults, not laws.

---

## Architecture

### Clean Architecture — three layers, dependencies point inward

```
ui  →  domain  ←  data
```

- **`ui`** (Compose, ViewModels) depends on `domain`. Knows nothing about Retrofit, JSON, DataStore, or any Android storage API.
- **`data`** (Retrofit, DataStore, repositories) depends on `domain`. Implements interfaces declared in `domain`.
- **`domain`** (pure Kotlin models, use cases, repository interfaces) depends on **nothing** Android-specific. This is what makes the whole app unit-testable on the JVM in milliseconds.

**Why it matters**: swap Retrofit for Ktor, or DataStore for Room, or Compose for XML — and `domain` doesn't move. Tests for business logic don't need an emulator.

### MVVM with Unidirectional Data Flow (UDF)

State flows **down** from `ViewModel` → `Composable`. Events flow **up** from `Composable` → `ViewModel`. A composable never mutates state it didn't create.

```
ViewModel.uiState (StateFlow<HomeUiState>)  ──▶  HomeScreen renders
HomeScreen onTap callback                   ──▶  viewModel.onTap()
```

Never call a repository from a composable. Never put business logic in a composable. The composable's only job is "given this state, draw this UI; on this gesture, call this lambda."

### Single source of truth

- Each piece of state has exactly one owner.
- `selectedBhaktName` lives only in `UserPrefs`. Nothing else stores or caches it.
- `serverBaseline` + `sessionDelta` live only in `CounterRepository`. The ViewModel observes; it doesn't duplicate.
- Dependency versions live only in `gradle/libs.versions.toml`.

---

## SOLID (applied to Kotlin)

- **S — Single Responsibility**: one reason for a file to change. `ApiClient` configures Retrofit; it doesn't define endpoints (that's `ApiService`) or transform responses (that's `Mappers`).
- **O — Open/Closed**: extend behavior by adding a new sealed-class case, a new use case, a new repository implementation — not by editing existing branches. UI states are sealed classes; adding `Refreshing` doesn't touch existing case handling beyond an exhaustive `when`.
- **L — Liskov**: a `FakeAuthRepository` must be substitutable for `RealAuthRepository` everywhere. Interfaces define behavior contracts; tests rely on them.
- **I — Interface Segregation**: small focused interfaces over fat ones. `ContestantRepository` has one job — `list()`. Don't bundle counter sync into it.
- **D — Dependency Inversion**: `HomeViewModel` depends on the `CounterRepository` *interface* in `domain`, not the Retrofit-backed implementation in `data`. ServiceLocator wires the concrete class in at app startup.

---

## Other guiding principles

- **DRY (Don't Repeat Yourself)** — extract when the same logic appears in 2+ places. Don't pre-extract for hypothetical reuse.
- **KISS (Keep It Simple)** — the dumb solution is the right one until proven otherwise. v1 has no DI framework, no navigation library beyond Compose Navigation, no fancy state machines. Add complexity only when pain demands it.
- **YAGNI (You Aren't Gonna Need It)** — don't build for v3 features in v1. The `domain/usecase/` layer is justified because it's three files; an `EventBus` abstraction is not.
- **Composition over inheritance** — especially in Compose. Build screens out of small composables, not by extending base composables.
- **Fail fast, explicit errors** — `Result<T>` or sealed `UiState` for outcomes that can fail at runtime. Don't swallow exceptions; surface them to the UI as visible error states the user can act on.

---

## Kotlin specifics

- **Immutability by default**: `val` over `var`; immutable `data class` for state; `List` over `MutableList` in public APIs.
- **Null safety**: prefer `?` and `?.` over `!!`. A `!!` in the codebase is a bug waiting to happen.
- **Data classes for pure state**: `HomeUiState`, DTOs, domain models. No logic in their bodies.
- **Sealed classes for finite alternatives**: `UiState = Loading | Ready | Error`. The compiler enforces exhaustive handling.
- **Extension functions when they read naturally**: `dto.toDomain()` reads better than `Mappers.toDomain(dto)`. Don't over-extend — only when it makes the call site clearer.
- **`object` for singletons**, `companion object` for static-like members only when they belong to a class.

---

## Coroutines & Flow

- **Structured concurrency**: every coroutine launches from a scope with a lifecycle — `viewModelScope`, `lifecycleScope`. Never `GlobalScope`.
- **`StateFlow` for state, `SharedFlow` for one-shot events** (toasts, navigation). Don't use `LiveData` in new code.
- **`collectAsStateWithLifecycle()`** in composables, not `collectAsState()` — stops collecting when the screen is off.
- **Dispatchers explicit at the IO boundary**: `withContext(Dispatchers.IO)` inside repositories for network/disk. Don't sprinkle `Dispatchers.Main` in the UI layer — composables already run on Main.
- **Debouncing**: `Flow.debounce()` over manual `delay()` loops. Counter sync uses this for the 3s window.

---

## Compose specifics

- **Stateless composables by default**. State hoists up to the nearest ViewModel.
- **`Modifier` is always the first optional parameter**, defaulted to `Modifier`. This is the standard, and tooling assumes it.
- **`@Preview` everything that's previewable** — composables should render without a ViewModel by accepting state + lambdas as parameters.
- **Recomposition awareness**: pass stable types, not lambdas-that-capture-everything. Read [the official guide](https://developer.android.com/jetpack/compose/performance) when you write a screen that scrolls thousands of items (we don't have that yet).
- **No business logic in `@Composable` functions** — only layout and event forwarding.

---

## Naming

- **PascalCase** — types, composables, files (`HomeScreen.kt`, `CounterButton`).
- **camelCase** — functions, properties, parameters.
- **SCREAMING_SNAKE_CASE** — `const val` constants.
- **Package names**: all lowercase, single-word per segment: `com.harekrishna.tournament.ui.home`.
- **UI state classes** end in `UiState`. **DTOs** end in `Dto`. **Use cases** end in `UseCase`. **Repositories** end in `Repository`. Predictable suffixes make the file tree readable.

---

## Modularity & scalability levers (in order of when to pull them)

These are *not* needed for v1. They are written down so the team knows when each lever is appropriate to pull — and just as importantly, when it isn't.

| Lever | Pull when | Don't pull just because |
|---|---|---|
| Add a `domain/usecase/` for a screen | Two ViewModels need the same orchestration | A single ViewModel calls a repo method once |
| Introduce Hilt (DI framework) | `ServiceLocator.kt` grows past ~10 entries or constructor wiring gets painful | "Best practices on the internet said so" |
| Split into Gradle modules (`:core`, `:feature-counter`, `:feature-leaderboard`) | Build times start hurting (>30s clean build) or team size grows past ~3 devs | Want to look "enterprise" |
| Add Room (local DB) | Need offline durability for `sessionDelta` or caching API responses | Just because DataStore "feels small" |
| Add WorkManager | Need guaranteed background sync after app close | Want to fire a one-off coroutine |
| Switch to Compose Multiplatform / KMM | iOS becomes a real target | Curiosity |

---

## Testing posture

- **Domain code**: unit tests are mandatory. They're cheap (pure Kotlin, JVM) and they catch the bugs that matter.
- **ViewModels**: unit tests with fake repositories. Verify state transitions on each event.
- **Repositories**: unit tests with fake API services / fake prefs.
- **Composables**: snapshot or interaction tests for non-trivial layouts. v1 keeps these light; add when a UI bug ships.
- **End-to-end**: real device run-through of the verification checklist in TASKS.md before each release.

A test that hits the real network or the real DataStore is an *integration* test — keep it out of the `src/test/` JVM suite.

---

## What we explicitly do NOT do

- **No comments explaining what code does** — the code says what it does. Comments only for *why* something non-obvious exists (a workaround, a domain quirk, a constraint that isn't visible from the line itself).
- **No premature abstractions**. Three similar functions is fine. Extract on the fourth, not the second.
- **No "manager", "helper", "util" classes** without a real reason — they're a sign someone couldn't decide where logic belonged. Put it on the thing it acts on.
- **No mutable global state** outside `ServiceLocator` (and even there, only for app-scoped singletons, never for mutable data).
- **No business logic outside `data` or `domain`**. If you find yourself doing math in a composable, lift it.

---

## Four hard rules (user-set)

These supersede gentler guidance above when they conflict.

1. **DRY — never repeat the same logic in two places.** Second occurrence ⇒ extract into a shared composable / helper / repository method. Don't paste.
2. **Enums (or sealed classes) over magic literals.** Roles, statuses, screen names, error codes, sync states, anything that lives in a fixed set — define a type. String literals scattered through call sites are a bug waiting to happen.
3. **Utilities belong in proper util files** (e.g. `domain/util/`, `ui/util/`). When a formatter / validator / date helper is needed in more than one place, it goes into a util — not as a private function copy-pasted into each feature file.
4. **Don't fragment into one-line wrapper functions.** A `fun foo() = bar()` adds noise without value. Extract only when a function has a real reason to exist (testability, reuse, a non-obvious step that deserves a name).

When auditing existing code for these rules: scan, fix the small ones in-PR, flag the bigger refactors as separate tasks. Don't ignore them because the surrounding code is "almost right" — small drift compounds fast.
