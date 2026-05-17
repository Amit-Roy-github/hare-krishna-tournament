import connectDB                        from '../DB/connection.js';
import { seedIfEmpty, findByBhaktName } from '../services/krishnaDasService.js';
import { upsertTodaySadhana, buildScoresResponse } from '../services/sadhanaService.js';

const DEFAULT_CONTESTANTS = [
  { bhaktName: 'Gopala Das',  defaultScore: 710 },
  { bhaktName: 'Mohona Das',  defaultScore: 820 },
  { bhaktName: 'Krishna Das', defaultScore: 230 },
  { bhaktName: 'Hari Das',    defaultScore: 950 },
  { bhaktName: 'Ramu Das',    defaultScore: 810 },
];

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();
    await seedIfEmpty(DEFAULT_CONTESTANTS);

    if (req.method === 'GET') {
      const scores = await buildScoresResponse(DEFAULT_CONTESTANTS);
      return res.json(scores);
    }

    if (req.method === 'POST') {
      const updates = Array.isArray(req.body) ? req.body : [req.body];

      for (const { bhaktName, ...fields } of updates) {
        const krishnaDas = await findByBhaktName(bhaktName);
        if (!krishnaDas) continue;
        await upsertTodaySadhana(krishnaDas._id, fields);
      }

      const scores = await buildScoresResponse(DEFAULT_CONTESTANTS);
      return res.json(scores);
    }

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
