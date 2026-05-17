// In-memory store — persists while the function is warm (active use keeps it alive)
let scores = [
  { name: 'Gopala Das',  defaultScore: 710, todayNaam: 0    },
  { name: 'Mohona Das',  defaultScore: 820, todayNaam: 5166 },
  { name: 'Krishna Das', defaultScore: 230, todayNaam: 0    },
  { name: 'Hari Das',    defaultScore: 950, todayNaam: 0    },
  { name: 'Ramu Das',    defaultScore: 810, todayNaam: 5566 },
];

const computeScore = (c) =>
  c.defaultScore + Math.floor((c.todayNaam || 0) / 1000) * 10;

export default function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') return res.status(200).end();

  if (req.method === 'GET') {
    return res.json(scores.map(c => ({ ...c, score: computeScore(c) })));
  }

  if (req.method === 'POST') {
    const updates = Array.isArray(req.body) ? req.body : [req.body];
    for (const { name, defaultScore, todayNaam } of updates) {
      const idx = scores.findIndex(s => s.name === name);
      if (idx === -1) continue;
      if (defaultScore !== undefined) scores[idx].defaultScore = defaultScore;
      if (todayNaam    !== undefined) scores[idx].todayNaam    = todayNaam;
    }
    return res.json(scores.map(c => ({ ...c, score: computeScore(c) })));
  }

  res.status(405).json({ error: 'Method not allowed' });
}
