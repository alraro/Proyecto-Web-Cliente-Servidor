import React from 'react';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import SecurePage from '../generalModules/SecurePage';
import { useAuth } from '../auth/useAuthHook';
import {useState, useEffect} from 'react';
import { Link, useNavigate } from 'react-router';

import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend} from "chart.js";
import {Bar} from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

function formatDate(date) {
    if (!date) return '-';

    const dia = new Date(date);
    if(isNaN(dia.getTime())) return date;

    return dia.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function Dashboard() {
    const { usuario } = useAuth();
    const username = usuario?.nombre ?? 'Admin';    
    const role = usuario?.role ?? 'ADMINISTRADOR';
    const token = sessionStorage.getItem('token');

    const [campaigns, setCampaigns] = useState([]);
    const [selectedCampaignId, setSelectedCampaignId] = useState('');
    const [refresh, setRefresh] = useState(60000); // 1 minuto
    const [lasUpdate, setLastUpdate] = useState('');

    const [metrics, setMetrics] = useState({
        chainData: null,
        localityData: null,
        zoneData: null
    });

    const [message, setMessage] = useState('');
    const navigate = useNavigate();


    // Uso useEffect para que se cargen las campañas al iniciar la página
    useEffect(() => {
        async function fetch() {
            try {
                const res = await fetch('/api/campaigns', {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                    }
                });

                if (res.ok) {
                    const data = await res.json();
                    setCampaigns(data);
                    setMessage('Todo bien');
                }

            } catch (error) {
                console.error('Error fetching campaigns:', error);
                setMessage('Error al cargar las campañas');
            }
        }
        fetch();
    }, [token]);



    
    return (
        <SecurePage >
            <GenericPageWrapper headerUsername={username}>
                <Link to="/admin">Volver al panel</Link>
            </GenericPageWrapper>

        </SecurePage>
    );
}
export default Dashboard;