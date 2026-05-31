import Sadhana    from '../DB/models/Sadhana.js';
import KrishnaDas from '../DB/models/KrishnaDas.js';

// ── Helpers ─────────────────────────────────────

// Returns midnight UTC for a given date (defaults to today)
function getDayStart(date = new Date()) {
  const d = new Date(date);
  d.setUTCHours(0, 0, 0, 0);
  return d;
}

// Parse a client-supplied day key — "YYYY-MM-DD", epoch ms, or Date — to
// midnight UTC. Falls back to today for missing/invalid input.
function parseDayStart(date) {
  if (date === undefined || date === null) return getDayStart();
  const d = typeof date === 'number'
    ? new Date(date)
    : new Date(`${date}T00:00:00.000Z`);
  if (Number.isNaN(d.getTime())) return getDayStart();
  d.setUTCHours(0, 0, 0, 0);
  return d;
}

// ── Upsert today's sadhana for one krishnadas ───
// If a record exists for today → update it
// If not → create a new one (one record per person per day)

export async function upsertTodaySadhana(krishnaDasId, fields) {
  const today = getDayStart();

  const allowed = ['naamJaapCount', 'niyam1', 'niyam2', 'niyam3'];
  const set = {};
  for (const key of allowed) {
    if (fields[key] !== undefined) set[key] = fields[key];
  }

  return Sadhana.findOneAndUpdate(
    { krishnadasId: krishnaDasId, date: today },
    { $set: set },
    { upsert: true, returnDocument: 'after', setDefaultsOnInsert: true }
  );
}

// ── Idempotent incremental naam sync (Android app) ──
// Per-device high-water mark. The client sends its absolute per-day device
// total; the server adds only the new part — max(0, total - storedSnapshot) —
// to naamJaapCount and advances the snapshot to MAX(storedSnapshot, total).
// The snapshot is monotonic by design, so out-of-order or retried POSTs can
// never drag it down and double-credit on the next sync. All of this happens
// in one atomic single-document update.
export async function applyDeviceCount(krishnaDasId, deviceId, date, total) {
  const day       = parseDayStart(date);
  const safeTotal = Math.max(0, Math.floor(Number(total) || 0));
  const snapPath  = `deviceSnapshots.${deviceId}.naamJaapCount`;

  // Single round-trip: atomic pipeline read+write. We used to do a separate
  // findOne first to log diffs, but each Atlas round-trip is ~200–500ms from
  // Vercel and the extra hop doubled the time per item — which, multiplied
  // by a Promise.all over the client's day backlog, was blowing past the
  // 300s function ceiling.
  const updated = await Sadhana.findOneAndUpdate(
    { krishnadasId: krishnaDasId, date: day },
    [
      {
        $set: {
          naamJaapCount: {
            $add: [
              { $ifNull: ['$naamJaapCount', 0] },
              { $max: [0, { $subtract: [safeTotal, { $ifNull: [`$${snapPath}`, 0] }] }] },
            ],
          },
          // MONOTONIC: snapshot can only move up. A late stale POST with a
          // smaller safeTotal becomes a no-op here. This is the rule the rest
          // of the design rests on.
          [snapPath]: { $max: [ { $ifNull: [`$${snapPath}`, 0] }, safeTotal ] },
        },
      },
    ],
    // Mongoose 9 requires explicit opt-in to treat the array as a pipeline.
    { upsert: true, returnDocument: 'after', updatePipeline: true }
  );

  return updated.naamJaapCount;
}

// ── Standalone per-user stats (Android app, profile views, etc.) ──
// NOT the leaderboard. Returns one contestant's own today / week / lifetime
// totals, with no `includeInKeliKunj` filter — every signed-in user can see
// their own numbers even if they're not in the active tournament.
export async function getKrishnaDasStats(krishnaDasId) {
  const today  = getDayStart();
  const monday = getWeekMonday();

  const [todayDoc, weekAgg, lifetimeAgg] = await Promise.all([
    Sadhana.findOne({ krishnadasId: krishnaDasId, date: today }).lean(),
    Sadhana.aggregate([
      { $match: { krishnadasId: krishnaDasId, date: { $gte: monday, $lte: today } } },
      { $group: { _id: null, total: { $sum: '$naamJaapCount' } } },
    ]),
    Sadhana.aggregate([
      { $match: { krishnadasId: krishnaDasId } },
      { $group: { _id: null, total: { $sum: '$naamJaapCount' } } },
    ]),
  ]);

  return {
    todayNaam:    todayDoc?.naamJaapCount  ?? 0,
    weekTotal:    weekAgg[0]?.total        ?? 0,
    lifetimeNaam: lifetimeAgg[0]?.total    ?? 0,
  };
}

// ── Get today's sadhana for all krishnadas ──────

export async function getTodaySadhanaMap() {
  const today    = getDayStart();
  const sadhanas = await Sadhana.find({ date: today }).lean();

  return Object.fromEntries(
    sadhanas.map(s => [s.krishnadasId.toString(), s])
  );
}

// ── Build leaderboard response ──────────────────
// Only includes contestants who are active in the current tournament

