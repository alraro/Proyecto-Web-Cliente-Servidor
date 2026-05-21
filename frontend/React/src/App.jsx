import { useState } from 'react';
import GestionVoluntarios from './pages/responsable/GestionVoluntarios';
import './App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('home');

  const renderPage = () => {
    switch (currentPage) {
      case 'volunteers':
        return <GestionVoluntarios />;
      default:
        return <HomePage onNavigate={setCurrentPage} />;
    }
  };

  return renderPage();
}

function HomePage({ onNavigate }) {
  return (
    <div className="home-page">
      <header className="topbar">
        <a className="brand" href="#" onClick={(e) => { e.preventDefault(); onNavigate('home'); }}>
          <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" className="logo" />
        </a>
        <nav className="main-nav">
          <a href="#" className="active">Inicio</a>
        </nav>
      </header>

      <main>
        <section className="hero">
          <div className="hero-copy">
            <span className="eyebrow">Banco de alimentos de Málaga</span>
            <h1>Bancosol transforma excedentes en ayuda real para miles de familias.</h1>
            <p>
              Somos una red solidaria que recupera alimentos, coordina voluntariado y distribuye recursos a
              entidades sociales para que nadie se quede atrás.
            </p>
            <div className="hero-actions">
              <a className="primary-action" href="#" onClick={(e) => { e.preventDefault(); onNavigate('volunteers'); }}>
                Gestión de Voluntarios
              </a>
            </div>
          </div>

          <aside className="hero-panel">
            <div className="panel-card panel-card-dark">
              <p>Misión principal</p>
              <h2>Recuperar y repartir</h2>
              <span>Con dignidad y transparencia</span>
            </div>
            <div className="panel-card">
              <p>Zona de responsables</p>
              <ul>
                <li><span>Gestionar voluntarios de mi entidad</span></li>
                <li><span>Ver campañas asignadas</span></li>
                <li><span>Administrar turnos</span></li>
              </ul>
            </div>
          </aside>
        </section>

        <section className="section-block">
          <div className="section-heading">
            <span className="eyebrow">Acceso rápido</span>
            <h2>¿Qué necesitas hacer?</h2>
          </div>
          <div className="service-grid">
            <article>
              <h3>Mis Voluntarios</h3>
              <p>Gestiona los voluntarios vinculados a tu entidad colaboradora.</p>
              <button className="action-btn" onClick={() => onNavigate('volunteers')}>
                Acceder
              </button>
            </article>
            <article>
              <h3>Mis Campañas</h3>
              <p>Consulta las campañas activas de tu entidad.</p>
              <button className="action-btn" disabled>
                Próximamente
              </button>
            </article>
            <article>
              <h3>Mis Turnos</h3>
              <p>Administra los turnos de voluntariado.</p>
              <button className="action-btn" disabled>
                Próximamente
              </button>
            </article>
          </div>
        </section>
      </main>

      <footer className="site-footer">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" className="logo" />
        <p>Bancosol · Banco de alimentos</p>
      </footer>
    </div>
  );
}

export default App;