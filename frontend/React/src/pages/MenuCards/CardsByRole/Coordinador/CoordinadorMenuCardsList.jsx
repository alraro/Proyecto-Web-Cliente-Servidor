import coordinadorCards from './sections/CoordinadorCards';
import MenuCardSection from '../../MenuCardSection';

function CoordinadorMenuCardsList() {
    return (
        <>
            <MenuCardSection title="Gestión" cards={coordinadorCards} />
        </>
    )
}

export default CoordinadorMenuCardsList;