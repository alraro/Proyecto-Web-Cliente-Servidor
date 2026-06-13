import { Link, useNavigate } from 'react-router';
import logoBancosol from '../assets/LOGO_BANCOSOL.png';
import Bancosol from '../assets/Bancosol.png';
import { useState } from 'react';
import './css/login.css';
import './css/register.css';

const ruta = "http://localhost:8080";

function Register() {
    const [message, setMessage] = useState('');
    const navigate = useNavigate();

    const handleFormSubmit = async (e) => {
        e.preventDefault();

        const nombre = e.target.nombre.value.trim();
        const email = e.target.email.value.trim();
        const telefono = e.target.telefono.value.trim();
        const password = e.target.password.value;
        const confirmPassword = e.target.confirmPassword.value;
        const domicilio = e.target.domicilio.value.trim();
        const cp = e.target.cp.value.trim();

        if(!nombre || !email || !password || !confirmPassword) {
            setMessage("Completa los campos obligatorios");
            return;
        }

        if(password.length < 6) {
            setMessage("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if(password !== confirmPassword) {
            setMessage("Las contraseñas no coinciden");
            return;
        }

        try {
            const res = await fetch(`${ruta}/api/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombre, email, telefono, password, domicilio, cp }),
            });

            const data = await res.json();

            if(res.ok) {
                setMessage("Solicitud registrada, espera aprobación del administrador");
                e.target.reset();
                setTimeout(() => navigate('/login'), 1500);
                return;
            }

            setMessage(data.message || "Error al registrar. Intenta nuevamente.");
        } catch (e) {
            console.error(e);
            setMessage("Error de conexión. Intenta nuevamente.");
        }
    };


    return (
        <div className="login-wrapper">
            <header className="topbar-login">
                <Link className="brand" to="/" aria-label="Bancosol inicio">
                    <img src={logoBancosol} alt="Logo Bancosol" className="logo" />
                </Link>

                <nav className="main-nav-login">
                    <Link to="/">Inicio</Link>
                    <Link to="/login">Iniciar sesión</Link>
                </nav>
            </header>

            <main className="login-page register-page">
                <section className="login-card register-card" aria-labelledby="register-title">
                    <div className="brand-lockup">
                        <img src={Bancosol} alt="Bancosol" className="logo" />
                        <div>
                            <p className="brand-name">Bancosol</p>
                            <p className="brand-subtitle">Crear una cuenta nueva</p>
                        </div>
                    </div>

                    <div className="card-copy">
                        <h2 id="register-title">Registro de usuario</h2>
                        <p>Rellena los datos básicos para darte de alta en el sistema.</p>
                    </div>

                    <form className="login-form register-form" onSubmit={handleFormSubmit}>
                        <div className="field-grid">
                            <div className="field-group">
                                <label htmlFor="nombre">Nombre completo *</label>
                                <div className="input-shell">
                                    <input id="nombre" name="nombre" type="text" placeholder="Nombre y apellidos" required />
                                </div>
                            </div>

                            <div className="field-group">
                                <label htmlFor="email">Correo *</label>
                                <div className="input-shell">
                                    <input id="email" name="email" type="email" placeholder="usuario@bancosol.org" required />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label htmlFor="telefono">Teléfono</label>
                                <div className="input-shell">
                                    <input id="telefono" name="telefono" type="tel" placeholder="600123123" />
                                </div>
                            </div>

                            <div className="field-group">
                                <label htmlFor="localidad">Localidad</label>
                                <div className="input-shell">
                                    <input id="localidad" name="localidad" type="text" placeholder="Málaga" />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label htmlFor="password">Contraseña *</label>
                                <div className="input-shell password-shell">
                                    <input 
                                        id="password" 
                                        name="password" 
                                        placeholder="Mínimo 6 caracteres" 
                                        required 
                                    />
                                </div>
                            </div>

                            <div className="field-group">
                                <label htmlFor="confirmPassword">Repetir contraseña *</label>
                                <div className="input-shell password-shell">
                                    <input 
                                        id="confirmPassword" 
                                        name="confirmPassword" 
                                        placeholder="Repite la contraseña" 
                                        required 
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group full-width">
                                <label htmlFor="domicilio">Domicilio</label>
                                <div className="input-shell">
                                    <input id="domicilio" name="domicilio" type="text" placeholder="Calle, número, piso..." />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label htmlFor="cp">Código postal</label>
                                <div className="input-shell">
                                    <input id="cp" name="cp" type="text" placeholder="29001" />
                                </div>
                            </div>

                            <p className="help-text full-width">Los datos se almacenan en la tabla Usuario de PostgreSQL y la contraseña se usa para autenticación inicial del proyecto.</p>
                        </div>

                        {message}

                        <button type="submit" className="login-button">Crear cuenta</button>
                        
                        <p className="auth-switch">¿Ya tienes cuenta? <Link className="forgot-link" to="/login">Iniciar sesión</Link></p>
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

export default Register;