export async function buildScoresResponse() {
  const contestants = await KrishnaDas.find({ includeInKeliKunj: { $ne: false } }).lean();
  const sadhanaMap  = await getTodaySadhanaMap();

  return contestants.map(c => {
    const sadhana  = sadhanaMap[c._id.toString()] || {};
    const naamPts  = Math.floor((sadhana.naamJaapCount || 0) / 1000) * 10;
    const score    = (sadhana.niyam1?.point || 0) + (sadhana.niyam2?.point || 0)
                   + (sadhana.niyam3?.point || 0) + naamPts;

    return {
      bhaktName:    c.bhaktName,
      todayNaam:    sadhana.naamJaapCount      || 0,
      niyam1Point:  sadhana.niyam1?.point      || 0,
      niyam1DoneAt: sadhana.niyam1?.doneAt     || null,
      niyam2Point:  sadhana.niyam2?.point      || 0,
      niyam2DoneAt: sadhana.niyam2?.doneAt     || null,
      niyam3Point:  sadhana.niyam3?.point      || 0,
      niyam3DoneAt: sadhana.niyam3?.doneAt     || null,
      score,
    };
  });
}

// ── Build weekly stats response ─────────────────
// Days = Monday of current week → today
// Returns { days, overall, dates, heroName, legendName }

function getWeekMonday() {
  const now = new Date();
  now.setUTCHours(0, 0, 0, 0);
  const day  = now.getUTCDay();                   // 0=Sun … 6=Sat
  const diff = day === 0 ? -6 : 1 - day;          // offset to Monday
  const mon  = new Date(now);
  mon.setUTCDate(now.getUTCDate() + diff);
  return mon;
}

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export async function buildStatsResponse() {
  const contestants = await KrishnaDas.find({ includeInKeliKunj: { $ne: false } }).lean();
  const monday      = getWeekMonday();
  const today       = getDayStart();

  // Two queries in parallel: this-week's per-day records, and the all-time
  // total per contestant (one aggregation pass instead of N queries).
  const [sadhanas, lifetimeAgg] = await Promise.all([
    Sadhana.find({ date: { $gte: monday, $lte: today } }).lean(),
    Sadhana.aggregate([
      { $group: { _id: '$krishnadasId', total: { $sum: '$naamJaapCount' } } },
    ]),
  ]);

  const lifetimeMap = Object.fromEntries(
    lifetimeAgg.map(a => [a._id.toString(), a.total])
  );

  // krishnadasId → dateKey → sadhana
  const sadhanaMap = {};
  for (const s of sadhanas) {
    const id      = s.krishnadasId.toString();
    const dateKey = s.date.toISOString().split('T')[0];
    if (!sadhanaMap[id]) sadhanaMap[id] = {};
    sadhanaMap[id][dateKey] = s;
  }

  // List of date strings Mon … today
  const dates = [];
  const cursor = new Date(monday);
  while (cursor <= today) {
    dates.push(new Date(cursor));
    cursor.setUTCDate(cursor.getUTCDate() + 1);
  }
  const dateKeys = dates.map(d => d.toISOString().split('T')[0]);

  // Day-wise breakdown
  const days = dates.map(date => {
    const dateKey = date.toISOString().split('T')[0];
    const dayName = DAY_NAMES[date.getUTCDay()];

    const records = contestants.map(c => {
      const s          = sadhanaMap[c._id.toString()]?.[dateKey] || {};
      const naamPoints = Math.floor((s.naamJaapCount || 0) / 1000) * 10;
      const dayTotal   = (s.niyam1?.point || 0) + (s.niyam2?.point || 0)
                       + (s.niyam3?.point || 0) + naamPoints;
      return {
        bhaktName:    c.bhaktName,
        niyam1Point:  s.niyam1?.point  || 0,
        niyam1DoneAt: s.niyam1?.doneAt || null,
        niyam2Point:  s.niyam2?.point  || 0,
        niyam2DoneAt: s.niyam2?.doneAt || null,
        niyam3Point:  s.niyam3?.point  || 0,
        niyam3DoneAt: s.niyam3?.doneAt || null,
        naamPoints,
        naamCount:    s.naamJaapCount  || 0,
        dayTotal,
      };
    }).sort((a, b) => b.dayTotal - a.dayTotal);

    return { date: dateKey, dayName, records };
  });

  // Overall breakdown
  const overall = contestants.map(c => {
    const id = c._id.toString();
    let totalScore = 0, totalNaamCount = 0, maxDayNaamCount = 0;
    const dayScores = {};

    for (const dateKey of dateKeys) {
      const s          = sadhanaMap[id]?.[dateKey] || {};
      const naamPoints = Math.floor((s.naamJaapCount || 0) / 1000) * 10;
      const dayTotal   = (s.niyam1?.point || 0) + (s.niyam2?.point || 0)
                       + (s.niyam3?.point || 0) + naamPoints;
      dayScores[dateKey]  = dayTotal;
      totalScore         += dayTotal;
      totalNaamCount     += (s.naamJaapCount || 0);
      maxDayNaamCount     = Math.max(maxDayNaamCount, s.naamJaapCount || 0);
    }

    return {
      bhaktName: c.bhaktName,
      dayScores,
      totalScore,
      totalNaamCount,
      maxDayNaamCount,
      lifetimeNaamCount: lifetimeMap[id] || 0,
    };
  }).sort((a, b) => b.totalScore - a.totalScore);

  // Hero = max single-day naam count, Legend = max total naam count
  let heroName = '', heroCount = 0, legendName = '', legendCount = 0;
  for (const o of overall) {
    if (o.maxDayNaamCount > heroCount)  { heroCount = o.maxDayNaamCount; heroName = o.bhaktName; }
    if (o.totalNaamCount  > legendCount) { legendCount = o.totalNaamCount; legendName = o.bhaktName; }
  }

  return { days, overall, dates: dateKeys, heroName, legendName };
}

// ── Get sadhana history for one krishnadas ──────

export async function getSadhanaHistory(krishnaDasId) {
  return Sadhana.find({ krishnadasId: krishnaDasId })
    .sort({ date: -1 })
    .lean();
}
