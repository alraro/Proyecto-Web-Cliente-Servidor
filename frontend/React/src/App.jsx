import { BrowserRouter, Routes, Route } from 'react-router';
import Index from './pages/Index';
import Login from './pages/Login';
import Register from './pages/Register';
import Admin from './pages/admin/Admin';
import Dashboard from './pages/admin/AdminDashboard';
import AdminChains from './pages/admin/AdminChains';
import AdminStores from './pages/admin/AdminStores';
import AdminCreateUser from './pages/admin/AdminCreateUser';
import AdminVolunteers from './pages/admin/AdminVolunteers';
import Coordinator from './pages/coordinator/Coordinator';
import Captain from './pages/captain/Captain';
import Colaborator from './pages/colaborator/Colaborator';
import Responsible from './pages/responsible/ResponsibleStore';
import ErrorPage from './pages/ErrorPage';
import Edit from './pages/Edit.jsx';

import './App.css';
import { RutaProtegida } from './pages/auth/RutaProtegida';
import { ProveedorAuten } from './pages/auth/ProveedorAuten';

function App() {
  return (
    <ProveedorAuten>
      <BrowserRouter>
        <Routes>

          {/* Account management routes */}
          <Route index element={<Index />} /> 
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/edit" element={<Edit />} />
          
          {/* Admin routes */}
          <Route element={<RutaProtegida roles={['ADMINISTRADOR']} />}>
            <Route path="/admin" element={<Admin />} />
            <Route path="/admin/dashboard" element={<Dashboard />} />
            <Route path="/admin/chains" element={<AdminChains />} />
            <Route path="/admin/stores" element={<AdminStores />} />
            <Route path="/admin/volunteers" element={<AdminVolunteers />} />
            <Route path="/admin/create-user" element={<AdminCreateUser />} />
          </Route>

          {/* Coordinator routes */}
          <Route element={<RutaProtegida roles={['COORDINADOR']} />}>
            <Route path="/coordinator" element={<Coordinator />} />
          </Route>

          {/* Captain routes */}
          <Route element={<RutaProtegida roles={['CAPITAN']} />}>
            <Route path="/captain" element={<Captain />} />
          </Route>

          {/* Colaborator routes */}
          <Route element={<RutaProtegida roles={['COLABORADOR']} />}>
            <Route path="/colaborator" element={<Colaborator />} />
          </Route>

          {/* Responsible routes */}
          <Route element={<RutaProtegida roles={['RESPONSABLE_TIENDA']} />}>
            <Route path="/responsible" element={<Responsible />} />
          </Route>

          {/* Error page route (fallback) */}
          <Route path="*" element={<ErrorPage />} />
        </Routes>
      </BrowserRouter>
    </ProveedorAuten>
  );
}
export default App