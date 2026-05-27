# Android v1 — Tasks & Project Structure

Native Kotlin Android app for Hare Krishna Tournament. Syncs to the existing Vercel API at `https://hare-krishna-tournament.vercel.app/api/*`. **v1 ships complete password-based auth, including backend + web admin changes** — see `AUTH.md` for every schema/endpoint/security decision. Full design lives in `~/.claude/plans/is-the-app-made-eager-sun.md`.

---

## Project Structure

The structure below follows **Clean Architecture** (data / domain / ui layers, dependencies point inward), **MVVM** with Compose (`ViewModel` + immutable `UiState`), **feature-grouped UI** (each screen in its own folder with screen-local components), and **single responsibility** per file. It's intentionally textbook-correct — the `domain/` layer with use cases is light for v1's two screens but earns its keep the moment a third screen lands.

```
hare-krishna-tournament/android/
├── settings.gradle.kts
├── build.gradle.kts                       # root: plugins, top-level config
├── gradle.properties                      # JVM args, AndroidX flags
├── gradle/
│   └── libs.versions.toml                 # version catalog — single source of truth for deps
├── .gitignore
├── README.md                              # one-screen "how to run" for any new dev
└── app/
    ├── build.gradle.kts                   # module: SDK versions, deps from catalog, signing
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml        # INTERNET permission, Application + Activity
        │   ├── res/                       # icons, strings, themes, colors, splash
        │   └── java/com/harekrishna/tournament/
        │       │
        │       ├── TournamentApp.kt       # Application class — wires the ServiceLocator
        │       ├── MainActivity.kt        # single Activity; hosts the Compose nav graph
        │       │
        │       ├── di/                    # dependency wiring (manual ServiceLocator for v1)
        │       │   └── ServiceLocator.kt  # exposes singletons: ApiService, repos, prefs
        │       │
        │       ├── data/                  # everything that talks to the outside world
        │       │   ├── remote/
        │       │   │   ├── ApiService.kt          # Retrofit interface — the 4 endpoints
        │       │   │   ├── ApiClient.kt           # Retrofit + OkHttp + JSON setup
        │       │   │   └── dto/                   # API shapes — never leak to UI
        │       │   │       ├── KrishnaDasDto.kt
        │       │   │       ├── ScoreDto.kt
        │       │   │       ├── StatsDto.kt
        │       │   │       └── ScoreUpdateRequest.kt
        │       │   ├── local/
        │       │   │   └── SessionPrefs.kt         # encrypted DataStore: bhaktName + token
        │       │   ├── mapper/
        │       │   │   └── Mappers.kt              # DTO ↔ domain conversions
        │       │   ├── auth/
        │       │   │   └── RealAuthRepository.kt   # POST /api/auth/login + change-password
        │       │   └── repository/
        │       │       ├── ContestantRepository.kt
        │       │       └── CounterRepository.kt    # serverBaseline + sessionDelta, sync
        │       │
        │       ├── domain/                # pure Kotlin — no Android, no Retrofit
        │       │   ├── model/
        │       │   │   ├── Contestant.kt
        │       │   │   ├── AuthSession.kt          # bhaktName, token, expiresAt
        │       │   │   └── CounterState.kt         # serverBaseline, sessionDelta, weekTotal, syncedAt
        │       │   ├── auth/
        │       │   │   ├── AuthRepository.kt       # interface — the seam (Fake/Real swap)
        │       │   │   └── AuthError.kt            # sealed: InvalidCredentials, Network, Unknown
        │       │   └── usecase/                    # thin orchestration; optional but consistent
        │       │       ├── GetContestantsUseCase.kt
        │       │       ├── LoadInitialCountsUseCase.kt
        │       │       └── SyncCounterUseCase.kt
        │       │
        │       └── ui/                    # all Compose code
        │           ├── navigation/
        │           │   └── AppNavigation.kt        # observes session → Login or Home
        │           ├── theme/
        │           │   ├── Color.kt
        │           │   ├── Type.kt
        │           │   └── Theme.kt
        │           ├── common/                     # reusable across screens
        │           │   ├── LoadingScreen.kt
        │           │   └── ErrorScreen.kt
        │           ├── auth/login/
        │           │   ├── LoginScreen.kt
        │           │   ├── LoginViewModel.kt
        │           │   └── LoginUiState.kt
        │           └── home/
        │               ├── HomeScreen.kt
        │               ├── HomeViewModel.kt
        │               ├── HomeUiState.kt
        │               └── components/
        │                   ├── CounterButton.kt
        │                   ├── StatTile.kt
        │                   └── SyncStatusBar.kt
        │
        ├── test/                          # JVM unit tests (no emulator needed)
        │   └── java/com/harekrishna/tournament/
        │       ├── data/repository/CounterRepositoryTest.kt
        │       └── ui/home/HomeViewModelTest.kt
        │
        └── androidTest/                   # instrumented UI tests
            └── java/com/harekrishna/tournament/
                └── ui/home/HomeScreenTest.kt
```

