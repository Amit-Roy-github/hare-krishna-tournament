import connectDB                  from '../DB/connection.js';
import { findByBhaktName }        from '../services/krishnaDasService.js';
import { getKrishnaDasStats }     from '../services/sadhanaService.js';
import { requireAuth }            from '../middleware/auth.js';

// GET /api/krishnaDasStats — the signed-in user's own naam totals.
//
// NOT the leaderboard. No `includeInKeliKunj` filter. Anyone who can log in
// can see their own today / week / lifetime here. Use this for any UI surface
// that displays a person's count (Counter screen tiles, Home stats, profile).
const handler = requireAuth(async (req, res) => {
  if (!req.auth) {
    return res.status(401).json({ error: 'Authentication required' });
  }

  const kd = await findByBhaktName(req.auth.sub);
  if (!kd) {
    return res.status(404).json({ error: 'Contestant not found' });
  }

  const stats = await getKrishnaDasStats(kd._id);
  return res.json(stats);
});

export default async function entry(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();
    if (req.method === 'GET') return handler(req, res);
    res.status(405).json({ error: 'Method not allowed' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
