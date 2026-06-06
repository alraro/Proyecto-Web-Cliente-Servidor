import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';
import ColaboradorMenuCardsList from '../MenuCards/CardsByRole/Colaborador/ColaboradorMenuCardsList';

function Colaborator() {

    const descripcionHeader = "Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol."

    return (
        <SecurePage >
            <GenericPageWrapper >
                <WelcomeBar description={descripcionHeader} />
                <ColaboradorMenuCardsList />
            </GenericPageWrapper>
        </SecurePage>
    );

}

export default Colaborator;
