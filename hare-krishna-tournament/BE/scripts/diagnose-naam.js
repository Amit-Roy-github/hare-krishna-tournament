// Diagnose why /api/naam is timing out: look for fat Sadhana docs (huge
// deviceSnapshots maps) and unusually large day counts per contestant.
//
// Usage:  node BE/scripts/diagnose-naam.js

import 'dotenv/config';
import mongoose    from 'mongoose';
import connectDB   from '../DB/connection.js';
import Sadhana     from '../DB/models/Sadhana.js';
import KrishnaDas  from '../DB/models/KrishnaDas.js';

await connectDB();
try {
  console.log('=== Sadhana doc size + snapshot count, top 10 by snapshot keys ===');
  // Use aggregation to compute snapshot-key count per doc, then sort.
  const fat = await Sadhana.aggregate([
    {
      $project: {
        krishnadasId: 1,
        date: 1,
        naamJaapCount: 1,
        snapKeys: {
          $cond: [
            { $eq: [{ $type: '$deviceSnapshots' }, 'object'] },
            { $size: { $objectToArray: { $ifNull: ['$deviceSnapshots', {}] } } },
            0,
          ],
        },
      },
    },
    { $sort: { snapKeys: -1 } },
    { $limit: 10 },
  ]);

  // Resolve bhakt names
  const ids = [...new Set(fat.map(d => d.krishnadasId.toString()))];
  const kds = await KrishnaDas.find({ _id: { $in: ids } }).lean();
  const nameById = Object.fromEntries(kds.map(k => [k._id.toString(), k.bhaktName]));

  fat.forEach(d => {
    console.log(
      `  ${(nameById[d.krishnadasId.toString()] ?? '?').padEnd(15)} ` +
      `date=${d.date.toISOString().slice(0,10)}  ` +
      `count=${String(d.naamJaapCount).padEnd(6)}  ` +
      `snapKeys=${d.snapKeys}`
    );
  });

  console.log('\n=== Day-doc count per contestant (top 10) ===');
  const byKd = await Sadhana.aggregate([
    { $group: { _id: '$krishnadasId', days: { $sum: 1 } } },
    { $sort: { days: -1 } },
    { $limit: 10 },
  ]);
  byKd.forEach(d => {
    console.log(`  ${(nameById[d._id.toString()] ?? '?').padEnd(15)} day-docs=${d.days}`);
  });
} finally {
  await mongoose.disconnect();
}
