import {Link} from 'react-router'
import logoBancosol from '../assets/LOGO_BANCOSOL.png';
import Bancosol from '../assets/Bancosol.png';
import {useState} from 'react'
import './css/login.css'

function Login() {
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');

    const handleFormSubmit = (e) => {
        e.preventDefault();
        setError('Selecciona un rol para iniciar sesión');
    }

    return (
        <div className="login-wrapper">
            <header className="topbar-login">
                <div className="brand">
                    <img src={logoBancosol} alt="Logo de Bancosol" className="logo" />
                
                </div>

                <nav className="main-nav-login">
                    <Link to="/">Inicio</Link>
                </nav>    

            </header>


            <main className="login-page">
                <div className="login-card">
                    
                    <div className="brand-lockup">
                        <img src={Bancosol} alt="Logo de Bancosol" className="logo" />
                        
                        <div>
                            <h3 className="brand-name">Bancosol</h3>
                            <p className="brand-subtitle">Sistema de encargados</p>
                        </div>
                    </div>


                    <div className="card-copy">
                        <h2>Iniciar Sesión</h2>
                        <p>Introduce tus credenciales para acceder a tu panel de control</p>
                    </div>

                    <form className="login-form" onSubmit={handleFormSubmit}>
                        
                        <label htmlFor="email">Correo Electrónico</label>
                        <div className="input-shell">
                            <input type="email" id="email" placeholder="example@bancosol.com" required />
                        </div>

                        <label htmlFor="password">Contraseña</label>
                        <div className="input-shell">
                            <input type={showPassword ? "text" : "password"} id="password" required />
                            
                            <button type="button" className="toggle-password" onClick={() => setShowPassword(!showPassword)}>
                                {showPassword ? "Ocultar" : "Mostrar"}
                            </button>

                        </div>


                        <button type="submit" className="login-button">
                            Entrar
                        </button>
                        
                    </form>

                </div>
            </main>

            <nav className="main-nav-login">
                    <Link to="/admin">Administrador</Link>
                    <Link to="/coordinator">Coordinador</Link>
                    <Link to="/captain">Capitán</Link>
                    <Link to="/colaborator">Colaborador</Link>
                    <Link to="/responsible">Responsable</Link>
            </nav>


            <br></br>

            <footer className="site-footer">
                <img src={logoBancosol} alt="Bancosol Logo" className="logo" />
                <p>© Bancosol Alimentos. Todos los derechos reservados.</p>
            </footer>

            
            
        </div>
    );
}

export default Login;