import axios from 'axios';

const BASE_URL = 'https://hare-krishna-tournament.vercel.app';

// ── Update scores ─────────────────────────────────────────────
// Each entry: { name, todayNaam?, defaultScore? }
// todayNaam   → today's naam jaap count
// defaultScore → base score (change only if needed)

const updates = [
  { name: 'Gopala Das',  todayNaam: 0    },
  { name: 'Mohona Das',  todayNaam: 5161 },
  { name: 'Krishna Das', todayNaam: 0    },
  { name: 'Hari Das',    todayNaam: 0    },
  { name: 'Ramu Das',    todayNaam: 7000 },
];

// ─────────────────────────────────────────────────────────────

async function updateScores() {
  try {
    const res = await axios.post(`${BASE_URL}/api/scores`, updates);
    console.log('\n✅ Scores updated successfully!\n');
    console.table(
      res.data.map(c => ({
        Name:         c.name,
        'Today Naam': c.todayNaam,
        'Naam Score': `+${Math.floor((c.todayNaam || 0) / 1000) * 10}`,
        'Total Score': c.score,
      }))
    );
  } catch (err) {
    console.error('❌ Failed to update scores:', err.message);
  }
}

updateScores();
