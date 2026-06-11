import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import GenericTable from "../generalModules/GenericTable";
import GenericModal from "../generalModules/GenericModal";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";

const STORE_FIELDS = [
    { name: "id", label: "ID", type: "text", readOnly: true },
    { name: "name", label: "Nombre", type: "text" },
    { name: "address", label: "Dirección", type: "text" },
    { name: "postalCode", label: "Código postal", type: "text" },
];

const apiUrl = "http://localhost:8080";
const STORE_PAGE_SIZE = 100;

async function fetchAllStores() {
    const firstResponse = await fetch(`${apiUrl}/api/stores?page=0&size=${STORE_PAGE_SIZE}`, { headers: authHeaders() });

    if (!firstResponse.ok) {
        throw new Error(`HTTP error! status: ${firstResponse.status}`);
    }

    const firstPage = await firstResponse.json();
    const stores = Array.isArray(firstPage.content) ? [...firstPage.content] : [];
    const totalPages = Number(firstPage.totalPages) || 0;

    for (let page = 1; page < totalPages; page += 1) {
        const response = await fetch(`${apiUrl}/api/stores?page=${page}&size=${STORE_PAGE_SIZE}`, { headers: authHeaders() });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        stores.push(...(Array.isArray(data.content) ? data.content : []));
    }

    return stores;
}

function mapStoreToTableRow(store) {
    return {
        ...store,
        locality: store.locality || "N/A",
        postalcode: store.postalCode || "N/A",
        zone: store.zone || "N/A",
        chainname: store.chainName || "N/A",
    };
}

function StoreFilters({ chains, zones, localities, value, onChange, onApply, onClear, onZoneChange }) {
    function handleChainChange(event) {
        onChange({ ...value, chainId: event.target.value });
    }

    function handleZoneChange(event) {
        const nextZoneId = event.target.value;
        onChange({ ...value, zoneId: nextZoneId, localityId: "" });
        if (onZoneChange) {
            onZoneChange(nextZoneId);
        }
    }

    function handleLocalityChange(event) {
        onChange({ ...value, localityId: event.target.value });
    }

    function handleClear() {
        onClear();
    }

    const filteredLocalities = value.zoneId
        ? localities.filter((locality) => String(locality.zoneId) === String(value.zoneId))
        : localities;

    return (
        <div className="filters-bar">
            <select value={value.chainId} onChange={handleChainChange}>
                <option value="">Todas las cadenas</option>
                {chains.map((chain) => (
                    <option key={chain.id} value={chain.id}>
                        {chain.name}
                    </option>
                ))}
            </select>

            <select value={value.zoneId} onChange={handleZoneChange}>
                <option value="">Todas las zonas</option>
                {zones.map((zone) => (
                    <option key={zone.id} value={zone.id}>
                        {zone.name}
                    </option>
                ))}
            </select>

            <select value={value.localityId} onChange={handleLocalityChange}>
                <option value="">Todas las localidades</option>
                {filteredLocalities.map((locality) => (
                    <option key={locality.id} value={locality.id}>
                        {locality.name}
                    </option>
                ))}
            </select>

            <button className="btn btn-primary btn-sm" onClick={() => onApply(value)}>
                Aplicar filtros
            </button>
            <button className="btn btn-secondary btn-sm" onClick={handleClear}>
                Limpiar
            </button>
        </div>
    );
}

