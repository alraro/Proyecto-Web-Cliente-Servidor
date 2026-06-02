
function WelcomeBar({username, role, description}) {
    return (
        <>
        <div className="welcome-bar">
            <div>
                <h2>Bienvenido, {username}</h2>
                <p>{description}</p>
            </div>
            <span className="role-pill">{role}</span>
        </div>
        </>
    )
}

export default WelcomeBar