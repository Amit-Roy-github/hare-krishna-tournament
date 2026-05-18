import { useState, useEffect }              from 'react'
import { useNavigate }                       from 'react-router-dom'
import toast, { Toaster }                   from 'react-hot-toast'
import * as Switch                           from '@radix-ui/react-switch'
import * as AlertDialog                      from '@radix-ui/react-alert-dialog'
import * as Tooltip                          from '@radix-ui/react-tooltip'
import { Pencil, Trash2, Check, X }         from 'lucide-react'
import { getScores, updateScores }           from '../api/sadhanaApi'
import { getKrishnaDasList, createKrishnaDas,
         updateKrishnaDas, deleteKrishnaDas } from '../api/krishnaDasApi'
import { getKeliKunjList, createKeliKunj,
         updateKeliKunj }                    from '../api/keliKunjApi'

// ── Constants ────────────────────────────────────
const EMPTY_USER = { bhaktName: '', email: '', phone: '', sansarName: '' }

const SADHANA_FIELDS = [
  { key: 'naamJaapCount', label: "Today's Naam" },
  { key: 'niyam1Point',   label: 'Niyam 1'      },
  { key: 'niyam2Point',   label: 'Niyam 2'      },
  { key: 'niyam3Point',   label: 'Niyam 3'      },
]

// ── Radix Switch — styled toggle ─────────────────
function PlaygroundSwitch({ checked, onCheckedChange, disabled }) {
  return (
    <Switch.Root
      checked={checked}
      onCheckedChange={onCheckedChange}
      disabled={disabled}
      className={`switch-root ${checked ? 'switch-root--on' : 'switch-root--off'}`}
    >
      <Switch.Thumb className="switch-thumb" />
    </Switch.Root>
  )
}

// ── Radix AlertDialog — delete confirmation ───────
function DeleteDialog({ bhaktName, onConfirm }) {
  return (
    <AlertDialog.Root>
      <Tooltip.Root>
        <AlertDialog.Trigger asChild>
          <Tooltip.Trigger asChild>
            <button className="kl-btn kl-btn--delete"><Trash2 size={15} /></button>
          </Tooltip.Trigger>
        </AlertDialog.Trigger>
        <Tooltip.Content className="tooltip-content" sideOffset={5}>
          Delete
        </Tooltip.Content>
      </Tooltip.Root>

      <AlertDialog.Portal>
        <AlertDialog.Overlay className="alert-overlay" />
        <AlertDialog.Content className="alert-content">
          <AlertDialog.Title className="alert-title">
            Delete Krishnadas
          </AlertDialog.Title>
          <AlertDialog.Description className="alert-desc">
            Are you sure you want to delete <strong>{bhaktName}</strong>?
            This will also remove all their sadhana records. This action cannot be undone.
          </AlertDialog.Description>
          <div className="alert-actions">
            <AlertDialog.Cancel asChild>
              <button className="alert-btn alert-btn--cancel">Cancel</button>
            </AlertDialog.Cancel>
            <AlertDialog.Action asChild>
              <button className="alert-btn alert-btn--confirm" onClick={onConfirm}>
                Yes, Delete
              </button>
            </AlertDialog.Action>
          </div>
        </AlertDialog.Content>
      </AlertDialog.Portal>
    </AlertDialog.Root>
  )
}

