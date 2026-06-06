import GenericTable from "../generalModules/GenericTable";
import GenericModal from "../generalModules/GenericModal";
import { useEffect, useState } from "react";
import { Link } from 'react-router-dom';
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
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

	const [isEditingModalOpen, setIsEditingModalOpen] = useState(false);
	const [selectedVolunteer, setSelectedVolunteer] = useState(null);

	const [isAddingModalOpen, setIsAddingModalOpen] = useState(false);

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
			const response = await fetch(`${apiUrl}${volunteersEndpoint}?entidadId=${entidadId}`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
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
			handleCloseEditingModal();
		} catch (error) {
			console.error("Error saving edited volunteer:", error);
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
					<div className="page-header">
						<nav className="admin-tabs" aria-label="Navegacion de administrador">
							<Link className="admin-tab" to="/admin">Volver al panel</Link>
						</nav>
						<h1>Admin Volunteers</h1>
						<p>Aqui puedes gestionar los voluntarios de cada entidad colaboradora</p>
						{partnerEntitiesSelector(partnerEntities, setEntidadId)}
						<button onClick={handleAddVolunteer}>Agregar Voluntario</button>
						<GenericTable
							title="Volunteers"
							headers={tableHeaders}
							data={volunteersData}
							addRowFunction={handleAddVolunteer}
							editRowFunction={handleEditVolunteer}
							deleteRowFunction={handleDeleteVolunteer}
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
					</div>
				</GenericPageWrapper>
			</SecurePage>
		</>
	);
}

export default AdminVolunteers;