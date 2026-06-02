import gestionCards from '../MenuCards/CardSections/GestionSection';
import pendingCards from '../MenuCards/CardSections/PendingSection';
import MenuCardSection from '../MenuCards/MenuCardSection';

function MenuCardsList() {
    return (
        <>
            <MenuCardSection title="Gestion" cards={gestionCards} />
            <MenuCardSection title="Pendiente en React" cards={pendingCards} />
        </>
    );
}

export default MenuCardsList;