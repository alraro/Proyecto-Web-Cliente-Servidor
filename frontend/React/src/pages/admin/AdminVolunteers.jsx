import GenericTable from "../generalModules/GenricTable";
import { useEffect, useState } from "react";
import { Link } from 'react-router-dom';
import GenericPageWrapper from "../generalModules/GenericPageWrapper";

function partnerEntitiesSelector(partnerEntities, setEntidadId) {
    return (
        <div className="filters-bar">
            <select name="entidadId" id="entidadId" onChange={(e) => setEntidadId(e.target.value)}>
                {partnerEntities.map((entity) => (
                    <option key={entity.id} value={entity.id}>
                        {entity.name}
                    </option>
                ))}
            </select>
        </div>
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
        <GenericPageWrapper headerUsername={"-----Placeholder------"}>
            <div>
                <h1>Admin Volunteers</h1>
                <nav className="admin-tabs" aria-label="Navegacion de administrador">
                    <Link className="admin-tab" to="/admin">Volver al panel</Link>
                    <Link className="admin-tab" to="/login">Cerrar sesion</Link>
                </nav>
                {partnerEntitiesSelector(partnerEntities, setEntidadId)}
                <GenericTable
                    title="Volunteers"
                    headers={tableHeaders}
                    data={volunteersData}
                    editRowFunction={() => {console.log("Editando voluntario")}}
                    deleteRowFunction={() => {console.log("Eliminando voluntario")}}
                    />
            </div>
        </GenericPageWrapper>
    );
}

export default AdminVolunteers;