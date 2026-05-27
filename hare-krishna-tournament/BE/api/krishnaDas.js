import connectDB                           from '../DB/connection.js';
import { findAll, createKrishnaDas,
         updateKrishnaDas, deleteKrishnaDas } from '../services/krishnaDasService.js';
import Sadhana                            from '../DB/models/Sadhana.js';
import { requireAdmin }                   from '../middleware/auth.js';

const getAll = async (req, res) => {
  const list = await findAll();
  return res.json(list);
};

const create = requireAdmin(async (req, res) => {
  const { bhaktName, email, phone, sansarName } = req.body;

  if (!bhaktName?.trim()) {
    return res.status(400).json({ error: 'Bhakt name is required' });
  }

  const krishnaDas = await createKrishnaDas({ bhaktName, email, phone, sansarName });

  const today = new Date();
  today.setUTCHours(0, 0, 0, 0);
  await Sadhana.create({ krishnadasId: krishnaDas._id, date: today });

  return res.status(201).json(krishnaDas);
});

const update = requireAdmin(async (req, res) => {
  const { id, ...fields } = req.body;
  if (!id) return res.status(400).json({ error: 'id is required' });

  const updated = await updateKrishnaDas(id, fields);
  if (!updated) return res.status(404).json({ error: 'KrishnaDas not found' });

  return res.json(updated);
});

const remove = requireAdmin(async (req, res) => {
  const { id } = req.body;
  if (!id) return res.status(400).json({ error: 'id is required' });

  await deleteKrishnaDas(id);
  await Sadhana.deleteMany({ krishnadasId: id });

  return res.json({ success: true });
});

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();

    if (req.method === 'GET')    return getAll(req, res);
    if (req.method === 'POST')   return create(req, res);
    if (req.method === 'PATCH')  return update(req, res);
    if (req.method === 'DELETE') return remove(req, res);

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    if (err.code === 11000) {
      return res.status(409).json({ error: 'A KrishnaDas with this bhakt name already exists' });
    }
    res.status(500).json({ error: err.message });
  }
}
