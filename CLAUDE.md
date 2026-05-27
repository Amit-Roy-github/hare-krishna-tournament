# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

This repo contains two unrelated projects. Almost all real work is in `hare-krishna-tournament/`; `vite-project/` is an untouched Vite+React starter and can be ignored unless explicitly asked.

All commands below are run from `hare-krishna-tournament/`.

## Runtime

**Node 25** is the target version for local dev going forward (matches what Vercel runs in prod). Node ≤ 18 will crash on Mongo handshake because `mongodb@7` requires `globalThis.crypto` (only available on Node ≥ 19). If `nvm` is installed: `nvm use 25` before running anything in this repo.

## Commands

- `npm run dev` — Vite dev server (frontend, serves `FE/`).
- `npm run server` (or `npm start`) — Express API on `http://localhost:3001`. Required alongside `dev` because Vite proxies `/api` → `:3001`. Loads `.env.local` via `dotenv/config` so `JWT_SECRET` and `STRICT_AUTH` are available.
- `npm run build` — `vite build` → outputs to `dist/` (Vercel's `outputDirectory`).
- `npm run preview` — serve the built bundle.
- `npm run lint` — ESLint over `.js`/`.jsx`, `--max-warnings 0`.
- `node scripts/updateScores.js` — one-off script that POSTs hardcoded naam-jaap counts to the production Vercel URL. Edit the `updates` array first; running it mutates prod data. **Will 401 once `STRICT_AUTH=true` is flipped on Vercel.**

There is no test suite.

## Architecture

### Dual runtime: Vercel serverless ↔ local Express

The API is written as Vercel function handlers (`(req, res) => …`), but the same handlers also serve the local dev API:

- `api/*.js` (top level) are one-line re-exports of `BE/api/*.js`. They exist solely because Vercel discovers serverless functions in `/api`. **Do not put logic here** — edit `BE/api/*.js`.
- `server/index.js` mounts those same handlers on an Express app (`app.all('/api/scores', wrap(scores))`, …) for `npm run server`.
- Adding a new endpoint means: (1) create `BE/api/foo.js`, (2) create one-line re-export `api/foo.js`, (3) mount it in `server/index.js`.

`vite.config.js` sets `root: 'FE'` and `build.outDir: '../dist'`, so the frontend lives under `FE/` (index.html, src/) but the dist is at the project root.

### Backend layers (`BE/`)

`api → services → DB/models`. Handlers are thin: connect, validate, delegate to a service, return JSON. All cross-cutting logic (scoring math, week boundaries, populate joins) lives in `BE/services/`.

`BE/DB/connection.js` caches the mongoose connection on `global._mongooseCache` so warm Vercel invocations reuse it. The Mongo URI and DB name (`shriKrishna`) are hardcoded — do not rewrite this to read from env without coordinating, since it will break on Vercel where the env var isn't set.

Models use the `mongoose.models.X || mongoose.model('X', …)` idiom to survive hot reloads / repeated imports in serverless. Keep this pattern when adding models.

### Domain model

Three collections, all in the `shriKrishna` DB:

- **KrishnaDas** — a contestant. `bhaktName` is the unique identifier used across the app (not `_id`). `includeInKeliKunj: false` excludes them from leaderboards and stats but keeps history.
- **Sadhana** — one record per contestant per day, keyed by `(krishnadasId, date)` where `date` is **midnight UTC** of that day. Use `getDayStart()` in `sadhanaService.js` for any date math; do not construct dates inline.
- **KeliKunj** — a weekly competition (`keliKunjWeek` is a number). Holds `winners` (refs to 5 KrishnaDas slots: `_1`, `_2`, `_3`, `maxNaamJaap`, `totalMaxNaamJaap`), `prizePool` (prize + title per slot), and two booleans the admin toggles: `resultDeclared` and `showLeaderboard`.

### Scoring rule

A day's score = `niyam1.point + niyam2.point + niyam3.point + floor(naamJaapCount / 1000) * 10`. This formula appears in both `buildScoresResponse` and `buildStatsResponse` in `sadhanaService.js` — keep them in sync if you change it.

`buildStatsResponse` also computes `heroName` (max single-day naam) and `legendName` (max total naam across the week). The week starts Monday UTC.

### Frontend (`FE/src`)

React 18 + react-router-dom v7. Five routes wired in `App.jsx`: `/`, `/announcement`, `/declaration`, `/admin`, `/results`. API calls go through `FE/src/api/axiosClient.js` (baseURL `/api`, which the Vite proxy forwards in dev and Vercel rewrites serve in prod).

`FE/src/utils/weekTitle.js#getWeekTitle(weekNo)` is the single source of truth for the week label shown across pages: `weekNo % 4 === 0` → `"GRAND FINALE"`, else Sunday → `"FINALE"`, else `"Week N"`. Reuse it rather than re-implementing.

### Vercel routing

`vercel.json` rewrites every non-API, non-asset path to `/index.html` for the SPA. Anything under `/api/*` hits the serverless function of the same name.
