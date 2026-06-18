import { useEffect, useState } from "react";
import { Link } from "react-router";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import GenericTable from "../generalModules/GenericTable";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";
import "../css/campaigns.css";
import "../css/admin-campaigns.css";

const apiUrl = "http://localhost:8080";
const campaignsEndpoint = "/api/campaigns";

function formatDate(iso) {
    if (!iso) return "—";
    const parts = String(iso).split("-");
    return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : String(iso);
}

// Modal de crear/editar campaña con el panel dual de tiendas (disponibles / seleccionadas)
export function CampaignModal({ title, isOpen, onClose, onSubmit, campaign, campaignTypes }) {
    const [formData, setFormData] = useState({ name: "", typeId: "", startDate: "", endDate: "" });
    const [available, setAvailable] = useState([]);
    const [selected, setSelected] = useState([]);
    const [chains, setChains] = useState([]);
    const [zones, setZones] = useState([]);
    const [localities, setLocalities] = useState([]);
    const [filters, setFilters] = useState({ chainId: "", zoneId: "", localityId: "" });
    const [error, setError] = useState("");

    async function loadStores(activeFilters) {
        const params = new URLSearchParams({ size: "100" });
        if (activeFilters.chainId) params.append("chainId", activeFilters.chainId);
        if (activeFilters.zoneId) params.append("zoneId", activeFilters.zoneId);
        if (activeFilters.localityId) params.append("localityId", activeFilters.localityId);
        try {
            const response = await fetch(`${apiUrl}/api/stores?${params.toString()}`, { headers: authHeaders() });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const data = await response.json();
            setAvailable(data.content || []);
        } catch (err) {
            console.error("Error fetching stores:", err);
        }
    }

    useEffect(() => {
        if (!isOpen) return;
        setError("");
        setFilters({ chainId: "", zoneId: "", localityId: "" });
        setFormData({
            name: campaign?.name ?? "",
            typeId: campaign?.typeId ?? "",
            startDate: campaign?.startDate ?? "",
            endDate: campaign?.endDate ?? "",
        });

        async function loadOptions() {
            try {
                const [chainsRes, zonesRes, localitiesRes] = await Promise.all([
                    fetch(`${apiUrl}/api/chains`, { headers: authHeaders() }),
                    fetch(`${apiUrl}/api/zones`, { headers: authHeaders() }),
                    fetch(`${apiUrl}/api/localities`, { headers: authHeaders() }),
                ]);
                setChains(chainsRes.ok ? await chainsRes.json() : []);
                setZones(zonesRes.ok ? await zonesRes.json() : []);
                setLocalities(localitiesRes.ok ? await localitiesRes.json() : []);
            } catch (err) {
                console.error("Error fetching filter options:", err);
            }
        }

        async function loadSelected() {
            if (!campaign?.id) {
                setSelected([]);
                return;
            }
            try {
                const response = await fetch(`${apiUrl}${campaignsEndpoint}/${campaign.id}/stores`, { headers: authHeaders() });
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                const data = await response.json();
                setSelected(data.stores || []);
            } catch (err) {
                console.error("Error fetching campaign stores:", err);
            }
        }

        loadOptions();
        loadStores({ chainId: "", zoneId: "", localityId: "" });
        loadSelected();
    }, [isOpen, campaign]);

    if (!isOpen) return null;

    function handleChange(name, value) {
        setFormData((prev) => ({ ...prev, [name]: value }));
    }

    function handleFilterChange(name, value) {
        const next = { ...filters, [name]: value };
        setFilters(next);
    }

    function addStore(store) {
        setSelected((prev) => (prev.some((s) => s.id === store.id) ? prev : [...prev, store]));
    }

    function removeStore(id) {
        setSelected((prev) => prev.filter((s) => s.id !== id));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        if (!formData.name.trim() || !formData.typeId || !formData.startDate || !formData.endDate) {
            setError("Todos los campos de la campaña son obligatorios.");
            return;
        }
        if (formData.endDate <= formData.startDate) {
            setError("La fecha de fin debe ser posterior a la de inicio.");
            return;
        }
        try {
            await onSubmit(formData, selected.map((s) => s.id));
        } catch (err) {
            setError(err.message || "No se pudo guardar la campaña.");
        }
    }

    const selectedIds = new Set(selected.map((s) => s.id));
    const availableToShow = available.filter((s) => !selectedIds.has(s.id));

    return (
        <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modal-title" onClick={onClose}>
            <div className="modal modal--wide" onClick={(event) => event.stopPropagation()}>
                <div className="modal-header">
                    <h3 id="modal-title">{title}</h3>
                    <button className="modal-close" onClick={onClose} aria-label="Cerrar modal">&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        {error && <p className="form-message">{error}</p>}

                        <div className="form-group">
                            <label htmlFor="name">Nombre</label>
                            <input id="name" type="text" value={formData.name}
                                onChange={(e) => handleChange("name", e.target.value)} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="typeId">Tipo</label>
                            <select id="typeId" value={formData.typeId}
                                onChange={(e) => handleChange("typeId", e.target.value)}>
                                <option value="">-- Seleccionar --</option>
                                {campaignTypes.map((t) => (
                                    <option key={t.id} value={t.id}>{t.name}</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label htmlFor="startDate">Fecha de inicio</label>
                            <input id="startDate" type="date" value={formData.startDate}
                                onChange={(e) => handleChange("startDate", e.target.value)} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="endDate">Fecha de fin</label>
                            <input id="endDate" type="date" value={formData.endDate}
                                onChange={(e) => handleChange("endDate", e.target.value)} />
                        </div>

                        <div className="store-selector-section">
                            <h3>Tiendas participantes</h3>
                            <div className="store-filter-row">
                                <select value={filters.chainId} onChange={(e) => handleFilterChange("chainId", e.target.value)}>
                                    <option value="">Todas las cadenas</option>
                                    {chains.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </select>
                                <select value={filters.zoneId} onChange={(e) => handleFilterChange("zoneId", e.target.value)}>
                                    <option value="">Todas las zonas</option>
                                    {zones.map((z) => <option key={z.id} value={z.id}>{z.name}</option>)}
                                </select>
                                <select value={filters.localityId} onChange={(e) => handleFilterChange("localityId", e.target.value)}>
                                    <option value="">Todas las localidades</option>
                                    {localities.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
                                </select>
                                <button type="button" className="btn btn-secondary btn-sm" onClick={() => loadStores(filters)}>Filtrar</button>
                                <button type="button" className="btn btn-secondary btn-sm" onClick={() => {
                                    const cleared = { chainId: "", zoneId: "", localityId: "" };
                                    setFilters(cleared);
                                    loadStores(cleared);
                                }}>Limpiar</button>
                            </div>

                            <div className="store-dual-panel">
                                <div className="store-panel">
                                    <h4>Disponibles <span className="badge">{availableToShow.length}</span></h4>
                                    <ul className="store-list">
                                        {availableToShow.length === 0 ? (
                                            <li className="store-list-empty">Sin tiendas disponibles.</li>
                                        ) : availableToShow.map((s) => (
                                            <li key={s.id}>
                                                <span>{s.name} — {s.chainName || "-"} — {s.locality || "-"}</span>
                                                <button type="button" className="btn-add-store" onClick={() => addStore(s)}>+</button>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                                <div className="store-panel">
                                    <h4>Seleccionadas <span className="badge">{selected.length}</span></h4>
                                    <ul className="store-list">
                                        {selected.length === 0 ? (
                                            <li className="store-list-empty">Sin tiendas seleccionadas.</li>
                                        ) : selected.map((s) => (
                                            <li key={s.id}>
                                                <span>{s.name} — {s.chainName || "-"}</span>
                                                <button type="button" className="btn-remove-store" onClick={() => removeStore(s.id)}>×</button>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-cancel" onClick={onClose}>Cancelar</button>
                        <button type="submit" className="btn btn-primary">Guardar</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default function AdminCampaigns() {
    const tableHeaders = { name: "Nombre", tipo: "Tipo", inicio: "Inicio", fin: "Fin" };

    const [campaigns, setCampaigns] = useState([]);
    const [campaignTypes, setCampaignTypes] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [filterString, setFilterString] = useState("");
    const [selectedCampaign, setSelectedCampaign] = useState(null);
    const [isEditingModalOpen, setIsEditingModalOpen] = useState(false);
    const [isAddingModalOpen, setIsAddingModalOpen] = useState(false);

    async function loadCampaigns() {
        try {
            const response = await fetch(`${apiUrl}${campaignsEndpoint}?size=200&sort=startDate,desc`, { headers: authHeaders() });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const data = await response.json();
            setCampaigns(data.content || []);
        } catch (error) {
            console.error("Error fetching campaigns:", error);
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        loadCampaigns();
        async function fetchTypes() {
            try {
                const response = await fetch(`${apiUrl}/api/campaign-types`, { headers: authHeaders() });
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                setCampaignTypes(await response.json());
            } catch (error) {
                console.error("Error fetching campaign types:", error);
            }
        }
        fetchTypes();
    }, []);

    function handleAddCampaign() {
        setSelectedCampaign(null);
        setIsAddingModalOpen(true);
    }

    function handleEditCampaign(row) {
        const raw = campaigns.find((c) => c.id === row.id) || row;
        setSelectedCampaign(raw);
        setIsEditingModalOpen(true);
    }

    function handleCloseModal() {
        setIsEditingModalOpen(false);
        setIsAddingModalOpen(false);
        setSelectedCampaign(null);
    }

    // Crea/edita la campaña y luego sincroniza sus tiendas, lanza Error con el mensaje del backend si falla
    async function handleSaveCampaign(formData, storeIds) {
        const isEditing = Boolean(selectedCampaign?.id);
        const url = isEditing ? `${apiUrl}${campaignsEndpoint}/${selectedCampaign.id}` : `${apiUrl}${campaignsEndpoint}`;
        const body = {
            name: formData.name.trim(),
            typeId: Number(formData.typeId),
            startDate: formData.startDate,
            endDate: formData.endDate,
        };

        const response = await fetch(url, {
            method: isEditing ? "PUT" : "POST",
            headers: authHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify(body),
        });
        const data = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(data.message || `Error ${response.status}`);

        const campaignId = isEditing ? selectedCampaign.id : data.campaign.id;
        const storesResponse = await fetch(`${apiUrl}${campaignsEndpoint}/${campaignId}/stores`, {
            method: "PUT",
            headers: authHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify({ storeIds }),
        });
        if (!storesResponse.ok) {
            const storesData = await storesResponse.json().catch(() => ({}));
            throw new Error(storesData.message || "No se pudieron asignar las tiendas.");
        }

        handleCloseModal();
        loadCampaigns();
    }

    async function handleDeleteCampaign(row) {
        if (!window.confirm(`¿Eliminar la campaña "${row.name}"? Se eliminarán también sus asignaciones.`)) return;
        try {
            const response = await fetch(`${apiUrl}${campaignsEndpoint}/${row.id}`, {
                method: "DELETE",
                headers: authHeaders(),
            });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            setCampaigns((prev) => prev.filter((c) => c.id !== row.id));
        } catch (error) {
            console.error("Error deleting campaign:", error);
        }
    }

    const displayCampaigns = campaigns.map((c) => ({
        id: c.id,
        name: c.name,
        tipo: c.typeName && c.typeName.length ? c.typeName : "—",
        inicio: formatDate(c.startDate),
        fin: formatDate(c.endDate),
    }));

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/admin">← Volver al panel</Link>
                    </nav>
                    <h1>Gestión de Campañas</h1>
                    <p>Crea, edita y elimina campañas de recogida de alimentos.</p>
                </div>

                <GenericTable
                    title="Campañas"
                    headers={tableHeaders}
                    data={displayCampaigns}
                    addRowFunction={handleAddCampaign}
                    editRowFunction={handleEditCampaign}
                    deleteRowFunction={handleDeleteCampaign}
                    itemName="Campaña"
                    onChangeSearch={setFilterString}
                    filterCondition={(c) => c.name.toLowerCase().includes(filterString.toLowerCase())}
                    isLoading={isLoading}
                />

                <CampaignModal
                    title="Editar campaña"
                    isOpen={isEditingModalOpen}
                    onClose={handleCloseModal}
                    onSubmit={handleSaveCampaign}
                    campaign={selectedCampaign}
                    campaignTypes={campaignTypes}
                />

                <CampaignModal
                    title="Agregar campaña"
                    isOpen={isAddingModalOpen}
                    onClose={handleCloseModal}
                    onSubmit={handleSaveCampaign}
                    campaign={null}
                    campaignTypes={campaignTypes}
                />
            </GenericPageWrapper>
        </SecurePage>
    );
}
