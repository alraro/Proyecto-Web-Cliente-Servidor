import React from 'react';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import SecurePage from '../generalModules/SecurePage';

function Dashboard() {
    
    return (
        <SecurePage >
            <GenericPageWrapper >
                <p>Bienvenido al dashboard de administrador</p>
            </GenericPageWrapper>

        </SecurePage>
    );
}
export default Dashboard;