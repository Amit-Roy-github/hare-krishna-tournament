import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axiosClient from '../api/axiosClient'
import { getStats } from '../api/statsApi'

const RANK_META = [
  { key: '_1',               rank: '1st', emoji: '🥇', color: '#FFD700', glow: '#FFD70088', label: 'Champion'         },
  { key: '_2',               rank: '2nd', emoji: '🥈', color: '#C0C0C0', glow: '#C0C0C088', label: 'Runner Up'        },
  { key: '_3',               rank: '3rd', emoji: '🥉', color: '#CD7F32', glow: '#CD7F3288', label: '2nd Runner Up'    },
  { key: 'maxNaamJaap',      rank: 'Hero',   emoji: '🎯', color: '#E91E63', glow: '#E91E6388', label: 'Naam Jaap Hero'   },
  { key: 'totalMaxNaamJaap', rank: 'Legend', emoji: '📿', color: '#9C27B0', glow: '#9C27B088', label: 'Naam Jaap Legend' },
]

function WinnerCard({ meta, week, index }) {
  const [visible, setVisible] = useState(false)
  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 400 + index * 250)
    return () => clearTimeout(t)
  }, [index])

  const declared  = week?.resultDeclared
  const name      = week?.winners?.[meta.key]?.bhaktName
  const prize     = week?.prizePool?.[meta.key]?.prize ?? 0

  return (
    <div
      className={`winner-card ${visible ? 'winner-card--visible' : ''}`}
      style={{ '--glow': meta.glow, '--border': meta.color }}
    >
      <div className="winner-medal">{meta.emoji}</div>
      <div className="winner-rank" style={{ color: meta.color }}>{meta.rank}</div>
      {declared && name
        ? <div className="winner-name">{name}</div>
        : <div className="winner-tbd">To be announced</div>
      }
      <div className="winner-label">{week?.prizePool?.[meta.key]?.title || meta.label}</div>
      {prize > 0 && (
        <div className="winner-prize">
          <span className="rupee">₹</span>
          <span className="amount">{prize}</span>
        </div>
      )}
    </div>
  )
}

// ── Stats Section ────────────────────────────────────────────────────────────

