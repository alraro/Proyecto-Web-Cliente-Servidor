import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../css/common.css';
import '../css/layout.css';
import '../css/admin.css';
import '../css/admin-stores.css';

const API_BASE = 'http://localhost:8080';

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    };
}

// Barra de filtros
function FiltrosTiendas({ cadenas, zonas, localidades, onAplicar, onLimpiar }) {
    const [filterChain, setFilterChain] = useState('');
    const [filterZone, setFilterZone] = useState('');
    const [filterLocality, setFilterLocality] = useState('');

    const localidadesFiltradas = filterZone
        ? localidades.filter(l => String(l.zoneId) === String(filterZone))
        : localidades;

    function handleZoneChange(e) {
        setFilterZone(e.target.value);
        setFilterLocality('');
    }

    function handleLimpiar() {
        setFilterChain('');
        setFilterZone('');
        setFilterLocality('');
        onLimpiar();
    }

    return (
        <div className="filters-bar">
            <select value={filterChain} onChange={e => setFilterChain(e.target.value)}>
                <option value="">Todas las cadenas</option>
                {cadenas.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>

            <select value={filterZone} onChange={handleZoneChange}>
                <option value="">Todas las zonas</option>
                {zonas.map(z => <option key={z.id} value={z.id}>{z.name}</option>)}
            </select>

            <select value={filterLocality} onChange={e => setFilterLocality(e.target.value)}>
                <option value="">Todas las localidades</option>
                {localidadesFiltradas.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
            </select>

            <button className="btn btn-primary btn-sm" onClick={() => onAplicar({ filterChain, filterZone, filterLocality })}>
                Aplicar filtros
            </button>
            <button className="btn btn-secondary btn-sm" onClick={handleLimpiar}>
                Limpiar
            </button>
        </div>
    );
}

// Componente principal
export default function AdminStores() {
    const [tiendas, setTiendas] = useState([]);
    const [cadenas, setCadenas] = useState([]);
    const [zonas, setZonas] = useState([]);
    const [localidades, setLocalidades] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [filtros, setFiltros] = useState({});
    const [paginaActual, setPaginaActual] = useState(0);
    const [totalPaginas, setTotalPaginas] = useState(1);
    const tamPagina = 20;

    useEffect(() => {
        cargarDatosAuxiliares();
    }, []);

    useEffect(() => {
        cargarTiendas(paginaActual, filtros);
    }, [paginaActual, filtros]);

    async function cargarDatosAuxiliares() {
        try {
            const [resCadenas, resLoc, resZonas] = await Promise.all([
                fetch(`${API_BASE}/api/chains`, { headers: authHeaders() }),
                fetch(`${API_BASE}/api/localities`, { headers: authHeaders() }),
                fetch(`${API_BASE}/api/zones`, { headers: authHeaders() })
            ]);
            if (resCadenas.ok) setCadenas(await resCadenas.json());
            if (resLoc.ok) setLocalidades(await resLoc.json());
            if (resZonas.ok) setZonas(await resZonas.json());
        } catch {}
    }

    async function cargarTiendas(pagina = 0, filtrosActivos = {}) {
        setCargando(true);
        setError('');
        const params = new URLSearchParams();
        if (filtrosActivos.filterChain) params.append('chainId', filtrosActivos.filterChain);
        if (filtrosActivos.filterLocality) params.append('localityId', filtrosActivos.filterLocality);
        if (filtrosActivos.filterZone) params.append('zoneId', filtrosActivos.filterZone);
        params.append('page', pagina);
        params.append('size', tamPagina);

        try {
            const res = await fetch(`${API_BASE}/api/stores?${params}`, { headers: authHeaders() });
            const data = await res.json();
            setTiendas(data.content || []);
            setTotalPaginas(data.totalPages || 1);
        } catch {
            setError('No se puede conectar con el servidor.');
        } finally {
            setCargando(false);
        }
    }

    function handleAplicarFiltros(nuevosFiltros) {
        setFiltros(nuevosFiltros);
        setPaginaActual(0);
    }

    function handleLimpiarFiltros() {
        setFiltros({});
        setPaginaActual(0);
    }

    return (
        <div className="admin-page">
            <header className="page-header">
                <h1>Gestión de Tiendas</h1>
                <nav className="admin-tabs" aria-label="Navegacion de administrador">
                    <Link className="admin-tab" to="/admin">Volver al panel</Link>
                    <Link className="admin-tab" to="/login">Cerrar sesion</Link>
                </nav>
            </header>

            <main className="page-main">
                <FiltrosTiendas
                    cadenas={cadenas}
                    zonas={zonas}
                    localidades={localidades}
                    onAplicar={handleAplicarFiltros}
                    onLimpiar={handleLimpiarFiltros}
                />

                {error && <p className="error-msg">{error}</p>}

                {cargando ? (
                    <p className="table-empty">Cargando tiendas...</p>
                ) : (
                    <>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Dirección</th>
                                    <th>Localidad</th>
                                    <th>CP</th>
                                    <th>Zona</th>
                                    <th>Cadena</th>
                                </tr>
                            </thead>
                            <tbody>
                                {tiendas.length === 0 ? (
                                    <tr>
                                        <td colSpan={7} className="table-empty">No hay tiendas que coincidan con los filtros.</td>
                                    </tr>
                                ) : (
                                    tiendas.map(s => (
                                        <tr key={s.id}>
                                            <td>{s.id}</td>
                                            <td><strong>{s.name}</strong></td>
                                            <td>{s.address || '—'}</td>
                                            <td>{s.locality || '—'}</td>
                                            <td>{s.postalCode || '—'}</td>
                                            <td>{s.zone || '—'}</td>
                                            <td>{s.chainName || '—'}</td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>

                        <div className="pagination">
                            <button className="btn btn-secondary btn-sm" disabled={paginaActual === 0} onClick={() => setPaginaActual(p => p - 1)}>
                                ← Anterior
                            </button>
                            <span>Página {paginaActual + 1} de {totalPaginas}</span>
                            <button className="btn btn-secondary btn-sm" disabled={paginaActual >= totalPaginas - 1} onClick={() => setPaginaActual(p => p + 1)}>
                                Siguiente →
                            </button>
                        </div>
                    </>
                )}
            </main>
        </div>
    );
}