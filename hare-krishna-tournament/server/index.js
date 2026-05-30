import 'dotenv/config';
import express              from 'express';
import scores               from '../api/scores.js';
import naam                 from '../api/naam.js';
import krishnaDas           from '../api/krishnaDas.js';
import krishnaDasStats      from '../api/krishnaDasStats.js';
import keliKunj             from '../api/keliKunj.js';
import stats                from '../api/stats.js';
import authLogin            from '../api/auth/login.js';
import authChangePassword   from '../api/auth/change-password.js';

const app  = express();
const PORT = 3001;

app.use(express.json());

// Mount each Vercel handler as an Express route
const wrap = (handler) => (req, res) => handler(req, res);

app.all('/api/scores',                wrap(scores));
app.all('/api/naam',                  wrap(naam));
app.all('/api/krishnaDas',            wrap(krishnaDas));
app.all('/api/krishnaDasStats',       wrap(krishnaDasStats));
app.all('/api/keliKunj',              wrap(keliKunj));
app.all('/api/stats',                 wrap(stats));
app.all('/api/auth/login',            wrap(authLogin));
app.all('/api/auth/change-password',  wrap(authChangePassword));

app.listen(PORT, () => {
  console.log(`✅  API server running at http://localhost:${PORT}`);
});
