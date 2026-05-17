import KeliKunj from '../DB/models/KeliKunj.js';

// ── Find ────────────────────────────────────────

export async function findAll() {
  return KeliKunj.find()
    .populate('winners._1 winners._2 winners._3', 'bhaktName')
    .sort({ keliKunjWeek: -1 })
    .lean();
}

export async function findByWeek(keliKunjWeek) {
  return KeliKunj.findOne({ keliKunjWeek }).lean();
}

// ── Create ──────────────────────────────────────

export async function createKeliKunj({ keliKunjWeek, winners, prizePool }) {
  return KeliKunj.create({ keliKunjWeek, winners, prizePool });
}

// ── Update ──────────────────────────────────────

export async function updateKeliKunj(id, { winners, prizePool, resultDeclared }) {
  const set = {};
  if (winners)                    set.winners         = winners;
  if (prizePool)                  set.prizePool       = prizePool;
  if (resultDeclared !== undefined) set.resultDeclared = resultDeclared;

  return KeliKunj.findByIdAndUpdate(
    id,
    { $set: set },
    { new: true }
  ).lean();
}
