import GenericTable from "../generalModules/GenricTable";
import { useEffect, useState } from "react";

function AdminVolunteers() {

    const apiUrl = "http://localhost:8080";
    const endpoint = "/api/voluntarios";
    const entidadId = 3

    const fullURL = `${apiUrl}${endpoint}?entidadId=${entidadId}`;

    const tableHeaders = {
        "id": "ID",
        "name": "Nombre",
        "phone": "Teléfono",
        "email": "Correo electrónico",
        "address": "Dirección",
        "id_partner_entity": "ID Entidad Asociada"
    }

    const [volunteersData, setVolunteersData] = useState([]);
    
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
        fetchVolunteersData().then(() => console.log(`Volunteers data fetched successfully: ${JSON.stringify(volunteersData)}`));
    }, [fullURL]);

    return (
        <div>
            <h1>Admin Volunteers</h1>
            <GenericTable
                title="Volunteers"
                headers={tableHeaders}
                data={volunteersData}
            />
        </div>
    );
}

export default AdminVolunteers;