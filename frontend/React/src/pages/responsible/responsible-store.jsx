import { Link } from 'react-router-dom';
import '../css/common.css';
import '../css/layout.css';
import '../css/admin.css';
import '../css/responsible-store.css';

const sampleStore = {
    name: 'ECHEVERRIA',
    address: 'Avda Pio Baroja, 6',
    postalCode: '29017',
    locality: 'Alameda',
    zone: 'Antequera',
    chainName: 'CARREFOUR',
    scheduledShifts: [
        {
            campaignName: 'Spring Campaign',
            volunteerName: 'Arantxa',
            endTime: '2026-05-09 14:00',
            attendance: true,
            notes: 'Llego temprano'
        },
        {
            campaignName: 'Spring Campaign',
            volunteerName: 'Diego Vazquez',
            endTime: '2026-05-10 12:00',
            attendance: false,
            notes: 'No se presento'
        }
    ]
};

function StoreInfo({ store }) {
    const fields = [
        { label: 'Nombre', value: store.name },
        { label: 'Direccion', value: store.address },
        { label: 'Codigo postal', value: store.postalCode },
        { label: 'Localidad', value: store.locality },
        { label: 'Zona', value: store.zone },
        { label: 'Cadena', value: store.chainName }
    ];

    return (
        <section className="card" id="card-tienda">
            <h2 id="store-title">{store.name || 'Tienda'}</h2>
            <div className="info-grid">
                {fields.map(field => (
                    <div key={field.label} className="info-item">
                        <label>{field.label}</label>
                        <span>{field.value || 'N/A'}</span>
                    </div>
                ))}
            </div>
        </section>
    );
}

function ShiftRow({ shift }) {
    let badgeClass = 'badge-pending';
    let badgeText = 'Pendiente';
    if (shift.attendance === true) { badgeClass = 'badge-attendance badge-yes'; badgeText = 'Si'; }
    if (shift.attendance === false) { badgeClass = 'badge-attendance badge-no'; badgeText = 'No'; }

    return (
        <tr>
            <td>{shift.campaignName || 'N/A'}</td>
            <td>{shift.volunteerName || 'N/A'}</td>
            <td>{shift.endTime || 'N/A'}</td>
            <td><span className={`badge-attendance ${badgeClass}`}>{badgeText}</span></td>
            <td>{shift.notes || 'N/A'}</td>
        </tr>
    );
}

function ShiftsTable({ shifts }) {
    return (
        <section className="card" id="card-turnos">
            <h2>Turnos programados</h2>
            <table className="data-table">
                <thead>
                    <tr>
                        <th>Campana</th>
                        <th>Voluntario</th>
                        <th>Fin del turno</th>
                        <th>Asistencia</th>
                        <th>Notas</th>
                    </tr>
                </thead>
                <tbody>
                    {!shifts || shifts.length === 0 ? (
                        <tr>
                            <td colSpan={5} className="table-empty">No hay turnos programados.</td>
                        </tr>
                    ) : (
                        shifts.map((shift, index) => <ShiftRow key={index} shift={shift} />)
                    )}
                </tbody>
            </table>
        </section>
    );
}

export default function ResponsibleStore() {
    return (
        <div className="responsible-page">
            <header className="page-header">
                <div className="page-header-row">
                    <div>
                        <h1>Panel del responsable</h1>
                        <p>Consulta el estado de tu tienda y los turnos programados.</p>
                    </div>
                    <div className="topbar-right">
                        <span className="user-badge">
                            <span className="dot"></span>
                            Usuario responsable
                        </span>
                        <Link to="/login" className="btn btn-secondary btn-sm">Cerrar sesion</Link>
                    </div>
                </div>
            </header>

            <main className="page-main">
                <StoreInfo store={sampleStore} />
                <ShiftsTable shifts={sampleStore.scheduledShifts} />
            </main>
        </div>
    );
}