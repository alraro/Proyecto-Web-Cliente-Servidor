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
        tbody.replaceChildren();
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.setAttribute('colspan', '5');
        td.className = 'table-empty';
        td.textContent = 'Error al cargar campañas. Inténtalo de nuevo.';
        tr.appendChild(td);
        tbody.appendChild(tr);
    }
}

function renderTable(campaigns) {
    const tbody = document.getElementById('campaigns-tbody');
    if (!campaigns.length) {
        tbody.replaceChildren();
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.setAttribute('colspan', '5');
        td.className = 'table-empty';
        td.textContent = 'No hay campañas con los filtros seleccionados.';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }
    tbody.replaceChildren();
    campaigns.forEach(c => {
        const s = STATUS[c.status] || STATUS.ACTIVE;
        const tr = document.createElement('tr');

        const tdName = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = c.name || '—';
        tdName.appendChild(strong);
        tr.appendChild(tdName);

        const tdType = document.createElement('td');
        tdType.textContent = (c.type && c.type.name) ? c.type.name : '—';
        tr.appendChild(tdType);

        const tdStart = document.createElement('td');
        tdStart.textContent = formatDate(c.startDate);
        tr.appendChild(tdStart);

        const tdEnd = document.createElement('td');
        tdEnd.textContent = formatDate(c.endDate);
        tr.appendChild(tdEnd);

        const tdStatus = document.createElement('td');
        const span = document.createElement('span');
        span.className = s.css;
        span.textContent = s.label;
        tdStatus.appendChild(span);
        tr.appendChild(tdStatus);

        tbody.appendChild(tr);
    });
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



    document.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            if (chip.classList.contains('chip-status-all')) {
                currentStatus = '';
            } else if (chip.classList.contains('chip-status-active')) {
                currentStatus = 'ACTIVE';
            } else if (chip.classList.contains('chip-status-future')) {
                currentStatus = 'FUTURE';
            } else if (chip.classList.contains('chip-status-past')) {
                currentStatus = 'PAST';
            }
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
