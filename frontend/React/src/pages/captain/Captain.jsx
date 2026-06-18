import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';
import CapitanMenuCardsList from '../MenuCards/CardsByRole/Capitan/CapitanMenuCardsList';



function Captain() {

    const descripcionHeader = "Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol."

    return (
        <SecurePage >
            <GenericPageWrapper >
                <WelcomeBar description={descripcionHeader} />
                <CapitanMenuCardsList />
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default Captain;