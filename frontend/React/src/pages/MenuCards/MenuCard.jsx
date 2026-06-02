import { Link } from 'react-router-dom';

function MenuCard({ icon, title, description, link }) {
    return (
        <>
        <Link className="menu-card" to={link}>
            <div className="menu-card-icon icon-blue">{icon}</div>
            <h3>{title}</h3>
            <p>{description}</p>
            <span className="menu-card-arrow">Ir a {title} &rarr;</span>
        </Link>
        </>
    );
}

export default MenuCard;