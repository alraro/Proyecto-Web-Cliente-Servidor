import React from 'react'
import {Link, useNavigate} from 'react-router'
import logoBancosol from '../assets/LOGO_BANCOSOL.png';
import Bancosol from '../assets/Bancosol.png';
import {useState, useEffect} from 'react'
import {useAuth} from './auth/useAuthHook'
import './css/login.css'

const ROLE_ROUTES = {
    ADMINISTRADOR: '/admin',
    COORDINADOR: '/coordinator',
    CAPITAN: '/captain',
    COLABORADOR: '/colaborator',
    RESPONSABLE_TIENDA: '/responsible',
}

function Edit() {
    const navigate = useNavigate();
    const {usuario} = useAuth();
    const role = usuario?.role;

    const [profileData, setProfileData] = useState(null);

    const [message, setMessage] = useState('');
    const [messageType, setMessageType] = useState('');
    const returnRoute = ROLE_ROUTES[role] || '/login';

    // Cargamos los datos del usuario al entrar
    useEffect(() => {
        const loadProfile = async () => {
            const token = sessionStorage.getItem('token');

            if(!token) {
                setMessage('No se ha encontrado una sesión activa para cargar el perfil.');
                setMessageType('error');
                
                setTimeout(() => navigate('/login'), 1500);
                return;
            }

            try {
                const res = await fetch('/api/auth/profile', {
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

                setProfileData(data);
                
            } catch (e) {
                console.log(e);
                setMessage('Error de conexión. Intenta nuevamente.');
                setMessageType('error');
            }
        };

        loadProfile();
    }, [navigate, usuario]);


    const validateForm = () => {
        const email = profileData?.email || '';
        const telefono = profileData?.telefono || '';
        const domicilio = profileData?.domicilio || '';
        const cp = profileData?.cp || '';

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

        const token = sessionStorage.getItem('token');

        if(!token) {
            setMessage('No se ha encontrado una sesión activa para guardar los cambios.');
            setMessageType('error');
            return;
        }

        const data = {
            email: (profileData?.email || '').trim(),
            telefono: (profileData?.telefono || '').trim(),
            domicilio: (profileData?.domicilio || '').trim(),
            cp: (profileData?.cp || '').trim()
        };

        try {
            const res = await fetch('/api/auth/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(data)
            });

            const responsedata = await res.json();

            if(!res.ok) {
                setMessage(responsedata.message || 'Error al actualizar perfil');
                setMessageType('error');
                return;
            }
            
            setMessage('Perfil actualizado correctamente');
            setMessageType('success');

            setTimeout(() => {
                navigate(returnRoute);
            }, 1500);
            return;

        } catch (e) {
            console.log(e);
            setMessage('Error de conexión. Intenta nuevamente.');
            setMessageType('error');
        }

    };

    const handleInputChange = (e) => {
        const {name, value} = e.target;
        setProfileData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    return (
        <div className="login-wrapper">
            <header className="topbar-login">
                <div className="brand">
                    <img src={logoBancosol} alt="Logo Bancosol" className="logo" />
                </div>

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
                                <label>Nombre completo</label>
                                <div className="input-shell readonly-shell">
                                    <label id="name" name="nombre">{profileData?.nombre}</label>
                                </div>
                                <p className="field-note">Este dato no se puede editar.</p>
                            </div>

                            <div className="field-group">
                                <label>Correo *</label>
                                <div className="input-shell">
                                    <input id="email" name="email" type="email" placeholder="usuario@bancosol.info" value={profileData?.email || ''}onChange={handleInputChange}required />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label>Teléfono</label>
                                <div className="input-shell">
                                    <input id="telefono" name="telefono" type="tel" placeholder="123456789" value={profileData?.telefono || ''}onChange={handleInputChange}/>
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group full-width">
                                <label>Domicilio</label>
                                <div className="input-shell">
                                    <input id="domicilio" name="domicilio" type="text" placeholder="Calle, número, piso..." value={profileData?.domicilio || ''}onChange={handleInputChange}/>
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label>Código postal</label>
                                <div className="input-shell">
                                    <input id="cp" name="cp" type="text" placeholder="29001" value={profileData?.cp || ''}onChange={handleInputChange}/>
                                </div>
                            </div>

                            <p className="help-text full-width">Los cambios se guardan en tu perfil y se aplican a tu sesión actual.</p>
                        </div>

                        <div className="edit-actions">
                            <button type="submit" className="login-button">Guardar cambios</button>
                            <button type="button" className="secondary-button" id="cancel-button"onClick={() => navigate(returnRoute)}>Cancelar</button>
                        </div>

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