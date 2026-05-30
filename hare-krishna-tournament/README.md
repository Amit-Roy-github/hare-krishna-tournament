# Hare Krishna Tournament

Vite + React frontend, Express/Vercel API backend, MongoDB Atlas storage, and an
Android counter app (`/android`). Deploys to Vercel from `main`.

## Runtime requirement — Node **25**

This project runs on **Node 25**. Use it for everything: `npm install`, `npm run
dev`, the local server, scripts under `BE/scripts/`, and **the Vercel CLI**
(deploy + `vercel git connect` etc.). Older Node versions (16, 20) miss APIs the
Vercel CLI uses (`ReadableStream`, etc.) and have failed locally in the past.

Switch via nvm before running anything:

```sh
nvm use 25
node -v   # → v25.x
```

Or invoke the v25 binary explicitly when Node isn't on the active PATH:

```sh
~/.nvm/versions/node/v25.9.0/bin/node ./node_modules/.bin/vercel --prod --yes
```

## Quick commands

| What                   | Command                                       |
|------------------------|-----------------------------------------------|
| Frontend dev server    | `npm run dev`                                 |
| Local Express server   | `npm run server`                              |
| Deploy to prod         | `vercel --prod --yes` (Node 25)               |
| Fix today count (one)  | `node BE/scripts/fix-today-count.js "<name>" <n>` |

## Database connection pattern

`BE/DB/connection.js` caches the Mongoose connection on `global._mongooseCache`
so it survives **warm** Vercel invocations. A real TCP connect only happens on
cold starts; warm requests reuse the cached `conn`. Don't refactor this — it's
the standard serverless + Mongoose pattern.
