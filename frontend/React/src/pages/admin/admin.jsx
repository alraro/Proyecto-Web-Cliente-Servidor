import { Link } from 'react-router-dom';
import '../css/common.css';
import '../css/admin.css';

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

            <p className="section-title">Gestion</p>
            <div className="menu-grid">
                <Link className="menu-card" to="/admin/chains">
                    <div className="menu-card-icon icon-blue">CH</div>
                    <h3>Cadenas de supermercados</h3>
                    <p>Crear, editar y eliminar cadenas. Activar o desactivar su participacion en campanas.</p>
                    <span className="menu-card-arrow">Ir a cadenas &rarr;</span>
                </Link>
                <Link className="menu-card" to="/admin/dashboard">
                    <div className="menu-card-icon icon-teal">DB</div>
                    <h3>Dashboard</h3>
                    <p>Visualiza cobertura por cadena, localidad y zona para cada campana.</p>
                    <span className="menu-card-arrow">Ir a dashboard &rarr;</span>
                </Link>
                <Link className="menu-card" to="/admin/stores">
                    <div className="menu-card-icon icon-green">ST</div>
                    <h3>Tiendas</h3>
                    <p>Gestionar las tiendas participantes, asignar cadenas y codigos postales.</p>
                    <span className="menu-card-arrow">Ir a tiendas &rarr;</span>
                </Link>
            </div>

            <p className="section-title">Pendiente en React</p>
            <div className="menu-grid">
                <div className="menu-card disabled" aria-disabled="true">
                    <div className="menu-card-icon icon-orange">CP</div>
                    <h3>Campanas</h3>
                    <p>Crear y gestionar campanas de recogida de alimentos, asignar fechas y zonas.</p>
                    <span className="menu-card-arrow">Proximamente</span>
                </div>
                <div className="menu-card disabled" aria-disabled="true">
                    <div className="menu-card-icon icon-blue">VW</div>
                    <h3>Ver campanas</h3>
                    <p>Consulta el estado de todas las campanas: activas, pasadas y futuras.</p>
                    <span className="menu-card-arrow">Proximamente</span>
                </div>
            </div>
        </main>
    );
}

export default Admin;
