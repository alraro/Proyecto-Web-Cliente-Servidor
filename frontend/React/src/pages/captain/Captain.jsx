import React from 'react';
import { Link } from 'react-router';
import { useAuth } from '../auth/useAuthHook';
import MenuCardsList from '../MenuCards/MenuCardsList';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';



function Captain() {

    const descripcionHeader = "Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol."

    return (
        <SecurePage >
            <GenericPageWrapper >
                <WelcomeBar description={descripcionHeader} />
                <MenuCardsList />
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default Captain;