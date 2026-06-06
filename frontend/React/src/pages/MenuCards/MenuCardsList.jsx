import gestionCards from './CardSections/AdminCards';
import capitanCards from './CardSections/CapitanCards';
import coordinadorCards from './CardSections/CoordinadorCards';
import colaboradorCards from './CardSections/ColaboradorCards';
import pendingCards from '../MenuCards/CardSections/PendingSection';
import MenuCardSection from '../MenuCards/MenuCardSection';
import {useAuth} from '../auth/useAuthHook';

function MenuCardsList({ role }) {
    const { usuario } = useAuth();
    const activeRole = role ?? usuario?.role;

    if(activeRole === 'ADMINISTRADOR'){
        return (
            <>
            <MenuCardSection title="Gestión" cards={gestionCards} />
            <MenuCardSection title="Pendientes" cards={pendingCards} />
            </>
        );
    }

    if (activeRole === 'CAPITAN') {
        return (
            <>
            <MenuCardSection title="Gestión" cards={capitanCards} />
            </>
        )
    }

    if (activeRole === 'COORDINADOR') {
        return (
            <>
            <MenuCardSection title="Gestión" cards={coordinadorCards} />
            </>
        )
    }

    if (activeRole === 'COLABORADOR') {
        return (
            <>
            <MenuCardSection title="Gestión" cards={colaboradorCards} />
            </>
        )
    }

    return null;
}

export default MenuCardsList;