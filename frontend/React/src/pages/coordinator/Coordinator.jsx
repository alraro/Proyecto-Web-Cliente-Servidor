import React from 'react';
import { Link } from 'react-router-dom';
function Coordinator() {
return (
    <div>
        <h1>Página de coordinador</h1>
        <Link to="/login">Cerrar sesión</Link> <br></br>

    </div>
);
}

export default Coordinator;