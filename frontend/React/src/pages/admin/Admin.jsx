import { Link } from 'react-router-dom';
import '../css/common.css';
import '../css/admin.css';
import MenuCardsList from '../MenuCards/MenuCardsList';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';

function Admin() {

    const username = "---USUARIO ADMIN PLACEHOLDER---"
    const role = "🔑 Administrador"
    const descripcionHeader = "Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol."

    return (
        <SecurePage >
            <GenericPageWrapper headerUsername={username}>
                <WelcomeBar username={username} role={role} description={descripcionHeader} />
                <MenuCardsList />
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default Admin;
