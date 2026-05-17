import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

const prizes = [
  { rank: '1st', amount: 400, emoji: '🥇', color: '#FFD700', glow: '#FFD70088', label: 'Champion' },
  { rank: '2nd', amount: 100, emoji: '🥈', color: '#C0C0C0', glow: '#C0C0C088', label: 'Runner Up' },
  { rank: '3rd', amount: 50,  emoji: '🥉', color: '#CD7F32', glow: '#CD7F3288', label: '2nd Runner Up' },
]

function PrizeCard({ prize, index }) {
  return (
    <div
      className="prize-card prize-card--visible"
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

export default function Announcement() {
  const navigate = useNavigate()
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
            <span>FINALE</span>
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
