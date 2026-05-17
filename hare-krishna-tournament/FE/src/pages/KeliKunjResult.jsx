import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getKeliKunjList } from '../api/keliKunjApi'

// ── Confetti particle ────────────────────────────
function Confetti({ count = 60 }) {
  const [items] = useState(() =>
    Array.from({ length: count }, (_, i) => ({
      id: i,
      left:    `${Math.random() * 100}%`,
      dur:     `${3 + Math.random() * 5}s`,
      delay:   `${Math.random() * 5}s`,
      size:    `${6 + Math.random() * 10}px`,
      color:   ['#FFD700','#FF9800','#FF5722','#E91E63','#9C27B0','#4CAF50','#2196F3'][Math.floor(Math.random() * 7)],
      rotate:  `${Math.random() * 360}deg`,
      shape:   Math.random() > 0.5 ? '50%' : '0%',
    }))
  )
  return (
    <>
      {items.map(p => (
        <div key={p.id} className="particle" style={{
          left: p.left, width: p.size, height: p.size,
          background: p.color, borderRadius: p.shape,
          animationDuration: p.dur, animationDelay: p.delay,
          transform: `rotate(${p.rotate})`,
        }} />
      ))}
    </>
  )
}

// ── Single week card ─────────────────────────────
const RANKS = [
  { key: '_1', emoji: '🥇', label: '1st Place',     color: '#FFD700', glow: '#FFD70055' },
  { key: '_2', emoji: '🥈', label: '2nd Place',     color: '#C0C0C0', glow: '#C0C0C055' },
  { key: '_3', emoji: '🥉', label: '3rd Place',     color: '#CD7F32', glow: '#CD7F3255' },
]

function WeekCard({ week, index }) {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 200 + index * 180)
    return () => clearTimeout(t)
  }, [index])

  const declared = week.resultDeclared

  return (
    <div className={`kk-week-card ${visible ? 'kk-week-card--visible' : ''} ${declared ? 'kk-week-card--declared' : ''}`}>
      {/* week header */}
      <div className="kk-week-header">
        <span className="kk-week-crown">{declared ? '👑' : '⏳'}</span>
        <h3 className="kk-week-title">Week {week.keliKunjWeek}</h3>
        <span className={`kk-week-badge ${declared ? 'kk-week-badge--declared' : 'kk-week-badge--pending'}`}>
          {declared ? 'Result Declared' : 'Awaiting Result'}
        </span>
      </div>

      {/* winners grid */}
      <div className="kk-week-winners">
        {RANKS.map(r => {
          const bhaktName = declared ? (week.winners?.[r.key]?.bhaktName || '—') : null
          return (
            <div
              key={r.key}
              className="kk-winner-slot"
              style={{ '--slot-color': r.color, '--slot-glow': r.glow }}
            >
              <div className="kk-winner-medal">{r.emoji}</div>
              <div className="kk-winner-rank" style={{ color: r.color }}>{r.label}</div>
              {declared ? (
                <div className="kk-winner-name">{bhaktName}</div>
              ) : (
                <div className="kk-winner-hidden">
                  <span className="kk-mystery-dots">• • •</span>
                  <span className="kk-mystery-label">To be announced</span>
                </div>
              )}
              {/* prize */}
              {declared && (
                <div className="kk-winner-prize">
                  <span className="kk-prize-rupee">₹</span>
                  <span className="kk-prize-amount">{week.prizePool?.[r.key] ?? 0}</span>
                </div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}

// ── Main page ────────────────────────────────────
export default function KeliKunjResult() {
  const navigate        = useNavigate()
  const [weeks, setWeeks] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getKeliKunjList()
      .then(({ data }) => setWeeks(data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const anyDeclared = weeks.some(w => w.resultDeclared)

  return (
    <div className="root">
      {anyDeclared && <Confetti count={70} />}

      <div className="container container--wide">

        {/* header */}
        <div className="top-banner">
          <span className="lotus">🪷</span>
          <span className="chant">Hare Krishna • Hare Krishna</span>
          <span className="lotus">🪷</span>
        </div>

        <div className="title-section">
          <h1 className="tournament-title">
            KeliKunj
            <span className="title-highlight"> Results</span>
          </h1>
          {anyDeclared && (
            <div className="badge-wrap">
              {/* burst particles */}
              {[...Array(12)].map((_, i) => (
                <span key={i} className="burst-dot" style={{ '--i': i }} />
              ))}
              <div className="final-day-badge">
                <span className="fire">🎉</span>
                <span>WINNERS DECLARED</span>
                <span className="fire">🎉</span>
              </div>
            </div>
          )}
        </div>

        {/* content */}
        {loading ? (
          <p style={{ textAlign: 'center', color: '#aaa', marginTop: '3rem' }}>Loading…</p>
        ) : weeks.length === 0 ? (
          <div className="kk-empty-state">
            <div style={{ fontSize: '3rem' }}>🕉️</div>
            <p>No results have been entered yet.</p>
            <p style={{ opacity: 0.6, fontSize: '0.9rem' }}>Check back soon, by the grace of Shri Krishna.</p>
          </div>
        ) : (
          <div className="kk-weeks-list">
            {weeks.map((w, i) => (
              <WeekCard key={w._id} week={w} index={i} />
            ))}
          </div>
        )}

        {anyDeclared && (
          <p className="winner-blessing" style={{ marginTop: '2rem' }}>
            🙏 Hare Krishna blessings be with all the winners 🙏
          </p>
        )}

        <button className="nav-btn" style={{ marginTop: '2rem' }} onClick={() => navigate('/')}>
          📊 View Live Leaderboard
        </button>

        <div className="footer-chant">
          Hare Rama • Hare Rama • Rama Rama • Hare Hare
        </div>
      </div>
    </div>
  )
}
