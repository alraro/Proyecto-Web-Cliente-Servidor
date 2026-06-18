import { useEffect, useState } from "react";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import SecurePage from "../generalModules/SecurePage";
import { useAuth } from "../auth/useAuthHook";
import "../css/responsible-store.css";

const apiUrl = "http://localhost:8080";

function StoreInfo({ store }) {
    return (
        <section className="card responsible-store-card" id="card-tienda">
            <h2 id="store-title">{store?.name || "ECHEVERRIA"}</h2>
            <div className="info-grid">
                <div className="info-item">
                    <label>Nombre</label>
                    <span>{store?.name || "ECHEVERRIA"}</span>
                </div>
                <div className="info-item">
                    <label>Domicilio</label>
                    <span>{store?.address || "Avda Pio Baroja,"}</span>
                </div>
                <div className="info-item">
                    <label>Código postal</label>
                    <span>{store?.postalCode || "29017"}</span>
                </div>
                <div className="info-item">
                    <label>Localidad</label>
                    <span>{store?.locality || "Málaga"}</span>
                </div>
                <div className="info-item">
                    <label>Zona geog.</label>
                    <span>{store?.zone || "Málaga"}</span>
                </div>
                <div className="info-item">
                    <label>Cadena</label>
                    <span>{store?.chainName || "CARREFOUR"}</span>
                </div>
            </div>
        </section>
    );
}

function ShiftRow({ shift }) {
    let badgeClass = "badge-pending";
    let badgeText = "Pendiente";

    if (shift.attendance === true) {
        badgeClass = "badge-yes";
        badgeText = "Sí";
    }

    if (shift.attendance === false) {
        badgeClass = "badge-no";
        badgeText = "No";
    }

    return (
        <tr>
            <td>{shift.campaignName || "N/A"}</td>
            <td>{shift.volunteerName || "N/A"}</td>
            <td>{shift.endTime || "N/A"}</td>
            <td>
                <span className={`badge-attendance ${badgeClass}`}>{badgeText}</span>
            </td>
            <td>{shift.notes || "N/A"}</td>
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
                        <th>Campaña</th>
                        <th>Voluntario</th>
                        <th>Hora fin</th>
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
                        shifts.map((shift, index) => <ShiftRow key={`${shift.campaignName || "shift"}-${index}`} shift={shift} />)
                    )}
                </tbody>
            </table>
        </section>
    );
}

export default function ResponsibleStore() {
    const [storeData, setStoreData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    const { usuario } = useAuth();

    useEffect(() => {
        async function fetchStoreDetail() {
            try {
                const token = sessionStorage.getItem("token");
                const storeId = usuario?.storeId ?? JSON.parse(sessionStorage.getItem("user") || "null")?.storeId;

                if (!token || !storeId) {
                    setError("No se ha podido identificar la tienda asignada.");
                    return;
                }

                const response = await fetch(`${apiUrl}/api/stores/${storeId}/detail`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                if (!response.ok) {
                    const payload = await response.json().catch(() => null);
                    throw new Error(payload?.message || `HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                setStoreData(data);
            } catch (fetchError) {
                console.error("Error fetching responsible store detail:", fetchError);
                setError(fetchError.message || "No se ha podido cargar la información de la tienda.");
            } finally {
                setIsLoading(false);
            }
        }

        fetchStoreDetail();
    }, [usuario]);

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <h1>Mi tienda</h1>
                    <p>Información de tu tienda asignada y turnos programados.</p>
                </div>

                {isLoading ? (
                    <section className="card responsible-state-card">
                        <p className="state-message">Cargando información de la tienda...</p>
                    </section>
                ) : error ? (
                    <section className="card responsible-state-card">
                        <p className="state-message state-error">{error}</p>
                    </section>
                ) : (
                    <>
                        <StoreInfo store={storeData} />
                        <ShiftsTable shifts={storeData?.scheduledShifts || []} />
                    </>
                )}
            </GenericPageWrapper>
        </SecurePage>
    );
}