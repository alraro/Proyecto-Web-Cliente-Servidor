import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";

const apiUrl = "http://localhost:8080";

function ColaboradorEntity() {
    const [entity, setEntity] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({ name: "", address: "", phone: "" });
    const [message, setMessage] = useState("");
    const [isError, setIsError] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [sinEntidad, setSinEntidad] = useState(false);

    useEffect(() => {
        async function loadEntity() {
            try {
                const meRes = await fetch(`${apiUrl}/api/partner-entity-managers/me`, { headers: authHeaders() });
                if (!meRes.ok) throw new Error("No se pudo obtener la información del colaborador");
                const me = await meRes.json();

                if (me.partnerEntityId == null) {
                    setSinEntidad(true);
                    return;
                }

                const entityRes = await fetch(`${apiUrl}/api/partner-entities/${me.partnerEntityId}`, { headers: authHeaders() });
                if (!entityRes.ok) throw new Error("No se pudo obtener los datos de la entidad");
                const entityData = await entityRes.json();

                setEntity(entityData);
                setFormData({ name: entityData.name, address: entityData.address || "", phone: entityData.phone || "" });
            } catch (error) {
                setMessage(error.message);
                setIsError(true);
            } finally {
                setIsLoading(false);
            }
        }
        loadEntity();
    }, []);

    async function handleSave() {
        if (!formData.name.trim()) {
            setMessage("El nombre es obligatorio.");
            setIsError(true);
            return;
        }

        try {
            const res = await fetch(`${apiUrl}/api/partner-entities/${entity.id}`, {
                method: "PUT",
                headers: authHeaders({ "Content-Type": "application/json" }),
                body: JSON.stringify({
                    name: formData.name.trim(),
                    address: formData.address.trim() || null,
                    phone: formData.phone.trim() || null,
                }),
            });

            if (!res.ok) {
                const err = await res.json();
                throw new Error(err.message || "Error al guardar");
            }

            const updated = await res.json();
            setEntity(updated);
            setFormData({ name: updated.name, address: updated.address || "", phone: updated.phone || "" });
            setIsEditing(false);
            setMessage("Entidad actualizada correctamente.");
            setIsError(false);
        } catch (error) {
            setMessage(error.message);
            setIsError(true);
        }
    }

    function handleCancel() {
        setFormData({ name: entity.name, address: entity.address || "", phone: entity.phone || "" });
        setIsEditing(false);
        setMessage("");
    }

    if (sinEntidad) {
        return (
            <SecurePage>
                <GenericPageWrapper>
                    <div className="page-header">
                        <nav>
                            <Link className="back-link-inline" to="/colaborator">← Volver al panel</Link>
                        </nav>
                        <h1>Mi entidad colaboradora</h1>
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

    if (isLoading) {
        return (
            <SecurePage>
                <GenericPageWrapper>
                    <p>Cargando...</p>
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
                    <h1>Mi entidad colaboradora</h1>
                </div>

                {message && (
                    <div className={isError ? "toast toast-error" : "toast toast-success"}>{message}</div>
                )}

                <div className="card">
                    <div className="card-header">
                        <h2>Datos de la entidad</h2>
                        {!isEditing && (
                            <div className="card-actions">
                                <button className="btn btn-primary" onClick={() => setIsEditing(true)}>Editar</button>
                            </div>
                        )}
                    </div>

                    {!isEditing ? (
                        <div className="card-body">
                            <p><strong>Nombre:</strong> {entity?.name}</p>
                            <p><strong>Dirección:</strong> {entity?.address || "—"}</p>
                            <p><strong>Teléfono:</strong> {entity?.phone || "—"}</p>
                        </div>
                    ) : (
                        <div className="card-body">
                            <div className="form-group">
                                <label>Nombre <span className="required-asterisk">*</span></label>
                                <input
                                    type="text"
                                    value={formData.name}
                                    maxLength={255}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                />
                            </div>
                            <div className="form-group">
                                <label>Dirección</label>
                                <input
                                    type="text"
                                    value={formData.address}
                                    maxLength={500}
                                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                                />
                            </div>
                            <div className="form-group">
                                <label>Teléfono</label>
                                <input
                                    type="text"
                                    value={formData.phone}
                                    maxLength={20}
                                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                />
                            </div>
                            <div className="form-actions">
                                <button className="btn btn-primary" onClick={handleSave}>Guardar</button>
                                <button className="btn btn-secondary" onClick={handleCancel}>Cancelar</button>
                            </div>
                        </div>
                    )}
                </div>
            </GenericPageWrapper>
        </SecurePage>
    );
}

export default ColaboradorEntity;
