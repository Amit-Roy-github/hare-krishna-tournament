// Time the actual applyDeviceCount call against prod Mongo, without Vercel
// in the loop. If this is fast (sub-second), the timeout is a Vercel runtime
// problem, not a DB problem.

import 'dotenv/config';
import mongoose             from 'mongoose';
import connectDB            from '../DB/connection.js';
import KrishnaDas           from '../DB/models/KrishnaDas.js';
import { applyDeviceCount } from '../services/sadhanaService.js';

await connectDB();
const t0 = Date.now();
console.log(`connected (${Date.now() - t0}ms)`);

try {
  const kd = await KrishnaDas.findOne({ bhaktName: 'Rama Das' });
  if (!kd) throw new Error('Rama Das not found');

  const today = new Date().toISOString().slice(0, 10);
  const fakeDeviceId = 'diagnostic-timing-test-device';

  // Same operation /api/naam does. Use a fake deviceId so we don't disturb
  // real device snapshots. Read the result, then clean up the fake entry.
  for (let i = 0; i < 5; i++) {
    const t = Date.now();
    const after = await applyDeviceCount(kd._id, fakeDeviceId, today, 100 + i);
    console.log(`  call ${i}: ${Date.now() - t}ms (returned naamJaapCount=${after})`);
  }

  // Cleanup
  await mongoose.connection.db.collection('sadhana').updateOne(
    { krishnadasId: kd._id, date: new Date(today + 'T00:00:00.000Z') },
    { $unset: { [`deviceSnapshots.${fakeDeviceId}`]: '' } },
  );
  console.log('cleaned up fake snapshot');
} finally {
  await mongoose.disconnect();
}
