import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../css/common.css';
import '../css/layout.css';
import '../css/admin.css';
import '../css/responsible-store.css';

const API_BASE = 'http://localhost:8080';

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    };
}

function InfoTienda({ tienda }) {
    const campos = [
        { label: 'Nombre',        valor: tienda.name       },
        { label: 'Domicilio',     valor: tienda.address    },
        { label: 'Código postal', valor: tienda.postalCode },
        { label: 'Localidad',     valor: tienda.locality   },
        { label: 'Zona geog.',    valor: tienda.zone       },
        { label: 'Cadena',        valor: tienda.chainName  },
    ];

    return (
        <section className="card" id="card-tienda">
            <h2 id="store-title">{tienda.name || 'Tienda'}</h2>
            <div className="info-grid">
                {campos.map(c => (
                    <div key={c.label} className="info-item">
                        <label>{c.label}</label>
                        <span>{c.valor || '—'}</span>
                    </div>
                ))}
            </div>
        </section>
    );
}

// Fila de un turno
function TurnoRow({ turno }) {
    let badgeClase = 'badge-pending';
    let badgeTexto = 'Pendiente';
    if (turno.attendance === true)  { badgeClase = 'badge-attendance badge-yes'; badgeTexto = '✓ Sí'; }
    if (turno.attendance === false) { badgeClase = 'badge-attendance badge-no';  badgeTexto = '✗ No'; }

    return (
        <tr>
            <td>{turno.campaignName || '—'}</td>
            <td>{turno.volunteerName || '—'}</td>
            <td>{turno.endTime || '—'}</td>
            <td><span className={`badge-attendance ${badgeClase}`}>{badgeTexto}</span></td>
            <td>{turno.notes || '—'}</td>
        </tr>
    );
}

function TablaTurnos({ turnos }) {
    return (
        <section className="card" id="card-turnos">
            <h2>Turnos programados</h2>
            <table className="data-table">
                <thead>
                    <tr>
                        <th>Campaña</th>
                        <th>Voluntario</th>
                        <th>Fin del turno</th>
                        <th>Asistencia</th>
                        <th>Notas</th>
                    </tr>
                </thead>
                <tbody>
                    {!turnos || turnos.length === 0 ? (
                        <tr>
                            <td colSpan={5} className="table-empty">No hay turnos programados.</td>
                        </tr>
                    ) : (
                        turnos.map((t, i) => <TurnoRow key={i} turno={t} />)
                    )}
                </tbody>
            </table>
        </section>
    );
}

export default function ResponsibleStore() {
    const [tienda, setTienda] = useState(null);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        cargarDetalleTienda();
    }, []);

    async function cargarDetalleTienda() {
        const storeId = localStorage.getItem('storeId');
        if (!storeId) {
            setError('No tienes ninguna tienda asignada. Contacta con el administrador.');
            setCargando(false);
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/api/stores/${storeId}/detail`, {
                headers: authHeaders()
            });

            if (res.status === 403) { setError('No tienes permiso para ver esta tienda.'); setCargando(false); return; }
            if (res.status === 404) { setError('Tienda no encontrada.'); setCargando(false); return; }
            if (!res.ok) { setError('Error al cargar la información de la tienda.'); setCargando(false); return; }

            const data = await res.json();
            setTienda(data);
        } catch {
            setError('Error de conexión con el servidor.');
        } finally {
            setCargando(false);
        }
    }

    return (
        <div className="responsible-page">
            <header className="page-header">
                <h1>Panel del Responsable</h1>
                <div className="header-info">
                    <span>Bienvenido, {localStorage.getItem('nombre') || 'Responsable'}</span>
                    <Link to="/login" className="btn btn-secondary btn-sm">Cerrar sesión</Link>
                </div>
            </header>

            <main className="page-main">
                {cargando && <p className="table-empty">Cargando información de tu tienda...</p>}

                {error && <p className="error-msg" id="error-msg">{error}</p>}

                {!cargando && tienda && (
                    <>
                        <InfoTienda tienda={tienda} />
                        <TablaTurnos turnos={tienda.scheduledShifts} />
                    </>
                )}
            </main>
        </div>
    );
}