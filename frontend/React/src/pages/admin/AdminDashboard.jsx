import React from 'react';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import SecurePage from '../generalModules/SecurePage';
import { useAuth } from '../auth/useAuthHook';

function Dashboard() {
    const { usuario } = useAuth();
    const username = usuario?.nombre ?? 'Admin';    
    const role = usuario?.role ?? 'ADMINISTRADOR';

    
    return (
        <SecurePage >
            <GenericPageWrapper headerUsername={username}>
                <p>Bienvenido al dashboard de administrador</p>
            </GenericPageWrapper>

        </SecurePage>
    );
}
export default Dashboard;