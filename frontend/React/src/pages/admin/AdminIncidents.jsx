import {React} from 'react';
import {Link, useNavigate} from 'react-router';
import {useAuth} from '../auth/useAuthHook'
import { useState, useEffect } from 'react';
import SecurePage from '../generalModules/SecurePage';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';

import { authHeaders } from '../auth/authUtils';

import '../css/common.css';
import '../css/layout.css';

const ruta = "http://localhost:8080";

function Incidencias() {
    const navigate = useNavigate();
    const {usuario} = useAuth();

    const [message, setMessage] = useState('');
    const [incidencias, setIncidencias] = useState([]);

    const incidenciasFetch = async () => {
        try {
            const res = await fetch(`${ruta}/api/admin/incidents`, {
                method: 'GET',
                headers: authHeaders({
                    'Content-Type': 'application/json'
                })
            });

            const data = await res.json();

            if (!res.ok) {
                setMessage('Error al cargar las incidencias');
            }

            setIncidencias(data);
        } catch (e) {
            console.log('Error al obtener incidencias: ', e);
            setMessage('Error al cargar las incidencias');
        }
    };

    useEffect(() => {
        incidenciasFetch();
    }, []); // Se ejecuta solo una vez al montar el componente

    const handleDelete = async (id) => {
        try {
            const res = await fetch(`${ruta}/api/admin/incidents/${id}`, {
                method: 'DELETE',
                headers: authHeaders()
            });

            const data = await res.json();

            if(!res.ok) throw new Error('No se puede eliminar');

            setIncidencias(data);

        } catch(e) {
            alert('Error al intentar eliminar');
        }
    }

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/admin">← Volver al panel</Link>
                    </nav>

                    <h1>Historial de incidencias</h1>
                    <p>Lista de todas las incidencias reportadas</p>
                </div>

                <section className="card">
                    <table border="1">
                        <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Campaña</th>
                                <th>Tienda</th>
                                <th>Reportado por</th>
                                <th>Descripción</th>
                                <th></th>
                            </tr>
                        </thead>

                        <tbody>
                            {message && (
                                <tr>
                                    <td>{message}</td>
                                </tr>
                            )}
                            
                            {incidencias.length === 0 && (
                                <tr>
                                    <td>No hay incidencias registradas</td>
                                </tr>
                            )}
                            {incidencias.length > 0 && incidencias.map((i, index) => (
                                <tr key={index}>
                                    <td>{i.createdAt || "-"}</td>
                                    <td>{i.campaignName || "-"}</td>
                                    <td>{i.storeName || "-"}</td>
                                    <td>{i.captainName || "-"}</td>
                                    <td>{i.description || "-"}</td>
                                    <td>
                                        {i.id ? (
                                            <button onClick={() => handleDelete(i.id)}>Eliminar</button>
                                        ) : "-"}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </section>


            </GenericPageWrapper>
        </SecurePage>
    )


}
export default Incidencias;