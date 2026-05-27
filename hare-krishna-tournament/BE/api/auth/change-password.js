import connectDB                          from '../../DB/connection.js';
import KrishnaDas                          from '../../DB/models/KrishnaDas.js';
import { hashPassword, verifyPassword }    from '../../services/authService.js';
import { requireAuth }                     from '../../middleware/auth.js';

const changePassword = async (req, res) => {
  if (!req.auth) return res.status(401).json({ error: 'Authentication required' });

  const { currentPassword, newPassword } = req.body || {};
  if (!currentPassword || !newPassword) {
    return res.status(400).json({ error: 'currentPassword and newPassword are required' });
  }
  if (newPassword.length < 6) {
    return res.status(400).json({ error: 'Password must be at least 6 characters' });
  }

  const user = await KrishnaDas.findOne({ bhaktName: req.auth.sub });
  if (!user || !user.auth?.passwordHash) {
    return res.status(400).json({ error: 'No password set' });
  }

  const ok = await verifyPassword(currentPassword, user.auth.passwordHash);
  if (!ok) return res.status(401).json({ error: 'Invalid current password' });

  user.auth.passwordHash  = await hashPassword(newPassword);
  user.auth.passwordSetAt = new Date();
  await user.save();

  return res.status(204).end();
};

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin',  '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') return res.status(200).end();
  if (req.method !== 'POST')    return res.status(405).json({ error: 'Method not allowed' });

  try {
    await connectDB();
    return requireAuth(changePassword)(req, res);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
