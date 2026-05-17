import connectDB                           from '../DB/connection.js';
import { findAll, createKrishnadas,
         updateKrishnadas, deleteKrishnadas } from '../services/krishnadasService.js';
import Sadhana                            from '../DB/models/Sadhana.js';

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    await connectDB();

    // GET — list all krishnadas
    if (req.method === 'GET') {
      const list = await findAll();
      return res.json(list);
    }

    // POST — register new krishnadas
    if (req.method === 'POST') {
      const { bhaktName, email, phone, sansarName } = req.body;

      if (!bhaktName?.trim()) {
        return res.status(400).json({ error: 'Bhakt name is required' });
      }

      const krishnadas = await createKrishnadas({ bhaktName, email, phone, sansarName });

      // initialise an empty sadhana record for today
      const today = new Date();
      today.setUTCHours(0, 0, 0, 0);
      await Sadhana.create({ krishnadasId: krishnadas._id, date: today });

      return res.status(201).json(krishnadas);
    }

    // PATCH — update fields (bhaktName, email, phone, sansarName, isActive)
    if (req.method === 'PATCH') {
      const { id, ...fields } = req.body;
      if (!id) return res.status(400).json({ error: 'id is required' });

      const updated = await updateKrishnadas(id, fields);
      if (!updated) return res.status(404).json({ error: 'Krishnadas not found' });

      return res.json(updated);
    }

    // DELETE — remove krishnadas and their sadhana records
    if (req.method === 'DELETE') {
      const { id } = req.body;
      if (!id) return res.status(400).json({ error: 'id is required' });

      await deleteKrishnadas(id);
      await Sadhana.deleteMany({ krishnadasId: id });

      return res.json({ success: true });
    }

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    if (err.code === 11000) {
      return res.status(409).json({ error: 'A Krishnadas with this bhakt name already exists' });
    }
    res.status(500).json({ error: err.message });
  }
}
