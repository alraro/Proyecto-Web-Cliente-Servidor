import { Navigate, Outlet } from 'react-router';
import { useAuth } from './useAuthHook';

export function RutaProtegida({ roles }) {
    const { usuario, estaAutenticado } = useAuth();

    if (!estaAutenticado) return <Navigate to="/login" replace />;

    if (roles && !roles.includes(usuario.role)) return <Navigate to="/login" replace />;

    return <Outlet />;
}