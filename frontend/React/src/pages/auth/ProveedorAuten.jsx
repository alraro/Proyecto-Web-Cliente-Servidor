import { useState } from 'react';
import { ContextoAuten } from './ContextoAuten';

export function ProveedorAuten({ children }) {
    const [usuario, setUsuario] = useState(() => {
        const guardado = sessionStorage.getItem('user');
        return guardado ? JSON.parse(guardado) : null;
    });

    const login = (userData, token) => {
        sessionStorage.setItem('user', JSON.stringify(userData));
        sessionStorage.setItem('token', token);
        setUsuario(userData);
    };

    const logout = () => {
        sessionStorage.removeItem('user');
        sessionStorage.removeItem('token');
        setUsuario(null);
    };

    return (
        <ContextoAuten value={{ usuario, estaAutenticado: !!usuario, login, logout}}>
            {children}
        </ContextoAuten>
    )

}