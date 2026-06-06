import GenericTable from "../generalModules/GenricTable";
import { useEffect, useState } from "react";

function partnerEntitiesSelector(partnerEntities, setEntidadId) {
    return (
        <select name="entidadId" id="entidadId" onChange={(e) => setEntidadId(e.target.value)}>
            {partnerEntities.map((entity) => (
                <option key={entity.id} value={entity.id}>
                    {entity.name}
                </option>
            ))}
        </select>
    );
}

function AdminVolunteers() {

    const apiUrl = "http://localhost:8080";

    const volunteersEndpoint = "/api/voluntarios";
    const partnerEntitiesEndpoint = "/api/partner-entities";

    const tableHeaders = {
        "id": "ID",
        "name": "Nombre",
        "phone": "Teléfono",
        "email": "Correo electrónico",
        "address": "Dirección",
    }

    const [entidadId, setEntidadId] = useState(1);
    const [volunteersData, setVolunteersData] = useState([]);
    const [partnerEntities, setPartnerEntities] = useState([]);

    useEffect(() => {
        async function fetchPartnerEntities() {
            try {
                const response = await fetch(`${apiUrl}${partnerEntitiesEndpoint}`);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();
                if (!data || !data['content'] || data['content'].length === 0) {
                    console.warn("No partner entities found.");
                    return;
                }
                setPartnerEntities(data['content']);
            } catch (error) {
                console.error("Error fetching partner entities:", error);
            }
        }
        fetchPartnerEntities()
    }, []);

    const fullURL = `${apiUrl}${volunteersEndpoint}?entidadId=${entidadId}`;
    
    useEffect(() => {
        const fetchVolunteersData = async () => {
            try {
                const response = await fetch(`${fullURL}`);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();
                setVolunteersData(data);
            } catch (error) {
                console.error("Error fetching volunteers data:", error);
            }
        };
        fetchVolunteersData()
    }, [fullURL]);

    return (
        <div>
            <h1>Admin Volunteers</h1>
            {partnerEntitiesSelector(partnerEntities, setEntidadId)}
            <GenericTable
                title="Volunteers"
                headers={tableHeaders}
                data={volunteersData}
            />
        </div>
    );
}

export default AdminVolunteers;