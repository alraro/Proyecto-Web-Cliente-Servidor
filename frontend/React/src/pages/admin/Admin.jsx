import { Link } from 'react-router';
import { useAuth } from '../auth/useAuthHook';

import '../css/common.css';
import '../css/admin.css';
import MenuCardsList from '../MenuCards/MenuCardsList';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';

function Admin() {

    const { usuario } = useAuth();
    const username = usuario?.nombre ?? 'Admin';    
    const role = usuario?.role ?? 'ADMINISTRADOR';
    const descripcionHeader = "Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol."

    return (
        <SecurePage >
            <GenericPageWrapper headerUsername={username}>
                <WelcomeBar username={username} role={role} description={descripcionHeader} />
                <MenuCardsList role={role} />
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default Admin;
