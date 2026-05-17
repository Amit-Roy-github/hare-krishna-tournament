import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axiosClient from '../api/axiosClient'

const winners = [
  { rank: '1st', emoji: '🥇', color: '#FFD700', glow: '#FFD70088', label: 'Champion',      prize: 400 },
  { rank: '2nd', emoji: '🥈', color: '#C0C0C0', glow: '#C0C0C088', label: 'Runner Up',     prize: 100 },
  { rank: '3rd', emoji: '🥉', color: '#CD7F32', glow: '#CD7F3288', label: '2nd Runner Up', prize: 50  },
]

function WinnerCard({ winner, index }) {
  const [visible, setVisible] = useState(false)
  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 400 + index * 250)
    return () => clearTimeout(t)
  }, [index])

  return (
    <div
      className={`winner-card ${visible ? 'winner-card--visible' : ''}`}
      style={{ '--glow': winner.glow, '--border': winner.color }}
    >
      <div className="winner-medal">{winner.emoji}</div>
      <div className="winner-rank" style={{ color: winner.color }}>{winner.rank}</div>
      <div className="winner-tbd">To be announced</div>
      <div className="winner-label">{winner.label}</div>
      <div className="winner-prize">
        <span className="rupee">₹</span>
        <span className="amount">{winner.prize}</span>
      </div>
    </div>
  )
}

export default function Home() {
  const navigate = useNavigate()
  const [contestants, setContestants] = useState([])

  useEffect(() => {
    const fetchScores = async () => {
      try {
        const { data } = await axiosClient.get('/scores')
        setContestants(data)
      } catch {
        // silent — keeps last known scores on screen
      }
    }
    fetchScores()
    const interval = setInterval(fetchScores, 5000)
    return () => clearInterval(interval)
  }, [])

  const rankedContestants = [...contestants]
    .map(c => ({
      ...c,
      score: c.score ?? ((c.defaultScore || 0) + Math.floor((c.todayNaam || 0) / 1000) * 10)
    }))
    .sort((a, b) => b.score - a.score)
    .map((c, i) => ({ ...c, rank: i + 1 }))

  const [particles] = useState(() =>
    Array.from({ length: 30 }, (_, i) => ({
      id: i,
      left: `${Math.random() * 100}%`,
      animationDuration: `${3 + Math.random() * 4}s`,
      animationDelay: `${Math.random() * 4}s`,
      size: `${6 + Math.random() * 10}px`,
      color: ['#FF9800', '#FFD700', '#FF5722', '#FFC107', '#FFEB3B'][Math.floor(Math.random() * 5)],
    }))
  )

  return (
    <div className="root">
      {particles.map(p => (
        <div key={p.id} className="particle" style={{
          left: p.left, width: p.size, height: p.size,
          background: p.color, animationDuration: p.animationDuration,
          animationDelay: p.animationDelay,
        }} />
      ))}

      <div className="container container--wide">

        {/* ── Header ──────────────────────────── */}
        <div className="top-banner">
          <span className="lotus">🪷</span>
          <span className="chant">Hare Krishna • Hare Krishna</span>
          <span className="lotus">🪷</span>
        </div>

        <div className="title-section">
          <h1 className="tournament-title">
            Hare Krishna
            <span className="title-highlight"> Tournament</span>
          </h1>
          <div className="final-day-badge">
            <span className="fire">🔥</span>
            <span>FINAL DAY</span>
            <span className="fire">🔥</span>
          </div>
        </div>

        {/* ── Two column layout ────────────────── */}
        <div className="home-columns">

          {/* LEFT — Winners */}
          <div className="col-winners">
            <div className="winner-crown">👑</div>
            <h2 className="winner-heading">Winners Declared!</h2>
            <p className="winner-sub">
              By the grace of Shri Krishna the champions have emerged.
            </p>
            <div className="winner-grid winner-grid--col">
              {winners.map((w, i) => (
                <WinnerCard key={w.rank} winner={w} index={i} />
              ))}
            </div>
            <p className="winner-blessing">
              🙏 Hare Krishna blessings be with you 🙏
            </p>
          </div>

          {/* RIGHT — Leaderboard */}
          <div className="col-scores">
            <h2 className="scoreboard-title">
              Leaderboard <span className="live-badge">● LIVE</span>
            </h2>
            <table className="scoreboard-table">
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>Name</th>
                  <th>Today's Naam</th>
                  <th>Naam Score</th>
                  <th>Total Score</th>
                </tr>
              </thead>
              <tbody>
                {rankedContestants.map(c => {
                  const naamScore = Math.floor((c.todayNaam || 0) / 1000) * 10
                  return (
                    <tr
                      key={c.bhaktName}
                      className={
                        c.rank === 1 ? 'row-gold'
                        : c.rank === 2 ? 'row-silver'
                        : c.rank === 3 ? 'row-bronze'
                        : c.rank === 4 ? 'row-4'
                        : 'row-5'
                      }
                    >
                      <td className="rank-cell">
                        {c.rank === 1 ? '🥇' : c.rank === 2 ? '🥈' : c.rank === 3 ? '🥉' : `#${c.rank}`}
                      </td>
                      <td className="name-cell">{c.bhaktName}</td>
                      <td className="today-naam-cell">
                        {c.todayNaam > 0 ? c.todayNaam.toLocaleString('en-IN') : '0'}
                      </td>
                      <td className="naam-score-cell">
                        {naamScore > 0 ? `+${naamScore}` : '0'}
                      </td>
                      <td className="score-cell">{c.score}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
            <div className="scoring-formula">
              🕉️ Every 1000 Naam → +10 points &nbsp;·&nbsp; 2000 Naam → +20 points
            </div>
          </div>

        </div>

        <div className="nav-btn-row">
          <button className="nav-btn" onClick={() => navigate('/announcement')}>
            📢 View Tournament Announcement
          </button>
          <button className="nav-btn" onClick={() => navigate('/declaration')}>
            🏆 Winner Declaration
          </button>
        </div>

        <div className="footer-chant">
          Hare Rama • Hare Rama • Rama Rama • Hare Hare
        </div>
      </div>
    </div>
  )
}