const fmtTime = (iso) => {
  if (!iso) return null
  const d = new Date(iso)
  if (isNaN(d)) return null
  return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

function OverallScoreBlock({ stats }) {
  if (!stats) return null
  const { days, overall, dates, heroName, legendName } = stats
  return (
    <div className="stats-block">
      <h3 className="stats-heading">📊 Overall Weekly Score</h3>
      <div className="stats-table-wrap">
        <table className="stats-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              {days.map(d => <th key={d.date}>{d.dayName}</th>)}
              <th>Best Day</th>
              <th>Naam Total</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {overall.map((o, i) => (
              <tr key={o.bhaktName} className={i === 0 ? 'row-gold' : i === 1 ? 'row-silver' : i === 2 ? 'row-bronze' : ''}>
                <td className="rank-cell">
                  {i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : `#${i + 1}`}
                </td>
                <td className="name-cell">
                  {o.bhaktName}
                  {o.bhaktName === heroName   && <span className="badge-hero"  title="Naam Jaap Hero">🎯</span>}
                  {o.bhaktName === legendName && <span className="badge-legend" title="Naam Jaap Legend">📿</span>}
                </td>
                {dates.map(dk => (
                  <td key={dk} className="score-cell">
                    {o.dayScores[dk] ?? 0}
                  </td>
                ))}
                <td className="score-cell">{(o.maxDayNaamCount || 0).toLocaleString('en-IN')}</td>
                <td className="score-cell">{(o.totalNaamCount  || 0).toLocaleString('en-IN')}</td>
                <td className="score-cell stats-total">{o.totalScore}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="stats-legend-row">
        <span className="stats-legend-item">
          <span className="stats-legend-label">🎯 Naam Jaap Hero</span>
          {heroName && <span className="stats-legend-name">{heroName}</span>}
        </span>
        <span className="stats-legend-item">
          <span className="stats-legend-label">📿 Naam Jaap Legend</span>
          {legendName && <span className="stats-legend-name">{legendName}</span>}
        </span>
      </div>
    </div>
  )
}

function StatsSection({ stats }) {
  if (!stats) return null
  const { days } = stats

  return (
    <div className="stats-section">
      {/* ── Overall Table ── */}
      <OverallScoreBlock stats={stats} />

      {/* ── Day-wise Tables ── (most recent first) */}
      {[...days].reverse().map(day => (
        <div key={day.date} className="stats-block">
          <h3 className="stats-heading">
            📅 {day.dayName} — <span className="stats-date">{day.date}</span>
          </h3>
          <div className="stats-table-wrap">
            <table className="stats-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Name</th>
                  <th>Niyam 1</th>
                  <th>Niyam 2</th>
                  <th>Niyam 3</th>
                  <th>Naam Pts</th>
                  <th>Naam Count</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {day.records.map((r, i) => (
                  <tr key={r.bhaktName} className={i === 0 ? 'row-gold' : i === 1 ? 'row-silver' : i === 2 ? 'row-bronze' : ''}>
                    <td className="rank-cell">
                      {i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : `#${i + 1}`}
                    </td>
                    <td className="name-cell">{r.bhaktName}</td>
                    {[
                      [r.niyam1Point, r.niyam1DoneAt],
                      [r.niyam2Point, r.niyam2DoneAt],
                      [r.niyam3Point, r.niyam3DoneAt],
                    ].map(([pt, at], idx) => (
                      <td key={idx} className="score-cell niyam-cell">
                        <span>{pt}</span>
                        {fmtTime(at) && <span className="niyam-time">{fmtTime(at)}</span>}
                      </td>
                    ))}
                    <td className="score-cell">
                      {r.naamPoints > 0 ? `+${r.naamPoints}` : '0'}
                    </td>
                    <td className="today-naam-cell">
                      {r.naamCount > 0 ? r.naamCount.toLocaleString('en-IN') : '0'}
                    </td>
                    <td className="score-cell stats-total">{r.dayTotal}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </div>
  )
}

// ── Home Page ────────────────────────────────────────────────────────────────

export default function Home() {
  const navigate = useNavigate()
  const [contestants,  setContestants]  = useState([])
  const [latestWeek,   setLatestWeek]   = useState(null)
  const [activeWeek,   setActiveWeek]   = useState(null)  // most-recent week (any)
  const [stats,        setStats]        = useState(null)
  const [bannerTaps,   setBannerTaps]   = useState(0)

  const handleBannerTap = () => {
    const next = bannerTaps + 1
    if (next >= 3) { navigate('/admin'); setBannerTaps(0) }
    else setBannerTaps(next)
  }

  useEffect(() => {
    // scores — poll every 5s
    const fetchScores = async () => {
      try {
        const { data } = await axiosClient.get('/scores')
        setContestants(data)
      } catch {}
    }
    fetchScores()
    const interval = setInterval(fetchScores, 5000)

    // keliKunj — fetch once
    axiosClient.get('/keliKunj').then(({ data }) => {
      // most-recent week (for showLeaderboard flag)
      if (data.length > 0) setActiveWeek(data[0])
      // most-recent *declared* week (for winner display)
      const declared = data.filter(w => w.resultDeclared)
      if (declared.length > 0) setLatestWeek(declared[0])
    }).catch(() => {})

    // stats — fetch once on mount
    getStats().then(({ data }) => setStats(data)).catch(() => {})

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
        <div className="top-banner" onClick={handleBannerTap} style={{ cursor: 'default', userSelect: 'none' }}>
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
            <span>
              {activeWeek && activeWeek.keliKunjWeek < 2 && !activeWeek.resultDeclared
                ? `Week ${activeWeek.keliKunjWeek}`
                : 'FINALE'}
            </span>
            <span className="fire">🔥</span>
          </div>
        </div>

        {/* ── Layout: row-based when showLeaderboard, day-wise tables otherwise ── */}
        {(activeWeek?.showLeaderboard ?? false) ? (
          <div className="home-rows">

            {/* TOP — Overall Weekly Score (same table as in day-wise stats) */}
            <OverallScoreBlock stats={stats} />

            {/* BOTTOM — Winners */}
            <div className="col-winners col-winners--row">
              <div className="winner-crown">👑</div>
              <h2 className="winner-heading">
                {latestWeek ? `Week ${latestWeek.keliKunjWeek} Winners!` : 'Be the Winners'}
              </h2>
              <p className="winner-sub">
                {latestWeek
                  ? 'By the grace of Shri Krishna the champions have emerged.'
                  : 'Results will be revealed once declared by the admin.'}
              </p>
              <div className="winner-grid winner-grid--col">
                {RANK_META.map((meta, i) => (
                  <WinnerCard key={meta.key} meta={meta} week={latestWeek} index={i} />
                ))}
              </div>
              <p className="winner-blessing">
                🙏 Hare Krishna blessings be with you 🙏
              </p>
            </div>

          </div>
        ) : (
          <StatsSection stats={stats} />
        )}

        <div className="nav-btn-row">
          <button className="nav-btn" onClick={() => navigate('/announcement')}>
            📢 View Tournament Announcement
          </button>
          <button className="nav-btn" onClick={() => navigate('/declaration')}>
            🏆 Winner Declaration
          </button>
          <button className="nav-btn" onClick={() => navigate('/results')}>
            🏅 KeliKunj Results
          </button>
        </div>

        <div className="footer-chant">
          Hare Rama • Hare Rama • Rama Rama • Hare Hare
        </div>
      </div>
    </div>
  )
}
