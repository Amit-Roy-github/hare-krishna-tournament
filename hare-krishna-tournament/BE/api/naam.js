import connectDB                                  from '../DB/connection.js';
import { findByBhaktName }                        from '../services/krishnaDasService.js';
import { applyDeviceCount, buildScoresResponse }  from '../services/sadhanaService.js';
import { requireAuth }                            from '../middleware/auth.js';

// POST /api/naam — idempotent incremental naam-jaap sync from the app.
// Body: a single { deviceId, date, total } or an array of them (one per
// un-synced day). `total` is the device's absolute per-day high-water mark;
// the server adds only the new part. Identity comes from the token — a
// contestant only ever syncs their own count.
const sync = requireAuth(async (req, res) => {
  if (!req.auth) {
    return res.status(401).json({ error: 'Authentication required' });
  }

  const krishnaDas = await findByBhaktName(req.auth.sub);
  if (!krishnaDas) {
    return res.status(404).json({ error: 'Contestant not found' });
  }

  const items = Array.isArray(req.body) ? req.body : [req.body];
  const days  = [];
  for (const item of items) {
    if (!item || !item.deviceId) continue;
    const naamJaapCount = await applyDeviceCount(
      krishnaDas._id, item.deviceId, item.date, item.total,
    );
    days.push({ date: item.date, naamJaapCount });
  }

  const scores = await buildScoresResponse();
  return res.json({ days, scores });
});

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();
    if (req.method === 'POST') return sync(req, res);
    res.status(405).json({ error: 'Method not allowed' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