// ── Register Krishnadas ──────────────────────────
function RegisterSection({ onCreated }) {
  const [form,    setForm]    = useState(EMPTY_USER)
  const [loading, setLoading] = useState(false)

  const handleChange = (key, value) =>
    setForm(prev => ({ ...prev, [key]: value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await createKrishnaDas(form)
      setForm(EMPTY_USER)
      onCreated()
      toast.success('Krishnadas registered!')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h2 className="admin-section-title">🪷 Register Krishnadas</h2>
      <form onSubmit={handleSubmit}>
        <div className="admin-fields">
          {[
            { key: 'bhaktName',  label: 'Bhakt Name *', placeholder: 'Gopala Das'        },
            { key: 'sansarName', label: 'Sansar Name',  placeholder: 'Ramesh Kumar'      },
            { key: 'email',      label: 'Email',        placeholder: 'email@example.com' },
            { key: 'phone',      label: 'Phone',        placeholder: '+91 9876543210'    },
          ].map(({ key, label, placeholder }) => (
            <div key={key} className="admin-field-row">
              <label className="admin-field-label">{label}</label>
              <input
                className="admin-input"
                type={key === 'email' ? 'email' : 'text'}
                value={form[key]}
                onChange={e => handleChange(key, e.target.value)}
                placeholder={placeholder}
                required={key === 'bhaktName'}
              />
            </div>
          ))}
        </div>
        <button className="admin-submit" type="submit" disabled={loading}>
          {loading ? 'Registering…' : '+ Register'}
        </button>
      </form>
    </div>
  )
}

// ── Update Sadhana ───────────────────────────────
function UpdateSadhanaSection({ scores, onUpdated }) {
  const [fields,  setFields]  = useState({})
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!scores.length) return
    const init = {}
    scores.forEach(c => {
      init[c.bhaktName] = {
        naamJaapCount: c.todayNaam   || 0,
        niyam1Point:   c.niyam1Point || 0,
        niyam2Point:   c.niyam2Point || 0,
        niyam3Point:   c.niyam3Point || 0,
      }
    })
    setFields(init)
  }, [scores])

  const handleChange = (bhaktName, key, value) =>
    setFields(prev => ({
      ...prev,
      [bhaktName]: { ...prev[bhaktName], [key]: value },   // keep raw string while typing
    }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      // Convert raw strings to numbers only on submit
      const updates = Object.entries(fields).map(([bhaktName, data]) => ({
        bhaktName,
        ...Object.fromEntries(
          Object.entries(data).map(([k, v]) => [k, Number(v) || 0])
        ),
      }))
      await updateScores(updates)
      onUpdated()
      toast.success('Sadhana saved!')
    } catch {
      toast.error('Failed to save sadhana')
    } finally {
      setLoading(false)
    }
  }

  if (!scores.length) return (
    <div className="admin-section">
      <h2 className="admin-section-title">📊 Update Sadhana</h2>
      <p className="admin-empty">No contestants yet. Register a Krishnadas first.</p>
    </div>
  )

  return (
    <div className="admin-section">
      <h2 className="admin-section-title">📊 Update Today's Sadhana</h2>
      <form onSubmit={handleSubmit}>
        <div className="admin-table">
          {/* Sticky header */}
          <div className="admin-table-header">
            <div className="admin-row admin-header admin-row--sadhana">
              <span>Bhakt Name</span>
              {SADHANA_FIELDS.map(f => <span key={f.key}>{f.label}</span>)}
            </div>
          </div>

          {/* Scrollable body — vertical scroll kicks in after 6 users */}
          <div className="admin-table-body">
            {scores.map(c => (
              <div key={c.bhaktName} className="admin-row admin-row--sadhana">
                <span className="admin-name">{c.bhaktName}</span>
                {SADHANA_FIELDS.map(f => (
                  <input
                    key={f.key}
                    className="admin-input"
                    type="number"
                    min="0"
                    value={fields[c.bhaktName]?.[f.key] ?? 0}
                    onChange={e => handleChange(c.bhaktName, f.key, e.target.value)}
                  />
                ))}
              </div>
            ))}
          </div>
        </div>
        <button className="admin-submit" type="submit" disabled={loading}>
          {loading ? 'Saving…' : '💾 Save Sadhana'}
        </button>
      </form>
    </div>
  )
}

