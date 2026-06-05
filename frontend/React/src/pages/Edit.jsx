import React from 'react'
import {Link, useNavigate} from 'react-router'
import logoBancosol from '../assets/LOGO_BANCOSOL.png';
import Bancosol from '../assets/Bancosol.png';
import {useState, useEffect} from 'react'
import {useAuth} from './auth/useAuthHook'
import './css/login.css'

const API_BASE = 'http://localhost:8080';

const ROLE_ROUTES = {
    ADMINISTRADOR: '/admin',
    COORDINADOR: '/coordinator',
    CAPITAN: '/captain',
    COLABORADOR: '/colaborator',
    RESPONSABLE_TIENDA: '/responsible',
}

function Edit() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        nombre: '',
        email: '',
        telefono: '',
        localidad: '',
        domicilio: '',
        cp: ''
    });

    const [message, setMessage] = useState('');
    const [messageType, setMessageType] = useState('');

    const {usuario} = useAuth();
    const role = usuario?.role;
    const returnRoute = ROLE_ROUTES[role] || '/login';

    // Cargamos los datos del usuario al montar el componente
    useEffect(() => {
        const loadProfile = async () => {
            const token = localStorage.getItem('token');

            if(!token) {
                navigate('/login');
                return;
            }

            try {
                const res = await fetch(`${API_BASE}/api/auth/profile`, {
                    method: 'GET',
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });

                const data = await res.json();

                if(!res.ok) {
                    setMessage(data.message || 'Error al cargar perfil');
                    setMessageType('error');
                    return;
                }

                setFormData({
                    nombre: data.nombre || localStorage.getItem('nombre') || '',
                    email: data.email || '',
                    telefono: data.telefono || '',
                    localidad: data.localidad || '',
                    domicilio: data.domicilio || '',
                    cp: data.cp || ''
                });
            } catch (e) {
                console.log(e);
                setMessage('Error de conexión. Intenta nuevamente.');
                setMessageType('error');
            }
        };

        loadProfile();
    }, [navigate]);


    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prev => ({
            ...prev, 
            [name]: value
        }));
    };

    const validateForm = () => {
        const {email, telefono, cp} = formData;

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if(!email.trim()) {
            setMessage('El email es obligatorio');
            setMessageType('error');
            return false;
        }

        if(!emailRegex.test(email)) {
            setMessage('El email no es válido');
            setMessageType('error');
            return false;
        }

        if(!telefono.trim()) {
            setMessage('El teléfono es obligatorio');
            setMessageType('error');
            return false;
        }

        if(cp.trim() && !/^[0-9]{5}$/.test(cp.trim())) {
            setMessage('El código postal debe tener 5 dígitos');
            setMessageType('error');
            return false;
        }

        return true;
    };


    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');

        if(!validateForm()) {
            return;
        }

        const token = localStorage.getItem('token');

        if(!token) {
            navigate('/login');
            return;
        }

        const data = {
            email: formData.email.trim(),
            telefono: formData.telefono.trim(),
            domicilio: formData.domicilio.trim(),
            cp: formData.cp.trim()
        };

        try {
            const res = await fetch(`${API_BASE}/api/auth/profile`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(data)
            });

            const data = await res.json();

            if(!res.ok) {
                setMessage(data.message || 'Error al actualizar perfil');
                setMessageType('error');
                return;
            }
            
            setMessage('Perfil actualizado correctamente');
            setMessageType('success');

            setTimeout(() => {
                navigate(returnRoute);
            }, 1500);

        } catch (e) {
            console.log(e);
            setMessage('Error de conexión. Intenta nuevamente.');
            setMessageType('error');
        }

    };

    return (
        <div className="login-wrapper">
            <header className="topbar-login">
                <Link className="brand" to="/" aria-label="Bancosol inicio">
                    <img src={logoBancosol} alt="Logo Bancosol" className="logo" />
                </Link>

                <nav className="main-nav-login">
                    <Link to={returnRoute} id="role-return-link">Mi panel</Link>
                </nav>
            </header>

            <main className="login-page register-page">
                <section className="login-card register-card edit-card" aria-labelledby="edit-title">
                    <div className="brand-lockup">
                        <img src={Bancosol} alt="Bancosol" className="logo" />
                        <div>
                            <p className="brand-name">Bancosol</p>
                            <p className="brand-subtitle">Editar información de usuario</p>
                        </div>
                    </div>

                    <div className="card-copy">
                        <h2 id="edit-title">Mi perfil</h2>
                    </div>

                    <form id="edit-form" className="login-form register-form edit-form" onSubmit={handleSubmit}>
                        <div className="field-grid">
                            <div className="field-group">
                                <label htmlFor="name">Nombre completo</label>
                                <div className="input-shell readonly-shell">
                                    <input 
                                        id="name" 
                                        name="nombre" 
                                        type="text" 
                                        value={formData.nombre} 
                                        readOnly 
                                        title="Este dato no se puede editar"
                                    />
                                </div>
                                <p className="field-note">Este dato no se puede editar.</p>
                            </div>

                            <div className="field-group">
                                <label htmlFor="email">Correo *</label>
                                <div className="input-shell">
                                    <input 
                                        id="email" 
                                        name="email" 
                                        type="email" 
                                        placeholder="usuario@bancosol.org" 
                                        value={formData.email}
                                        onChange={handleChange}
                                        required 
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label htmlFor="telefono">Teléfono</label>
                                <div className="input-shell">
                                    <input 
                                        id="telefono" 
                                        name="telefono" 
                                        type="tel" 
                                        placeholder="600123123" 
                                        value={formData.telefono}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>

                            <div className="field-group">
                                <label htmlFor="localidad">Localidad</label>
                                <div className="input-shell">
                                    <input 
                                        id="localidad" 
                                        name="localidad" 
                                        type="text" 
                                        placeholder="Málaga" 
                                        value={formData.localidad}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group full-width">
                                <label htmlFor="domicilio">Domicilio</label>
                                <div className="input-shell">
                                    <input 
                                        id="domicilio" 
                                        name="domicilio" 
                                        type="text" 
                                        placeholder="Calle, número, piso..." 
                                        value={formData.domicilio}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label htmlFor="cp">Código postal</label>
                                <div className="input-shell">
                                    <input 
                                        id="cp" 
                                        name="cp" 
                                        type="text" 
                                        placeholder="29001" 
                                        value={formData.cp}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>

                            <p className="help-text full-width">Los cambios se guardan en tu perfil y se aplican a tu sesión actual.</p>
                        </div>

                        <div className="edit-actions">
                            <button type="submit" className="login-button">Guardar cambios</button>
                            <button 
                                type="button" 
                                className="secondary-button" 
                                id="cancel-button"
                                onClick={() => navigate(returnRoute)}
                            >
                                Cancelar
                            </button>
                        </div>

                        {/* Mensaje de estado (Error/Éxito) */}
                        {message && (
                            <p 
                                className={`form-message ${messageType === 'error' ? 'is-error' : 'is-success'}`} 
                                style={{ color: messageType === 'error' ? '#d32f2f' : '#2e7d32', marginTop: '15px' }}
                                role="status" 
                                aria-live="polite"
                            >
                                {message}
                            </p>
                        )}
                    </form>
                </section>
            </main>

            <footer className="site-footer" aria-label="Pie de página">
                <img src={logoBancosol} alt="Logo Bancosol" className="logo" />
                <p>Bancosol · Banco de alimentos</p>
            </footer>
        </div>
    );
}

export default Edit;