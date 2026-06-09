import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import GenericTable from "../generalModules/GenericTable";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";

const apiUrl = "http://localhost:8080";

function ColaboradorCampaigns() {
    const [campaigns, setCampaigns] = useState([]);
    const [entityName, setEntityName] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [sinEntidad, setSinEntidad] = useState(false);
    const [filterString, setFilterString] = useState("");

    const tableHeaders = {
        name: "Nombre",
        start: "Fecha inicio",
        end: "Fecha fin",
        volunteers: "Voluntarios",
    };

    useEffect(() => {
        async function loadCampaigns() {
            try {
                const meRes = await fetch(`${apiUrl}/api/partner-entity-managers/me`, { headers: authHeaders() });
                if (!meRes.ok) throw new Error("No se pudo obtener la información del colaborador");
                const me = await meRes.json();

                if (me.partnerEntityId == null) {
                    setSinEntidad(true);
                    return;
                }

                setEntityName(me.partnerEntityName || "Entidad");

                const campaignsRes = await fetch(`${apiUrl}/api/partner-entity-managers/me/campaigns`, { headers: authHeaders() });
                if (!campaignsRes.ok) throw new Error("No se pudieron cargar las campañas");
                const data = await campaignsRes.json();
                setCampaigns(data);
            } catch (error) {
                console.error("Error loading campaigns:", error);
            } finally {
                setIsLoading(false);
            }
        }
        loadCampaigns();
    }, []);

    function formatDate(dateStr) {
        if (!dateStr) return "—";
        const parts = dateStr.split("-");
        return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : dateStr;
    }

    if (sinEntidad) {
        return (
            <SecurePage>
                <GenericPageWrapper>
                    <div className="page-header">
                        <nav>
                            <Link className="back-link-inline" to="/colaborator">← Volver al panel</Link>
                        </nav>
                        <h1>Campañas</h1>
                    </div>
                    <div className="card">
                        <div className="card-body">
                            <p>No tienes una entidad colaboradora asignada.</p>
                            <p>Contacta con un administrador para que te asigne una.</p>
                        </div>
                    </div>
                </GenericPageWrapper>
            </SecurePage>
        );
    }

    const formattedCampaigns = campaigns.map(c => ({
        name: c.name,
        start: formatDate(c.startDate),
        end: formatDate(c.endDate),
        volunteers: c.volunteerCount,
    }));

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/colaborator">← Volver al panel</Link>
                    </nav>
                    <h1>Campañas de {entityName}</h1>
                    <p>Campañas en las que participan los voluntarios de tu entidad.</p>
                </div>

                <GenericTable
                    title="Listado de campañas"
                    headers={tableHeaders}
                    data={formattedCampaigns}
                    isLoading={isLoading}
                    itemName="Campaña"
                    onChangeSearch={setFilterString}
                    filterCondition={(c) => c.name.toLowerCase().includes(filterString.toLowerCase())}
                />
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default ColaboradorCampaigns;