### Principles this enforces

| Principle | How |
|---|---|
| **Separation of concerns** | `data` / `domain` / `ui` are three distinct layers with one job each. |
| **Dependency rule** | `ui` → `domain` → `data`. `domain` has zero Android/Retrofit imports → trivially unit-testable. |
| **DTOs vs domain models** | Server JSON shapes (`*Dto`) never reach the UI. Mappers convert at the boundary, so a backend rename doesn't ripple through Compose code. |
| **Single source of truth** | `libs.versions.toml` centralizes all dependency versions. `UserPrefs` is the only place that reads/writes `selectedBhaktName`. |
| **Feature-grouped UI** | Each screen has its own folder containing the screen, its ViewModel, its UI state, and screen-local components. Cross-screen widgets live in `ui/common/`. |
| **Single Activity, many composables** | `MainActivity` only hosts the nav graph. All screen logic lives in composables + ViewModels. |
| **Testability** | `domain` is plain Kotlin → fast JVM tests. ViewModels depend on interfaces from `domain`, easy to fake. |
| **Future-proofed without over-building** | NavHost is in place even for two screens — adding a third doesn't require restructuring. `ServiceLocator` is a one-file shim that can be swapped for Hilt later without touching call sites. |

---

## Tasks

Sequenced top-to-bottom; later tasks depend on earlier ones. Mark with `[x]` as you go.

