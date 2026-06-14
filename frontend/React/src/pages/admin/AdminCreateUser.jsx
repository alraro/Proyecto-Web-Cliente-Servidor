import {React} from 'react';
import {Link, useNavigate} from 'react-router';
import {useAuth} from '../auth/useAuthHook'
import { useState } from 'react';
import SecurePage from '../generalModules/SecurePage';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';

import '../css/login.css';
import '../css/register.css';
import { authHeaders } from '../auth/authUtils';

const ruta = "http://localhost:8080";

function CrearUsuario() {
    const navigate = useNavigate();
    const {usuario} = useAuth();

    const [message, setMessage] = useState('');
    
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setMessage('');

        const nombre = e.target.nombre.value.trim();
        const email = e.target.email.value.trim();
        const phone = e.target.phone.value.trim();
        const password = e.target.password.value;
        const confirmPassword = e.target.confirmPassword.value;
        const address = e.target.address.value.trim();
        const postalCode = e.target.postalCode.value.trim();

        if(!nombre || !email || !password || !confirmPassword) {
            setMessage('Completa los campos obligatorios');
            return;
        }

        if(password.length < 6) {
            setMessage('La contraseña debe tener al menos 6 caracteres');
            return;
        }

        if(password !== confirmPassword) {
            setMessage('Las contraseñas no coinciden');
            return;
        }

        try {
            const res = await fetch(`${ruta}/api/auth/admin/users`, {
                method: 'POST',
                headers: authHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify({nombre, email, phone, password, confirmPassword, address, postalCode}),
            });

            const data = await res.json();

            if (res.ok) {
                    setMessage(`Usuario "${nombre}" creado, esperando validación de rol.`);
                    e.target.reset();
                    setTimeout(() => {
                        navigate('/admin');
                    }, 1500);

                    return;
            }

            setMessage(data.message || 'Error al crear el usuario');
        } catch(e) {
            console.log('Error al crear el usuario');
            setMessage('Error al crear el usuario');
        }
    };


    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/admin">← Volver al panel</Link>
                    </nav>

                    <h1>Crear usuario</h1>
                    <p>Crea un nuevo usuario con el rol asignado</p>
                </div>

                <section className="login-card register-card" aria-labelledby="create-user-title">
                    <div className="card-copy">
                        <h2 id="create-user-title">Nuevo usuario</h2>
                    </div>


                    <form className="login-form register-form" onSubmit={handleFormSubmit}>

                        <div className="field-grid">
                            <div className="field-group">
                                <label>Nombre</label>
                                <div className="input-shell">
                                    <input type="text" name="nombre" id="nombre" required />
                                </div>
                            </div>


                            <div className="field-group">
                                <label>Email</label>
                                <div className="input-shell">
                                    <input type="email" name="email" id="email" required />
                                </div>
                            </div>
                        </div>
                        
                        <div className="field-grid">
                            <div className="field-group">
                                <label>Contraseña</label>
                                <div className="input-shell">
                                    <input type="password" name="password" id="password" required />
                                </div>
                            </div>


                            <div className="field-group">
                                <label>Confirmar contraseña</label>
                                <div className="input-shell">
                                    <input type="password" name="confirmPassword" id="confirmPassword" required />
                                </div>
                            </div>
                        </div>

                        <div className="field-grid">
                            <div className="field-group">
                                <label>Telefono</label>
                                <div className="input-shell">
                                    <input type="text" name="phone" id="phone" />
                                </div>
                            </div>

                            <div className="field-group">
                                <label>Código postal</label>
                                <div className="input-shell">
                                    <input id="postalCode" name="postalCode" type="text" placeholder="29001"/>
                                </div>
                            </div>
                        </div>

                         <div className="field-grid">
                            <div className="field-group full-width">
                                <label>Domicilio</label>
                                <div className="input-shell">
                                    <input id="address" name="address" type="text" placeholder="Calle, número, piso..."/>
                                </div>
                            </div>
                        </div>

                        {message}

                        <button type="submit" className="login-button">Crear usuario</button>
                    </form>
                </section>
            </GenericPageWrapper>
        </SecurePage>
    );
}
export default CrearUsuario;