# Authentication — Single Source of Truth

**Decision: password-based auth, fully built in v1, self-hosted on the existing Vercel/Node backend. No SMS, no Firebase, no third-party providers. Two new npm packages (`bcryptjs`, `jsonwebtoken`); zero external services.**

This document is the canonical reference for every auth-related choice in this project — schemas, endpoints, tokens, password rules, admin bootstrap, client integration. If a code change conflicts with this document, update the doc first.

---

## 1. Identity model

The login identifier is **`bhaktName`** — it's already unique in `KrishnaDas` and already used as the cross-app key (FE → BE → MongoDB). Reusing it means:

- No new "username" concept to maintain or migrate.
- The Android login dropdown can be populated directly from `GET /api/krishnaDas`.
- Existing data (sadhana, keliKunj winners) needs no relinking.

`phone` and `email` on `KrishnaDas` stay as **contact-only** fields. They are not login identifiers in v1. (Adding phone-based login later only requires another endpoint; the schema already has the field.)

---

## 2. Schema changes — `KrishnaDas` (only collection that changes)

File: `hare-krishna-tournament/BE/DB/models/KrishnaDas.js`

All four auth-related fields live under a single **`auth` subdocument** so credential/role state is cleanly separated from the contestant's profile data.

```js
const AuthSchema = new mongoose.Schema({
  passwordHash:  { type: String, default: null },   // bcrypt hash (60 chars). null = no password set yet
  passwordSetAt: { type: Date,   default: null },   // last time password was set/changed
  role:          { type: String, enum: ['contestant', 'admin'], default: 'contestant' },
  lastLoginAt:   { type: Date,   default: null },   // activity monitoring; nullable
}, { _id: false });

const KrishnaDasSchema = new mongoose.Schema({
  bhaktName:         { type: String,  required: true, trim: true, unique: true },
  email:             { type: String,  trim: true, lowercase: true },
  phone:             { type: String,  trim: true },
  sansarName:        { type: String,  trim: true },
  includeInKeliKunj: { type: Boolean, default: true },

  auth:              { type: AuthSchema, default: () => ({}) },
}, { ... });
```

`default: () => ({})` on the `auth` field guarantees every doc has at least an empty `auth` subdoc with defaults populated — so `user.auth.role === 'contestant'` is true for every new contestant without explicit assignment, and `user.auth?.passwordHash === null` for everyone until an admin sets one.

**Field rationale:**

