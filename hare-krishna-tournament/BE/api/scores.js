import connectDB    from '../DB/connection.js';
import Krishnadas  from '../DB/models/Krishnadas.js';
import Sadhana     from '../DB/models/Sadhana.js';

const DEFAULT_CONTESTANTS = [
  { name: 'Gopala Das',  defaultScore: 710 },
  { name: 'Mohona Das',  defaultScore: 820 },
  { name: 'Krishna Das', defaultScore: 230 },
  { name: 'Hari Das',    defaultScore: 950 },
  { name: 'Ramu Das',    defaultScore: 810 },
];

const computeScore = (defaultScore, sadhana) => {
  const naamScore = Math.floor((sadhana?.naamJaapCount || 0) / 1000) * 10;
  return defaultScore + naamScore;
};

// Seed krishnadas + sadhana if DB is empty
async function seedIfEmpty() {
  const count = await Krishnadas.countDocuments();
  if (count > 0) return;

  for (const c of DEFAULT_CONTESTANTS) {
    const krishnadas = await Krishnadas.create({ name: c.name });
    await Sadhana.create({
      krishnadasId:  krishnadas._id,
      naamJaapCount: 0,
    });
  }
}

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();
    await seedIfEmpty();

    // GET — return all contestants with computed score
    if (req.method === 'GET') {
      const contestants = await Krishnadas.find().lean();
      const sadhanas    = await Sadhana.find().lean();

      const sadhanaMap = Object.fromEntries(
        sadhanas.map(s => [s.krishnadasId.toString(), s])
      );

      const result = contestants.map(c => {
        const sadhana      = sadhanaMap[c._id.toString()] || {};
        const defaultScore = DEFAULT_CONTESTANTS.find(d => d.name === c.name)?.defaultScore || 0;
        return {
          name:         c.name,
          todayNaam:    sadhana.naamJaapCount || 0,
          niyam1Point:  sadhana.niyam1Point   || 0,
          niyam2Point:  sadhana.niyam2Point   || 0,
          niyam3Point:  sadhana.niyam3Point   || 0,
          score:        computeScore(defaultScore, sadhana),
        };
      });

      return res.json(result);
    }

    // POST — update naamJaapCount (and optionally niyam points)
    // Single: { name, naamJaapCount, niyam1Point, niyam2Point, niyam3Point }
    // Bulk:   [ { name, naamJaapCount }, ... ]
    if (req.method === 'POST') {
      const updates = Array.isArray(req.body) ? req.body : [req.body];

      for (const update of updates) {
        const { name, naamJaapCount, niyam1Point, niyam2Point, niyam3Point } = update;

        const krishnadas = await Krishnadas.findOne({ name });
        if (!krishnadas) continue;

        const set = {};
        if (naamJaapCount !== undefined) set.naamJaapCount = naamJaapCount;
        if (niyam1Point   !== undefined) set.niyam1Point   = niyam1Point;
        if (niyam2Point   !== undefined) set.niyam2Point   = niyam2Point;
        if (niyam3Point   !== undefined) set.niyam3Point   = niyam3Point;

        await Sadhana.findOneAndUpdate(
          { krishnadasId: krishnadas._id },
          { $set: set },
          { upsert: true, new: true }
        );
      }

      // Return updated scores (same as GET)
      const contestants = await Krishnadas.find().lean();
      const sadhanas    = await Sadhana.find().lean();

      const sadhanaMap = Object.fromEntries(
        sadhanas.map(s => [s.krishnadasId.toString(), s])
      );

      const result = contestants.map(c => {
        const sadhana      = sadhanaMap[c._id.toString()] || {};
        const defaultScore = DEFAULT_CONTESTANTS.find(d => d.name === c.name)?.defaultScore || 0;
        return {
          name:         c.name,
          todayNaam:    sadhana.naamJaapCount || 0,
          niyam1Point:  sadhana.niyam1Point   || 0,
          niyam2Point:  sadhana.niyam2Point   || 0,
          niyam3Point:  sadhana.niyam3Point   || 0,
          score:        computeScore(defaultScore, sadhana),
        };
      });

      return res.json(result);
    }

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
