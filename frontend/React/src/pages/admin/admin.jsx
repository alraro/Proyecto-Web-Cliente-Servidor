import React from 'react';
import { Link } from 'react-router-dom';

function Admin() {

    return (
        <div>
            <h1>Página de administrador</h1>
            <Link to="/login">Cerrar sesión</Link> <br></br>
            <Link to="/admin/dashboard">Ir al dashboard</Link>
        </div>
    );
}

export default Admin;
