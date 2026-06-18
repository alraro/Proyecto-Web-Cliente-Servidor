import gestionCards from './sections/AdminCards';
import MenuCardSection from '../../MenuCardSection';

function AdminMenuCardsList() {

    return (
        <>
            <MenuCardSection title="Gestión" cards={gestionCards} />
        </>
    );
}

export default AdminMenuCardsList;