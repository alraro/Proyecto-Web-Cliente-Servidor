import React from 'react';
import { Link } from 'react-router';
import { useAuth } from '../auth/useAuthHook';
import MenuCardsList from '../MenuCards/MenuCardsList';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';

function Coordinator() {

    const { usuario } = useAuth();
    const username = usuario?.nombre ?? 'Coordinador';    
    const role = usuario?.role ?? 'COORDINADOR';
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

export default Coordinator;