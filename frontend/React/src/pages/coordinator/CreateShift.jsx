import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import SecurePage from '../generalModules/SecurePage';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import GenericTable from '../generalModules/GenericTable';
import GenericModal from '../generalModules/GenericModal';
import '../css/common.css';
import '../css/admin.css';

/**
 * CreateShift — coordinator page for creating and listing volunteer shifts.
 *
 * API:
 *   GET  /api/coordinator/my-campaigns             → load coordinator campaigns
 *   GET  /api/shifts/campaign/{campaignId}/stores  → load stores for campaign
 *   GET  /api/shifts?campaignId=X                  → list shifts for campaign
 *   POST /api/shifts                               → create a new shift
 */
function CreateShift() {
    const token = sessionStorage.getItem('token');

    const [campaigns, setCampaigns]           = useState([]);
    const [stores, setStores]                 = useState([]);
    const [shifts, setShifts]                 = useState([]);
    const [selectedCampaign, setSelectedCampaign] = useState('');
    const [isLoading, setIsLoading]           = useState(false);
    const [isModalOpen, setIsModalOpen]       = useState(false);
    const [message, setMessage]               = useState({ text: '', error: false });

    const tableHeaders = {
        tienda:      'Tienda',
        fecha:       'Fecha',
        inicio:      'Hora inicio',
        fin:         'Hora fin',
        voluntarios: 'Voluntarios necesarios',
        ubicacion:   'Ubicación',
    };

    /* Load coordinator campaigns on mount */
    useEffect(() => {
        async function loadCampaigns() {
            try {
                const res = await fetch('/api/coordinator/my-campaigns', {
                    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
                });
                if (!res.ok) throw new Error('Error al cargar campañas');
                const data = await res.json();
                setCampaigns(Array.isArray(data) ? data : []);
            } catch (err) {
                showMessage(err.message || 'No se pudieron cargar las campañas', true);
            }
        }
        loadCampaigns();
    }, [token]);

    /* Load stores and shifts when campaign changes */
    useEffect(() => {
        if (!selectedCampaign) { setStores([]); setShifts([]); return; }

        async function loadStoresAndShifts() {
            setIsLoading(true);
            try {
                const [storesRes, shiftsRes] = await Promise.all([
                    fetch(`/api/shifts/campaign/${selectedCampaign}/stores`, {
                        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
                    }),
                    fetch(`/api/shifts?campaignId=${selectedCampaign}`, {
                        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
                    })
                ]);

                if (storesRes.ok) {
                    const data = await storesRes.json();
                    setStores(Array.isArray(data) ? data : []);
                }

                if (shiftsRes.ok) {
                    const data = await shiftsRes.json();
                    setShifts(mapShifts(Array.isArray(data) ? data : []));
                }
            } catch (err) {
                showMessage(err.message || 'Error al cargar datos', true);
            } finally {
                setIsLoading(false);
            }
        }
        loadStoresAndShifts();
    }, [selectedCampaign, token]);

    /* Map API response to the flat keys that GenericTable expects */
    function mapShifts(data) {
        return data.map(s => ({
            id:          s.shiftId,
            tienda:      s.storeName   || '-',
            fecha:       s.day         || '-',
            inicio:      s.startTime   || '-',
            fin:         s.endTime     || '-',
            voluntarios: s.volunteersNeeded ?? '-',
            ubicacion:   s.observations || '-',
        }));
    }

    function showMessage(text, error) {
        setMessage({ text, error });
        setTimeout(() => setMessage({ text: '', error: false }), 4000);
    }

    async function handleCreateShift(formData) {
        try {
            const res = await fetch('/api/shifts', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    campaignId:       Number(selectedCampaign),
                    storeId:          Number(formData.storeId),
                    day:              formData.day,
                    startTime:        formData.startTime,
                    endTime:          formData.endTime,
                    volunteersNeeded: Number(formData.volunteersNeeded),
                    location:         formData.location   || null,
                    observations:     formData.observations || null,
                })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.message || 'Error al crear el turno');

            showMessage('Turno creado correctamente', false);
            setIsModalOpen(false);

            /* Reload shifts list */
            const refreshRes = await fetch(`/api/shifts?campaignId=${selectedCampaign}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
            });
            if (refreshRes.ok) {
                const refreshed = await refreshRes.json();
                setShifts(mapShifts(Array.isArray(refreshed) ? refreshed : []));
            }
        } catch (err) {
            showMessage(err.message || 'Error al crear el turno', true);
        }
    }

    /* Build GenericModal field definitions.
     * campaignId is not in the modal — it comes from the page filter (selectedCampaign).
     * Stores are pre-loaded for that campaign, so no cascading dependency inside the modal. */
    const modalFields = [
        {
            name: 'storeId', label: 'Tienda *', type: 'select',
            options: stores.map(s => ({ value: String(s.id), label: s.name }))
        },
        { name: 'day',              label: 'Fecha *',                  type: 'date'   },
        { name: 'startTime',        label: 'Hora inicio *',            type: 'time'   },
        { name: 'endTime',          label: 'Hora fin *',               type: 'time'   },
        { name: 'volunteersNeeded', label: 'Voluntarios necesarios *', type: 'number' },
        { name: 'location',         label: 'Ubicación',                type: 'text'   },
        { name: 'observations',     label: 'Observaciones',            type: 'text'   },
    ];

    const modalDefaultValues = { volunteersNeeded: 1 };

    return (
        <SecurePage>
            <GenericPageWrapper>

                <WelcomeBar description="Crea y consulta los turnos de recogida de tus campañas." />

                {message.text && (
                    <div id="global-message" className={message.error ? 'error' : 'success'}>
                        {message.text}
                    </div>
                )}

                {/* Campaign filter */}
                <div className="filters-bar">
                    <label htmlFor="campaign-filter"><strong>Campaña: </strong></label>
                    <select
                        id="campaign-filter"
                        value={selectedCampaign}
                        onChange={e => setSelectedCampaign(e.target.value)}
                    >
                        <option value="">Selecciona una campaña...</option>
                        {campaigns.map(c => (
                            <option key={c.id} value={c.id}>
                                {c.name} ({c.startDate} – {c.endDate})
                            </option>
                        ))}
                    </select>
                </div>

                <GenericTable
                    title="Turnos de recogida"
                    headers={tableHeaders}
                    data={selectedCampaign ? shifts : []}
                    isLoading={isLoading}
                    itemName="Turno"
                    addRowFunction={selectedCampaign ? () => setIsModalOpen(true) : null}
                />

                {/* key forces remount on each open, resetting GenericModal's internal form state */}
                <GenericModal
                    key={String(isModalOpen)}
                    title="Crear turno"
                    fields={modalFields}
                    values={modalDefaultValues}
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    onSubmit={handleCreateShift}
                />

                <div style={{ marginTop: '1rem' }}>
                    <Link to="/coordinator" className="back-link-inline">← Volver al panel</Link>
                </div>

            </GenericPageWrapper>
        </SecurePage>
    );
}

export default CreateShift;
