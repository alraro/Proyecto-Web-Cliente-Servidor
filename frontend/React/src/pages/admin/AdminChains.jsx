import { Link } from 'react-router-dom';
import '../css/admin.css';

export default function AdminChains() {
    const chains = [
        { id: 1, name: 'CARREFOUR', code: 'CARREFOUR', participation: true },
        { id: 2, name: 'DIA', code: 'DIA', participation: true },
        { id: 9, name: 'MERCADONA', code: 'MERCADONA', participation: false }
    ];

    return (
        <div className="admin-page">
            <header className="page-header">
                <h1>Gestion de Cadenas</h1>
                <nav className="admin-tabs" aria-label="Navegacion de administrador">
                    <Link className="admin-tab" to="/admin">Volver al panel</Link>
                    <Link className="admin-tab" to="/login">Cerrar sesion</Link>
                </nav>
            </header>

            <main className="page-main">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Codigo</th>
                            <th>Participa</th>
                        </tr>
                    </thead>
                    <tbody>
                        {chains.map(chain => (
                            <tr key={chain.id}>
                                <td>{chain.id}</td>
                                <td><strong>{chain.name}</strong></td>
                                <td><code>{chain.code}</code></td>
                                <td>
                                    <span className={`badge ${chain.participation ? 'badge-yes' : 'badge-no'}`}>
                                        {chain.participation ? 'Si' : 'No'}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </main>
        </div>
    );
}