| Field | Why included | Why this shape |
|---|---|---|
| `auth.passwordHash` | The actual credential. | `String`, nullable. `null` means "no password yet, can't log in" — admin needs to set one. |
| `auth.passwordSetAt` | Lets the admin spot stale passwords and supports future "rotate every N days" policy. | Plain `Date`. |
| `auth.role` | Distinguishes the admin (who can edit other contestants' data, set initial passwords, manage KeliKunj weeks) from everyone else. | Enum to prevent typos. Default `'contestant'` is safe — new docs aren't accidentally admins. |
| `auth.lastLoginAt` | Lightweight activity signal. Cheap to write, useful for "who hasn't logged in in 30 days?". | Plain `Date`, nullable. |

**Fields explicitly skipped for v1** (documented so we don't second-guess later):

- `loginAttempts` / `lockedUntil` — no brute-force lockout. The audience is ~50 trusted contestants; the attack surface doesn't justify the complexity. Revisit if abuse appears.
- `refreshToken` — no refresh tokens. 30-day JWT, re-login on expiry. Keeps the implementation stateless.
- `sessions` collection — not stateful sessions, JWT only.
- `username` separate from `bhaktName` — reusing `bhaktName` (see §1).
- `passwordResetToken` / `passwordResetExpires` — no self-service reset. Admin resets manually from the web admin page.

No migration script needed: Mongoose applies the defaults on read for existing documents. Documents created before this change simply have `auth.passwordHash: null` (can't log in) and `auth.role: 'contestant'` until the admin updates them.

---

## 3. Token strategy — JWT (HS256)

**Choice: JSON Web Tokens, HS256-signed, stateless.**

| Decision | Value | Why |
|---|---|---|
| Algorithm | `HS256` | Symmetric, single secret, no key-pair management. Fine for a single-backend deployment. RS256 only matters when separate services need to verify without holding the signing key. |
| Secret | `JWT_SECRET` env var on Vercel | Generated once: `openssl rand -base64 48` (48 bytes → ~64 chars). Stored only in Vercel project settings and a sealed local note. |
| Expiry | **30 days** | Sangha context, low-risk audience, friction-minimizing. Users re-login monthly. |
| Refresh tokens | **None** | Adds storage + revocation complexity not worth it at this scale. Re-login is acceptable. |
| Claims | `{ sub: bhaktName, role, iat, exp }` | `sub` = subject = identity. `role` cached in the token so authorization checks don't hit the DB on every request. |
| Header | `Authorization: Bearer <jwt>` | Standard. |
| Rotation | `JWT_SECRET` rotated annually — all sessions invalidate, users re-login. Document the rotation date in the Vercel project notes. |

**No server-side logout.** JWT is stateless: a logout is "client deletes the token." If a token is leaked, the only mitigation in v1 is rotating `JWT_SECRET` (nuclear option, signs everyone out). Acceptable trade-off for v1.

---

## 4. Password policy

| Rule | Value | Why |
|---|---|---|
| Hashing | `bcryptjs` cost factor 10 | Vercel-safe (no native compile), standard cost. ~80ms per hash/compare on Vercel cold start. |
| Min length | 6 characters | Low friction. Stronger rules deter contestants more than attackers at this scale. |
| Max length | 72 bytes (bcrypt's natural limit) | Validated client + server. |
| Complexity rules | **None** | No "must have uppercase + symbol" — empirically these reduce real security by pushing users to predictable patterns. |
| Storage | Hash only — plaintext password never stored, never logged | Standard. Verify with `bcrypt.compare()`. |

Password is required at login time. `auth.passwordHash === null` → return 401 (`"Invalid credentials"`, generic to avoid enumeration).

---

## 5. Endpoints — full list

Two new endpoints; existing endpoints get auth checks added where appropriate.

### New: `POST /api/auth/login`

Public. Body: `{ bhaktName, password }`.

- Lookup `KrishnaDas` by `bhaktName`.
- If not found OR `auth.passwordHash === null` → `401 { error: 'Invalid credentials' }`.
- `bcrypt.compare(password, auth.passwordHash)`. If false → `401 { error: 'Invalid credentials' }`.
- Update `auth.lastLoginAt = now` (fire-and-forget; don't block the response).
- Sign JWT with `{ sub: bhaktName, role, iat: now, exp: now + 30d }`.
- Return `200 { token, bhaktName, role }`.

Always return the same `"Invalid credentials"` message for both "user not found" and "wrong password" — prevents username enumeration.

### New: `POST /api/auth/change-password`

Bearer-token required. Body: `{ currentPassword, newPassword }`.

- Verify JWT, extract `bhaktName`.
- Lookup contestant. If `auth.passwordHash === null` → `400 { error: 'No password set' }`.
- `bcrypt.compare(currentPassword, auth.passwordHash)`. If false → `401 { error: 'Invalid current password' }`.
- Validate `newPassword` length ≥ 6.
- Hash new password (cost 10). Update `auth.passwordHash` + `auth.passwordSetAt = now`.
- Return `204` (no body).

### Existing endpoints — auth additions

| Endpoint | Auth required? | Notes |
|---|---|---|
| `GET /api/krishnaDas` | **Public** | Login screen needs the contestant list before any token exists. Strips `auth.passwordHash`, `auth.passwordSetAt`, `auth.lastLoginAt` from the response (`auth.role` stays visible — not sensitive). |
| `POST /api/krishnaDas` | Admin | Create contestant. |
| `PATCH /api/krishnaDas` | Admin | If body contains `password`, hashes and sets `auth.passwordHash` + `auth.passwordSetAt`. If body contains `role`, sets `auth.role`. (Self-update path skipped in v1 for simplicity.) |
| `DELETE /api/krishnaDas` | Admin | |
| `GET /api/scores` | **Public** | Home page shows leaderboard. |
| `POST /api/scores` | Contestant (or admin) | Verify token. For each `{ bhaktName, naamJaapCount }` in the body, **require the bhaktName to match the token's `sub`** unless `role === 'admin'`. Prevents one user from updating another's count. |
| `GET /api/stats` | **Public** | Public leaderboard. |
| `GET /api/keliKunj` | **Public** | Results / winners displayed publicly. |
| `POST /api/keliKunj` | Admin | Create week. |
| `PATCH /api/keliKunj` | Admin | Declare winners, toggle leaderboard visibility. |

The pattern: **reads are public, writes are authenticated, admin-only writes check the role.**

---

## 6. Backend implementation — file plan

### Files to create

```
hare-krishna-tournament/BE/
├── services/authService.js          # bcrypt + JWT sign/verify
└── middleware/auth.js               # requireAuth(handler) + requireAdmin(handler)

hare-krishna-tournament/BE/api/
└── auth.js                          # POST /login, POST /change-password

hare-krishna-tournament/api/
└── auth.js                          # one-line Vercel re-export of BE/api/auth.js
```

`authService.js` exports:

```js
export function hashPassword(plain)            // → Promise<string>
export function verifyPassword(plain, hash)    // → Promise<boolean>
export function signToken({ bhaktName, role }) // → string
export function verifyToken(token)             // → { sub, role, iat, exp } | throws
```

`middleware/auth.js` exports two higher-order functions that wrap a Vercel handler:

```js
export const requireAuth  = (handler) => (req, res) => { /* verify, attach req.auth, call handler */ }
export const requireAdmin = (handler) => requireAuth((req, res) => { /* check req.auth.role === 'admin' */ })
```

Existing handlers are wrapped where the table above requires it — e.g. `BE/api/scores.js`'s POST branch is gated by token verification + bhaktName-vs-sub check; `BE/api/keliKunj.js`'s PATCH branch is wrapped in `requireAdmin`.

### Files to modify

- `hare-krishna-tournament/BE/DB/models/KrishnaDas.js` — add four fields (§2).
- `hare-krishna-tournament/BE/services/krishnaDasService.js` — `updateKrishnaDas` accepts an optional `password`, hashes it; `findAll` strips sensitive fields when called from a public context (or expose a separate `findAllPublic`).
- `hare-krishna-tournament/BE/api/krishnaDas.js` — wrap POST/PATCH/DELETE in `requireAdmin`; allow self-update for limited fields.
- `hare-krishna-tournament/BE/api/scores.js` — wrap POST in `requireAuth`; verify bhaktName matches token subject (or role is admin).
- `hare-krishna-tournament/BE/api/keliKunj.js` — wrap POST/PATCH in `requireAdmin`.
- `hare-krishna-tournament/server/index.js` — mount `/api/auth` route.
- `hare-krishna-tournament/package.json` — add `bcryptjs`, `jsonwebtoken`.

### Vercel env vars to add

- `JWT_SECRET` — `openssl rand -base64 48` output. Add via Vercel dashboard → Project Settings → Environment Variables → Production + Preview + Development.

### CORS

Existing handlers send `Access-Control-Allow-Origin: *` and allow specific methods. Add `Authorization` to `Access-Control-Allow-Headers` everywhere a write endpoint exists. The middleware can do this in one place.

---

## 7. Admin bootstrap — how the first admin gets created

There's a chicken-and-egg problem: the admin endpoints require admin auth, but no admin exists yet. Choose one (recommended first):

### Option A — manual MongoDB update (recommended for the very first admin)

One-time, via MongoDB Atlas UI or `mongosh`:

```js
use shriKrishna
db.krishnaDas.updateOne(
  { bhaktName: 'Amit' },
  { $set: {
      'auth.role':          'admin',
      'auth.passwordHash':  '$2a$10$...<run bcrypt locally on a chosen initial password>...',
      'auth.passwordSetAt': new Date(),
  }}
)
```

Dot-notation sets the nested fields without overwriting the whole `auth` subdoc — `auth.lastLoginAt` stays untouched (`null` for a fresh admin).

To generate the bcrypt hash locally: `node -e "console.log(require('bcryptjs').hashSync('your-password', 10))"`.

After this, the admin logs into the Android app (or the web admin login form — §9) and can set passwords for every other contestant from the existing `/admin` page (§9).

### Option B — `JWT_SECRET`-protected bootstrap endpoint (skip unless needed)

A `POST /api/auth/bootstrap-admin` that accepts the `JWT_SECRET` value as a one-time auth header and promotes the named contestant. More complex, more surface area. Only worth it if you'll bootstrap admins frequently — which we won't.

**Decision: Option A.** One admin, one MongoDB update, documented in this file.

---

## 8. Android client integration

Since auth is real in v1, there is **no `FakeAuthRepository`** — only `AuthRepository` (interface) and `RealAuthRepository` (implementation). The interface still lives in `domain/` so a future swap (e.g. to phone OTP) stays a single-file change.

```
domain/
├── model/AuthSession.kt              # bhaktName, role, token, expiresAt
└── auth/
    ├── AuthRepository.kt             # interface
    └── AuthError.kt                  # sealed: InvalidCredentials, NoPasswordSet, Network, Unknown

data/
├── auth/
│   └── RealAuthRepository.kt         # POST /api/auth/login, change-password, signOut
└── local/
    └── SessionPrefs.kt               # EncryptedDataStore — AuthSession

ui/auth/login/
├── LoginScreen.kt
├── LoginViewModel.kt
└── LoginUiState.kt
```

### Interface

```kotlin
interface AuthRepository {
    suspend fun login(bhaktName: String, password: String): Result<AuthSession>
    suspend fun changePassword(current: String, new: String): Result<Unit>
    suspend fun currentSession(): AuthSession?
    suspend fun signOut()
}
```

### Behavior

- Storage: `EncryptedDataStore` (Jetpack Security) — token never lands in plain SharedPreferences.
- Attachment: `ApiClient` has an OkHttp interceptor that injects `Authorization: Bearer <token>` on every request when a session exists.
- 401 handling: a second interceptor catches `401`, calls `authRepository.signOut()`, and the navigation observer kicks the user back to `LoginScreen` with a "Session expired, log in again" toast.
- Expiry awareness: on cold start, compare `expiresAt` to now — if past, sign out before the user even hits a 401.

### Login screen

Single screen. Searchable `bhaktName` dropdown (loaded from `GET /api/krishnaDas`), password field with show/hide toggle, "Log in" button, inline error label. Footer: *"Forgot password? Contact admin."* (no self-service reset in v1).

---

## 9. Web admin integration (v1 scope — small but necessary)

Adding `requireAdmin` to `POST /api/krishnaDas`, `PATCH /api/krishnaDas`, `POST /api/keliKunj`, `PATCH /api/keliKunj` **will break the existing `/admin` page** unless the page sends an admin JWT. Therefore the web admin needs a login flow as part of v1.

Scope on the web side (kept minimal):

- **`FE/src/pages/AdminLogin.jsx`** (new) — bhaktName text input + password input → calls `POST /api/auth/login` → stores `{ token, bhaktName, role }` in `localStorage` → redirects to `/admin`.
- **`FE/src/pages/Admin.jsx`** — on mount, read token from `localStorage`. If missing or role !== admin → redirect to `/admin-login`. Add a password field to the contestant row so admin can set/reset each contestant's initial password.
- **`FE/src/api/axiosClient.js`** — request interceptor that injects `Authorization: Bearer <token>` if present in `localStorage`; response interceptor that clears the token on 401 and redirects to `/admin-login`.
- **`FE/src/App.jsx`** — add `<Route path="/admin-login" element={<AdminLogin />} />`.

Public pages (`/`, `/announcement`, `/declaration`, `/results`) need no changes — they only read.

Token in `localStorage` is acceptable here because:
- The admin web is used by one or two trusted operators on their own machines.
- The bar for "good enough" web admin auth on a sangha tool is low.
- Migrating to a cookie-based session adds CSRF concerns we don't otherwise have.

---

## 10. Security posture (v1)

What's covered:

- HTTPS only (Vercel terminates TLS).
- Passwords stored as bcrypt hashes only.
- JWT signed with a strong server-side secret.
- Per-user authorization on `POST /api/scores` (you can only update your own count).
- Admin-only mutations on `KrishnaDas` and `KeliKunj`.
- 401-driven sign-out on the client.

What's deferred:

- **Brute-force lockout** — no rate limiting on `POST /api/auth/login`. Small audience, low risk. Add Vercel Edge Middleware or Upstash Ratelimit if abuse appears.
- **Refresh tokens / silent re-auth** — users re-login every 30 days. Acceptable.
- **CSRF** — no cookies = no CSRF concern. Stay on bearer tokens.
- **Password reset by self-service** — admin resets manually.
- **Audit log** of admin actions — useful, not v1.
- **Per-IP allowlist for admin** — overkill at this scale.

---

## 11. Rollout order (so nothing breaks mid-deploy)

Deploys are atomic per Vercel push, but the human-side rollout sequence matters because the web admin and Android app will be in different states:

1. **Backend deploy** with the new fields, endpoints, and middleware **wrapped in feature-flag-style "if token present, verify; if absent, allow" mode** for `POST /api/scores` — keeps the existing web admin and `scripts/updateScores.js` working during the transition.
2. **Set `JWT_SECRET`** in Vercel env.
3. **Manually promote one admin** via MongoDB Atlas (§7 Option A).
4. **Web admin login + password-setting** ships. Admin logs in, sets every contestant's password.
5. **Notify contestants** of their passwords via WhatsApp.
6. **Android app ships** with real auth.
7. **Flip the feature flag**: `POST /api/scores` now requires a token always. `scripts/updateScores.js` updated to acquire and pass a token, or retired in favor of the app.

After step 7, the system is fully authenticated end-to-end. Steps 1–6 can run over a few days; step 7 should follow within a week to close the gap.

---

## 12. Files touched — complete checklist

### Backend (new)
- `BE/services/authService.js`
- `BE/middleware/auth.js`
- `BE/api/auth.js`
- `api/auth.js` (one-line Vercel re-export)

### Backend (modified)
- `BE/DB/models/KrishnaDas.js`
- `BE/services/krishnaDasService.js`
- `BE/api/krishnaDas.js`
- `BE/api/scores.js`
- `BE/api/keliKunj.js`
- `server/index.js`
- `package.json`
- Vercel env vars: add `JWT_SECRET`

### Frontend web (new)
- `FE/src/pages/AdminLogin.jsx`

### Frontend web (modified)
- `FE/src/App.jsx` (route)
- `FE/src/pages/Admin.jsx` (auth guard + password field)
- `FE/src/api/axiosClient.js` (token interceptor)

### Android — see `TASKS.md` Phase 3 for the full breakdown
- `domain/model/AuthSession.kt`, `domain/auth/AuthRepository.kt`, `domain/auth/AuthError.kt`
- `data/auth/RealAuthRepository.kt`
- `data/local/SessionPrefs.kt`
- `data/remote/ApiClient.kt` (bearer + 401 interceptors)
- `ui/auth/login/LoginScreen.kt`, `LoginViewModel.kt`, `LoginUiState.kt`
- `ui/navigation/AppNavigation.kt` (observe session)
- `di/ServiceLocator.kt` (wire `RealAuthRepository`)

### One-time MongoDB operation
- Promote the first admin via Atlas (§7 Option A). Document the chosen `bhaktName` in a private note (not in the repo).


  - role (String) = "admin"
  - passwordHash (String) = "$2a$10$i96bjm3YNu8a3xlKTWscUuoFXfFJpW0dPSPw36IphKthJtqaLo7L6"
  - passwordSetAt (Date) = today