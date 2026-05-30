# Counter — Sync Model

The chant counter on the Counter screen is the reason the app exists. It must feel instant, never lose a tap, sync efficiently, recover from failures, support a client-only reset, and stay accurate as days roll over.

Source of truth in code:
- Server schema      → `BE/DB/models/Sadhana.js`
- Server math        → `BE/services/sadhanaService.js` (`applyDeviceCount`)
- Per-user stats     → `BE/services/sadhanaService.js` (`getKrishnaDasStats`)
- Per-user endpoint  → `BE/api/krishnaDasStats.js` — `GET /api/krishnaDasStats`
- Client store       → `android/.../data/local/CounterStore.kt`
- Client repository  → `android/.../data/repository/CounterRepository.kt`
- Client ViewModel   → `android/.../ui/counter/CounterViewModel.kt`

> **Do not use `/api/scores` or `/api/stats` for per-user display.** Those are the leaderboard and filter by `includeInKeliKunj`. For anything that shows a single user's numbers, use `getKrishnaDasStats` / `GET /api/krishnaDasStats`.

---

## 1. Server schema (`Sadhana`)

One document per **(krishnadas, day UTC)**. Unique index on the pair.

```js
{
  krishnadasId:  ObjectId,
  date:          Date,        // midnight UTC
  naamJaapCount: Number,      // the truth value; sums all devices
  niyam1/2/3:    { point, doneAt },
  deviceSnapshots: {          // per-device high-water marks
    "<deviceId>": { naamJaapCount: Number }
  }
}
```

- `naamJaapCount` is the leaderboard number.
- `deviceSnapshots[deviceId].naamJaapCount` is the last `dayTotal` the server accepted from that device.
- Snapshots are per-device so multiple devices for the same user add cleanly.

---

## 2. Client data model

```kotlin
DayEntry(date: String, dayTotal: Int, daySynced: Int)
```

Kept in `CounterStore` (DataStore), keyed by `bhaktName`:

- `displayCount` — user's personal continuous counter. Only `resetToday()` zeros it.
- `days[]` — one entry per UTC day the user tapped.
  - `dayTotal`  = taps on this device that day. Monotonic.
  - `daySynced` = how much of `dayTotal` the server has confirmed.
- `deviceId` — UUID, generated once per install, stored separately.

---

## 3. Tap → increment (instant)

`CounterRepository.increment()` on every tap:

- Find today's `DayEntry` (create if absent).
- `dayTotal += 1`.
- `displayCount += 1`.
- Emit new state → UI updates immediately.

No network. No disk. RAM only.

---

## 4. Persist (`flush`, debounced)

`flush()` writes `displayCount` + `days[]` to DataStore. Fired by:

- 2-second debounce after the last tap.
- Inside `sync()` on success.
- On screen leave and sign-out.

Local durability only — never hits the server.

---

## 5. Sync to server (mutex-guarded)

`sync()` does:

```
pending = days.filter { dayTotal > daySynced }
if empty → flush() and return success
POST /api/naam with [{ deviceId, date, total: dayTotal } for each pending day]
on success:
  for each (date, total) we sent:
    days[date].daySynced = max(daySynced, total)
  prune past days where dayTotal == daySynced
  recompute pendingCount
  update todayServer from response
  flush()
on failure:
  isSyncing = false, error = "Sync failed"
```

Triggers:
1. **5-min idle debounce** after the last tap.
2. **108-tap threshold** (one mala) → immediate sync.
3. **Screen leave** (`ON_PAUSE`, `onDispose`) → flush + sync.
4. **Sign-out** → sync first, then clear session.

A `Mutex` around the whole sync body prevents two triggers from posting in parallel.

---

## 6. Server math (`applyDeviceCount`)

One atomic aggregation-pipeline update per `(krishnadas, date)` doc:

```
snap = doc.deviceSnapshots[deviceId].naamJaapCount  ?? 0
diff = max(0, clientTotal − snap)
naamJaapCount += diff
deviceSnapshots[deviceId].naamJaapCount = max(snap, clientTotal)   ← MONOTONIC
```

- `diff` clamped at 0 → a late stale POST can never subtract.
- Snapshot is a **high-water mark**: `max(snap, clientTotal)`. A reordered or retried POST with a smaller `clientTotal` is a no-op for the snapshot, so it can never drag it down and prime a future over-count.
- The whole update is one `findOneAndUpdate` with `updatePipeline: true` → atomic per doc.
- Retry of the same `clientTotal` re-computes `diff = 0` → idempotent.
- Different devices have different snapshot keys → multi-device adds cleanly.
- Defense-in-depth: if a single call's `diff > 5000` (`SUSPICIOUS_DIFF`), the server logs a warning — full diff is still applied so we don't silently under-count.

---

## 7. Day rollover

- The day key comes from `TimeUtil.todayKeyUtc()` at tap time.
- Past midnight UTC, new taps land in a new `DayEntry` with `daySynced = 0`.
- Yesterday's entry stays in `days[]` until its `daySynced == dayTotal`, then pruning removes it.
- No midnight event needed; the app can be closed all night.
- `displayCount` is **not** reset at midnight — only the user resets it.

---

## 8. Invariant the whole design rests on

> `deviceSnapshots[deviceId].naamJaapCount` is a monotonically non-decreasing high-water mark of what that device has reported for this day.

Nothing — not the sync handler, not the admin tools, not a manual Mongo query — should ever write a snapshot value lower than the current one.

If you break this invariant, the next sync from that device re-credits the gap and the count blows up. (This is what bit us once: the original code did `snapshot ← safeTotal` instead of `snapshot ← max(snap, safeTotal)`, and the recovery script cleared snapshots entirely — both leaked from the same invariant.)

The recovery script `BE/scripts/fix-today-count.js` deliberately leaves `deviceSnapshots` alone for this reason.

---

## 9. Sync triggers — at a glance

| Trigger                 | When                                  | Effect           |
|-------------------------|---------------------------------------|------------------|
| Idle debounce           | 5 min after the last tap              | sync             |
| Mala threshold          | `pendingCount >= 108`                 | immediate sync   |
| Screen leave / pause    | `ON_PAUSE`, `onDispose`               | flush + sync     |
| Sign-out                | before clearing session               | sync             |
| Persist debounce        | 2 sec after the last tap              | flush only       |

---

## 10. Recovery script

`BE/scripts/fix-today-count.js "<bhaktName>" <targetCount>`

Sets today's `naamJaapCount` to the target and clears `deviceSnapshots` (so the next sync doesn't immediately re-bump the count by `dayTotal − 0`). Run with Node 25.
