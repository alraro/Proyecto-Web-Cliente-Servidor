import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";
import "../css/coordinator.css";
import "../css/create-shift.css";

const apiUrl = "http://localhost:8080";

function ShiftCard({ shift, onAssign }) {
    return (
        <div className="shift-item">
            <div className="shift-info">
                <h4>{shift.storeName}</h4>
                <p>{shift.day} · {shift.startTime} – {shift.endTime}</p>
                <div className="shift-details">
                    <span className="shift-detail">👤 {shift.volunteersNeeded} voluntarios</span>
                    {shift.location && <span className="shift-detail">📍 {shift.location}</span>}
                    {shift.observations && <span className="shift-detail">📝 {shift.observations}</span>}
                </div>
            </div>
            <button
                className="btn-edit"
                style={{ whiteSpace: "nowrap" }}
                onClick={() => onAssign(shift.shiftId)}
            >
                Asignar →
            </button>
        </div>
    );
}

function AssignmentModal({ shiftId, onClose }) {
    const [volunteersData, setVolunteersData] = useState({ volunteers: [], volunteersAssigned: 0, volunteersNeeded: 0 });
    const [captainsData, setCaptainsData] = useState({ captains: [] });
    const [availableVolunteers, setAvailableVolunteers] = useState([]);
    const [availableCaptains, setAvailableCaptains] = useState([]);
    const [selectedVolunteerId, setSelectedVolunteerId] = useState("");
    const [selectedCaptainId, setSelectedCaptainId] = useState("");
    const [volunteerFeedback, setVolunteerFeedback] = useState({ text: "", type: "" });
    const [captainFeedback, setCaptainFeedback] = useState({ text: "", type: "" });

    useEffect(() => {
        let ignore = false;
        async function loadModalData() {
            try {
                const [volRes, capRes, availVolRes, availCapRes] = await Promise.all([
                    fetch(`${apiUrl}/api/shifts/${shiftId}/volunteers`, { headers: authHeaders() }),
                    fetch(`${apiUrl}/api/shifts/${shiftId}/captains`, { headers: authHeaders() }),
                    fetch(`${apiUrl}/api/shifts/${shiftId}/available-volunteers`, { headers: authHeaders() }),
                    fetch(`${apiUrl}/api/shifts/${shiftId}/available-captains`, { headers: authHeaders() }),
                ]);
                const [volData, capData, availVol, availCap] = await Promise.all([
                    volRes.json(), capRes.json(), availVolRes.json(), availCapRes.json(),
                ]);
                if (!ignore) {
                    setVolunteersData(volData);
                    setCaptainsData(capData);
                    setAvailableVolunteers(Array.isArray(availVol) ? availVol : []);
                    setAvailableCaptains(Array.isArray(availCap) ? availCap : []);
                }
            } catch {}
        }
        loadModalData();
        return () => { ignore = true; };
    }, [shiftId]);

    async function reloadModalData() {
        try {
            const [volRes, capRes, availVolRes, availCapRes] = await Promise.all([
                fetch(`${apiUrl}/api/shifts/${shiftId}/volunteers`, { headers: authHeaders() }),
                fetch(`${apiUrl}/api/shifts/${shiftId}/captains`, { headers: authHeaders() }),
                fetch(`${apiUrl}/api/shifts/${shiftId}/available-volunteers`, { headers: authHeaders() }),
                fetch(`${apiUrl}/api/shifts/${shiftId}/available-captains`, { headers: authHeaders() }),
            ]);
            const [volData, capData, availVol, availCap] = await Promise.all([
                volRes.json(), capRes.json(), availVolRes.json(), availCapRes.json(),
            ]);
            setVolunteersData(volData);
            setCaptainsData(capData);
            setAvailableVolunteers(Array.isArray(availVol) ? availVol : []);
            setAvailableCaptains(Array.isArray(availCap) ? availCap : []);
        } catch {}
    }

    async function handleAssignVolunteer() {
        if (!selectedVolunteerId) {
            setVolunteerFeedback({ text: "Selecciona un voluntario.", type: "error" });
            return;
        }
        try {
            const res = await fetch(`${apiUrl}/api/shifts/${shiftId}/volunteers`, {
                method: "POST",
                headers: authHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify({ volunteerId: parseInt(selectedVolunteerId, 10) }),
            });
            const data = await res.json();
            if (!res.ok) {
                const isAlert = data.conflict === "OVERLAP" || data.conflict === "CAPACITY_EXCEEDED";
                setVolunteerFeedback({ text: data.message || "Error al asignar.", type: isAlert ? "warning" : "error" });
                return;
            }
            setVolunteerFeedback({ text: data.message, type: "success" });
            setSelectedVolunteerId("");
            await reloadModalData();
        } catch {
            setVolunteerFeedback({ text: "Error de conexión.", type: "error" });
        }
    }

    async function handleAssignCaptain() {
        if (!selectedCaptainId) {
            setCaptainFeedback({ text: "Selecciona un capitán.", type: "error" });
            return;
        }
        try {
            const res = await fetch(`${apiUrl}/api/shifts/${shiftId}/captains`, {
                method: "POST",
                headers: authHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify({ userId: parseInt(selectedCaptainId, 10) }),
            });
            const data = await res.json();
            if (!res.ok) {
                const isAlert = data.conflict === "OVERLAP";
                setCaptainFeedback({ text: data.message || "Error al asignar.", type: isAlert ? "warning" : "error" });
                return;
            }
            setCaptainFeedback({ text: data.message, type: "success" });
            setSelectedCaptainId("");
            await reloadModalData();
        } catch {
            setCaptainFeedback({ text: "Error de conexión.", type: "error" });
        }
    }

    async function handleUnassignVolunteer(volunteerId) {
        try {
            const res = await fetch(`${apiUrl}/api/shifts/${shiftId}/volunteers/${volunteerId}`, {
                method: "DELETE",
                headers: authHeaders(),
            });
            if (!res.ok) {
                const d = await res.json();
                setVolunteerFeedback({ text: d.message, type: "error" });
                return;
            }
            await reloadModalData();
        } catch {
            setVolunteerFeedback({ text: "Error de conexión.", type: "error" });
        }
    }

    async function handleUnassignCaptain(userId) {
        try {
            const res = await fetch(`${apiUrl}/api/shifts/${shiftId}/captains/${userId}`, {
                method: "DELETE",
                headers: authHeaders(),
            });
            if (!res.ok) {
                const d = await res.json();
                setCaptainFeedback({ text: d.message, type: "error" });
                return;
            }
            await reloadModalData();
        } catch {
            setCaptainFeedback({ text: "Error de conexión.", type: "error" });
        }
    }

    const isFull = volunteersData.volunteersAssigned >= volunteersData.volunteersNeeded;

    return (
        <div className="coordinator-modal-overlay" onClick={onClose}>
            <div className="coordinator-modal-content" onClick={(e) => e.stopPropagation()}>
                <span className="coordinator-modal-close" role="button" aria-label="Cerrar" onClick={onClose}>×</span>
                <h3>Asignaciones — Turno #{shiftId}</h3>

                <div className="shift-info-box">
                    Voluntarios: {volunteersData.volunteersAssigned} / {volunteersData.volunteersNeeded} asignados
                </div>

                <div
                    className="capacity-indicator"
                    style={{ color: isFull ? "var(--error-color)" : "var(--success-color)" }}
                >
                    {isFull
                        ? "⚠️ Aforo completo"
                        : `${volunteersData.volunteersAssigned} de ${volunteersData.volunteersNeeded} voluntarios asignados`}
                </div>

                <h4 className="modal-section-title">Voluntarios</h4>
                <div className="modal-assignments-list">
                    {(volunteersData.volunteers || []).length === 0 ? (
                        <p style={{ fontSize: ".88rem", color: "var(--text-light)" }}>Sin asignaciones.</p>
                    ) : (
                        (volunteersData.volunteers || []).map((v) => (
                            <div
                                key={v.volunteerId}
                                style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: ".4rem 0", borderBottom: "1px solid var(--border-color)" }}
                            >
                                <span style={{ fontSize: ".9rem" }}>{v.name} ({v.email || ""})</span>
                                <button
                                    className="btn-secondary"
                                    style={{ padding: ".25rem .75rem", fontSize: ".8rem" }}
                                    onClick={() => handleUnassignVolunteer(v.volunteerId)}
                                >
                                    Quitar
                                </button>
                            </div>
                        ))
                    )}
                </div>

                <div className="form-group">
                    <label>Añadir voluntario</label>
                    <div className="modal-assign-row">
                        <select
                            className="modal-select"
                            value={selectedVolunteerId}
                            onChange={(e) => setSelectedVolunteerId(e.target.value)}
                        >
                            <option value="">Selecciona un voluntario...</option>
                            {availableVolunteers.map((v) => (
                                <option key={v.volunteerId} value={v.volunteerId}>
                                    {v.name}{v.phone ? ` · ${v.phone}` : ""}{v.partnerEntityName ? ` (${v.partnerEntityName})` : " (Independiente)"}
                                </option>
                            ))}
                        </select>
                        <button className="btn-primary" onClick={handleAssignVolunteer}>Asignar</button>
                    </div>
                    {volunteerFeedback.text && (
                        <p className={`form-message ${volunteerFeedback.type}`}>{volunteerFeedback.text}</p>
                    )}
                </div>

                <h4 className="modal-section-title modal-section-title-spaced">Capitanes</h4>
                <div className="modal-assignments-list">
                    {(captainsData.captains || []).length === 0 ? (
                        <p style={{ fontSize: ".88rem", color: "var(--text-light)" }}>Sin asignaciones.</p>
                    ) : (
                        (captainsData.captains || []).map((c) => (
                            <div
                                key={c.userId}
                                style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: ".4rem 0", borderBottom: "1px solid var(--border-color)" }}
                            >
                                <span style={{ fontSize: ".9rem" }}>{c.name} ({c.email || ""})</span>
                                <button
                                    className="btn-secondary"
                                    style={{ padding: ".25rem .75rem", fontSize: ".8rem" }}
                                    onClick={() => handleUnassignCaptain(c.userId)}
                                >
                                    Quitar
                                </button>
                            </div>
                        ))
                    )}
                </div>

                <div className="form-group">
                    <label>Añadir capitán</label>
                    <div className="modal-assign-row">
                        <select
                            className="modal-select"
                            value={selectedCaptainId}
                            onChange={(e) => setSelectedCaptainId(e.target.value)}
                        >
                            <option value="">Selecciona un capitán...</option>
                            {availableCaptains.map((c) => (
                                <option key={c.userId} value={c.userId}>{c.name}</option>
                            ))}
                        </select>
                        <button className="btn-primary" onClick={handleAssignCaptain}>Asignar</button>
                    </div>
                    {captainFeedback.text && (
                        <p className={`form-message ${captainFeedback.type}`}>{captainFeedback.text}</p>
                    )}
                </div>
            </div>
        </div>
    );
}

