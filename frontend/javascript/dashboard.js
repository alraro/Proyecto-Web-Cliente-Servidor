const API_BASE = 'http://localhost:8080';
const getToken  = () => sessionStorage.getItem('token');

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

function authHeaders() {
    return { 
        'Authorization': `Bearer ${getToken()}`, 
        'Content-Type': 'application/json' 
    };
}

async function apiFetch(url) {
    const res = await fetch(`${API_BASE}${url}`, { 
        headers: authHeaders() 
    });
    
    if (!res.ok) { 
        throw new Error(`Error ${res.status}`); 
    }

    return res.json();
}


let charts = {};
let refreshTimer = null;
let currentCampaignId = null;


document.addEventListener('DOMContentLoaded', async () => {
    if (!getToken() || sessionStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }


    await loadCampaigns();

    document.querySelector('#campaignSelect').addEventListener('change', onCampaignChange);
    document.querySelector('#refreshInterval').addEventListener('change', resetTimer);
    document.querySelector('#refreshBtn').addEventListener('click', () => loadMetrics(currentCampaignId));
});

// Función para cargar campañas y llenar el select
async function loadCampaigns() {
    try {
        const campaigns = await apiFetch('/api/dashboard/campaigns');
        const sel = document.querySelector('#campaignSelect');
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
        document.querySelector('#kpiStatus') && (document.querySelector('#kpiChains').textContent = campaigns.length);
    } catch(e) {
        showError(e.message);
    }
}
// Se ejecuta cuando se cambia la campaña seleccionada
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
        renderChart('chainChart', chainData, 'bar', 'Cadenas');
        renderChart('localityChart', localityData, 'bar', 'Localidades');
        renderChart('zoneChart', zoneData, 'horizontalBar', 'Zonas');

        // Se muestran contenedores de datos
        document.querySelector('#kpiRow').classList.remove('hidden');
        document.querySelector('#chartsGrid').classList.remove('hidden');
        document.querySelector('#noSelection').classList.add('hidden');
        document.querySelector('#lastUpdated').textContent = `Actualizado: ${new Date().toLocaleTimeString('es-ES')}`;

    } catch(e) {
        showError(e.message);
    }
}

function updateKPIs(chainData, zoneData) {
    const totalStores  = chainData.reduce((s, c) => s + c.storesInCampaign, 0);
    const chainsActive = chainData.filter(c => c.storesInCampaign > 0).length;
    const zonesActive  = zoneData.filter(z => z.storesInCampaign > 0).length;

    document.querySelector('#kpiStores').textContent = totalStores;
    document.querySelector('#kpiChains').textContent = chainsActive;
    document.querySelector('#kpiZones').textContent  = zonesActive;
}

// Configuramos y dibujamos el gráfico usando Chart.js
function renderChart(canvasId, data, type, dimensionLabel) {
    const canvas = document.querySelector(`#${canvasId}`);
    const labels  = data.map(d => d.label);
    const covered = data.map(d => d.storesInCampaign);
    const total   = data.map(d => d.totalStores);
    const pct     = data.map(d => d.coveragePercent);

    // Destruir instancia previa si existe
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
    const ms = parseInt(document.querySelector('#refreshInterval').value);
    if (ms > 0 && currentCampaignId) {
        refreshTimer = setInterval(() => loadMetrics(currentCampaignId), ms);
    }
}

// Mostramos vista inicial
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