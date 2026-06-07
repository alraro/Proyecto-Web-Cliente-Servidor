import gestionCards from './sections/AdminCards';
import pendingCards from './sections/AdminPendingCards';
import MenuCardSection from '../../MenuCardSection';

function AdminMenuCardsList() {

    return (
        <>
            <MenuCardSection title="Gestión" cards={gestionCards} />
            <MenuCardSection title="Pendientes" cards={pendingCards} />
        </>
    );
}

export default AdminMenuCardsList;