export default function CreateShift() {
    const [campaigns, setCampaigns] = useState([]);
    const [stores, setStores] = useState([]);
    const [shifts, setShifts] = useState([]);
    const [selectedCampaign, setSelectedCampaign] = useState(null);
    const [selectedStoreId, setSelectedStoreId] = useState("");
    const [formVisible, setFormVisible] = useState(false);
    const [shiftsVisible, setShiftsVisible] = useState(false);
    const [modalShiftId, setModalShiftId] = useState(null);
    const [formMessage, setFormMessage] = useState({ text: "", type: "" });
    const [formData, setFormData] = useState({
        day: "", startTime: "", endTime: "", volunteersNeeded: "", location: "", observations: "",
    });

    useEffect(() => {
        let ignore = false;
        async function fetchCampaigns() {
            try {
                const res = await fetch(`${apiUrl}/api/coordinator/my-campaigns`, { headers: authHeaders() });
                if (!res.ok) return;
                const data = await res.json();
                if (!ignore) setCampaigns(Array.isArray(data) ? data : (data.content || []));
            } catch {}
        }
        fetchCampaigns();
        return () => { ignore = true; };
    }, []);

    async function handleCampaignChange(e) {
        const campaignId = e.target.value;
        const campaign = campaigns.find((c) => String(c.id) === campaignId);

        setSelectedStoreId("");
        setStores([]);
        setShifts([]);
        setFormVisible(false);
        setShiftsVisible(false);
        setFormMessage({ text: "", type: "" });

        if (!campaignId || !campaign) {
            setSelectedCampaign(null);
            return;
        }

        setSelectedCampaign({ id: campaignId, start: campaign.startDate, end: campaign.endDate });

        try {
            const res = await fetch(`${apiUrl}/api/shifts/campaign/${campaignId}/stores`, { headers: authHeaders() });
            if (!res.ok) return;
            const data = await res.json();
            setStores(Array.isArray(data) ? data : []);
            setFormVisible(true);
            await loadShifts(campaignId);
            setShiftsVisible(true);
        } catch {}
    }

    async function loadShifts(campaignId) {
        try {
            const res = await fetch(`${apiUrl}/api/shifts?campaignId=${campaignId}`, { headers: authHeaders() });
            if (!res.ok) return;
            const data = await res.json();
            setShifts(Array.isArray(data) ? data : []);
        } catch {}
    }

    function handleFormChange(field, value) {
        setFormData((prev) => ({ ...prev, [field]: value }));
    }

    async function handleSubmit() {
        const { day, startTime, endTime, volunteersNeeded, location, observations } = formData;

        if (!selectedCampaign?.id) { setFormMessage({ text: "Selecciona una campaña.", type: "error" }); return; }
        if (!selectedStoreId)     { setFormMessage({ text: "Selecciona una tienda.", type: "error" }); return; }
        if (!day)                 { setFormMessage({ text: "El día es obligatorio.", type: "error" }); return; }
        if (!startTime)           { setFormMessage({ text: "La hora de inicio es obligatoria.", type: "error" }); return; }
        if (!endTime)             { setFormMessage({ text: "La hora de fin es obligatoria.", type: "error" }); return; }

        const volunteers = parseInt(volunteersNeeded, 10);
        if (!volunteersNeeded || isNaN(volunteers) || volunteers <= 0) {
            setFormMessage({ text: "El número de voluntarios debe ser mayor que 0.", type: "error" }); return;
        }
        if (startTime >= endTime) {
            setFormMessage({ text: "La hora de inicio debe ser anterior a la hora de fin.", type: "error" }); return;
        }
        if (day < selectedCampaign.start || day > selectedCampaign.end) {
            setFormMessage({ text: `El día debe estar dentro del rango de la campaña (${selectedCampaign.start} – ${selectedCampaign.end}).`, type: "error" }); return;
        }

        try {
            const res = await fetch(`${apiUrl}/api/shifts`, {
                method: "POST",
                headers: authHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify({
                    campaignId:       parseInt(selectedCampaign.id, 10),
                    storeId:          parseInt(selectedStoreId, 10),
                    day,
                    startTime,
                    endTime,
                    volunteersNeeded: volunteers,
                    location:         location || null,
                    observations:     observations || null,
                }),
            });
            const data = await res.json();
            if (!res.ok) { setFormMessage({ text: data.message || "Error al crear el turno.", type: "error" }); return; }
            setFormMessage({ text: "Turno creado correctamente.", type: "success" });
            setFormData({ day: "", startTime: "", endTime: "", volunteersNeeded: "", location: "", observations: "" });
            await loadShifts(selectedCampaign.id);
        } catch {
            setFormMessage({ text: "Error de conexión con el servidor.", type: "error" });
        }
    }

    function handleReset() {
        setFormData({ day: "", startTime: "", endTime: "", volunteersNeeded: "", location: "", observations: "" });
        setFormMessage({ text: "", type: "" });
    }

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/coordinator">← Volver al panel</Link>
                    </nav>
                    <div className="page-header-row">
                        <div>
                            <h1>Crear turno de recogida</h1>
                            <p>Define turnos de voluntarios para una campaña y tienda, y asigna participantes.</p>
                        </div>
                    </div>
                </div>

                <div className="form-card">
                    <h3>Selecciona campaña y tienda</h3>
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="campaign-select">
                                Campaña <span className="required-asterisk">*</span>
                            </label>
                            <select id="campaign-select" onChange={handleCampaignChange}>
                                <option value="">Selecciona una campaña...</option>
                                {campaigns.map((c) => (
                                    <option key={c.id} value={c.id}>
                                        {c.name} ({c.startDate} – {c.endDate})
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label htmlFor="store-select">
                                Tienda <span className="required-asterisk">*</span>
                            </label>
                            <select
                                id="store-select"
                                disabled={stores.length === 0}
                                value={selectedStoreId}
                                onChange={(e) => setSelectedStoreId(e.target.value)}
                            >
                                {stores.length === 0 ? (
                                    <option value="">Selecciona primero una campaña...</option>
                                ) : (
                                    <>
                                        <option value="">Selecciona una tienda...</option>
                                        {stores.map((s) => (
                                            <option key={s.id} value={s.id}>{s.name}</option>
                                        ))}
                                    </>
                                )}
                            </select>
                        </div>
                    </div>
                </div>

                {formVisible && (
                    <div className="form-card">
                        <h3>Datos del turno</h3>
                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="shift-day">Día <span className="required-asterisk">*</span></label>
                                <input
                                    type="date"
                                    id="shift-day"
                                    value={formData.day}
                                    min={selectedCampaign?.start}
                                    max={selectedCampaign?.end}
                                    onChange={(e) => handleFormChange("day", e.target.value)}
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="volunteers-needed">
                                    Nº voluntarios necesarios <span className="required-asterisk">*</span>
                                </label>
                                <input
                                    type="number"
                                    id="volunteers-needed"
                                    min="1"
                                    placeholder="Ej: 5"
                                    value={formData.volunteersNeeded}
                                    onChange={(e) => handleFormChange("volunteersNeeded", e.target.value)}
                                />
                            </div>
                        </div>
                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="start-time">Hora de inicio <span className="required-asterisk">*</span></label>
                                <input
                                    type="time"
                                    id="start-time"
                                    value={formData.startTime}
                                    onChange={(e) => handleFormChange("startTime", e.target.value)}
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="end-time">Hora de fin <span className="required-asterisk">*</span></label>
                                <input
                                    type="time"
                                    id="end-time"
                                    value={formData.endTime}
                                    onChange={(e) => handleFormChange("endTime", e.target.value)}
                                />
                            </div>
                        </div>
                        <div className="form-group">
                            <label htmlFor="location">Ubicación específica</label>
                            <input
                                type="text"
                                id="location"
                                placeholder="Ej: Entrada principal, junto a los carritos"
                                maxLength={500}
                                value={formData.location}
                                onChange={(e) => handleFormChange("location", e.target.value)}
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="observations">Observaciones</label>
                            <textarea
                                id="observations"
                                rows={3}
                                placeholder="Observaciones adicionales..."
                                maxLength={1000}
                                value={formData.observations}
                                onChange={(e) => handleFormChange("observations", e.target.value)}
                            />
                        </div>
                        {formMessage.text && (
                            <div className={`form-message ${formMessage.type}`}>{formMessage.text}</div>
                        )}
                        <div className="form-actions">
                            <button type="button" className="btn-primary" onClick={handleSubmit}>Crear turno</button>
                            <button type="button" className="btn-secondary" onClick={handleReset}>Limpiar</button>
                        </div>
                    </div>
                )}

                {shiftsVisible && (
                    <div className="shifts-list-card">
                        <div className="shifts-list-header">
                            <h3>Turnos creados para esta campaña</h3>
                            <button className="btn-refresh" onClick={() => loadShifts(selectedCampaign.id)}>
                                ↻ Actualizar
                            </button>
                        </div>
                        <div>
                            {shifts.length === 0 ? (
                                <p className="empty-message">No hay turnos creados para esta campaña.</p>
                            ) : (
                                shifts.map((s) => (
                                    <ShiftCard key={s.shiftId} shift={s} onAssign={setModalShiftId} />
                                ))
                            )}
                        </div>
                    </div>
                )}

                {modalShiftId !== null && (
                    <AssignmentModal shiftId={modalShiftId} onClose={() => setModalShiftId(null)} />
                )}
            </GenericPageWrapper>
        </SecurePage>
    );
}
