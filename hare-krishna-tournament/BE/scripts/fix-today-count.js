// One-off recovery: set today's naamJaapCount for one contestant to a target.
//
// IMPORTANT: This script intentionally does NOT touch deviceSnapshots. The
// snapshots are each device's high-water mark — clearing them tells the
// server "this device has never reported", and the next sync re-credits
// the device's entire dayTotal. That's the bug that re-inflated Rama Das
// after we "reset" to 100.
//
// After this script runs:
// - naamJaapCount = target.
// - deviceSnapshots are preserved.
// - Future syncs only add the NEW delta (clientTotal - snapshot), not the
//   whole dayTotal. So if each device's snapshot is already aligned with
//   its current clientTotal, the count stays at target + future taps only.
//
// Caveat: if a device's snapshot is currently LOWER than its clientTotal
// (e.g. due to the now-fixed unconditional overwrite bug), the next sync
// from that device will still credit (clientTotal - snapshot). To fully
// freeze a corrupted device's contribution, also bump its snapshot — but
// do that as a separate, deliberate decision per device.
//
// Usage:   node BE/scripts/fix-today-count.js "<bhaktName>" <targetCount>
// Example: node BE/scripts/fix-today-count.js "Rama Das" 100

import 'dotenv/config';
import connectDB    from '../DB/connection.js';
import Sadhana      from '../DB/models/Sadhana.js';
import KrishnaDas   from '../DB/models/KrishnaDas.js';
import mongoose     from 'mongoose';

function getDayStart() {
  const d = new Date();
  d.setUTCHours(0, 0, 0, 0);
  return d;
}

const [, , bhaktName, targetStr] = process.argv;
if (!bhaktName || !targetStr) {
  console.error('Usage: node BE/scripts/fix-today-count.js "<bhaktName>" <targetCount>');
  process.exit(1);
}
const target = Math.max(0, Math.floor(Number(targetStr)));
if (!Number.isFinite(target)) {
  console.error('targetCount must be a non-negative integer');
  process.exit(1);
}

await connectDB();
try {
  const kd = await KrishnaDas.findOne({ bhaktName });
  if (!kd) {
    console.error(`Contestant not found: ${bhaktName}`);
    process.exit(2);
  }

  const today = getDayStart();
  const before = await Sadhana.findOne({ krishnadasId: kd._id, date: today }).lean();
  console.log('BEFORE:', JSON.stringify({
    naamJaapCount:   before?.naamJaapCount   ?? null,
    deviceSnapshots: before?.deviceSnapshots ?? null,
  }, null, 2));

  const updated = await Sadhana.findOneAndUpdate(
    { krishnadasId: kd._id, date: today },
    { $set: { naamJaapCount: target } },     // snapshots left alone
    { upsert: true, returnDocument: 'after' }
  );

  console.log('AFTER: ', JSON.stringify({
    naamJaapCount:   updated.naamJaapCount,
    deviceSnapshots: updated.deviceSnapshots,
  }, null, 2));
  console.log(`✓ ${bhaktName} today (${today.toISOString().slice(0,10)}) naamJaapCount set to ${target}; snapshots preserved.`);
} finally {
  await mongoose.disconnect();
}
