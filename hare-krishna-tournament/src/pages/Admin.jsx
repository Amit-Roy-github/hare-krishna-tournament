import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

const CONTESTANTS = ['Gopala Das', 'Mohona Das', 'Krishna Das', 'Hari Das', 'Ramu Das']

export default function Admin() {
  const navigate = useNavigate()
  const [naamCounts, setNaamCounts] = useState(
    Object.fromEntries(CONTESTANTS.map(n => [n, '']))
  )
  const [status, setStatus]   = useState(null)   // 'loading' | 'success' | 'error'
  const [current, setCurrent] = useState([])

  // load current scores on mount
  useEffect(() => {
    fetch('/api/scores')
      .then(r => r.json())
      .then(data => {
        setCurrent(data)
        setNaamCounts(Object.fromEntries(data.map(c => [c.name, c.todayNaam ?? 0])))
      })
      .catch(() => {})
  }, [])

  const handleChange = (name, value) => {
    setNaamCounts(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setStatus('loading')
    try {
      const updates = CONTESTANTS.map(name => ({
        name,
        todayNaam: parseInt(naamCounts[name]) || 0,
      }))
      const res = await fetch('/api/scores', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates),
      })
      const data = await res.json()
      setCurrent(data)
      setStatus('success')
      setTimeout(() => setStatus(null), 3000)
    } catch {
      setStatus('error')
      setTimeout(() => setStatus(null), 3000)
    }
  }

  return (
    <div className="root">
      <div className="container" style={{ maxWidth: 520 }}>

        <div className="top-banner">
          <span className="lotus">🪷</span>
          <span>Admin Panel</span>
          <span className="lotus">🪷</span>
        </div>

        <h1 className="tournament-title" style={{ fontSize: '2rem', marginBottom: 32 }}>
          Update <span className="title-highlight">Scores</span>
        </h1>

        <form onSubmit={handleSubmit}>
          <div className="admin-table">
            {/* Header */}
            <div className="admin-row admin-header">
              <span>Name</span>
              <span>Today's Naam</span>
              <span>Naam Score</span>
            </div>

            {/* Rows */}
            {CONTESTANTS.map(name => {
              const val      = parseInt(naamCounts[name]) || 0
              const naamScore = Math.floor(val / 1000) * 10
              return (
                <div key={name} className="admin-row">
                  <span className="admin-name">{name}</span>
                  <input
                    className="admin-input"
                    type="number"
                    min="0"
                    value={naamCounts[name]}
                    onChange={e => handleChange(name, e.target.value)}
                    placeholder="0"
                  />
                  <span className="admin-preview">
                    {naamScore > 0 ? `+${naamScore}` : '0'}
                  </span>
                </div>
              )
            })}
          </div>

          <button
            className="admin-submit"
            type="submit"
            disabled={status === 'loading'}
          >
            {status === 'loading' ? '⏳ Updating…'
              : status === 'success' ? '✅ Updated!'
              : status === 'error'   ? '❌ Failed'
              : '🔄 Update Scores'}
          </button>
        </form>

        {/* Current scores preview */}
        {current.length > 0 && (
          <div className="admin-preview-section">
            <p className="admin-preview-title">Current Leaderboard</p>
            {[...current]
              .sort((a, b) => b.score - a.score)
              .map((c, i) => (
                <div key={c.name} className="admin-preview-row">
                  <span className="admin-preview-rank">
                    {i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : `#${i + 1}`}
                  </span>
                  <span className="admin-preview-name">{c.name}</span>
                  <span className="admin-preview-score">{c.score}</span>
                </div>
              ))}
          </div>
        )}

        <button className="nav-btn" style={{ marginTop: 16 }} onClick={() => navigate('/')}>
          ← Back to Leaderboard
        </button>

      </div>
    </div>
  )
}
