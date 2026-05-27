import connectDB                        from '../DB/connection.js';
import { findByBhaktName }              from '../services/krishnaDasService.js';
import { upsertTodaySadhana, buildScoresResponse } from '../services/sadhanaService.js';
import { requireAuth }                  from '../middleware/auth.js';

const createOrUpdate = requireAuth(async (req, res) => {
  const updates = Array.isArray(req.body) ? req.body : [req.body];

  // When a token is present, enforce per-user ownership: a contestant can only
  // update their own bhaktName. Admins can update anyone.
  // When STRICT_AUTH=false and no token is provided (legacy mode), allow all
  // updates so the existing web admin keeps working during rollout.
  if (req.auth) {
    for (const u of updates) {
      if (req.auth.role !== 'admin' && u.bhaktName !== req.auth.sub) {
        return res.status(403).json({
          error: `Cannot update another contestant's score (${u.bhaktName})`,
        });
      }
    }
  }

  for (const { bhaktName, ...fields } of updates) {
    const krishnaDas = await findByBhaktName(bhaktName);
    if (!krishnaDas) continue;
    await upsertTodaySadhana(krishnaDas._id, fields);
  }

  const scores = await buildScoresResponse();
  return res.json(scores);
});

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();

    if (req.method === 'GET') {
      const scores = await buildScoresResponse();
      return res.json(scores);
    }

    if (req.method === 'POST') {
      return createOrUpdate(req, res);
    }

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
