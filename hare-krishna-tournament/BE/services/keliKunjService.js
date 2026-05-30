import KeliKunj   from '../DB/models/KeliKunj.js';
import '../DB/models/KrishnaDas.js'; // ensure KrishnaDas is registered for populate()

// ── Find ────────────────────────────────────────

export async function findAll() {
  return KeliKunj.find()
    .populate('winners._1 winners._2 winners._3 winners.maxNaamJaap winners.totalMaxNaamJaap', 'bhaktName')
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

export async function updateKeliKunj(id, { winners, prizePool, resultDeclared, showLeaderboard }) {
  const set = {};
  if (winners)                       set.winners         = winners;
  if (prizePool)                     set.prizePool       = prizePool;
  if (resultDeclared  !== undefined) set.resultDeclared  = resultDeclared;
  if (showLeaderboard !== undefined) set.showLeaderboard = showLeaderboard;

  return KeliKunj.findByIdAndUpdate(
    id,
    { $set: set },
    { returnDocument: 'after' }
  ).lean();
}
