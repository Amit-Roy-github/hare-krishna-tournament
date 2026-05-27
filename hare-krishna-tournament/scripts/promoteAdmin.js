// One-off: promote a contestant to admin and set their initial password.
// Usage:  node scripts/promoteAdmin.js "<bhaktName>" "<password>"
// Run with Node ≥ 20 so mongodb's WebCrypto needs are met.

import 'dotenv/config';
import mongoose   from 'mongoose';
import bcrypt     from 'bcryptjs';
import connectDB  from '../BE/DB/connection.js';
import KrishnaDas from '../BE/DB/models/KrishnaDas.js';

const [, , bhaktName, password] = process.argv;

if (!bhaktName || !password) {
  console.error('Usage: node scripts/promoteAdmin.js "<bhaktName>" "<password>"');
  process.exit(1);
}
if (password.length < 6) {
  console.error('Password must be at least 6 characters.');
  process.exit(1);
}

await connectDB();

const hash = bcrypt.hashSync(password, 10);
const res  = await KrishnaDas.updateOne(
  { bhaktName },
  { $set: {
      'auth.role':          'admin',
      'auth.passwordHash':  hash,
      'auth.passwordSetAt': new Date(),
  }}
);

if (res.matchedCount === 0) {
  console.error(`No contestant with bhaktName="${bhaktName}". Spelling? Use one from GET /api/krishnaDas.`);
} else {
  console.log(`✅ Promoted "${bhaktName}" to admin. Password hash saved.`);
}

await mongoose.disconnect();
