import { useEffect, useState } from 'react'
import './App.css'

const prizes = [
  { rank: '1st', amount: 400, emoji: '🥇', color: '#FFD700', glow: '#FFD70088', label: 'Champion' },
  { rank: '2nd', amount: 100, emoji: '🥈', color: '#C0C0C0', glow: '#C0C0C088', label: 'Runner Up' },
  { rank: '3rd', amount: 50,  emoji: '🥉', color: '#CD7F32', glow: '#CD7F3288', label: '2nd Runner Up' },
]

const winners = [
  { rank: '1st', name: 'Kishori', emoji: '🥇', color: '#FFD700', glow: '#FFD70088', label: 'Champion',      prize: 400 },
  { rank: '2nd', name: 'Shree',   emoji: '🥈', color: '#C0C0C0', glow: '#C0C0C088', label: 'Runner Up',     prize: 100 },
  { rank: '3rd', name: 'Radha',   emoji: '🥉', color: '#CD7F32', glow: '#CD7F3288', label: '2nd Runner Up', prize: 50  },
]

function Particle({ style }) {
  return <div className="particle" style={style} />
}

function PrizeCard({ prize, index }) {
  const [visible, setVisible] = useState(false)
  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 400 + index * 200)
    return () => clearTimeout(t)
  }, [index])

  return (
    <div
      className={`prize-card ${visible ? 'prize-card--visible' : ''}`}
      style={{ '--glow': prize.glow, '--border': prize.color }}
    >
      <div className="prize-medal">{prize.emoji}</div>
      <div className="prize-rank" style={{ color: prize.color }}>{prize.rank}</div>
      <div className="prize-label">{prize.label}</div>
      <div className="prize-amount">
        <span className="rupee">₹</span>
        <span className="amount">{prize.amount}</span>
      </div>
    </div>
  )
}

function WinnerCard({ winner, index }) {
  const [visible, setVisible] = useState(false)
  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 600 + index * 250)
    return () => clearTimeout(t)
  }, [index])

  return (
    <div
      className={`winner-card ${visible ? 'winner-card--visible' : ''}`}
      style={{ '--glow': winner.glow, '--border': winner.color }}
    >
      <div className="winner-medal">{winner.emoji}</div>
      <div className="winner-rank" style={{ color: winner.color }}>{winner.rank}</div>
      <div className="winner-name">{winner.name}</div>
      <div className="winner-label">{winner.label}</div>
      <div className="winner-prize">
        <span className="rupee">₹</span>
        <span className="amount">{winner.prize}</span>
      </div>
    </div>
  )
}

export default function App() {
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
        <Particle
          key={p.id}
          style={{
            left: p.left,
            width: p.size,
            height: p.size,
            background: p.color,
            animationDuration: p.animationDuration,
            animationDelay: p.animationDelay,
          }}
        />
      ))}

      <div className="container">

        {/* ── Tournament Info ─────────────────── */}
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
            {prizes.map((prize, i) => (
              <PrizeCard key={prize.rank} prize={prize} index={i} />
            ))}
          </div>
        </div>

        <div className="call-to-action">
          <p className="cta-text">⚡ Give it your all — this is YOUR moment! ⚡</p>
          <p className="cta-sub">Jai Shri Krishna 🙏</p>
        </div>

        {/* ── Divider ─────────────────────────── */}
        <div className="section-divider">
          <span className="divider-lotus">🪷</span>
          <span className="divider-line" />
          <span className="divider-lotus">🪷</span>
        </div>

        {/* ── Winner Declaration ──────────────── */}
        <div className="winner-section">
          <div className="winner-crown">👑</div>
          <h2 className="winner-heading">Winners Declared!</h2>
          <p className="winner-sub">
            By the grace of Shri Krishna, the champions have emerged.<br />
            Congratulations to our glorious winners!
          </p>

          <div className="winner-grid">
            {winners.map((w, i) => (
              <WinnerCard key={w.rank} winner={w} index={i} />
            ))}
          </div>

          <p className="winner-blessing">
            🙏 May the divine blessings of Radha Krishna be always with you 🙏
          </p>
        </div>

        <div className="footer-chant">
          Hare Rama • Hare Rama • Rama Rama • Hare Hare
        </div>

      </div>
    </div>
  )
}
