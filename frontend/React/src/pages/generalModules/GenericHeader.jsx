import logoBancosol from '../../assets/LOGO_BANCOSOL.png'
import { Link, useNavigate } from 'react-router';
import { useAuth } from '../auth/useAuthHook';
import edit from '../Edit.jsx';


function GenericHeader({ username }) {

    const editProfileLink = "/edit" // Placeholder, update with actual route when available
    const { logout } = useAuth();
    const navigate = useNavigate();
    
    const handleLogout = () => {
        logout();
        navigate('/login');
    }


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
                <button className="btn-logout" id="btn-logout" onClick={handleLogout}>
                    Cerrar sesión x
                </button>
            </div>
            </header>
        </>
    );
}

export default GenericHeader;