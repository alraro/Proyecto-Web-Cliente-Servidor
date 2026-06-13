import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { ProveedorAuten } from '../pages/auth/ProveedorAuten';

// Renderiza un componente con router en memoria y sesión de admin (token + user),
// que es lo que necesitan GenericHeader (useAuth/Link) y authHeaders().
export function renderWithProviders(ui, { route = '/' } = {}) {
    sessionStorage.setItem('user', JSON.stringify({ nombre: 'Admin Test', role: 'ADMINISTRADOR' }));
    sessionStorage.setItem('token', 'test-token');

    return render(
        <ProveedorAuten>
            <MemoryRouter initialEntries={[route]}>
                {ui}
            </MemoryRouter>
        </ProveedorAuten>
    );
}
