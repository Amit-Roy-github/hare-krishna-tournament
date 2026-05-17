import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import Announcement from './pages/Announcement'
import Admin from './pages/Admin'
import './App.css'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"             element={<Home />} />
        <Route path="/announcement" element={<Announcement />} />
        <Route path="/admin"        element={<Admin />} />
      </Routes>
    </BrowserRouter>
  )
}
