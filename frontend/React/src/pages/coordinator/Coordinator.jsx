import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';
import CoordinadorMenuCardsList from '../MenuCards/CardsByRole/Coordinador/CoordinadorMenuCardsList';

function Coordinator() {

    const descripcionHeader = "Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol."

    return (
        <SecurePage >
            <GenericPageWrapper >
                <WelcomeBar description={descripcionHeader} />
                <CoordinadorMenuCardsList />
            </GenericPageWrapper>
        </SecurePage>
    );

}

export default Coordinator;