import colaboradorCards from './sections/ColaboradorCards';
import MenuCardSection from '../../MenuCardSection';

function ColaboradorMenuCardsList() {
    return (
        <>
            <MenuCardSection title="Gestión" cards={colaboradorCards} />
        </>
    )
}

export default ColaboradorMenuCardsList;