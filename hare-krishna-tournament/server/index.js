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
  { name: 'Gopala Das',  score: 710 },
  { name: 'Mohona Das',  score: 820 },
  { name: 'Krishna Das', score: 230 },
  { name: 'Hari Das',    score: 950 },
  { name: 'Ramu Das',    score: 860 },
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

// GET all scores
app.get('/api/scores', (req, res) => {
  res.json(getScores());
});

// POST update a score  →  { name: "Hari Das", score: 1020 }
app.post('/api/scores', (req, res) => {
  const { name, score } = req.body;
  const scores = getScores();
  const idx = scores.findIndex(s => s.name === name);
  if (idx === -1) return res.status(404).json({ error: 'Contestant not found' });
  scores[idx].score = score;
  saveScores(scores);
  console.log(`Updated: ${name} → ${score}`);
  res.json(scores);
});

app.listen(3001, () => {
  console.log('Scores server running at http://localhost:3001');
});
