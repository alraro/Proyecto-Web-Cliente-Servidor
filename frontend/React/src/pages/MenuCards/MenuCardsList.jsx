import gestionCards from './CardSections/AdminCards';
import capitanCards from './CardSections/CapitanCards';
import coordinadorCards from './CardSections/CoordinadorCards';
import colaboradorCards from './CardSections/ColaboradorCards';
import pendingCards from '../MenuCards/CardSections/PendingSection';
import MenuCardSection from '../MenuCards/MenuCardSection';
import {useAuth} from '../auth/useAuthHook';

function MenuCardsList({ role }) {
    if(role === 'ADMINISTRADOR'){
        return (
            <>
            <MenuCardSection title="Gestión" cards={gestionCards} />
            <MenuCardSection title="Pendientes" cards={pendingCards} />
            </>
        );
    }

    if (role === 'CAPITAN') {
        return (
            <>
            <MenuCardSection title="Gestión" cards={capitanCards} />
            </>
        )
    }

    if (role === 'COORDINADOR') {
        return (
            <>
            <MenuCardSection title="Gestión" cards={coordinadorCards} />
            </>
        )
    }

    if (role === 'COLABORADOR') {
        return (
            <>
            <MenuCardSection title="Gestión" cards={colaboradorCards} />
            </>
        )
    }
}

export default MenuCardsList;