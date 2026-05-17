import Krishnadas from '../DB/models/Krishnadas.js';

// ── Find ────────────────────────────────────────

export async function findAll() {
  const docs = await Krishnadas.find().lean();
  // Apply schema defaults that lean() skips for older documents
  return docs.map(d => ({ includeInPlayground: true, ...d }));
}

export async function findByBhaktName(bhaktName) {
  return Krishnadas.findOne({ bhaktName }).lean();
}

export async function findById(id) {
  return Krishnadas.findById(id).lean();
}

// ── Create ──────────────────────────────────────

export async function createKrishnadas({ bhaktName, email, phone, sansarName }) {
  return Krishnadas.create({ bhaktName, email, phone, sansarName });
}

export async function updateKrishnadas(id, fields) {
  const allowed = ['bhaktName', 'email', 'phone', 'sansarName', 'includeInPlayground'];
  const set = {};
  for (const key of allowed) {
    if (fields[key] !== undefined) set[key] = fields[key];
  }
  return Krishnadas.findByIdAndUpdate(id, { $set: set }, { new: true }).lean();
}

export async function deleteKrishnadas(id) {
  return Krishnadas.findByIdAndDelete(id);
}

// ── Seed ────────────────────────────────────────

export async function seedIfEmpty(defaultContestants) {
  const count = await Krishnadas.countDocuments();
  if (count > 0) return;

  await Krishnadas.insertMany(
    defaultContestants.map(c => ({ bhaktName: c.bhaktName }))
  );
}
