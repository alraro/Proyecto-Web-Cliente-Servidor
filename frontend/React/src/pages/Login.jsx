import {Link, useNavigate} from 'react-router'
import logoBancosol from '../assets/LOGO_BANCOSOL.png';
import Bancosol from '../assets/Bancosol.png';
import {useState, useEffect} from 'react'
import {useAuth} from './auth/useAuthHook'
import {getRoleRoute} from './auth/roleRoutes'
import './css/login.css'

function Login() {
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const {login, usuario, estaAutenticado} = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (estaAutenticado && usuario?.role) {
            navigate(getRoleRoute(usuario.role), { replace: true });
        }
    }, [estaAutenticado, usuario, navigate]);

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const email = e.target.email.value.trim();
        const password = e.target.password.value;

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
            });

            if (!res.ok) {
                setError("Credenciales incorrectas");
                return;
            }

            const data = await res.json();

            login({
                nombre: data.nombre,
                role: data.role,
                storeId: data.storeId ?? null
            }, data.token);

            navigate(getRoleRoute(data.role));
        } catch (e) {
            console.error("Login error:", e);
            setError("Error de conexión. Intenta nuevamente.");
        }
    }

    if (estaAutenticado) return null;

    return (
        <div className="login-wrapper">
            <header className="topbar-login">
                <div className="brand">
                    <img src={logoBancosol} alt="Logo de Bancosol" className="logo" />
                </div>

                <nav className="main-nav-login">
                    <Link to="/">Inicio</Link>
                    <Link to="/register">Registrarse</Link>
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
                            <input type="email" id="email" name="email" placeholder="example@bancosol.com" required />
                        </div>

                        <label htmlFor="password">Contraseña</label>
                        <div className="input-shell">
                            <input type={showPassword ? "text" : "password"} id="password" name="password" required />
                            
                            <button type="button" className="toggle-password" onClick={() => setShowPassword(!showPassword)}>
                                {showPassword ? "Ocultar" : "Mostrar"}
                            </button>
                        </div>

                        {error && <p className="login-error">{error}</p>}


                        <button type="submit" className="login-button">
                            Entrar
                        </button>
                        <br></br>
                        <Link to="/register" className="register-link">¿No tienes cuenta? Regístrate</Link>
                        
                    </form>

                </div>
            </main>

            <footer className="site-footer">
                <img src={logoBancosol} alt="Bancosol Logo" className="logo" />
                <p>© Bancosol Alimentos. Todos los derechos reservados.</p>
            </footer>

            
            
        </div>
    );
}

export default Login;
