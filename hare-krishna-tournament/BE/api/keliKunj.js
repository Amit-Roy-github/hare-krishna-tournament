import connectDB from '../DB/connection.js';
import {
  findAll,
  createKeliKunj,
  updateKeliKunj,
} from '../services/keliKunjService.js';
import { requireAdmin } from '../middleware/auth.js';

const create = requireAdmin(async (req, res) => {
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
});

const update = requireAdmin(async (req, res) => {
  const { id, winners, prizePool, resultDeclared, showLeaderboard } = req.body;

  if (!id) return res.status(400).json({ error: 'id is required' });

  const updated = await updateKeliKunj(id, { winners, prizePool, resultDeclared, showLeaderboard });
  if (!updated) return res.status(404).json({ error: 'KeliKunj week not found' });

  return res.json(updated);
});

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();

    if (req.method === 'GET') {
      const list = await findAll();
      return res.json(list);
    }

    if (req.method === 'POST')  return create(req, res);
    if (req.method === 'PATCH') return update(req, res);

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    if (err.code === 11000) {
      return res.status(409).json({ error: `Week ${req.body?.keliKunjWeek} already exists` });
    }
    res.status(500).json({ error: err.message });
  }
}
