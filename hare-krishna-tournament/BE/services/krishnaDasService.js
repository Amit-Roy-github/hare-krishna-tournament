import KrishnaDas from '../DB/models/KrishnaDas.js';

// ── Find ────────────────────────────────────────

export async function findAll() {
  const docs = await KrishnaDas.find().lean();
  // Apply schema defaults that lean() skips for older documents
  return docs.map(d => ({ includeInPlayground: true, ...d }));
}

export async function findByBhaktName(bhaktName) {
  return KrishnaDas.findOne({ bhaktName }).lean();
}

export async function findById(id) {
  return KrishnaDas.findById(id).lean();
}

// ── Create ──────────────────────────────────────

export async function createKrishnaDas({ bhaktName, email, phone, sansarName }) {
  return KrishnaDas.create({ bhaktName, email, phone, sansarName });
}

export async function updateKrishnaDas(id, fields) {
  const allowed = ['bhaktName', 'email', 'phone', 'sansarName', 'includeInPlayground'];
  const set = {};
  for (const key of allowed) {
    if (fields[key] !== undefined) set[key] = fields[key];
  }
  return KrishnaDas.findByIdAndUpdate(id, { $set: set }, { new: true }).lean();
}

export async function deleteKrishnaDas(id) {
  return KrishnaDas.findByIdAndDelete(id);
}

// ── Seed ────────────────────────────────────────

export async function seedIfEmpty(defaultContestants) {
  const count = await KrishnaDas.countDocuments();
  if (count > 0) return;

  await KrishnaDas.insertMany(
    defaultContestants.map(c => ({ bhaktName: c.bhaktName }))
  );
}
