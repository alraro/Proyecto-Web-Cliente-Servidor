import logoBancosol from '../../assets/LOGO_BANCOSOL.png'

function GenericHeader() {
    return (
        <>
            <header className="topbar">  
            <a className="brand" href="index.html">
                <img src={logoBancosol} alt="Bancosol" className="logo" />
            </a>
            <div className="topbar-right">
                <div className="user-badge">
                    <span className="dot"></span>
                <span id="user-name">NOMBRE GENERICO POR CAMBIAR CON COOKIES</span>
                </div>
                <button className="btn-edit" id="btn-edit">Editar perfil 🖉</button>
                <button className="btn-logout" id="btn-logout">Cerrar sesión x</button>
            </div>
            </header>
        </>
    );
}

export default GenericHeader;