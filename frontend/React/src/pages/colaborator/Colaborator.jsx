import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';
import ColaboradorMenuCardsList from '../MenuCards/CardsByRole/Colaborador/ColaboradorMenuCardsList';

function Colaborator() {

    const descripcionHeader = "Desde aquí puedes gestionar los datos de tu entidad colaboradora y sus voluntarios."

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
