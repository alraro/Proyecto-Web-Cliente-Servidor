import capitanCards from './sections/CapitanCards';
import MenuCardSection from '../../MenuCardSection';

function CapitanMenuCardsList() {
    return (
        <>
            <MenuCardSection title="Gestión" cards={capitanCards} />
        </>
    )
}

export default CapitanMenuCardsList;