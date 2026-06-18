import { useEffect, useState } from "react";
import { Link } from "react-router";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import GenericTable from "../generalModules/GenericTable";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";
import "../css/campaigns.css";

const apiUrl = "http://localhost:8080";

const tableHeaders = {
    name: "Nombre",
    tipo: "Tipo",
    inicio: "Inicio",
    fin: "Fin",
    estado: "Estado",
};

// Estado del backend -> etiqueta y clase de badge
const STATUS = {
    ACTIVE: { label: "Activa", badge: "badge-active" },
    FUTURE: { label: "Futura", badge: "badge-future" },
    PAST: { label: "Pasada", badge: "badge-past" },
};

function formatDate(iso) {
    if (!iso) return "—";
    const parts = String(iso).split("-");
    return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : String(iso);
}

export default function Campaigns() {
    const [campaigns, setCampaigns] = useState([]);
    const [summary, setSummary] = useState({ totalActive: 0, totalFuture: 0, totalPast: 0 });
    const [statusFilter, setStatusFilter] = useState("");
    const [search, setSearch] = useState("");
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        async function fetchCampaigns() {
            try {
                const response = await fetch(`${apiUrl}/api/campaigns?size=50&sort=startDate,desc`, {
                    headers: authHeaders(),
                });
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                const data = await response.json();
                setCampaigns(data.content || []);
                setSummary(data.summary || { totalActive: 0, totalFuture: 0, totalPast: 0 });
            } catch (error) {
                console.error("Error fetching campaigns:", error);
            } finally {
                setIsLoading(false);
            }
        }
        fetchCampaigns();
    }, []);

    // Filas para la tabla (claves en minúscula como espera GenericTable)
    const displayCampaigns = campaigns.map((c) => {
        const status = STATUS[c.status] || { label: "—", badge: "badge" };
        return {
            id: c.id,
            name: c.name,
            tipo: c.typeName && c.typeName.length ? c.typeName : "—",
            inicio: formatDate(c.startDate),
            fin: formatDate(c.endDate),
            estado: <span className={status.badge}>{status.label}</span>,
            _status: c.status,
        };
    });

    const chips = [
        { value: "", label: "Todas" },
        { value: "ACTIVE", label: `Activas: ${summary.totalActive ?? 0}` },
        { value: "FUTURE", label: `Futuras: ${summary.totalFuture ?? 0}` },
        { value: "PAST", label: `Pasadas: ${summary.totalPast ?? 0}` },
    ];

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/admin">← Volver al panel</Link>
                    </nav>
                    <h1>Campañas de recogida</h1>
                    <p>Consulta el estado de todas las campañas de Bancosol.</p>
                </div>

                <div className="chips-bar">
                    {chips.map((chip) => (
                        <button
                            key={chip.value || "all"}
                            className={`chip${statusFilter === chip.value ? " chip-selected" : ""}`}
                            onClick={() => setStatusFilter(chip.value)}
                        >
                            {chip.label}
                        </button>
                    ))}
                </div>

                <GenericTable
                    title="Listado de campañas"
                    headers={tableHeaders}
                    data={displayCampaigns}
                    itemName="Campaña"
                    onChangeSearch={setSearch}
                    filterCondition={(row) =>
                        (!statusFilter || row._status === statusFilter) &&
                        row.name.toLowerCase().includes(search.toLowerCase())
                    }
                    isLoading={isLoading}
                />
            </GenericPageWrapper>
        </SecurePage>
    );
}
