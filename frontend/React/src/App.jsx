import { BrowserRouter, Routes, Route } from 'react-router';
import Index from './pages/index';
import Login from './pages/login';
import Admin from './pages/admin/admin';
import Dashboard from './pages/admin/admin-dashboard';
import AdminChains from './pages/admin/admin-chains';
import AdminStores from './pages/admin/admin-stores';
import Coordinator from './pages/coordinator/coordinator';
import Captain from './pages/captain/captain';
import Colaborator from './pages/colaborator/colaborator';
import Responsible from './pages/responsible/responsible-store';

import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route index element={<Index />} />
        <Route path="/login" element={<Login />} />
        <Route path="/admin" element={<Admin />} />
        <Route path="/admin/dashboard" element={<Dashboard />} />
        <Route path="/admin/chains" element={<AdminChains />} />
        <Route path="/admin/stores" element={<AdminStores />} />
        <Route path="/coordinator" element={<Coordinator />} />
        <Route path="/captain" element={<Captain />} />
        <Route path="/colaborator" element={<Colaborator />} />
        <Route path="/responsible" element={<Responsible />} />
      </Routes>
    </BrowserRouter>
  )
}
export default App