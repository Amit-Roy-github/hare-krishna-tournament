import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

function getTimeLeft() {
  const now       = new Date()
  const target    = new Date()
  target.setHours(17, 0, 0, 0) // 5:00 PM today

  const diff = target - now

  if (diff <= 0) return null  // time reached

  const hours   = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)

  return { hours, minutes, seconds }
}

export default function Declaration() {
  const navigate = useNavigate()
  const [timeLeft, setTimeLeft] = useState(getTimeLeft())

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

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(getTimeLeft())
    }, 1000)
    return () => clearInterval(timer)
  }, [])

  const pad = (n) => String(n).padStart(2, '0')

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
          <span>Hare Krishna • Hare Krishna</span>
          <span className="lotus">🪷</span>
        </div>

        <h1 className="tournament-title">
          Hare Krishna
          <span className="title-highlight"> Tournament</span>
        </h1>

        {/* Main announcement */}
        <div className="declaration-box">
          <div className="declaration-crown">👑</div>
          <p className="declaration-label">Winner Declaration</p>
          <p className="declaration-time">5:00 PM</p>
          <p className="declaration-sub">
            Today's champion will be announced at<br />
            <strong>5:00 PM sharp</strong> — stay tuned!
          </p>
        </div>

        {/* Countdown */}
        {timeLeft ? (
          <div className="countdown-section">
            <p className="countdown-label">⏳ Time Remaining</p>
            <div className="countdown-grid">
              <div className="countdown-unit">
                <span className="countdown-num">{pad(timeLeft.hours)}</span>
                <span className="countdown-tag">Hours</span>
              </div>
              <span className="countdown-sep">:</span>
              <div className="countdown-unit">
                <span className="countdown-num">{pad(timeLeft.minutes)}</span>
                <span className="countdown-tag">Minutes</span>
              </div>
              <span className="countdown-sep">:</span>
              <div className="countdown-unit">
                <span className="countdown-num">{pad(timeLeft.seconds)}</span>
                <span className="countdown-tag">Seconds</span>
              </div>
            </div>
          </div>
        ) : (
          <div className="declaration-arrived">
            🎉 The moment has arrived! 🎉
          </div>
        )}

        <button className="nav-btn" onClick={() => navigate('/')}>
          📊 View Live Leaderboard
        </button>

        <div className="footer-chant">
          Hare Rama • Hare Rama • Rama Rama • Hare Hare
        </div>

      </div>
    </div>
  )
}
