import { BrowserRouter, Routes, Route } from 'react-router';
import Index from './pages/Index';
import Login from './pages/Login';
import Admin from './pages/admin/Admin';
import Dashboard from './pages/admin/AdminDashboard';
import AdminChains from './pages/admin/AdminChains';
import AdminStores from './pages/admin/AdminStores';
import Coordinator from './pages/coordinator/Coordinator';
import Captain from './pages/captain/Captain';
import Colaborator from './pages/colaborator/Colaborator';
import Responsible from './pages/responsible/ResponsibleStore';
import ErrorPage from './pages/ErrorPage';

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
        <Route path="*" element={<ErrorPage />} />
      </Routes>
    </BrowserRouter>
  )
}
export default App