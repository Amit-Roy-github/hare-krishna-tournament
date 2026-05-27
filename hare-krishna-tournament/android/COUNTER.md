# Counter Logic — Full Reference

The chant counter on the Home screen is the entire reason the app exists. It needs to feel instant on every tap, never lose a count, sync efficiently, recover from network failures, support a client-side "reset" without lying to the server, and stay accurate as days roll over. This doc lays out the mental model, the state machine, and every edge case it handles.

Source of truth in code:
- State shape  → `domain/model/CounterState.kt`
- Repository   → `data/repository/CounterRepository.kt`
- ViewModel    → `ui/home/HomeViewModel.kt`
- Persistence  → `data/local/UserPrefs.kt`
- Day boundary → `domain/util/TimeUtil.kt`

---

## 1. State model

```kotlin
data class CounterState(
    val serverBaseline:   Int  = 0,    // last value the server confirmed for today
    val sessionDelta:     Int  = 0,    // taps since last successful sync
    val todayResetOffset: Int  = 0,    // local "hide everything before this" offset
    val weekTotal:        Int  = 0,    // Monday→today (server)
    val lifetimeTotal:    Int  = 0,    // all-time (server)
    val syncedAt:         Long? = null,
    val isLoading:        Boolean = false,
    val isSyncing:        Boolean = false,
    val error:            String? = null,
)
```

### Display derivation

```
displayedToday = max(0, serverBaseline + sessionDelta - todayResetOffset)
hasPending     = sessionDelta > 0
```

`weekTotal` and `lifetimeTotal` are shown **as-is** from the server. The reset offset is a today-only display tweak — it never touches week or lifetime values.

### Why three numbers for "today"

| Field | What it is | Lives in | Refreshed when |
|---|---|---|---|
| `serverBaseline` | What the server thinks today's count is | RAM (lost on app kill) | `loadInitial()` — on app open + every resume |
| `sessionDelta` | Local-only buffer of taps not yet sent | RAM (lost on app kill) | Every tap (`++`); zeroed on successful sync |
| `todayResetOffset` | How much to hide from today's display | Disk (`UserPrefs`, encrypted? no — plain DataStore; keyed by `bhaktName` + timestamp) | User taps "Reset today's display"; auto-cleared at midnight UTC |

This split is intentional: the UI updates from `sessionDelta` instantly (0ms tap latency), the server reconciles in the background, and the reset offset is a user-visible illusion layered on top.

---

## 2. Sync flow

The hard constraint: **the backend's `POST /api/scores` does `$set: { naamJaapCount }` — it replaces, not increments.** So the client always POSTs an *absolute* count, never a delta.

```
TAP                          serverBaseline=500   sessionDelta=0   reset=0   display=500
 │
 │  sessionDelta++             serverBaseline=500   sessionDelta=1   reset=0   display=501
 │
 │  schedule debounced sync (3s after last tap; reset on every new tap)
 │
 │  …user taps a few more…    serverBaseline=500   sessionDelta=5   reset=0   display=505
 │
 │  (3s elapses with no new tap)
 │
 ▼
SYNC
 │  flushedDelta := sessionDelta     // capture for race-safety
 │  targetCount  := serverBaseline + flushedDelta
 │  POST /api/scores [{ bhaktName, naamJaapCount: targetCount }]
 │
 │  ON SUCCESS:
 │    serverBaseline = targetCount             // server confirmed
 │    sessionDelta  -= flushedDelta            // subtract what we flushed
 │                                             // (more taps may have come in
 │                                             //  during the POST — they roll
 │                                             //  into the next sync)
 │    weekTotal     += flushedDelta            // bump display optimistically
 │    lifetimeTotal += flushedDelta
 │    syncedAt       = now
 │
 │  ON FAILURE:
 │    sessionDelta stays as-is                 // retry on next tap or resume
 │    isSyncing = false, error = "Sync failed"
```

### Two sync triggers

1. **Debounce** — `tapTrigger.debounce(3_000ms).collect { sync() }` in `HomeViewModel.init`. Quiet user means quiet network.
2. **Tap threshold** — if `sessionDelta >= 25` after a tap, fire sync immediately. Protects against losing big bursts to an app crash.

### Resume refresh