### Phase 1 — Environment & project skeleton
- [ ] Install Android Studio (Hedgehog or newer) + Android SDK 35 + AVD with Android 14 image.
- [ ] In Android Studio: New Project → Empty Activity (Compose). Package `com.harekrishna.tournament`. Save location: `hare-krishna-tournament/android/`. Min SDK 24, Target SDK 35.
- [ ] Create `gradle/libs.versions.toml` (version catalog). Add: kotlin, agp, compose-bom, material3, lifecycle-viewmodel-compose, navigation-compose, datastore-preferences, retrofit, okhttp-logging-interceptor, retrofit-kotlinx-serialization-converter, kotlinx-serialization-json, coroutines.
- [ ] Wire the catalog into `app/build.gradle.kts`. Remove any hardcoded versions.
- [ ] Add `<uses-permission android:name="android.permission.INTERNET" />` to `AndroidManifest.xml`.
- [ ] Add `.gitignore` (Android Studio's default is fine: `/build`, `/.gradle`, `local.properties`, `*.iml`, `.idea/`, keystore files).
- [ ] Verify: empty project builds and "Hello World" runs on the emulator.

### Phase 2 — Data layer (no UI yet, all unit-testable)
- [ ] `data/remote/dto/` — define DTOs matching the four API shapes. Use `@Serializable`.
- [ ] `domain/model/` — define `Contestant` and `CounterState`. Pure Kotlin.
- [ ] `data/mapper/Mappers.kt` — DTO → domain converters.
- [ ] `data/remote/ApiService.kt` — Retrofit interface for `GET /krishnaDas`, `GET /scores`, `POST /scores`, `GET /stats`.
- [ ] `data/remote/ApiClient.kt` — Retrofit instance with OkHttp logging (debug only) and kotlinx-serialization. Base URL constant.
- [ ] `data/local/UserPrefs.kt` — DataStore-backed read/write for `selectedBhaktName` as a `Flow<String?>`.
- [ ] `data/repository/ContestantRepository.kt` — `suspend fun list(): List<Contestant>`.
- [ ] `data/repository/CounterRepository.kt` — holds `serverBaseline` + `sessionDelta` in a `StateFlow<CounterState>`. Methods: `loadInitial(bhaktName)`, `increment()`, `sync()`.
- [ ] `di/ServiceLocator.kt` — lazy singletons for `ApiService`, `UserPrefs`, both repositories.
- [ ] `TournamentApp.kt` (Application class) — initialize `ServiceLocator` with the app context. Register in manifest.
- [ ] Unit test: `CounterRepositoryTest` — fake `ApiService`, verify `sync()` POSTs `baseline + delta` and resets `delta`.

### Phase 3 — Backend auth (do this BEFORE the Android login screen)

Reference: `AUTH.md` §2–§7 for every schema/endpoint/security decision.

- [ ] `BE/DB/models/KrishnaDas.js` — add `passwordHash`, `passwordSetAt`, `role`, `lastLoginAt`. Defaults per AUTH.md §2.
- [ ] `package.json` — add `bcryptjs`, `jsonwebtoken`.
- [ ] Vercel: add `JWT_SECRET` env var (Production + Preview + Development). Generate with `openssl rand -base64 48`.
- [ ] `BE/services/authService.js` — `hashPassword`, `verifyPassword`, `signToken`, `verifyToken`.
- [ ] `BE/middleware/auth.js` — `requireAuth(handler)`, `requireAdmin(handler)`.
- [ ] `BE/api/auth.js` — `POST /login`, `POST /change-password`. Always return generic `"Invalid credentials"` to prevent username enumeration.
- [ ] `api/auth.js` — one-line Vercel re-export.
- [ ] `server/index.js` — mount `/api/auth`.
- [ ] `BE/services/krishnaDasService.js` — `updateKrishnaDas` accepts optional `password` and hashes it; `findAll` strips `passwordHash` + `phone` + `email` for unauthenticated callers.
- [ ] `BE/api/krishnaDas.js` — wrap POST/DELETE in `requireAdmin`; PATCH allows admin OR self (limited fields).
- [ ] `BE/api/scores.js` — wrap POST in `requireAuth`; verify each row's `bhaktName` matches `req.auth.sub` unless `role === 'admin'`. (Use "feature-flag" allow-if-no-token mode during rollout per AUTH.md §11 step 1.)
- [ ] `BE/api/keliKunj.js` — wrap POST/PATCH in `requireAdmin`.
- [ ] **One-time MongoDB op**: promote one admin via Atlas (AUTH.md §7 Option A). Note the chosen `bhaktName` privately.
- [ ] Verify (curl): login as admin → token returned. Login as non-admin without password set → 401. POST to `/api/scores` with another's bhaktName → 403.

### Phase 4 — Web admin login (closes the v1 auth gap on the web)

Reference: `AUTH.md` §9.

- [ ] `FE/src/api/axiosClient.js` — request interceptor injects `Authorization: Bearer <token>` from `localStorage` if present; response interceptor clears token + redirects to `/admin-login` on 401.
- [ ] `FE/src/pages/AdminLogin.jsx` — `bhaktName` + password form → calls `POST /api/auth/login` → stores `{ token, bhaktName, role }` in `localStorage` → `navigate('/admin')`.
- [ ] `FE/src/App.jsx` — add `<Route path="/admin-login" element={<AdminLogin />} />`.
- [ ] `FE/src/pages/Admin.jsx` — guard on mount (redirect if no token or `role !== 'admin'`); add a password text field to each contestant row → PATCH `/api/krishnaDas` with `{ id, password }`.
- [ ] Admin uses this page to set every contestant's initial password. Distribute via WhatsApp.
- [ ] Verify: open `/admin` while logged out → redirected to `/admin-login`. Log in as admin → land on `/admin`. Set a password for a contestant → row updates. Open in incognito → still locked out.

### Phase 5 — Android login screen (real auth from day one)

Reference: `AUTH.md` §8.

- [ ] `domain/model/AuthSession.kt` — data class: `bhaktName`, `role`, `token`, `expiresAt`.
- [ ] `domain/auth/AuthRepository.kt` — interface: `login`, `changePassword`, `currentSession`, `signOut`.
- [ ] `domain/auth/AuthError.kt` — sealed: `InvalidCredentials`, `NoPasswordSet`, `Network`, `Unknown`.
- [ ] `data/local/SessionPrefs.kt` — encrypted DataStore wrapper for `AuthSession`. Use `androidx.security:security-crypto`.
- [ ] `data/auth/RealAuthRepository.kt` — calls `POST /api/auth/login` + `POST /api/auth/change-password`; persists session via `SessionPrefs`.
- [ ] `data/remote/ApiClient.kt` — OkHttp interceptors: (a) inject `Authorization: Bearer <token>` from `SessionPrefs`; (b) on 401, call `authRepository.signOut()`.
- [ ] `ui/auth/login/LoginUiState.kt` — sealed: `Idle`, `Submitting`, with fields for `bhaktName`, `password`, inline `error`.
- [ ] `ui/auth/login/LoginViewModel.kt` — exposes `uiState`, `onNameChange`, `onPasswordChange`, `onSubmit`.
- [ ] `ui/auth/login/LoginScreen.kt` — searchable `bhaktName` dropdown (from `ContestantRepository`), password field with show/hide, submit button, inline error, "Forgot password? Contact admin." footer.
- [ ] `ui/navigation/AppNavigation.kt` — observe `authRepository.currentSession()`: null/expired → `Login`, valid → `Home`.
- [ ] `di/ServiceLocator.kt` — wire `RealAuthRepository`.
- [ ] Wire `MainActivity` to render `AppNavigation()` inside the app theme.
- [ ] Verify: fresh install → Login shows names from prod API → wrong password → inline error → correct password → land on Home. Kill app → reopen → goes straight to Home. Wait 30 days (or manually expire) → next launch → back to Login.

### Phase 6 — Home screen
- [ ] `ui/home/HomeUiState.kt` — `bhaktName`, `todayCount`, `weekCount`, `syncedAt`, `isSyncing`, `error`.
- [ ] `ui/home/HomeViewModel.kt` — collects from `CounterRepository`, exposes `onTap()`, debounces sync (3s after last tap OR every 25 taps), refreshes from server on resume.
- [ ] `ui/home/components/CounterButton.kt` — large circular tappable button showing today count, haptic feedback on tap.
- [ ] `ui/home/components/StatTile.kt` — reusable tile for "Today" and "This Week".
- [ ] `ui/home/components/SyncStatusBar.kt` — "Synced 3s ago" / "Sync pending…" / "Sync failed — retrying".
- [ ] `ui/home/HomeScreen.kt` — assembles header (bhaktName + Sign out button), counter, stat tiles, sync bar.
- [ ] Unit test: `HomeViewModelTest` — taps update UI state immediately; sync fires after debounce window.
- [ ] Verify: tap 10 times → UI shows immediately → wait 3s → web admin shows updated count.

### Phase 7 — Polish
- [ ] App icon via Image Asset Studio (use a simple mala / Krishna motif).
- [ ] Splash screen (`androidx.core:core-splashscreen`).
- [ ] Status bar color matching theme.
- [ ] Error toast on sync failure with retry behavior.
- [ ] "Sign out" button on Home → clears `SessionPrefs` → returns to Login.
- [ ] "Change password" menu item on Home → calls `authRepository.changePassword()`.
- [ ] Loading skeletons for first fetch on Home.
- [ ] Pull-to-refresh on Home (re-runs `loadInitial`).

### Phase 8 — Ship & document
- [ ] Flip the rollout feature flag on `POST /api/scores` from "verify-if-token" to "always require token" (AUTH.md §11 step 7).
- [ ] Build debug APK (`./gradlew assembleDebug`), install on a real phone, run the full verification checklist from the plan.
- [ ] Generate a release keystore (`keytool -genkey ...`). Store outside the repo. Document the path in a private note.
- [ ] Configure release signing in `app/build.gradle.kts` reading from `~/.gradle/gradle.properties`.
- [ ] Build release APK (`./gradlew assembleRelease`).
- [ ] Update root `CLAUDE.md` with the Android section (project location, prod API dependency, known limitation: app POST replaces today's count and can overwrite admin edits made mid-session).
- [ ] Decide distribution: direct APK (WhatsApp the contestants) vs. Play Store internal testing track.

---

## Out of scope for v1 (parked for v2)

- Niyam 1/2/3 entry (only naam jaap in v1).
- KeliKunj week leaderboard view, winners, prize pool — view-only would be the v2 add.
- Push notifications (daily reminder, result announcements).
- Offline-first persistence with Room + WorkManager-backed sync queue. Current v1 keeps `sessionDelta` in memory only — if the user kills the app before sync, those increments are lost.
- Phone OTP / SMS / WhatsApp / Google Sign-In — explicitly rejected for v1 (zero external dependencies is a hard constraint).
- Self-service password reset — admin resets manually in v1.
- Brute-force lockout on `POST /api/auth/login` — small audience, low risk in v1.
- Refresh tokens — 30-day JWT, re-login on expiry is acceptable.
- iOS — would be a separate Swift project, or a migration to Compose Multiplatform / Kotlin Multiplatform Mobile from this codebase.

## Known v1 limitations to document

- **App POST replaces today's count.** If the web admin edits a contestant's `naamJaapCount` while that contestant is actively tapping the app, the next sync overwrites the admin's edit. Admins should avoid touching `naamJaapCount` directly; they edit niyam points only.
- **Single-device assumption.** If the same contestant is logged in on two phones simultaneously, the last sync wins. Out of scope to detect/merge.
- **No offline durability.** Increments held in `sessionDelta` are RAM-only until synced. App kill before sync = lost increments.
