import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import axiosClient from '../api/axiosClient'
import { getWeekTitle } from '../utils/weekTitle'

const PRIZE_META = [
  { key: '_1',               emoji: '🥇', color: '#FFD700', glow: '#FFD70088', fallback: 'Champion'         },
  { key: '_2',               emoji: '🥈', color: '#C0C0C0', glow: '#C0C0C088', fallback: 'Runner Up'        },
  { key: '_3',               emoji: '🥉', color: '#CD7F32', glow: '#CD7F3288', fallback: '2nd Runner Up'    },
  { key: 'maxNaamJaap',      emoji: '🎯', color: '#E91E63', glow: '#E91E6388', fallback: 'Naam Jaap Hero'   },
  { key: 'totalMaxNaamJaap', emoji: '📿', color: '#9C27B0', glow: '#9C27B088', fallback: 'Naam Jaap Legend' },
]

const getPoolVal = (pp, key, field) => {
  const entry = pp?.[key]
  if (entry && typeof entry === 'object') return entry[field] ?? (field === 'prize' ? 0 : '')
  return field === 'prize' ? (entry ?? 0) : ''
}

function PrizeCard({ meta, week }) {
  const title  = getPoolVal(week?.prizePool, meta.key, 'title') || meta.fallback
  const amount = getPoolVal(week?.prizePool, meta.key, 'prize')

  return (
    <div
      className="prize-card prize-card--visible"
      style={{ '--glow': meta.glow, '--border': meta.color }}
    >
      <div className="prize-medal">{meta.emoji}</div>
      <div className="prize-rank" style={{ color: meta.color }}>{title}</div>
      <div className="prize-amount">
        <span className="rupee">₹</span>
        <span className="amount">{amount}</span>
      </div>
    </div>
  )
}

export default function Announcement() {
  const navigate = useNavigate()
  const [latestWeek, setLatestWeek] = useState(null)

  useEffect(() => {
    axiosClient.get('/keliKunj').then(({ data }) => {
      // pick the most recent week (sorted desc already)
      if (data.length > 0) setLatestWeek(data[0])
    }).catch(() => {})
  }, [])

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

      <div className="container">
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
            <span>{getWeekTitle(latestWeek?.keliKunjWeek)}</span>
            <span className="fire">🔥</span>
          </div>

          <p className="subtitle">
            The moment you've been waiting for has arrived.<br />
            Glory awaits the worthy. May Krishna's blessings be with you!
          </p>
        </div>

        <div className="prize-section">
          <h2 className="prize-title">
            <span className="trophy">🏆</span> Prize Pool <span className="trophy">🏆</span>
          </h2>
          <div className="prize-grid">
            {PRIZE_META.map(meta => (
              <PrizeCard key={meta.key} meta={meta} week={latestWeek} />
            ))}
          </div>
        </div>

        <div className="call-to-action">
          <p className="cta-text">⚡ Give it your all — this is YOUR moment! ⚡</p>
          <p className="cta-sub">Jai Shri Krishna 🙏</p>
        </div>

        <button className="nav-btn" onClick={() => navigate('/')}>
          📊 View Live Scores & Winners
        </button>

        <div className="footer-chant">
          Hare Rama • Hare Rama • Rama Rama • Hare Hare
        </div>
      </div>
    </div>
  )
}
