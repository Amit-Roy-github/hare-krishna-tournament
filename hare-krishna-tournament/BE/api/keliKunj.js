import connectDB from '../DB/connection.js';
import {
  findAll,
  createKeliKunj,
  updateKeliKunj,
} from '../services/keliKunjService.js';

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();

    // GET — list all keliKunj weeks
    if (req.method === 'GET') {
      const list = await findAll();
      return res.json(list);
    }

    // POST — create a new keliKunj week
    if (req.method === 'POST') {
      const { keliKunjWeek, winners, prizePool } = req.body;

      if (!keliKunjWeek || isNaN(Number(keliKunjWeek))) {
        return res.status(400).json({ error: 'keliKunjWeek must be a valid number' });
      }

      const doc = await createKeliKunj({
        keliKunjWeek: Number(keliKunjWeek),
        winners:   winners   || {},
        prizePool: prizePool || {},
      });

      return res.status(201).json(doc);
    }

    // PATCH — update winners / prizePool for an existing week
    if (req.method === 'PATCH') {
      const { id, winners, prizePool } = req.body;

      if (!id) return res.status(400).json({ error: 'id is required' });

      const updated = await updateKeliKunj(id, { winners, prizePool });
      if (!updated) return res.status(404).json({ error: 'KeliKunj week not found' });

      return res.json(updated);
    }

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    if (err.code === 11000) {
      return res.status(409).json({ error: `Week ${req.body?.keliKunjWeek} already exists` });
    }
    res.status(500).json({ error: err.message });
  }
}
