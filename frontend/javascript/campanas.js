const API_BASE = 'http://localhost:8080';

const STATUS = {
    ACTIVE: { label: 'Activa',  css: 'badge-active' },
    FUTURE: { label: 'Futura',  css: 'badge-future' },
    PAST:   { label: 'Pasada',  css: 'badge-past'   }
};

let currentPage   = 0;
let currentStatus = '';
let currentSort   = 'startDate,desc';
const PAGE_SIZE   = 10;

function formatDate(iso) {
    if (!iso) return '—';
    const p = String(iso).split('-');
    return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] : String(iso);
}

async function loadCampaigns() {
    const token = localStorage.getItem('token');
    const tbody = document.getElementById('campaigns-tbody');
    try {
        let url = API_BASE + '/api/campaigns?page=' + currentPage + '&size=' + PAGE_SIZE
                  + '&sort=' + encodeURIComponent(currentSort);
        if (currentStatus) url += '&status=' + encodeURIComponent(currentStatus);

        const res = await fetch(url, { headers: { Authorization: 'Bearer ' + token } });
        if (res.status === 401) { window.location.href = 'login.html'; return; }
        if (!res.ok) throw new Error('No se pudieron cargar las campañas.');

        const data = await res.json();
        renderTable(data.content || []);
        renderPagination(data.pagination || { page:0, totalPages:0, totalElements:0, isFirst:true, isLast:true });
        renderSummary(data.summary   || { totalActive:0, totalFuture:0, totalPast:0 });
    } catch {
        tbody.innerHTML = "<tr><td colspan='5' class='table-empty'>Error al cargar campañas. Inténtalo de nuevo.</td></tr>";
    }
}

function renderTable(campaigns) {
    const tbody = document.getElementById('campaigns-tbody');
    if (!campaigns.length) {
        tbody.innerHTML = "<tr><td colspan='5' class='table-empty'>No hay campañas con los filtros seleccionados.</td></tr>";
        return;
    }
    tbody.innerHTML = campaigns.map(c => {
        const s = STATUS[c.status] || STATUS.ACTIVE;
        return `<tr>
            <td><strong>${c.name || '—'}</strong></td>
            <td>${(c.type && c.type.name) ? c.type.name : '—'}</td>
            <td>${formatDate(c.startDate)}</td>
            <td>${formatDate(c.endDate)}</td>
            <td><span class="${s.css}">${s.label}</span></td>
        </tr>`;
    }).join('');
}

function renderPagination(p) {
    const page  = p.page        || 0;
    const total = p.totalPages  || 0;
    const count = p.totalElements || 0;
    document.getElementById('page-info').textContent =
        'Página ' + (page + 1) + ' de ' + total + ' (' + count + ' resultados)';
    document.getElementById('btn-prev').disabled = Boolean(p.isFirst);
    document.getElementById('btn-next').disabled = Boolean(p.isLast);
}

function renderSummary(s) {
    document.getElementById('chip-active').textContent = 'Activas: '  + (s.totalActive  ?? 0);
    document.getElementById('chip-future').textContent = 'Futuras: '  + (s.totalFuture  ?? 0);
    document.getElementById('chip-past').textContent   = 'Pasadas: '  + (s.totalPast    ?? 0);
}

function updateChips() {
    document.querySelectorAll('.chip').forEach(c => c.classList.remove('chip-selected'));
    const sel = [...document.querySelectorAll('.chip')].find(c => c.dataset.status === currentStatus);
    if (sel) sel.classList.add('chip-selected');
}

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token') || localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }


    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
            
        } else if(e.target.id === 'btn-logout'){
            localStorage.clear();
            window.location.href = 'login.html';
        }
    })



    document.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            currentStatus = chip.dataset.status;
            currentPage   = 0;
            updateChips();
            loadCampaigns();
        });
    });

    document.getElementById('sort-select').addEventListener('change', e => {
        currentSort = e.target.value;
        currentPage = 0;
        loadCampaigns();
    });

    document.getElementById('btn-prev').addEventListener('click', () => { currentPage--; loadCampaigns(); });
    document.getElementById('btn-next').addEventListener('click', () => { currentPage++; loadCampaigns(); });

    loadCampaigns();
});