export default function AdminStores() {
    const tableHeaders = {
        id: "ID",
        name: "Nombre",
        address: "Dirección",
        locality: "Localidad",
        postalcode: "CP",
        zone: "Zona",
        chainname: "Cadena",
    };

    const [storesData, setStoresData] = useState([]);
    const [chainsData, setChainsData] = useState([]);
    const [zonesData, setZonesData] = useState([]);
    const [localitiesData, setLocalitiesData] = useState([]);
    const [filters, setFilters] = useState({ chainId: "", zoneId: "", localityId: "" });
    const [filterString, setFilterString] = useState("");
    const [selectedStore, setSelectedStore] = useState(null);
    const [isEditingModalOpen, setIsEditingModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        async function fetchInitialData() {
            try {
                const [stores, chainsResponse, zonesResponse] = await Promise.all([
                    fetchAllStores(),
                    fetch(`${apiUrl}/api/chains`, { headers: authHeaders() }),
                    fetch(`${apiUrl}/api/zones`, { headers: authHeaders() }),
                ]);

                if (!chainsResponse.ok) {
                    throw new Error(`HTTP error! status: ${chainsResponse.status}`);
                }
                if (!zonesResponse.ok) {
                    throw new Error(`HTTP error! status: ${zonesResponse.status}`);
                }

                const chainsJson = await chainsResponse.json();
                const zonesJson = await zonesResponse.json();

                setStoresData(stores);
                setChainsData(Array.isArray(chainsJson) ? chainsJson : []);
                setZonesData(Array.isArray(zonesJson) ? zonesJson : []);
            } catch (error) {
                console.error("Error fetching stores data:", error);
            } finally {
                setIsLoading(false);
            }
        }

        fetchInitialData();
    }, []);

    useEffect(() => {
        async function fetchLocalities() {
            try {
                const url = filters.zoneId ? `${apiUrl}/api/localities?zoneId=${filters.zoneId}` : `${apiUrl}/api/localities`;
                const response = await fetch(url, { headers: authHeaders() });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                const nextLocalities = Array.isArray(data) ? data : [];
                setLocalitiesData(nextLocalities);
            } catch (error) {
                console.error("Error fetching localities data:", error);
            }
        }

        fetchLocalities();
    }, [filters.zoneId]);

    function handleApplyFilters(nextFilters) {
        setFilters(nextFilters);
    }

    function handleClearFilters() {
        setFilters({ chainId: "", zoneId: "", localityId: "" });
        setFilterString("");
    }

    function handleUpdateFilters(nextFilters) {
        setFilters(nextFilters);
    }

    function handleEditStore(store) {
        setSelectedStore(store);
        setIsEditingModalOpen(true);
    }

    function handleCloseEditingModal() {
        setIsEditingModalOpen(false);
        setSelectedStore(null);
    }

    async function handleSaveStore(formData) {
        try {
            const response = await fetch(`${apiUrl}/api/stores/${formData.id}`, {
                method: "PUT",
                headers: authHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify({
                    name: formData.name,
                    address: formData.address,
                    postalCode: formData.postalCode,
                    chainId: selectedStore?.chainId,
                }),
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const updatedStore = await response.json();
            setStoresData((prev) => prev.map((store) => (store.id === updatedStore.id ? updatedStore : store)));
            handleCloseEditingModal();
        } catch (error) {
            console.error("Error saving store:", error);
        }
    }

    const filteredStores = storesData.filter((store) => {
        if (filters.chainId && String(store.chainId) !== String(filters.chainId)) return false;
        if (filters.zoneId && String(store.zoneId) !== String(filters.zoneId)) return false;
        if (filters.localityId && String(store.localityId) !== String(filters.localityId)) return false;
        return true;
    });

    const visibleStores = filteredStores.map(mapStoreToTableRow);

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/admin">← Volver al panel</Link>
                    </nav>
                    <h1>Gestión de Tiendas</h1>
                    <p>Consulta y modifica las tiendas registradas en la plataforma.</p>
                </div>

                <StoreFilters
                    chains={chainsData}
                    zones={zonesData}
                    localities={localitiesData}
                    value={filters}
                    onChange={handleUpdateFilters}
                    onApply={handleApplyFilters}
                    onClear={handleClearFilters}
                    onZoneChange={(zoneId) => setFilters((prev) => ({ ...prev, zoneId, localityId: "" }))}
                />

                <GenericTable
                    title="Tiendas"
                    headers={tableHeaders}
                    data={visibleStores}
                    editRowFunction={handleEditStore}
                    itemName="Tienda"
                    onChangeSearch={setFilterString}
                    filterCondition={(store) => store.name.toLowerCase().includes(filterString.toLowerCase())}
                    isLoading={isLoading}
                />

                <GenericModal
                    key={selectedStore?.id}
                    title="Editar tienda"
                    fields={STORE_FIELDS}
                    values={selectedStore}
                    isOpen={isEditingModalOpen}
                    onClose={handleCloseEditingModal}
                    onSubmit={handleSaveStore}
                />
            </GenericPageWrapper>
        </SecurePage>
    );
}
