<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Dashboard de Cobertura</title>
    
    <link rel="stylesheet" href="/css/dashboard.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <script>
        // Configuración global de la API
        const TOKEN_KEY = 'token';
        const API_BASE = 'http://localhost:8080'; 
        
        const getToken = () => localStorage.getItem(TOKEN_KEY);

        function formatDate(dateString) {
            if (!dateString) return '-';
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return dateString;

            return date.toLocaleDateString('es-ES', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
            });
        }

        function authHeaders() {
            return { 'Authorization': `Bearer ${getToken()}`, 'Content-Type': 'application/json' };
        }

        async function apiFetch(url) {
            const res = await fetch(`${API_BASE}${url}`, { headers: authHeaders() });
            if (res.status === 401) { window.location.href = 'login.html'; throw new Error('Unauthorized'); }
            if (res.status === 403) { throw new Error('Acceso denegado'); }
            if (!res.ok) { throw new Error(`Error ${res.status}`); }
            return res.json();
        }

        let charts = {};
        let refreshTimer = null;
        let currentCampaignId = null;

        document.addEventListener('DOMContentLoaded', async () => {
            if (!getToken() || localStorage.getItem('role') !== 'ADMINISTRADOR') {
                window.location.href = 'login.html';
                return;
            }

            await loadCampaigns();

            document.querySelector('#campaignSelect').addEventListener('change', onCampaignChange);
            document.querySelector('#refreshInterval').addEventListener('change', resetTimer);
            document.querySelector('#refreshBtn').addEventListener('click', () => loadMetrics(currentCampaignId));
        });

        async function loadCampaigns() {
            try {
                const campaigns = await apiFetch('/api/dashboard/campaigns');
                const sel = document.querySelector('#campaignSelect');
                
                // Limpiar opciones previas excepto la primera
                sel.innerHTML = '<option value="">Selecciona una campaña</option>';

                campaigns.forEach(c => {
                    const opt = document.createElement('option');
                    opt.value = c.id;

                    const start = formatDate(c.startDate);
                    const end = formatDate(c.endDate);

                    opt.textContent = `${c.name} ${c.active ? '🔄' : '✅'} (${start} → ${end})`;
                    sel.appendChild(opt);
                });

                // Arreglado bug del script original: se valida kpiStatus antes de asignar el valor a kpiChains
                if (document.querySelector('#kpiStatus')) {
                    document.querySelector('#kpiStatus').textContent = campaigns.filter(c => c.active).length + " Activas";
                }
            } catch (e) {
                showError(e.message);
            }
        }

        function onCampaignChange(e) {
            currentCampaignId = e.target.value ? parseInt(e.target.value) : null;
            if (currentCampaignId) {
                loadMetrics(currentCampaignId);
                resetTimer();
            } else {
                showNoSelection();
            }
        }

        async function loadMetrics(campaignId) {
            if (!campaignId) return;
            showLoading(true);
            hideError();

            try {
                const [chainData, localityData, zoneData] = await Promise.all([
                    apiFetch(`/api/dashboard/campaigns/${campaignId}/coverage/chain`),
                    apiFetch(`/api/dashboard/campaigns/${campaignId}/coverage/locality`),
                    apiFetch(`/api/dashboard/campaigns/${campaignId}/coverage/zone`),
                ]);

                updateKPIs(chainData, zoneData);
                renderChart('chainChart', chainData, 'bar', 'Cadenas');
                renderChart('localityChart', localityData, 'bar', 'Localidades');
                renderChart('zoneChart', zoneData, 'horizontalBar', 'Zonas');

                document.querySelector('#kpiRow').classList.remove('hidden');
                document.querySelector('#chartsGrid').classList.remove('hidden');
                document.querySelector('#noSelection').classList.add('hidden');
                document.querySelector('#lastUpdated').textContent = `Actualizado: ${new Date().toLocaleTimeString('es-ES')}`;

            } catch (e) {
                showError(e.message);
            } finally {
                showLoading(false);
            }
        }

        function updateKPIs(chainData, zoneData) {
            const totalStores = chainData.reduce((s, c) => s + c.storesInCampaign, 0);
            const chainsActive = chainData.filter(c => c.storesInCampaign > 0).length;
            const zonesActive = zoneData.filter(z => z.storesInCampaign > 0).length;

            document.querySelector('#kpiStores').textContent = totalStores;
            document.querySelector('#kpiChains').textContent = chainsActive;
            document.querySelector('#kpiZones').textContent = zonesActive;
        }

        function renderChart(canvasId, data, type, dimensionLabel) {
            const canvas = document.querySelector(`#${canvasId}`);
            const labels = data.map(d => d.label);
            const covered = data.map(d => d.storesInCampaign);
            const total = data.map(d => d.totalStores);
            const pct = data.map(d => d.coveragePercent);

            if (charts[canvasId]) { 
                charts[canvasId].destroy(); 
            }

            const isHorizontal = (type === 'horizontalBar');
            charts[canvasId] = new Chart(canvas, {
                type: 'bar',
                data: {
                    labels,
                    datasets: [
                        {
                            label: 'Tiendas en campaña',
                            data: covered,
                            backgroundColor: 'rgba(59,130,246,0.75)',
                            borderColor: 'rgba(59,130,246,1)',
                            borderWidth: 1,
                        },
                        {
                            label: 'Total tiendas',
                            data: total,
                            backgroundColor: 'rgba(209,213,219,0.5)',
                            borderColor: 'rgba(156,163,175,1)',
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
                                    return [`Cobertura: ${pct[i]}%`];
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
            });
        }

        function resetTimer() {
            if (refreshTimer) clearInterval(refreshTimer);
            const ms = parseInt(document.querySelector('#refreshInterval').value);
            if (ms > 0 && currentCampaignId) {
                refreshTimer = setInterval(() => loadMetrics(currentCampaignId), ms);
            }
        }

        function showLoading(on) {
            const el = document.querySelector('#loadingSpinner');
            if (on) { el.classList.remove('hidden'); } else { el.classList.add('hidden'); }
        }

        function showNoSelection() {
            document.querySelector('#kpiRow').classList.add('hidden');
            document.querySelector('#chartsGrid').classList.add('hidden');
            document.querySelector('#noSelection').classList.remove('hidden');
        }

        function showError(msg) {
            const el = document.querySelector('#errorMsg');
            el.textContent = `Error: ${msg}`;
            el.classList.remove('hidden');
        }

        function hideError() {
            document.querySelector('#errorMsg').classList.add('hidden');
        }
    </script>
</head>
<body>


<main class="dashboard-main">

    <div class="page-header">
        <a href="/admin" class="back-link">← Volver al panel</a>
        <h1>Dashboard de Cobertura</h1>
        <p>Visualiza métricas de cobertura por cadena, localidad y zona geográfica.</p>
    </div>

    <section class="controls">
        <label for="campaignSelect">Campaña:</label>
        <select id="campaignSelect">
            <option value="">Selecciona una campaña</option>
        </select>
        
        <label for="refreshInterval">Actualizar cada:</label>
        <select id="refreshInterval">
            <option value="0">Manual</option>
            <option value="30000">30 segundos</option>
            <option value="60000" selected>1 minuto</option>
            <option value="300000">5 minutos</option>
        </select>
        <button id="refreshBtn" class="btn-refresh">↻ Actualizar</button>
        <span id="lastUpdated" class="last-updated"></span>
    </section>

    <section class="kpi-row hidden" id="kpiRow">
        <div class="kpi-card">
            <span class="kpi-label">Tiendas en campaña</span>
            <span class="kpi-value" id="kpiStores">—</span>
        </div>
        <div class="kpi-card">
            <span class="kpi-label">Estado</span>
            <span class="kpi-value" id="kpiStatus">—</span>
        </div>
        <div class="kpi-card">
            <span class="kpi-label">Cadenas cubiertas</span>
            <span class="kpi-value" id="kpiChains">—</span>
        </div>
        <div class="kpi-card">
            <span class="kpi-label">Zonas cubiertas</span>
            <span class="kpi-value" id="kpiZones">—</span>
        </div>
    </section>

    <section class="charts-grid hidden" id="chartsGrid">
        <div class="chart-card">
            <h3>Cobertura por Cadena</h3>
            <div class="chart-wrap"><canvas id="chainChart"></canvas></div>
        </div>
        <div class="chart-card">
            <h3>Cobertura por Localidad</h3>
            <div class="chart-wrap"><canvas id="localityChart"></canvas></div>
        </div>
        <div class="chart-card chart-card--wide">
            <h3>Cobertura por Zona Geográfica</h3>
            <div class="chart-wrap"><canvas id="zoneChart"></canvas></div>
        </div>
    </section>

    <div id="noSelection" class="no-selection">
        Selecciona una campaña para ver las métricas de cobertura.
    </div>
    <div id="loadingSpinner" class="spinner hidden">Cargando datos…</div>
    <div id="errorMsg" class="error-msg hidden"></div>

</main>

</body>
</html>