import '../css/admin.css';
import AdminMenuCardsList from '../MenuCards/CardsByRole/Admin/AdminMenuCardsList';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';

function Admin() {
    const descripcionHeader = "Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol."

    return (
        <SecurePage >
            <GenericPageWrapper >
                <WelcomeBar description={descripcionHeader} />
                <AdminMenuCardsList />
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default Admin;
