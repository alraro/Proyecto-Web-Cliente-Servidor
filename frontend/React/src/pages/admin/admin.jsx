import { Link } from 'react-router-dom';
import '../css/common.css';
import '../css/admin.css';
import MenuCardsList from '../MenuCards/MenuCardsList';

function Admin() {
    return (
        <main className="page-wrapper">
            <div className="welcome-bar">
                <div>
                    <h2>Bienvenido, Administrador</h2>
                    <p>Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol.</p>
                </div>
                <span className="role-pill">Administrador</span>
            </div>
            <MenuCardsList />
        </main>
    );
}

export default Admin;