`HomeScreen` adds a `DisposableEffect` lifecycle observer that calls `viewModel.refresh()` on every `ON_RESUME`. This catches:
- Admin edits made via the web while the user was away
- Day rollover (resets `serverBaseline` to the new day's value)
- Token expiry catching up

---

## 3. Reset flow (client-side only)

**Server is never touched.** The reset is a local display tweak.

```
BEFORE RESET                 serverBaseline=500   sessionDelta=0   reset=0     display=500

user opens Settings → Reset today's display → confirms

RESET ACTION (CounterRepository.resetToday)
 │
 │  newOffset := serverBaseline + sessionDelta = 500
 │  UserPrefs.setTodayResetOffset(bhaktName, 500)
 │      → stored under  today_reset_offset_<bhaktName>  (Int)
 │      → setAt stored under  today_reset_set_at_<bhaktName>  (Long ms)
 │  state.update { todayResetOffset = 500 }
 │
 ▼
AFTER RESET                  serverBaseline=500   sessionDelta=0   reset=500   display=0   ✓

tap counter once

                             serverBaseline=500   sessionDelta=1   reset=500   display=1

sync fires (3s)
  POST /api/scores { naamJaapCount: 501 }    ← absolute true count, NOT 1
  SERVER  todayNaam = 501                    ← unaware of the reset

                             serverBaseline=501   sessionDelta=0   reset=500   display=1   ✓
                             weekTotal     += 1                                            ✓
                             lifetimeTotal += 1                                            ✓
```

Tournament leaderboards, admin views, /api/stats, /api/scores, the prod scoreboard — **none of it sees the reset**. The user's true contribution is preserved.

---

## 4. Auto-expiration

The reset offset auto-clears at the next midnight UTC. Implementation:

```kotlin
// UserPrefs.kt
suspend fun getTodayResetOffset(bhaktName: String): Int {
    val setAt  = data[resetSetAtKey(bhaktName)]  ?: 0L
    val offset = data[resetOffsetKey(bhaktName)] ?: 0
    return if (setAt >= TimeUtil.todayStartUtcMs()) offset else 0
}

// TimeUtil.kt — matches BE/services/sadhanaService.js:getDayStart
fun todayStartUtcMs(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
```

Yesterday's reset doesn't suppress today's chants. No background job, no scheduler — just a timestamp comparison on every `loadInitial()`.

---

## 5. Per-user storage

The reset offset is keyed by `bhaktName`:

```
today_reset_offset_Rama Das    →  500
today_reset_set_at_Rama Das    →  1748332800000
today_reset_offset_Gopala Das  →  0
today_reset_set_at_Gopala Das  →  0
```

If two users share a phone, each gets their own offset. `UserPrefs.getTodayResetOffset(currentBhaktName)` looks up the right one.

(`bounceEnabled` and `tapAnywhereEnabled` are *not* per-user — they're device-wide preferences. Different design intent.)

---

## 6. Race conditions & how they're handled

| Scenario | What could break | How it's handled |
|---|---|---|
| User taps **during** an in-flight sync | The new tap might get "lost" if we zero `sessionDelta` on success | `sync()` captures `flushedDelta` at the start, then *subtracts* it (rather than zeroing) on success. Mid-sync taps roll into the next batch. |
| Two consecutive syncs in flight | Out-of-order responses corrupt state | Not possible in practice — the debounce flow guarantees only one sync goes out at a time. Tap-threshold sync runs in a separate coroutine but updates the same atomic `_state`. |
| Sync succeeds, `loadInitial` runs immediately after | `loadInitial` resets `sessionDelta` to 0; if a tap came in between, it's lost | `loadInitial` does set `sessionDelta = 0` — but it also re-fetches `serverBaseline` from the server (which now has the flushed taps included), so the count is correct. A tap that arrived strictly between sync-success and loadInitial-success is technically lost. Acceptable v1 edge case. |
| App killed before sync | Unsynced taps in RAM die with the process | Acceptable for v1. Room + WorkManager would fix this; out of scope. |
| Network flaky, retries | Old POST eventually succeeds with stale count | Server uses `$set`, so the most recent POST wins. Loss is bounded to whatever delta was in flight at the moment of bad connectivity. |
| Day rolls over mid-session | App still has yesterday's `serverBaseline` | `ON_RESUME` triggers `refresh()` which `loadInitial`s fresh values. Next interaction sees today's truth. |
| Token expires (30d) | Sync POSTs return 401 | `ApiClient`'s OkHttp interceptor catches 401 → clears `SessionPrefs` → `AppNavigation` observes the null session → user routed back to Login. |
| Admin edits user's naamJaap via web while user is mid-session | Next sync POST overwrites the admin's edit | **Documented limitation.** Admins shouldn't touch naam-jaap directly; they edit niyam points only. |
| User signs out with pending taps | Unsynced count is lost | `signOut()` does a final `sync()` before clearing the session. Worst case: that sync fails too, and the taps are gone. |

---

## 7. Knobs

Constants in `HomeViewModel.kt`:

```kotlin
private const val SYNC_DEBOUNCE_MS   = 3_000L  // ms after last tap before sync fires
private const val SYNC_TAP_THRESHOLD = 25      // unsynced taps that force immediate sync
```

`bounceEnabled` (pulse animation on tap) and `tapAnywhereEnabled` (whole screen as tap target) are user-controllable from the Settings sheet; defaults `true` and `false` respectively.

The counter size is hardcoded `280.dp` in `CounterButton.kt`. Haptic style is `HapticFeedbackType.TextHandleMove` and lives in `HomeScreen` (the composable) not the button (the button is pure visual).

---

## 8. What the server sees vs what the user sees

| Moment | Server's `naamJaapCount` for today | App display |
|---|---|---|
| Cold start, no taps | 0 | 0 |
| User taps 50 times, hasn't synced | 0 | 50 (sessionDelta) |
| Sync fires | 50 | 50 |
| User taps 10 more, syncs | 60 | 60 |
| User resets, taps 5 more, syncs | 65 | 5 |
| Admin opens `/admin` and edits user to 100 | 100 | 5 *(next refresh: 100 - 65 = 35)* |
| Day rolls over | 0 | 0 *(offset auto-cleared)* |

The leaderboard sees the true server count throughout. The user sees what they personally find meaningful, with no way to lie to the tournament.

---

## 9. Out of scope (parked)

- **Offline durability** of `sessionDelta` — Room DB + WorkManager would persist taps across app death. Not in v1.
- **Pull-to-refresh** on Home — `refresh()` only fires on `ON_RESUME` today. PTR adds a SwipeRefresh wrapper. Easy add when needed.
- **Visual cue that today is in "reset display" mode** — currently no on-screen indication that the offset is active. Could add a small "↺ display reset earlier" chip near the Today tile. Skipped to keep v1 clean.
- **Sync timestamp ticker** — `SyncStatusBar` shows "Synced 5s ago" but doesn't auto-tick. Recomposes on state change. Add a `LaunchedEffect { while (true) { delay(15s); trigger recompose } }` when annoyance is observed.
