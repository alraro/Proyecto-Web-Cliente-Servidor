import { Link } from 'react-router';
import logoBancosol from '../assets/LOGO_BANCOSOL.png'; 
import { useAuth } from './auth/useAuthHook';
import { getRoleRoute } from './auth/roleRoutes';
import './css/index.css'; 

function Index() {
    const { usuario, estaAutenticado } = useAuth();
    const authRoute = estaAutenticado ? getRoleRoute(usuario.role) : '/login';

    return (
        <div className="index-wrapper">
            
            <header className="topbar">
                <div className="brand">
                    <img src={logoBancosol} alt="Logo de Bancosol" className="logo" />
                </div>
                
                <nav className="main-nav">
                    <Link to="/" aria-current="page">Inicio</Link>
                    <Link to={authRoute} className="primary-action">Área Encargados</Link>
                </nav>
            </header>

            <main className="hero">
                
                <section className="hero-copy">
                    <span className="eyebrow">Portal Oficial</span>
                    <h1>Bienvenido a la página de Bancosol para encargados</h1>
                    <p>
                        Gestiona de forma eficiente la distribución, control de stock y asignación de 
                        recursos para la delegación de la Costa del Sol.
                    </p>
                    <div className="hero-actions">
                        <Link to={authRoute} className="primary-action">
                            {estaAutenticado ? 'Ir a mi panel' : 'Iniciar Sesión'}
                        </Link>
                    </div>
                </section>

            </main>

            <section className="section-block">
                <div className="section-heading">
                    <h2>Nuestros Perfiles de Gestión</h2>
                    <p>El sistema cuenta con accesos restringidos según el cargo asignado:</p>
                </div>

                <div className="service-grid">
                    <article>
                        <h3>Administración</h3>
                        <p>Control global de usuarios, altas de encargados y auditoría del sistema.</p>
                    </article>
                    <article>
                        <h3>Coordinación</h3>
                        <p>Supervisión de campañas y control de los almacenes principales.</p>
                    </article>
                    <article>
                        <h3>Capitanes y Equipos</h3>
                        <p>Gestión directa a pie de campo del voluntariado y colaboradores asignados.</p>
                    </article>
                </div>
            </section>

            <footer className="site-footer">
                <img src={logoBancosol} alt="Bancosol Alimentos" className="logo" />
                <p>© Bancosol Alimentos. Todos los derechos reservados.</p>
            </footer>

        </div>
    );
}

export default Index;
