import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Home            from './pages/Home'
import Announcement    from './pages/Announcement'
import Declaration     from './pages/Declaration'
import Admin           from './pages/Admin'
import AdminLogin      from './pages/AdminLogin'
import KeliKunjResult  from './pages/KeliKunjResult'
import './App.css'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"             element={<Home />} />
        <Route path="/announcement" element={<Announcement />} />
        <Route path="/declaration"  element={<Declaration />} />
        <Route path="/admin-login"  element={<AdminLogin />} />
        <Route path="/admin"        element={<Admin />} />
        <Route path="/results"      element={<KeliKunjResult />} />
      </Routes>
    </BrowserRouter>
  )
}
