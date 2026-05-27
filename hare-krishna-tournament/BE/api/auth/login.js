import connectDB                       from '../../DB/connection.js';
import KrishnaDas                       from '../../DB/models/KrishnaDas.js';
import { verifyPassword, signToken }    from '../../services/authService.js';

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') return res.status(200).end();
  if (req.method !== 'POST')    return res.status(405).json({ error: 'Method not allowed' });

  try {
    await connectDB();

    const { bhaktName, password } = req.body || {};
    if (!bhaktName || !password) {
      return res.status(400).json({ error: 'bhaktName and password are required' });
    }

    const user = await KrishnaDas.findOne({ bhaktName });
    // Always return generic 'Invalid credentials' — prevents username enumeration
    if (!user || !user.auth?.passwordHash) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const ok = await verifyPassword(password, user.auth.passwordHash);
    if (!ok) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    user.auth.lastLoginAt = new Date();
    user.save().catch(err => console.error('lastLoginAt update failed', err));

    const role  = user.auth.role ?? 'contestant';
    const token = signToken({ bhaktName: user.bhaktName, role });
    return res.json({ token, bhaktName: user.bhaktName, role });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
