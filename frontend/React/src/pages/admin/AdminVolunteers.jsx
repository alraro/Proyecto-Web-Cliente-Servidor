import GenericTable from "../generalModules/GenericTable";
import GenericModal from "../generalModules/GenericModal";
import { useEffect, useState } from "react";
import { Link } from 'react-router-dom';
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import '../css/common.css';
import SecurePage from "../generalModules/SecurePage";

const VOLUNTEER_FIELDS = [
    { name: "id", label: "ID", type: "text", readOnly: true },
    { name: "name", label: "Nombre", type: "text" },
    { name: "phone", label: "Teléfono", type: "text" },
    { name: "email", label: "Correo electrónico", type: "email" },
    { name: "address", label: "Dirección", type: "text" },
];

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

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedVolunteer, setSelectedVolunteer] = useState(null);

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

    function handleEditVolunteer(volunteer) {
        setSelectedVolunteer(volunteer);
        setIsModalOpen(true);
    }

    function handleCloseModal() {
        setIsModalOpen(false);
        setSelectedVolunteer(null);
    }

    async function handleSaveVolunteer(formData) {
        try {
            const response = await fetch(`${apiUrl}${volunteersEndpoint}/${formData.id}?entidadId=${entidadId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData),
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            setVolunteersData((prev) =>
                prev.map((v) => (v.id === formData.id ? { ...v, ...formData } : v))
            );
            handleCloseModal();
        } catch (error) {
            console.error("Error saving volunteer:", error);
        }
    }

    async function handleDeleteVolunteer(volunteer) {
        try {
            const response = await fetch(`${apiUrl}${volunteersEndpoint}/${volunteer.id}?entidadId=${entidadId}`, {
                method: "DELETE",
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            setVolunteersData((prev) => prev.filter((v) => v.id !== volunteer.id));
        } catch (error) {
            console.error("Error deleting volunteer:", error);
        }
    }

    return (
        <>
            <SecurePage >
                <GenericPageWrapper >
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
                            editRowFunction={handleEditVolunteer}
                            deleteRowFunction={handleDeleteVolunteer}
                            />
                        <GenericModal
                            title="Editar Voluntario"
                            fields={VOLUNTEER_FIELDS}
                            values={selectedVolunteer}
                            isOpen={isModalOpen}
                            onClose={handleCloseModal}
                            onSubmit={handleSaveVolunteer}
                        />
                    </div>
                </GenericPageWrapper>
            </SecurePage>
        </>
    );
}

export default AdminVolunteers;