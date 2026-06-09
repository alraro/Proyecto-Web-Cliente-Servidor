import GenericTable from "../generalModules/GenericTable";
import GenericModal from "../generalModules/GenericModal";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";

const apiUrl = "http://localhost:8080";
const VOLUNTEER_FIELDS = [
    { name: "id", label: "ID", type: "text", readOnly: true },
    { name: "name", label: "Nombre", type: "text" },
    { name: "phone", label: "Teléfono", type: "text" },
    { name: "email", label: "Correo electrónico", type: "email" },
    { name: "address", label: "Dirección", type: "text" },
];

function ColaboradorVolunteers() {
    const [entidadId, setEntidadId] = useState(null);
    const [entityName, setEntityName] = useState("");
    const [volunteersData, setVolunteersData] = useState([]);
    const [isEditingModalOpen, setIsEditingModalOpen] = useState(false);
    const [selectedVolunteer, setSelectedVolunteer] = useState(null);
    const [isAddingModalOpen, setIsAddingModalOpen] = useState(false);
    const [filterString, setFilterString] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [sinEntidad, setSinEntidad] = useState(false);

    const tableHeaders = {
        id: "ID",
        name: "Nombre",
        phone: "Teléfono",
        email: "Correo electrónico",
        address: "Dirección",
    };

    useEffect(() => {
        async function loadManagerInfo() {
            try {
                const meRes = await fetch(`${apiUrl}/api/partner-entity-managers/me`, { headers: authHeaders() });
                if (!meRes.ok) throw new Error("No se pudo obtener la información del colaborador");
                const me = await meRes.json();

                if (me.partnerEntityId == null) {
                    setSinEntidad(true);
                    setIsLoading(false);
                    return;
                }

                setEntidadId(me.partnerEntityId);
                setEntityName(me.partnerEntityName || "Entidad");
            } catch (error) {
                console.error("Error loading manager info:", error);
            }
        }
        loadManagerInfo();
    }, []);

    useEffect(() => {
        if (entidadId == null) return;

        async function fetchVolunteers() {
            try {
                const response = await fetch(`${apiUrl}/api/voluntarios?entidadId=${entidadId}`, { headers: authHeaders() });
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                const data = await response.json();
                setVolunteersData(data);
            } catch (error) {
                console.error("Error fetching volunteers:", error);
            } finally {
                setIsLoading(false);
            }
        }
        fetchVolunteers();
    }, [entidadId]);

    function handleEditVolunteer(volunteer) {
        setSelectedVolunteer(volunteer);
        setIsEditingModalOpen(true);
    }

    function handleCloseEditingModal() {
        setIsEditingModalOpen(false);
        setSelectedVolunteer(null);
    }

    function handleAddVolunteer() {
        setIsAddingModalOpen(true);
    }

    function handleCloseAddingModal() {
        setIsAddingModalOpen(false);
        setSelectedVolunteer(null);
    }

    async function handleSaveVolunteerAdd(formData) {
        try {
            const response = await fetch(`${apiUrl}/api/voluntarios?entidadId=${entidadId}`, {
                method: "POST",
                headers: authHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify(formData),
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const result = await response.json();
            setVolunteersData((prev) =>
                [...prev, { ...formData, id: result.id }]
            );
            handleCloseAddingModal();
        } catch (error) {
            console.error("Error saving new volunteer:", error);
        }
    }

    async function handleSaveVolunteerEdit(formData) {
        try {
            const response = await fetch(`${apiUrl}/api/voluntarios/${formData.id}?entidadId=${entidadId}`, {
                method: "PUT",
                headers: authHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify(formData),
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            setVolunteersData((prev) =>
                prev.map((v) => (v.id === formData.id ? { ...v, ...formData } : v))
            );
            handleCloseEditingModal();
        } catch (error) {
            console.error("Error saving edited volunteer:", error);
        }
    }

    async function handleDeleteVolunteer(volunteer) {
        try {
            const response = await fetch(`${apiUrl}/api/voluntarios/${volunteer.id}?entidadId=${entidadId}`, {
                method: "DELETE",
                headers: authHeaders(),
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            setVolunteersData((prev) => prev.filter((v) => v.id !== volunteer.id));
        } catch (error) {
            console.error("Error deleting volunteer:", error);
        }
    }

    if (sinEntidad) {
        return (
            <SecurePage>
                <GenericPageWrapper>
                    <div className="page-header">
                        <nav>
                            <Link className="back-link-inline" to="/colaborator">← Volver al panel</Link>
                        </nav>
                        <h1>Voluntarios</h1>
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

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/colaborator">← Volver al panel</Link>
                    </nav>
                    <h1>Voluntarios de {entityName}</h1>
                    <p>Gestiona los voluntarios de tu entidad colaboradora.</p>
                </div>

                <GenericTable
                    title="Voluntarios"
                    headers={tableHeaders}
                    data={volunteersData}
                    addRowFunction={handleAddVolunteer}
                    editRowFunction={handleEditVolunteer}
                    deleteRowFunction={handleDeleteVolunteer}
                    itemName="Voluntario"
                    onChangeSearch={setFilterString}
                    filterCondition={(volunteer) =>
                        volunteer.name.toLowerCase().includes(filterString.toLowerCase()) ||
                        volunteer.email.toLowerCase().includes(filterString.toLowerCase())
                    }
                    isLoading={isLoading && entidadId == null}
                />

                <GenericModal
                    key={selectedVolunteer?.id}
                    title="Editar Voluntario"
                    fields={VOLUNTEER_FIELDS}
                    values={selectedVolunteer}
                    isOpen={isEditingModalOpen}
                    onClose={handleCloseEditingModal}
                    onSubmit={handleSaveVolunteerEdit}
                />

                <GenericModal
                    title="Agregar Voluntario"
                    fields={VOLUNTEER_FIELDS.filter(f => f.name !== "id")}
                    values={{}}
                    isOpen={isAddingModalOpen}
                    onClose={handleCloseAddingModal}
                    onSubmit={handleSaveVolunteerAdd}
                />
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default ColaboradorVolunteers;
