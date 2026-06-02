import MenuCard from './MenuCard';

function MenuCardSection({ title, cards }) {
    return (
        <>
            <p className="section-title">{title}</p>
            <div className="menu-grid">
                {cards.map((card, index) => (
                    <MenuCard
                        key={index}
                        icon={card.icon} 
                        title={card.title} 
                        description={card.description} 
                        link={card.link} 
                    />
                ))}
            </div>
        </>
    )
}

export default MenuCardSection;