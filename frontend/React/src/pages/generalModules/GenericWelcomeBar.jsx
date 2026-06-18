import { useAuth } from '../auth/useAuthHook';

function WelcomeBar({ description }) {

    const { usuario } = useAuth();

    const username = usuario?.nombre ?? '----ESTE USUARIO NO TIENE NOMBRE----';    
    const role = usuario?.role ?? '----ESTE USUARIO NO TIENE ROL----';

    return (
        <>
            <div className={`welcome-bar ${role}`}>
                <div>
                    <h2>Bienvenido, {username} 👋</h2>
                    <p>{description}</p>
                </div>
                <span className="role-pill">{role}</span>
            </div>
        </>
    )
}

export default WelcomeBar