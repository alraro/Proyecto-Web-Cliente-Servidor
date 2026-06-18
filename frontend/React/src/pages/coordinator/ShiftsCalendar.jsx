import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";
import "../css/shifts-calendar.css";

const apiUrl = "http://localhost:8080";

function ShiftCard({ shift }) {
    const needed   = shift.volunteersNeeded   || 0;
    const assigned = shift.volunteersAssigned || 0;
    const pct      = needed > 0 ? assigned / needed : 0;
    const pct100   = Math.min(Math.round(pct * 100), 100);

    let statusClass;
    if (pct >= 1)     statusClass = "shift-full";
    else if (pct > 0) statusClass = "shift-partial";
    else              statusClass = "shift-empty";

    return (
        <div className={`shift-card ${statusClass}`}>
            <div className="shift-time">{shift.startTime} – {shift.endTime}</div>
            <div className="shift-vol">
                <span className="vol-count">{assigned}/{needed}</span>
                <span className="vol-label"> voluntarios</span>
            </div>
            <div className="vol-bar">
                <div className="vol-bar-fill" style={{ width: `${pct100}%` }} />
            </div>
            {shift.observations && (
                <div className="shift-obs">{shift.observations}</div>
            )}
        </div>
    );
}

function formatDate(dateStr) {
    if (!dateStr) return "";
    const [y, m, d] = dateStr.split("-").map(Number);
    const date = new Date(y, m - 1, d);
    const dayNames = ["dom", "lun", "mar", "mié", "jue", "vie", "sáb"];
    return `${dayNames[date.getDay()]} ${String(d).padStart(2, "0")}/${String(m).padStart(2, "0")}`;
}

export default function ShiftsCalendar() {
    const [campaigns, setCampaigns] = useState([]);
    const [calendarData, setCalendarData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

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
        setCalendarData(null);
        setError("");

        if (!campaignId) return;

        setLoading(true);
        try {
            const res = await fetch(`${apiUrl}/api/shifts/calendar?campaignId=${campaignId}`, { headers: authHeaders() });
            if (!res.ok) { setError("Error al cargar el calendario."); return; }
            const data = await res.json();
            setCalendarData(Array.isArray(data) ? data : []);
        } catch {
            setError("Error al conectar con el servidor.");
        } finally {
            setLoading(false);
        }
    }

    const showLegend = calendarData !== null && calendarData.length > 0;

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/coordinator">← Volver al panel</Link>
                    </nav>
                    <div className="page-header-row">
                        <div>
                            <h1>Calendario de Turnos</h1>
                            <p>Vista de turnos por tienda, día y franja horaria.</p>
                        </div>
                    </div>
                </div>

                <div className="cal-filter-bar">
                    <div className="form-group">
                        <label htmlFor="campaign-select">Campaña</label>
                        <select id="campaign-select" onChange={handleCampaignChange}>
                            <option value="">Selecciona una campaña...</option>
                            {campaigns.map((c) => (
                                <option key={c.id} value={c.id}>
                                    {c.name}{c.startDate ? ` (${c.startDate} – ${c.endDate || ""})` : ""}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                {showLegend && (
                    <div className="cal-legend">
                        <span className="legend-item legend-full">Completo</span>
                        <span className="legend-item legend-partial">Parcial</span>
                        <span className="legend-item legend-empty">Sin voluntarios</span>
                    </div>
                )}

                <div>
                    {loading && (
                        <p className="cal-placeholder">Cargando calendario...</p>
                    )}
                    {!loading && error && (
                        <p className="cal-placeholder cal-error">{error}</p>
                    )}
                    {!loading && !error && calendarData === null && (
                        <p className="cal-placeholder">Selecciona una campaña para ver el calendario de turnos.</p>
                    )}
                    {!loading && !error && calendarData !== null && calendarData.length === 0 && (
                        <p className="cal-placeholder">No hay turnos para esta campaña.</p>
                    )}
                    {!loading && !error && calendarData !== null && calendarData.length > 0 && (
                        calendarData.map((store) => (
                            <section key={store.storeName} className="store-section">
                                <div className="store-header">
                                    <span className="store-icon">🏬</span>
                                    <h2>{store.storeName}</h2>
                                </div>
                                <div className="days-grid">
                                    {(store.days || []).map((day) => (
                                        <div key={day.date} className="day-col">
                                            <div className="day-label">{formatDate(day.date)}</div>
                                            {(day.shifts || []).map((shift) => (
                                                <ShiftCard key={`${shift.startTime}-${shift.endTime}`} shift={shift} />
                                            ))}
                                        </div>
                                    ))}
                                </div>
                            </section>
                        ))
                    )}
                </div>
            </GenericPageWrapper>
        </SecurePage>
    );
}
