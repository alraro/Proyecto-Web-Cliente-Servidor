import { useState } from 'react';
import { Link } from 'react-router-dom';
import '../css/admin.css';
import '../css/admin-stores.css';

const sampleChains = [
    { id: 1, name: 'CARREFOUR' },
    { id: 2, name: 'DIA' },
    { id: 9, name: 'MERCADONA' }
];

const sampleZones = [
    { id: 1, name: 'Antequera' },
    { id: 7, name: 'Malaga' }
];

const sampleLocalities = [
    { id: 1, name: 'Alameda', zoneId: 1 },
    { id: 12, name: 'Antequera', zoneId: 1 },
    { id: 37, name: 'Malaga', zoneId: 7 }
];

const sampleStores = [
    {
        id: 1,
        name: 'ECHEVERRIA',
        address: 'Avda Pio Baroja, 6',
        localityId: 37,
        localityName: 'Malaga',
        postalCode: '29017',
        zoneId: 7,
        zoneName: 'Malaga',
        chainId: 1,
        chainName: 'CARREFOUR'
    },
    {
        id: 14,
        name: 'DIA',
        address: 'Avda Malaga Oloroso 30',
        localityId: 37,
        localityName: 'Malaga',
        postalCode: '29014',
        zoneId: 7,
        zoneName: 'Malaga',
        chainId: 2,
        chainName: 'DIA'
    }
];

function StoreFilters({ chains, zones, localities, onApply, onClear }) {
    const [chainId, setChainId] = useState('');
    const [zoneId, setZoneId] = useState('');
    const [localityId, setLocalityId] = useState('');

    const filteredLocalities = zoneId
        ? localities.filter(locality => String(locality.zoneId) === String(zoneId))
        : localities;

    function handleZoneChange(event) {
        setZoneId(event.target.value);
        setLocalityId('');
    }

    function handleClear() {
        setChainId('');
        setZoneId('');
        setLocalityId('');
        onClear();
    }

    return (
        <div className="filters-bar">
            <select value={chainId} onChange={event => setChainId(event.target.value)}>
                <option value="">Todas las cadenas</option>
                {chains.map(chain => <option key={chain.id} value={chain.id}>{chain.name}</option>)}
            </select>

            <select value={zoneId} onChange={handleZoneChange}>
                <option value="">Todas las zonas</option>
                {zones.map(zone => <option key={zone.id} value={zone.id}>{zone.name}</option>)}
            </select>

            <select value={localityId} onChange={event => setLocalityId(event.target.value)}>
                <option value="">Todas las localidades</option>
                {filteredLocalities.map(locality => (
                    <option key={locality.id} value={locality.id}>{locality.name}</option>
                ))}
            </select>

            <button className="btn btn-primary btn-sm" onClick={() => onApply({ chainId, zoneId, localityId })}>
                Aplicar filtros
            </button>
            <button className="btn btn-secondary btn-sm" onClick={handleClear}>
                Limpiar
            </button>
        </div>
    );
}

export default function AdminStores() {
    const [filters, setFilters] = useState({ chainId: '', zoneId: '', localityId: '' });

    const filteredStores = sampleStores.filter(store => {
        if (filters.chainId && String(store.chainId) !== String(filters.chainId)) return false;
        if (filters.zoneId && String(store.zoneId) !== String(filters.zoneId)) return false;
        if (filters.localityId && String(store.localityId) !== String(filters.localityId)) return false;
        return true;
    });

    function handleApplyFilters(nextFilters) {
        setFilters(nextFilters);
    }

    function handleClearFilters() {
        setFilters({ chainId: '', zoneId: '', localityId: '' });
    }

    return (
        <div className="admin-page">
            <header className="page-header">
                <h1>Gestion de Tiendas</h1>
                <nav className="admin-tabs" aria-label="Navegacion de administrador">
                    <Link className="admin-tab" to="/admin">Volver al panel</Link>
                    <Link className="admin-tab" to="/login">Cerrar sesion</Link>
                </nav>
            </header>

            <main className="page-main">
                <StoreFilters
                    chains={sampleChains}
                    zones={sampleZones}
                    localities={sampleLocalities}
                    onApply={handleApplyFilters}
                    onClear={handleClearFilters}
                />

                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Direccion</th>
                            <th>Localidad</th>
                            <th>Codigo postal</th>
                            <th>Zona</th>
                            <th>Cadena</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredStores.length === 0 ? (
                            <tr>
                                <td colSpan={7} className="table-empty">No hay tiendas que coincidan con los filtros.</td>
                            </tr>
                        ) : (
                            filteredStores.map(store => (
                                <tr key={store.id}>
                                    <td>{store.id}</td>
                                    <td><strong>{store.name}</strong></td>
                                    <td>{store.address || 'N/A'}</td>
                                    <td>{store.localityName || 'N/A'}</td>
                                    <td>{store.postalCode || 'N/A'}</td>
                                    <td>{store.zoneName || 'N/A'}</td>
                                    <td>{store.chainName || 'N/A'}</td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </main>
        </div>
    );
}