// Diagnostic: dump today's Sadhana doc for one contestant — full BSON shape,
// not just naamJaapCount. Specifically, we want to SEE deviceSnapshots to
// verify whether `[snapPath]: safeTotal` in the aggregation-pipeline $set is
// actually writing the nested path or fumbling it.
//
// Usage:  node BE/scripts/inspect-today.js "Rama Das"

import 'dotenv/config';
import mongoose    from 'mongoose';
import connectDB   from '../DB/connection.js';
import Sadhana     from '../DB/models/Sadhana.js';
import KrishnaDas  from '../DB/models/KrishnaDas.js';

const [, , bhaktName] = process.argv;
if (!bhaktName) {
  console.error('Usage: node BE/scripts/inspect-today.js "<bhaktName>"');
  process.exit(1);
}

function getDayStart() {
  const d = new Date();
  d.setUTCHours(0, 0, 0, 0);
  return d;
}

await connectDB();
try {
  const kd = await KrishnaDas.findOne({ bhaktName });
  if (!kd) {
    console.error(`Contestant not found: ${bhaktName}`);
    process.exit(2);
  }

  const today = getDayStart();
  const doc = await Sadhana.findOne({ krishnadasId: kd._id, date: today }).lean();

  if (!doc) {
    console.log(`No Sadhana doc for ${bhaktName} on ${today.toISOString().slice(0,10)}`);
    process.exit(0);
  }

  console.log('=== RAW STORED DOC ===');
  console.log(JSON.stringify(doc, null, 2));

  console.log('\n=== deviceSnapshots TOP-LEVEL KEYS ===');
  if (doc.deviceSnapshots && typeof doc.deviceSnapshots === 'object') {
    Object.entries(doc.deviceSnapshots).forEach(([k, v]) => {
      console.log(`  ${JSON.stringify(k)} → ${JSON.stringify(v)}`);
    });
  } else {
    console.log('  (none)');
  }
} finally {
  await mongoose.disconnect();
}
