import React from 'react';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import SecurePage from '../generalModules/SecurePage';
import { useAuth } from '../auth/useAuthHook';
import {useState, useEffect} from 'react';
import { Link, useNavigate } from 'react-router';
import { authHeaders } from '../auth/authUtils';

import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, PointElement, LineElement, RadialLinearScale, Filler, ArcElement, Title, Tooltip, Legend} from "chart.js";
import {Bar, Line} from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, RadialLinearScale, Filler, ArcElement, Title, Tooltip, Legend);

const ruta = "http://localhost:8080";

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

    const [campaigns, setCampaigns] = useState([]);
    const [selectedCampaignId, setSelectedCampaignId] = useState('');
    const [refresh, setRefresh] = useState(60000);
    const [lastUpdate, setLastUpdate] = useState('');

    const [metrics, setMetrics] = useState({
        chainData: null,
        localityData: null,
        zoneData: null
    });

    const navigate = useNavigate();


    // Uso useEffect para que se cargen las campañas al iniciar la página
    useEffect(() => {
        async function fetchCampaigns() {
            try {
                const res = await fetch(`${ruta}/api/dashboard/campaigns`, {
                    headers: authHeaders({ 'Content-Type': 'application/json' })
                });

                if (res.ok) {
                    const data = await res.json();
                    setCampaigns(data);
                }

            } catch (error) {
                console.error('Error fetching campaigns:', error);
            }
        }
        fetchCampaigns();
    }, []);

    // Cargamos las métricas a mostrar
    useEffect(() => {
        if (!selectedCampaignId) return;

        async function fetchMetricas() {
            try {

                const headers = authHeaders({ 'Content-Type': 'application/json' });

                const [chainfetch, localityfetch, zonefetch] = await Promise.all([
                    fetch(`${ruta}/api/dashboard/campaigns/${selectedCampaignId}/coverage/chain`, {headers}),
                    fetch(`${ruta}/api/dashboard/campaigns/${selectedCampaignId}/coverage/locality`, {headers}),
                    fetch(`${ruta}/api/dashboard/campaigns/${selectedCampaignId}/coverage/zone`, {headers})
                ]);

                if (!chainfetch.ok || !localityfetch.ok || !zonefetch.ok) {
                    throw new Error("Error al obtener los datos");
                }

                const chainData = await chainfetch.json();
                const localityData = await localityfetch.json();
                const zoneData = await zonefetch.json();

                setMetrics({
                    chainData,
                    localityData,
                    zoneData
                });

                setLastUpdate(new Date().toLocaleTimeString('es-ES'));
            } catch (e) {
                console.log(e);
            }
        }
        fetchMetricas();

        let timer = null;
        if (refresh > 0) {
            timer = setInterval(fetchMetricas, refresh);
        }

        return () => clearInterval(timer);

    }, [selectedCampaignId, refresh]);


    const totalStores = metrics.chainData ? metrics.chainData.reduce((s, c) => s + c.storesInCampaign, 0) : '-';
    const chainsActive = metrics.chainData ? metrics.chainData.filter(c => c.storesInCampaign > 0).length : '-';
    const zonesActive = metrics.zoneData ? metrics.zoneData.filter(z => z.storesInCampaign > 0).length : '-';

    const generarChart = (data, isHorizontal = false) => {
        if (!data) return null;

        return {
            data: {
                labels: data.map(d => d.label),
                datasets: [
                    {
                        label: 'Tiendas en campaña',
                        data: data.map(d => d.storesInCampaign),
                        backgroundColor: 'rgba(59, 130, 246, 0.75)',
                        borderColor: 'rgba(59, 130, 246, 1)',
                        borderWidth: 1,
                    },
                    {
                        label: 'Total tiendas',
                        data: data.map(d => d.totalStores),
                        backgroundColor: 'rgba(209,213,219,0.5)',
                        borderColor: 'rgba(156, 163, 175, 1)',
                        borderWidth: 1,
                    },
                ]
            },

            options: {
                indexAxis: isHorizontal ? 'y' : 'x',
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    tooltip: {
                        callbacks: {
                            afterBody: (items) => {
                                const i = items[0].dataIndex;
                                return [`Cobertura: ${data[i].coveragePercent}%`];
                            }
                        }
                    },
                    legend: { position: 'top' }
                },
                scales: {
                    x: { stacked: false },
                    y: { stacked: false, beginAtZero: true }
                }
            }
        };
    };

    const chainChart = generarChart(metrics.chainData);
    const localityChart = generarChart(metrics.localityData);
    const zoneChart = generarChart(metrics.zoneData, true);

    return (
        <SecurePage >
            <GenericPageWrapper >
                <main className="dashboard-main">
                    <div className="page-header">
                        <Link to="/admin" className="back-link">← Volver al panel</Link>
                        <h1>Dashboard de cobertura</h1>
                        <p>Visualizar métricas por cadena, localidad y zona geográfica.</p>
                    </div>


                    <section className="controls">
                        <label>Campaña: </label>

                        <select id="campaignSelect" value={selectedCampaignId} onChange={(e) => setSelectedCampaignId(e.target.value)}>

                            <option value="">Selecciona una campaña</option>

                            {campaigns.map((c) => (
                                <option key={c.id} value={c.id}>
                                {c.name} {c.active ? '🔄' : '✅'} ({formatDate(c.startDate)} → {formatDate(c.endDate)})
                                </option>
                            ) )}
                        </select>

                        <label>Actualizar cada: </label>
                        <select id="refres" value={refresh} onChange={(t) => setRefresh(Number(t.target.value))}>
                            <option value="30000">30 segundos</option>
                            <option value="60000">1 minuto</option>
                            <option value="300000">5 minutos</option>
                        </select>
                        {lastUpdate && <span>Actualizado: {lastUpdate}</span>}

                    </section>

                    {!selectedCampaignId && (
                        <div>Selecciona campaña para ver las métricas</div>
                    )}

                    {selectedCampaignId && (
                        <div>
                            <section className="kpi-row">
                                <div className="kpi-card">
                                    <span className="kpi-label">Tiendas en campaña</span>
                                    <span className="kpi-value">{totalStores}</span>
                                </div>
                                <div className="kpi-card">
                                    <span className="kpi-label">Cadenas activas</span>
                                    <span className="kpi-value">{campaigns.length}</span>
                                </div>
                                <div className="kpi-card">
                                    <span className="kpi-label">Cadenas cubiertas</span>
                                    <span className="kpi-value">{chainsActive}</span>
                                </div>
                                <div className="kpi-card">
                                    <span className="kpi-label">Zonas cubiertas</span>
                                    <span className="kpi-value">{zonesActive}</span>
                                </div>
                            </section>


                            <section className="charts-grid">
                                <div className="chart-card">
                                    <h3>Cobertura por cadena</h3>
                                    <div className="chart-wrap">
                                        {chainChart && 
                                            <Bar data={chainChart.data} options={chainChart.options}/>
                                        }
                                    </div>
                                </div>

                                <div className="chart-card">
                                    <h3>Cobertura por localidad</h3>
                                    <div className="chart-wrap">
                                        {localityChart &&
                                            <Line data={localityChart.data} options={localityChart.options}/>
                                        }
                                    </div>
                                </div>

                                <div className="chart-card">
                                    <h3>Cobertura por zona geográfica</h3>
                                    <div className="chart-wrap">
                                        {zoneChart &&
                                            <Line data={zoneChart.data} options={zoneChart.options}/>
                                        }
                                    </div>
                                </div>
                            </section>

                        </div>
                    )}
                </main>
            </GenericPageWrapper>
        </SecurePage>
    );
}
export default Dashboard;