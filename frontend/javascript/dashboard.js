const TOKEN_KEY = 'token';
const API_BASE = 'http://localhost:8080';
const getToken  = () => localStorage.getItem(TOKEN_KEY);

function formatDate(dateString){
    if (!dateString) return '-';
    const date = new Date(dateString);

    if(isNaN(date.getTime())) return dateString;

    return date.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    })
}

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
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
    if (!getToken()) { window.location.href = 'login.html'; return; }

    const userNameEl = document.getElementById('user-name');
    const btnLogout = document.getElementById('btn-logout');
    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Administrador';
    }
    if (btnLogout) {
        btnLogout.addEventListener('click', logout);
    }

    await loadCampaigns();

    document.getElementById('campaignSelect').addEventListener('change', onCampaignChange);
    document.getElementById('refreshInterval').addEventListener('change', resetTimer);
    document.getElementById('refreshBtn').addEventListener('click', () => loadMetrics(currentCampaignId));
});

// Función para cargar campañas y llenar el select
async function loadCampaigns() {
    try {
        const campaigns = await apiFetch('/api/dashboard/campaigns');
        const sel = document.getElementById('campaignSelect');
        campaigns.forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.id;

            const start = formatDate(c.startDate);
            const end   = formatDate(c.endDate);

            opt.textContent = `${c.name} ${c.active ? '🔄' : '✅'} (${start} → ${end})`;
            sel.appendChild(opt);
        });

        // KPI global de campañas activas
        const actives = campaigns.filter(c => c.active);
        document.getElementById('kpiStatus') && (
            document.getElementById('kpiChains').textContent = campaigns.length
        );
    } catch(e) {
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

// Cargamos datos para mostrar KPIs y gráficos
async function loadMetrics(campaignId) {
    if (!campaignId) return;
    showLoading(true); // Mostramos spinner de carga
    hideError();

    try {
        // Obtenemos los datos por cadena, localidad y zona
        const [chainData, localityData, zoneData] = await Promise.all([
            apiFetch(`/api/dashboard/campaigns/${campaignId}/coverage/chain`),
            apiFetch(`/api/dashboard/campaigns/${campaignId}/coverage/locality`),
            apiFetch(`/api/dashboard/campaigns/${campaignId}/coverage/zone`),
        ]);

        // Mostramos el gráfico de cada una de las coberturas
        updateKPIs(chainData, zoneData);
        renderChart('chainChart',    chainData,    'bar',        'Cadenas');
        renderChart('localityChart', localityData, 'bar',        'Localidades');
        renderChart('zoneChart',     zoneData,     'horizontalBar', 'Zonas');

        // Se muestran contenedores de datos
        document.getElementById('kpiRow').style.display    = 'flex';
        document.getElementById('chartsGrid').style.display = 'grid';
        document.getElementById('noSelection').style.display = 'none';
        document.getElementById('lastUpdated').textContent =
            `Actualizado: ${new Date().toLocaleTimeString('es-ES')}`;

    } catch(e) {
        showError(e.message);
    } finally {
        showLoading(false);
    }
}

function updateKPIs(chainData, zoneData) {
    const totalStores  = chainData.reduce((s, c) => s + c.storesInCampaign, 0);
    const chainsActive = chainData.filter(c => c.storesInCampaign > 0).length;
    const zonesActive  = zoneData.filter(z => z.storesInCampaign > 0).length;

    document.getElementById('kpiStores').textContent = totalStores;
    document.getElementById('kpiChains').textContent = chainsActive;
    document.getElementById('kpiZones').textContent  = zonesActive;
}

// Configuramos y dibujamos el gráfico usando Chart.js
function renderChart(canvasId, data, type, dimensionLabel) {
    const canvas = document.getElementById(canvasId);
    const labels  = data.map(d => d.label);
    const covered = data.map(d => d.storesInCampaign);
    const total   = data.map(d => d.totalStores);
    const pct     = data.map(d => d.coveragePercent);

    // Destruir instancia previa si existe
    if (charts[canvasId]) { charts[canvasId].destroy(); }

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
                    borderColor:     'rgba(59,130,246,1)',
                    borderWidth: 1,
                },
                {
                    label: 'Total tiendas',
                    data: total,
                    backgroundColor: 'rgba(209,213,219,0.5)',
                    borderColor:     'rgba(156,163,175,1)',
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


// Actualizamos por el intervalo seleccionado
function resetTimer() {
    if (refreshTimer) clearInterval(refreshTimer);
    const ms = parseInt(document.getElementById('refreshInterval').value);
    if (ms > 0 && currentCampaignId) {
        refreshTimer = setInterval(() => loadMetrics(currentCampaignId), ms);
    }
}


function showLoading(on) {
    document.getElementById('loadingSpinner').style.display = on ? 'block' : 'none';
}
// Mostramos vista inicial
function showNoSelection() {
    document.getElementById('kpiRow').style.display     = 'none';
    document.getElementById('chartsGrid').style.display = 'none';
    document.getElementById('noSelection').style.display = 'block';
}
function showError(msg) {
    const el = document.getElementById('errorMsg');
    el.textContent = `Error: ${msg}`;
    el.style.display = 'block';
}
function hideError() {
    document.getElementById('errorMsg').style.display = 'none';
}