// ── KrishnaDas List ──────────────────────────────
function KrishnaDasList({ users, onRefresh }) {
  const [editId,   setEditId]   = useState(null)
  const [editForm, setEditForm] = useState({})

  const startEdit = (u) => {
    setEditId(u._id)
    setEditForm({
      sansarName:          u.sansarName || '',
      email:               u.email      || '',
      phone:               u.phone      || '',
      includeInPlayground: u.includeInPlayground, // exact DB value — no defaulting here
    })
  }

  const cancelEdit = () => { setEditId(null); setEditForm({}) }

  const saveEdit = async (id) => {
    try {
      await updateKrishnaDas(id, editForm)
      setEditId(null)
      onRefresh()
      toast.success('Updated successfully!')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Update failed')
    }
  }

  const handleToggle = async (u) => {
    try {
      await updateKrishnaDas(u._id, { includeInPlayground: !u.includeInPlayground })
      onRefresh()
      toast.success(`${u.bhaktName} ${!u.includeInPlayground ? 'added to' : 'removed from'} tournament`)
    } catch {
      toast.error('Failed to update')
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteKrishnaDas(id)
      onRefresh()
      toast.success('Krishnadas deleted')
    } catch {
      toast.error('Delete failed')
    }
  }

  if (!users.length) return (
    <div className="admin-section">
      <h2 className="admin-section-title">👥 All Krishnadas</h2>
      <p className="admin-empty">No users registered yet.</p>
    </div>
  )

  return (
    <Tooltip.Provider delayDuration={300}>
      <div className="admin-section">
        <h2 className="admin-section-title">👥 All Krishnadas</h2>

        <div className="kl-table">
          <div className="kl-row kl-header">
            <span>Bhakt Name</span>
            <span>Sansar Name</span>
            <span>Phone</span>
            <span>In Tournament</span>
            <span>Actions</span>
          </div>

          {users.map(u => (
            <div key={u._id} className="kl-row">
              {editId === u._id ? (
                /* ── Edit mode ── */
                <>
                  {/* Bhakt name is read-only — cannot be changed */}
                  <span className="kl-name kl-name--readonly">{u.bhaktName}</span>

                  <input
                    className="admin-input"
                    value={editForm.sansarName}
                    onChange={e => setEditForm(p => ({ ...p, sansarName: e.target.value }))}
                    placeholder="Sansar Name"
                  />
                  <input
                    className="admin-input"
                    value={editForm.phone}
                    onChange={e => setEditForm(p => ({ ...p, phone: e.target.value }))}
                    placeholder="Phone"
                  />

                  <PlaygroundSwitch
                    checked={editForm.includeInPlayground}
                    onCheckedChange={val => setEditForm(p => ({ ...p, includeInPlayground: val }))}
                  />

                  <div className="kl-actions">
                    <Tooltip.Root>
                      <Tooltip.Trigger asChild>
                        <button className="kl-btn kl-btn--save" onClick={() => saveEdit(u._id)}>
                          <Check size={15} />
                        </button>
                      </Tooltip.Trigger>
                      <Tooltip.Content className="tooltip-content" sideOffset={5}>Save</Tooltip.Content>
                    </Tooltip.Root>

                    <Tooltip.Root>
                      <Tooltip.Trigger asChild>
                        <button className="kl-btn kl-btn--cancel" onClick={cancelEdit}>
                          <X size={15} />
                        </button>
                      </Tooltip.Trigger>
                      <Tooltip.Content className="tooltip-content" sideOffset={5}>Cancel</Tooltip.Content>
                    </Tooltip.Root>
                  </div>
                </>
              ) : (
                /* ── View mode ── */
                <>
                  <span className="kl-name">{u.bhaktName}</span>
                  <span className="kl-meta">{u.sansarName || '—'}</span>
                  <span className="kl-meta">{u.phone      || '—'}</span>

                  <PlaygroundSwitch
                    checked={u.includeInPlayground}
                    onCheckedChange={() => handleToggle(u)}
                  />

                  <div className="kl-actions">
                    <Tooltip.Root>
                      <Tooltip.Trigger asChild>
                        <button className="kl-btn kl-btn--edit" onClick={() => startEdit(u)}>
                          <Pencil size={15} />
                        </button>
                      </Tooltip.Trigger>
                      <Tooltip.Content className="tooltip-content" sideOffset={5}>Edit</Tooltip.Content>
                    </Tooltip.Root>

                    <DeleteDialog bhaktName={u.bhaktName} onConfirm={() => handleDelete(u._id)} />
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
      </div>
    </Tooltip.Provider>
  )
}

// ── KeliKunj Section ─────────────────────────────

const PLACE_CONFIG = [
  { key: '_1',               emoji: '🥇', label: '1st Place',      defaultTitle: 'Champion'         },
  { key: '_2',               emoji: '🥈', label: '2nd Place',      defaultTitle: 'Runner Up'        },
  { key: '_3',               emoji: '🥉', label: '3rd Place',      defaultTitle: '2nd Runner Up'    },
  { key: 'maxNaamJaap',      emoji: '🎯', label: 'Max Naam Jaap',  defaultTitle: 'Naam Jaap Hero'   },
  { key: 'totalMaxNaamJaap', emoji: '📿', label: 'Total Max Naam', defaultTitle: 'Naam Jaap Legend' },
]

const PRIZE_DEFAULTS = {
  _1: 'Champion', _2: 'Runner Up', _3: '2nd Runner Up',
  maxNaamJaap: 'Naam Jaap Hero', totalMaxNaamJaap: 'Naam Jaap Legend',
}

const EMPTY_POOL = Object.fromEntries(
  Object.keys(PRIZE_DEFAULTS).map(k => [k, { prize: '', title: '' }])
)

const EMPTY_KELIKUNJ = {
  keliKunjWeek: '',
  winners:   { _1: '', _2: '', _3: '', maxNaamJaap: '', totalMaxNaamJaap: '' },
  prizePool: EMPTY_POOL,
}

const buildPrizePool = (pp) =>
  Object.fromEntries(
    Object.keys(PRIZE_DEFAULTS).map(k => [k, {
      prize: Number(pp[k]?.prize) || 0,
      title: pp[k]?.title || PRIZE_DEFAULTS[k],
    }])
  )

// helper to read prize/title from old (number) or new (object) prizePool format
const getPoolVal = (pp, key, field) => {
  const entry = pp?.[key]
  if (entry && typeof entry === 'object') return entry[field] ?? (field === 'prize' ? 0 : '')
  return field === 'prize' ? (entry ?? 0) : ''
}

function KeliKunjSection({ users, keliKunjList, onRefresh }) {
  const [form,    setForm]    = useState(EMPTY_KELIKUNJ)
  const [editId,  setEditId]  = useState(null)
  const [editForm,setEditForm]= useState({})
  const [loading, setLoading] = useState(false)

  const setWinner = (key, val) => setForm(p => ({ ...p, winners: { ...p.winners, [key]: val } }))
  const setPrize  = (key, val) => setForm(p => ({ ...p, prizePool: { ...p.prizePool, [key]: { ...p.prizePool[key], prize: val } } }))
  const setTitle  = (key, val) => setForm(p => ({ ...p, prizePool: { ...p.prizePool, [key]: { ...p.prizePool[key], title: val } } }))

  const setEditWinner = (key, val) => setEditForm(p => ({ ...p, winners: { ...p.winners, [key]: val } }))
  const setEditPrize  = (key, val) => setEditForm(p => ({ ...p, prizePool: { ...p.prizePool, [key]: { ...p.prizePool[key], prize: val } } }))
  const setEditTitle  = (key, val) => setEditForm(p => ({ ...p, prizePool: { ...p.prizePool, [key]: { ...p.prizePool[key], title: val } } }))

  const buildWinners = (w) => Object.fromEntries(
    Object.keys(PRIZE_DEFAULTS).map(k => [k, w[k] || null])
  )

  const handleCreate = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await createKeliKunj({
        keliKunjWeek: Number(form.keliKunjWeek),
        winners:   buildWinners(form.winners),
        prizePool: buildPrizePool(form.prizePool),
      })
      setForm(EMPTY_KELIKUNJ)
      onRefresh()
      toast.success('KeliKunj week created!')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to create')
    } finally {
      setLoading(false)
    }
  }

  const startEdit = (k) => {
    setEditId(k._id)
    setEditForm({
      winners: Object.fromEntries(
        Object.keys(PRIZE_DEFAULTS).map(key => [key, k.winners?.[key]?._id || ''])
      ),
      prizePool: Object.fromEntries(
        Object.keys(PRIZE_DEFAULTS).map(key => [key, {
          prize: getPoolVal(k.prizePool, key, 'prize'),
          title: getPoolVal(k.prizePool, key, 'title'),
        }])
      ),
      resultDeclared: k.resultDeclared ?? false,
    })
  }

  const saveEdit = async () => {
    try {
      await updateKeliKunj(editId, {
        winners:        buildWinners(editForm.winners),
        prizePool:      buildPrizePool(editForm.prizePool),
        resultDeclared: editForm.resultDeclared,
      })
      setEditId(null)
      onRefresh()
      toast.success('KeliKunj updated!')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Update failed')
    }
  }

  const toggleDeclared = async (k) => {
    try {
      await updateKeliKunj(k._id, {
        winners:        Object.fromEntries(Object.keys(PRIZE_DEFAULTS).map(key => [key, k.winners?.[key]?._id || null])),
        prizePool:      k.prizePool,
        resultDeclared: !k.resultDeclared,
      })
      onRefresh()
      toast.success(`Week ${k.keliKunjWeek}: result ${!k.resultDeclared ? 'declared' : 'hidden'}`)
    } catch (err) {
      toast.error(err.response?.data?.error || 'Update failed')
    }
  }

  const WinnerSelect = ({ value, onChange }) => (
    <select className="admin-input kk-select" value={value} onChange={e => onChange(e.target.value)}>
      <option value="">— None —</option>
      {users.map(u => (
        <option key={u._id} value={u._id}>{u.bhaktName}</option>
      ))}
    </select>
  )

  return (
    <div className="admin-section">
      <h2 className="admin-section-title">🏆 KeliKunj — Winners</h2>

      {/* ── Create form ── */}
      <form onSubmit={handleCreate}>
        <div className="kk-card kk-card--create">
          <div className="kk-card-top">
            <div className="kk-inline-field">
              <label className="admin-field-label">Week No. *</label>
              <input
                className="admin-input kk-week-input"
                type="number" min="1" placeholder="e.g. 1"
                value={form.keliKunjWeek}
                onChange={e => setForm(p => ({ ...p, keliKunjWeek: e.target.value }))}
                required
              />
            </div>
            <button className="admin-submit kk-create-btn" type="submit" disabled={loading}>
              {loading ? 'Creating…' : '+ Create Week'}
            </button>
          </div>

          {PLACE_CONFIG.map(({ key, emoji, label, defaultTitle }) => (
            <div key={key} className="kk-place-row">
              <span className="kk-place-label">{emoji} {label}</span>
              <WinnerSelect value={form.winners[key]} onChange={v => setWinner(key, v)} />
              <span className="kk-place-label kk-title-label">Title</span>
              <input className="admin-input" type="text" placeholder={defaultTitle}
                value={form.prizePool[key].title} onChange={e => setTitle(key, e.target.value)} />
              <span className="kk-place-label kk-prize-label">Prize (₹)</span>
              <input className="admin-input" type="number" min="0"
                value={form.prizePool[key].prize} onChange={e => setPrize(key, e.target.value)} />
            </div>
          ))}
        </div>
      </form>

      {/* ── List ── */}
      {keliKunjList.length > 0 && (
        <div className="kk-cards-list">
          {keliKunjList.map(k => (
            <div key={k._id} className={`kk-card ${editId === k._id ? 'kk-card--editing' : ''}`}>

              <div className="kk-card-top">
                <span className="kk-week-title-label">Week {k.keliKunjWeek}</span>
                <div className="kk-card-right">
                  {editId === k._id ? (
                    <>
                      <span className="admin-field-label" style={{ marginRight: '0.4rem' }}>Result Declared</span>
                      <Switch.Root
                        className={`switch-root ${editForm.resultDeclared ? 'switch-root--on' : 'switch-root--off'}`}
                        checked={editForm.resultDeclared}
                        onCheckedChange={v => setEditForm(p => ({ ...p, resultDeclared: v }))}
                      >
                        <Switch.Thumb className="switch-thumb" />
                      </Switch.Root>
                      <button className="kl-btn kl-btn--save"   onClick={saveEdit}><Check size={15}/></button>
                      <button className="kl-btn kl-btn--cancel" onClick={() => setEditId(null)}><X size={15}/></button>
                    </>
                  ) : (
                    <>
                      <span className="admin-field-label" style={{ fontSize: '0.8rem', opacity: 0.7 }}>Result Declared</span>
                      <Switch.Root
                        className={`switch-root ${k.resultDeclared ? 'switch-root--on' : 'switch-root--off'}`}
                        checked={!!k.resultDeclared}
                        onCheckedChange={() => toggleDeclared(k)}
                      >
                        <Switch.Thumb className="switch-thumb" />
                      </Switch.Root>
                      <button className="kl-btn kl-btn--edit" onClick={() => startEdit(k)}><Pencil size={15}/></button>
                    </>
                  )}
                </div>
              </div>

              {PLACE_CONFIG.map(({ key, emoji, label }) => (
                <div key={key} className="kk-place-row">
                  <span className="kk-place-label">{emoji} {label}</span>
                  {editId === k._id
                    ? <WinnerSelect value={editForm.winners[key]} onChange={v => setEditWinner(key, v)} />
                    : <span className="kl-meta">{k.winners?.[key]?.bhaktName || '—'}</span>
                  }
                  <span className="kk-place-label kk-title-label">Title</span>
                  {editId === k._id
                    ? <input className="admin-input" type="text"
                        value={editForm.prizePool[key]?.title || ''} onChange={e => setEditTitle(key, e.target.value)} />
                    : <span className="kl-meta">{getPoolVal(k.prizePool, key, 'title') || '—'}</span>
                  }
                  <span className="kk-place-label kk-prize-label">Prize (₹)</span>
                  {editId === k._id
                    ? <input className="admin-input" type="number" min="0"
                        value={editForm.prizePool[key]?.prize ?? ''} onChange={e => setEditPrize(key, e.target.value)} />
                    : <span className="kl-meta">₹{getPoolVal(k.prizePool, key, 'prize')}</span>
                  }
                </div>
              ))}

            </div>
          ))}
        </div>
      )}
    </div>
  )
}

// ── Main Admin page ──────────────────────────────
export default function Admin() {
  const navigate = useNavigate()
  const [scores,       setScores]       = useState([])
  const [users,        setUsers]        = useState([])
  const [keliKunjList, setKeliKunjList] = useState([])

  const loadScores    = async () => { try { const { data } = await getScores();         setScores(data)       } catch {} }
  const loadUsers     = async () => { try { const { data } = await getKrishnaDasList(); setUsers(data)        } catch {} }
  const loadKeliKunj  = async () => { try { const { data } = await getKeliKunjList();   setKeliKunjList(data) } catch {} }

  const refresh = () => { loadScores(); loadUsers(); loadKeliKunj() }

  useEffect(() => { refresh() }, [])

  return (
    <div className="root">
      <Toaster
        position="top-right"
        toastOptions={{
          style:   { background: '#1a1025', color: '#fff', border: '1px solid #FF980055', fontSize: '0.95rem' },
          success: { iconTheme: { primary: '#FFD700', secondary: '#000' } },
        }}
      />

      <div className="container container--wide">
        <div className="top-banner">
          <span className="lotus">🪷</span>
          <span>Admin Panel</span>
          <span className="lotus">🪷</span>
        </div>

        <h1 className="tournament-title" style={{ fontSize: '2rem', marginBottom: '2rem' }}>
          Admin <span className="title-highlight">Dashboard</span>
        </h1>

        <div className="admin-columns">
          <RegisterSection onCreated={refresh} />
          <UpdateSadhanaSection scores={scores} onUpdated={refresh} />
        </div>

        <div className="admin-divider" />

        <KrishnaDasList users={users} onRefresh={refresh} />

        <div className="admin-divider" />

        <KeliKunjSection users={users} keliKunjList={keliKunjList} onRefresh={refresh} />

        <button className="nav-btn" style={{ marginTop: '1.5rem' }} onClick={() => navigate('/')}>
          ← Back to Leaderboard
        </button>
      </div>
    </div>
  )
}
