import Sadhana    from '../DB/models/Sadhana.js';
import Krishnadas from '../DB/models/Krishnadas.js';

// ── Helpers ─────────────────────────────────────

// Returns midnight UTC for a given date (defaults to today)
function getDayStart(date = new Date()) {
  const d = new Date(date);
  d.setUTCHours(0, 0, 0, 0);
  return d;
}

function computeScore(defaultScore, sadhana) {
  const naamScore = Math.floor((sadhana?.naamJaapCount || 0) / 1000) * 10;
  return defaultScore + naamScore;
}

// ── Upsert today's sadhana for one krishnadas ───
// If a record exists for today → update it
// If not → create a new one (one record per person per day)

export async function upsertTodaySadhana(krishnadasId, fields) {
  const today = getDayStart();

  const allowed = ['naamJaapCount', 'niyam1Point', 'niyam2Point', 'niyam3Point'];
  const set = {};
  for (const key of allowed) {
    if (fields[key] !== undefined) set[key] = fields[key];
  }

  return Sadhana.findOneAndUpdate(
    { krishnadasId, date: today },
    { $set: set },
    { upsert: true, new: true, setDefaultsOnInsert: true }
  );
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

export async function buildScoresResponse(defaultContestants) {
  const contestants = await Krishnadas.find({ includeInPlayground: true }).lean();
  const sadhanaMap  = await getTodaySadhanaMap();

  return contestants.map(c => {
    const sadhana      = sadhanaMap[c._id.toString()] || {};
    const defaultScore = defaultContestants.find(d => d.bhaktName === c.bhaktName)?.defaultScore || 0;

    return {
      bhaktName:     c.bhaktName,
      todayNaam:     sadhana.naamJaapCount || 0,
      niyam1Point:   sadhana.niyam1Point   || 0,
      niyam2Point:   sadhana.niyam2Point   || 0,
      niyam3Point:   sadhana.niyam3Point   || 0,
      score:         computeScore(defaultScore, sadhana),
    };
  });
}

// ── Get sadhana history for one krishnadas ──────

export async function getSadhanaHistory(krishnadasId) {
  return Sadhana.find({ krishnadasId })
    .sort({ date: -1 })
    .lean();
}
