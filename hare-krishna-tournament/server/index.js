import express from 'express';
import fs from 'fs';
import path from 'path';
import cors from 'cors';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
app.use(cors());
app.use(express.json());

const SCORES_FILE = path.join(__dirname, 'scores.json');

const defaultScores = [
  { name: 'Gopala Das',  defaultScore: 710, todayNaam: 0    },
  { name: 'Mohona Das',  defaultScore: 820, todayNaam: 5166 },
  { name: 'Krishna Das', defaultScore: 230, todayNaam: 0    },
  { name: 'Hari Das',    defaultScore: 950, todayNaam: 0    },
  { name: 'Ramu Das',    defaultScore: 810, todayNaam: 5566 },
];

const getScores = () => {
  if (fs.existsSync(SCORES_FILE)) {
    return JSON.parse(fs.readFileSync(SCORES_FILE, 'utf-8'));
  }
  return defaultScores;
};

const saveScores = (scores) => {
  fs.writeFileSync(SCORES_FILE, JSON.stringify(scores, null, 2));
};

const computeScore = (c) =>
  c.defaultScore + Math.floor((c.todayNaam || 0) / 1000) * 10;

// GET all scores — returns computed score alongside raw fields
app.get('/api/scores', (req, res) => {
  const scores = getScores().map(c => ({ ...c, score: computeScore(c) }));
  res.json(scores);
});

// POST update todayNaam (or defaultScore)  →  { name: "Hari Das", todayNaam: 3000 }
app.post('/api/scores', (req, res) => {
  const { name, defaultScore, todayNaam } = req.body;
  const scores = getScores();
  const idx = scores.findIndex(s => s.name === name);
  if (idx === -1) return res.status(404).json({ error: 'Contestant not found' });
  if (defaultScore !== undefined) scores[idx].defaultScore = defaultScore;
  if (todayNaam    !== undefined) scores[idx].todayNaam    = todayNaam;
  saveScores(scores);
  const updated = { ...scores[idx], score: computeScore(scores[idx]) };
  console.log(`Updated: ${name} → defaultScore:${updated.defaultScore} todayNaam:${updated.todayNaam} score:${updated.score}`);
  res.json(scores.map(c => ({ ...c, score: computeScore(c) })));
});

app.listen(3001, () => {
  console.log('Scores server running at http://localhost:3001');
});
