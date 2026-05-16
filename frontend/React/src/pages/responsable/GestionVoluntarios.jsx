import { useState, useEffect } from 'react';
import './GestionVoluntarios.css';

const API_BASE_URL = 'http://localhost:8080/api';

function GestionVoluntarios() {
  const [volunteers, setVolunteers] = useState([]);
  const [entityName, setEntityName] = useState('');
  const [entityId, setEntityId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingVolunteer, setEditingVolunteer] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    email: '',
    address: ''
  });

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('entidadId');
    if (id) {
      setEntityId(parseInt(id, 10));
      fetchEntityName(id);
      fetchVolunteers(id);
    } else {
      setEntityId(1);
      fetchEntityName(1);
      fetchVolunteers(1);
    }
  }, []);

  const fetchEntityName = async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/partner-entities/${id}`);
      if (response.ok) {
        const data = await response.json();
        setEntityName(data.name || '');
      }
    } catch (err) {
      console.error('Error fetching entity name:', err);
    }
  };

  const fetchVolunteers = async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/voluntarios?entidadId=${id}`);
      if (response.ok) {
        const data = await response.json();
        setVolunteers(data);
      } else {
        setError('Error al cargar los voluntarios');
      }
    } catch (err) {
      setError('Error de conexión');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const openCreateModal = () => {
    setEditingVolunteer(null);
    setFormData({ name: '', phone: '', email: '', address: '' });
    setShowModal(true);
  };

  const openEditModal = (volunteer) => {
    setEditingVolunteer(volunteer);
    setFormData({
      name: volunteer.name || '',
      phone: volunteer.phone || '',
      email: volunteer.email || '',
      address: volunteer.address || ''
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingVolunteer(null);
    setFormData({ name: '', phone: '', email: '', address: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!entityId) return;

    const url = editingVolunteer
      ? `${API_BASE_URL}/voluntarios/${editingVolunteer.id}?entidadId=${entityId}`
      : `${API_BASE_URL}/voluntarios?entidadId=${entityId}`;

    const method = editingVolunteer ? 'PUT' : 'POST';

    try {
      const response = await fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        fetchVolunteers(entityId);
        closeModal();
      } else {
        const data = await response.json();
        setError(data.message || 'Error al guardar el voluntario');
      }
    } catch (err) {
      setError('Error de conexión');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de que desea eliminar este voluntario?')) {
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/voluntarios/${id}?entidadId=${entityId}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        fetchVolunteers(entityId);
      } else {
        const data = await response.json();
        setError(data.message || 'Error al eliminar el voluntario');
      }
    } catch (err) {
      setError('Error de conexión');
    }
  };

  const goBack = () => {
    window.location.href = '/';
  };

  if (loading) {
    return <div className="loading">Cargando...</div>;
  }

  return (
    <div className="gestion-voluntarios">
      <header className="header">
        <a className="brand" href="#" onClick={(e) => { e.preventDefault(); goBack(); }}>
          <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" className="logo" />
        </a>
        <nav className="main-nav">
          <a href="#" onClick={(e) => { e.preventDefault(); goBack(); }}>Volver</a>
        </nav>
      </header>

      <main className="main-content">
        <span className="back-link" onClick={goBack}>
          ← Volver al inicio
        </span>

        <h2 className="page-title">Voluntarios de {entityName || 'Entidad'}</h2>

        {error && <div className="error-message">{error}</div>}

        <button className="btn-add" onClick={openCreateModal}>
          Añadir Voluntario
        </button>

        <table className="volunteers-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Teléfono</th>
              <th>Email</th>
              <th>Dirección</th>
              <th>Campañas</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {volunteers.length === 0 ? (
              <tr>
                <td colSpan="6" className="no-data">No hay voluntarios registrados</td>
              </tr>
            ) : (
              volunteers.map(volunteer => (
                <tr key={volunteer.id}>
                  <td>{volunteer.name}</td>
                  <td>{volunteer.phone || '-'}</td>
                  <td>{volunteer.email || '-'}</td>
                  <td>{volunteer.address || '-'}</td>
                  <td>
                    {volunteer.campaigns && volunteer.campaigns.length > 0 ? (
                      <div className="campaigns-list">
                        {volunteer.campaigns.map(camp => (
                          <span key={camp.id} className="campaign-badge">{camp.name}</span>
                        ))}
                      </div>
                    ) : (
                      <span className="no-campaigns">Sin campañas</span>
                    )}
                  </td>
                  <td className="actions">
                    <button className="btn-edit" onClick={() => openEditModal(volunteer)}>
                      Editar
                    </button>
                    <button className="btn-delete" onClick={() => handleDelete(volunteer.id)}>
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </main>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>{editingVolunteer ? 'Editar Voluntario' : 'Nuevo Voluntario'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="name">Nombre *</label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="phone">Teléfono</label>
                <input
                  type="text"
                  id="phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label htmlFor="address">Dirección</label>
                <input
                  type="text"
                  id="address"
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                />
              </div>
              <div className="form-actions">
                <button type="submit" className="btn-save">
                  Guardar
                </button>
                <button type="button" className="btn-cancel" onClick={closeModal}>
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <footer className="footer">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" className="logo" style={{width: '130px'}} />
        <p>Bancosol · Banco de alimentos</p>
      </footer>
    </div>
  );
}

export default GestionVoluntarios;