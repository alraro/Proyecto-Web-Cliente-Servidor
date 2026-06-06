import { useState } from "react";

function GenericModal({ title, fields, values, isOpen, onClose, onSubmit }) {

    const [formData, setFormData] = useState(() => ({ ...(values || {}) }));

    if (!isOpen) return null;

    function handleChange(name, value) {
        setFormData((prev) => ({ ...prev, [name]: value }));
    }

    function handleSubmit(e) {
        e.preventDefault();
        if (onSubmit) {
            onSubmit(formData);
        }
    }

    function renderField(field) {
        const value = formData[field.name] ?? "";

        const commonProps = {
            id: field.name,
            value: value,
            onChange: (e) => handleChange(field.name, e.target.value),
            disabled: field.readOnly || false,
        };

        if (field.type === "select") {
            return (
                <select {...commonProps}>
                    <option value="">-- Seleccionar --</option>
                    {(field.options || []).map((opt) => (
                        <option key={opt.value} value={opt.value}>
                            {opt.label}
                        </option>
                    ))}
                </select>
            );
        }

        return <input type={field.type || "text"} {...commonProps} />;
    }

    return (
        <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modal-title" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h3 id="modal-title">{title}</h3>
                    <button className="modal-close" onClick={onClose} aria-label="Cerrar modal">&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        {fields.map((field) => (
                            <div className="form-group" key={field.name}>
                                <label htmlFor={field.name}>{field.label}</label>
                                {renderField(field)}
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

export default GenericModal;
