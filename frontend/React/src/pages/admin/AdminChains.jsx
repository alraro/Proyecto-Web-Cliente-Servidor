import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import GenericTable from "../generalModules/GenericTable";
import SecurePage from "../generalModules/SecurePage";

const CHAIN_FIELDS = [
    { name: "id", label: "ID", type: "text", readOnly: true },
    { name: "name", label: "Nombre", type: "text" },
    { name: "code", label: "Código", type: "text" },
    { name: "participation", label: "Participa", type: "checkbox" },
];

const apiUrl = "http://localhost:8080";
const chainsEndpoint = "/api/chains";

function getAuthToken() {
    return sessionStorage.getItem("token");
}

function ChainModal({ title, fields, values, isOpen, onClose, onSubmit }) {
    const [formData, setFormData] = useState(() => ({ ...(values || {}) }));

    useEffect(() => {
        if (isOpen) {
            setFormData({ ...(values || {}) });
        }
    }, [isOpen, values]);

    if (!isOpen) return null;

    function handleChange(name, value) {
        setFormData((prev) => ({ ...prev, [name]: value }));
    }

    function handleSubmit(event) {
        event.preventDefault();
        if (onSubmit) onSubmit(formData);
    }

    return (
        <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modal-title" onClick={onClose}>
            <div className="modal" onClick={(event) => event.stopPropagation()}>
                <div className="modal-header">
                    <h3 id="modal-title">{title}</h3>
                    <button className="modal-close" onClick={onClose} aria-label="Cerrar modal">&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        {fields.map((field) => (
                            <div className="form-group" key={field.name}>
                                <label htmlFor={field.name}>{field.label}</label>
                                {field.type === "checkbox" ? (
                                    <input
                                        id={field.name}
                                        type="checkbox"
                                        checked={Boolean(formData[field.name])}
                                        onChange={(event) => handleChange(field.name, event.target.checked)}
                                        disabled={field.readOnly || false}
                                    />
                                ) : (
                                    <input
                                        id={field.name}
                                        type={field.type || "text"}
                                        value={formData[field.name] ?? ""}
                                        onChange={(event) => handleChange(field.name, event.target.value)}
                                        disabled={field.readOnly || false}
                                    />
                                )}
                            </div>
                        ))}
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-cancel" onClick={onClose}>
                            Cancelar
                        </button>
                        <button type="submit" className="btn btn-primary">
                            Guardar
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default function AdminChains() {
    const tableHeaders = {
        id: "ID",
        name: "Nombre",
        code: "Código",
        participation: "Participa",
    };

    const [chainsData, setChainsData] = useState([]);
    const [chainsDisplay, setChainsDisplay] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [filterString, setFilterString] = useState("");
    const [selectedChain, setSelectedChain] = useState(null);
    const [isEditingModalOpen, setIsEditingModalOpen] = useState(false);
    const [isAddingModalOpen, setIsAddingModalOpen] = useState(false);

    useEffect(() => {
        async function fetchChains() {
            try {
                const token = getAuthToken();
                const response = await fetch(`${apiUrl}${chainsEndpoint}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                const rawData = Array.isArray(data) ? data : [];

                setChainsData(rawData);
                setChainsDisplay(rawData.map((chain) => ({
                    ...chain,
                    participation: chain.participation ? "✓ Sí" : "— No",
                })));
            } catch (error) {
                console.error("Error fetching chains data:", error);
            } finally {
                setIsLoading(false);
            }
        }

        fetchChains();
    }, []);

    function handleAddChain() {
        setSelectedChain({ id: "", name: "", code: "", participation: false });
        setIsAddingModalOpen(true);
    }

    function handleEditChain(chain) {
        const rawChain = chainsData.find((item) => item.id === chain.id) || chain;
        setSelectedChain(rawChain);
        setIsEditingModalOpen(true);
    }

    function handleCloseModal() {
        setIsEditingModalOpen(false);
        setIsAddingModalOpen(false);
        setSelectedChain(null);
    }

    async function handleSaveChain(formData) {
        const isEditing = Boolean(formData.id);
        const endpoint = isEditing ? `${apiUrl}${chainsEndpoint}/${formData.id}` : `${apiUrl}${chainsEndpoint}`;
        const method = isEditing ? "PUT" : "POST";

        try {
            const token = getAuthToken();
            const response = await fetch(endpoint, {
                method,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    name: formData.name,
                    code: formData.code,
                    participation: Boolean(formData.participation),
                }),
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const savedChain = await response.json();
            setChainsData((prev) => {
                const next = isEditing
                    ? prev.map((chain) => (chain.id === savedChain.id ? savedChain : chain))
                    : [...prev, savedChain];

                setChainsDisplay(next.map((chain) => ({
                    ...chain,
                    participation: chain.participation ? "✓ Sí" : "— No",
                })));

                return next;
            });

            handleCloseModal();
        } catch (error) {
            console.error("Error saving chain:", error);
        }
    }

    async function handleDeleteChain(chain) {
        try {
            const token = getAuthToken();
            const response = await fetch(`${apiUrl}${chainsEndpoint}/${chain.id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` },
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            setChainsData((prev) => {
                const next = prev.filter((item) => item.id !== chain.id);
                setChainsDisplay(next.map((item) => ({
                    ...item,
                    participation: item.participation ? "✓ Sí" : "— No",
                })));
                return next;
            });
        } catch (error) {
            console.error("Error deleting chain:", error);
        }
    }

    const filteredChains = chainsDisplay.filter((chain) => {
        const search = filterString.toLowerCase();
        return chain.name.toLowerCase().includes(search) || chain.code.toLowerCase().includes(search);
    });

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/admin">← Volver al panel</Link>
                    </nav>
                    <h1>Gestión de Cadenas</h1>
                    <p>Aquí puedes consultar, crear, editar y eliminar cadenas.</p>
                </div>

                <GenericTable
                    title="Cadenas"
                    headers={tableHeaders}
                    data={filteredChains}
                    addRowFunction={handleAddChain}
                    editRowFunction={handleEditChain}
                    deleteRowFunction={handleDeleteChain}
                    itemName="Cadena"
                    onChangeSearch={setFilterString}
                    filterCondition={(chain) => chain.name.toLowerCase().includes(filterString.toLowerCase()) || chain.code.toLowerCase().includes(filterString.toLowerCase())}
                    isLoading={isLoading}
                />

                <ChainModal
                    title="Editar cadena"
                    fields={CHAIN_FIELDS}
                    values={selectedChain}
                    isOpen={isEditingModalOpen}
                    onClose={handleCloseModal}
                    onSubmit={handleSaveChain}
                />

                <ChainModal
                    title="Agregar cadena"
                    fields={CHAIN_FIELDS.filter((field) => field.name !== "id")}
                    values={selectedChain}
                    isOpen={isAddingModalOpen}
                    onClose={handleCloseModal}
                    onSubmit={handleSaveChain}
                />
            </GenericPageWrapper>
        </SecurePage>
    );
}
