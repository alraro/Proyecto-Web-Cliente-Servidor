import GenericTable from "../generalModules/GenricTable";

function AdminVolunteers() {

    const tableHeaders = {
        "id": "ID",
        "name": "Nombre",
        "phone": "Teléfono",
        "email": "Correo electrónico",
        "address": "Dirección",
        "id_partner_entity": "ID Entidad Asociada"
    }

    const volunteersData = [
        {
            id: 1,
            name: "Juan Perez",
            phone: "123456789",
            email: "juan.perez@example.com",
            address: "Calle Principal 123",
            id_partner_entity: 1
        },
        {
            id: 2,
            name: "Maria Gomez",
            phone: "987654321",
            email: "maria.gomez@example.com",
            address: "Avenida Secundaria 456",
            id_partner_entity: 2
        }
    ];

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