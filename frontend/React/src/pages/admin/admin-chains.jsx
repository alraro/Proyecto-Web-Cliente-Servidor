import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../css/common.css';
import '../css/layout.css';
import '../css/admin.css';

const API_BASE = 'http://localhost:8080';

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    };
}

// --- Componente principal ---
export default function AdminChains() {
    const [cadenas, setCadenas] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    useEffect(() => {
        cargarCadenas();
    }, []);

    async function cargarCadenas() {
        setCargando(true);
        setError('');
        try {
            const res = await fetch(`${API_BASE}/api/chains`, { headers: authHeaders() });
            if (!res.ok) { setError('Error al cargar las cadenas.'); return; }
            setCadenas(await res.json());
        } catch {
            setError('No se puede conectar con el servidor. ¿Está el backend en marcha?');
        } finally {
            setCargando(false);
        }
    }

    return (
        <div className="admin-page">
            <header className="page-header">
                <h1>Gestión de Cadenas</h1>
                <nav className="admin-tabs" aria-label="Navegacion de administrador">
                    <Link className="admin-tab" to="/admin">Volver al panel</Link>
                    <Link className="admin-tab" to="/login" onClick={() => localStorage.clear()}>Cerrar sesion</Link>
                </nav>
            </header>

            <main className="page-main">
                {error && <p className="error-msg">{error}</p>}

                {cargando ? (
                    <p className="table-empty">Cargando cadenas...</p>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Código</th>
                                <th>Participa</th>
                            </tr>
                        </thead>
                        <tbody>
                            {cadenas.length === 0 ? (
                                <tr><td colSpan={4} className="table-empty">No hay cadenas registradas.</td></tr>
                            ) : (
                                cadenas.map(c => (
                                    <tr key={c.id}>
                                        <td>{c.id}</td>
                                        <td><strong>{c.name}</strong></td>
                                        <td><code>{c.code}</code></td>
                                        <td>
                                                <span className={`badge ${c.participation ? 'badge-yes' : 'badge-no'}`}>
                                                {c.participation ? '✓ Sí' : '— No'}
                                            </span>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                )}
            </main>
        </div>
    );
}