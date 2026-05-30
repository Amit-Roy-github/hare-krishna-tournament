import KrishnaDas       from '../DB/models/KrishnaDas.js';
import { hashPassword } from './authService.js';

// Strip credential material from list responses. `auth.role` stays visible —
// it's useful in the admin UI and not sensitive.
const PUBLIC_SELECT = '-auth.passwordHash -auth.passwordSetAt -auth.lastLoginAt';

// ── Find ────────────────────────────────────────

export async function findAll() {
  const docs = await KrishnaDas.find().select(PUBLIC_SELECT).lean();
  // Apply schema defaults that lean() skips for older documents
  return docs.map(d => ({ includeInKeliKunj: true, ...d }));
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
  const allowed = ['bhaktName', 'email', 'phone', 'sansarName', 'includeInKeliKunj'];
  const set = {};
  for (const key of allowed) {
    if (fields[key] !== undefined) set[key] = fields[key];
  }
  // Accept flat `role` and `password` from callers (web admin form, API consumers)
  // and translate to nested auth.* paths so the schema stays separated.
  if (fields.role !== undefined) {
    set['auth.role'] = fields.role;
  }
  if (fields.password) {
    if (fields.password.length < 6) {
      throw new Error('Password must be at least 6 characters');
    }
    set['auth.passwordHash']  = await hashPassword(fields.password);
    set['auth.passwordSetAt'] = new Date();
  }
  return KrishnaDas.findByIdAndUpdate(id, { $set: set }, { returnDocument: 'after' })
    .select(PUBLIC_SELECT)
    .lean();
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
