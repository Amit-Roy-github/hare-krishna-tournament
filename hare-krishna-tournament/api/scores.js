import clientPromise from './lib/mongodb.js';

const DEFAULT_SCORES = [
  { name: 'Gopala Das',  defaultScore: 710, todayNaam: 0    },
  { name: 'Mohona Das',  defaultScore: 820, todayNaam: 5161 },
  { name: 'Krishna Das', defaultScore: 230, todayNaam: 0    },
  { name: 'Hari Das',    defaultScore: 950, todayNaam: 0    },
  { name: 'Ramu Das',    defaultScore: 810, todayNaam: 7000 },
];

const computeScore = (c) =>
  c.defaultScore + Math.floor((c.todayNaam || 0) / 1000) * 10;

async function getCollection() {
  const client = await clientPromise;
  return client.db('tournament').collection('scores');
}

async function getScores(col) {
  let scores = await col.find({}, { projection: { _id: 0 } }).toArray();

  // Seed default data if collection is empty
  if (scores.length === 0) {
    await col.insertMany(DEFAULT_SCORES);
    scores = DEFAULT_SCORES;
  }

  return scores;
}

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    const col = await getCollection();

    if (req.method === 'GET') {
      const scores = await getScores(col);
      return res.json(scores.map(c => ({ ...c, score: computeScore(c) })));
    }

    if (req.method === 'POST') {
      const updates = Array.isArray(req.body) ? req.body : [req.body];

      for (const { name, defaultScore, todayNaam } of updates) {
        const set = {};
        if (defaultScore !== undefined) set.defaultScore = defaultScore;
        if (todayNaam    !== undefined) set.todayNaam    = todayNaam;
        if (Object.keys(set).length > 0) {
          await col.updateOne({ name }, { $set: set }, { upsert: true });
        }
      }

      const scores = await col.find({}, { projection: { _id: 0 } }).toArray();
      return res.json(scores.map(c => ({ ...c, score: computeScore(c) })));
    }

    res.status(405).json({ error: 'Method not allowed' });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
}
