import logoBancosol from '../../assets/LOGO_BANCOSOL.png'
import { Link } from 'react-router-dom';

function GenericHeader({ username }) {

    const editProfileLink = "/edit-profile" // Placeholder, update with actual route when available
    const logoutLink = "/logout" // Placeholder, update with actual route when available

    return (
        <>
            <header className="topbar">  
            <a className="brand" href="index.html">
                <img src={logoBancosol} alt="Bancosol" className="logo" />
            </a>
            <div className="topbar-right">
                <div className="user-badge">
                    <span className="dot"></span>
                    <span id="user-name">{username}</span>
                </div>
                <Link to={editProfileLink}>
                    <button className="btn-edit" id="btn-edit">Editar perfil 🖉</button>
                </Link>
                <Link to={logoutLink}>
                    <button className="btn-logout" id="btn-logout">Cerrar sesión x</button>
                </Link>
            </div>
            </header>
        </>
    );
}

export default GenericHeader;