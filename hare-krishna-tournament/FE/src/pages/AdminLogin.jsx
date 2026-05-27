import { useState, useEffect } from 'react'
import { useNavigate }          from 'react-router-dom'
import toast, { Toaster }       from 'react-hot-toast'
import axiosClient, { TOKEN_KEY, BHAKT_KEY, ROLE_KEY } from '../api/axiosClient'
import { getKrishnaDasList }    from '../api/krishnaDasApi'

export default function AdminLogin() {
  const navigate = useNavigate()
  const [contestants, setContestants] = useState([])
  const [form,        setForm]        = useState({ bhaktName: '', password: '' })
  const [loading,     setLoading]     = useState(false)

  useEffect(() => {
    // If already logged in as admin, skip the form.
    const token = localStorage.getItem(TOKEN_KEY)
    const role  = localStorage.getItem(ROLE_KEY)
    if (token && role === 'admin') {
      navigate('/admin')
      return
    }
    getKrishnaDasList()
      .then(({ data }) => setContestants(data.map(d => d.bhaktName)))
      .catch(() => toast.error('Failed to load contestant list'))
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.bhaktName || !form.password) return
    setLoading(true)
    try {
      const { data } = await axiosClient.post('/auth/login', form)
      if (data.role !== 'admin') {
        toast.error('Admin access required')
        return
      }
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(BHAKT_KEY, data.bhaktName)
      localStorage.setItem(ROLE_KEY,  data.role)
      navigate('/admin')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="root">
      <Toaster
        position="top-right"
        toastOptions={{
          style:   { background: '#1a1025', color: '#fff', border: '1px solid #FF980055', fontSize: '0.95rem' },
          success: { iconTheme: { primary: '#FFD700', secondary: '#000' } },
        }}
      />

      <div className="container" style={{ maxWidth: '440px' }}>
        <div className="top-banner">
          <span className="lotus">🪷</span>
          <span>Admin Login</span>
          <span className="lotus">🪷</span>
        </div>

        <h1 className="tournament-title" style={{ fontSize: '1.75rem', marginBottom: '1.5rem', textAlign: 'center' }}>
          Admin <span className="title-highlight">Access</span>
        </h1>

        <form onSubmit={handleSubmit} className="admin-section" style={{ padding: '1.5rem' }}>
          <div className="admin-field-row">
            <label className="admin-field-label">Bhakt Name</label>
            <select
              className="admin-input"
              value={form.bhaktName}
              onChange={e => setForm(p => ({ ...p, bhaktName: e.target.value }))}
              required
            >
              <option value="">— Select —</option>
              {contestants.map(name => (
                <option key={name} value={name}>{name}</option>
              ))}
            </select>
          </div>

          <div className="admin-field-row" style={{ marginTop: '1rem' }}>
            <label className="admin-field-label">Password</label>
            <input
              className="admin-input"
              type="password"
              autoComplete="current-password"
              value={form.password}
              onChange={e => setForm(p => ({ ...p, password: e.target.value }))}
              required
            />
          </div>

          <button
            className="admin-submit"
            type="submit"
            disabled={loading}
            style={{ width: '100%', marginTop: '1.5rem' }}
          >
            {loading ? 'Logging in…' : 'Log in'}
          </button>

          <p style={{ marginTop: '1rem', textAlign: 'center', opacity: 0.7, fontSize: '0.85rem' }}>
            Forgot password? Contact the tournament admin.
          </p>
        </form>

        <button className="nav-btn" style={{ marginTop: '1.5rem' }} onClick={() => navigate('/')}>
          ← Back to Leaderboard
        </button>
      </div>
    </div>
  )
}
