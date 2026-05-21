const API_BASE = 'http://localhost:8080';

let currentCampaignId = null;
let selectedStores = new Map();
let allFilteredStores = [];
let cachedTypes = [];

function getToken() { return localStorage.getItem('token'); }


function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getToken()
    };
}

function formatDate(iso) {
    if (!iso) return '-';
    const p = String(iso).split('-');
    return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] : String(iso);
}

function escapeHtml(v) {
    return String(v || '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function escapeJs(v) {
    return String(v || '')
        .replace(/\\/g, '\\\\').replace(/'/g, "\\'")
        .replace(/\r/g, ' ').replace(/\n/g, ' ');
}

async function fetchJson(url, options) {
    const res = await fetch(url, options);
    const data = await res.json().catch(() => ({}));
    if (res.status === 401 || res.status === 403) throw new Error('Tu sesión no es válida o ha expirado.');
    if (!res.ok) throw new Error(data.message || 'Error ' + res.status);
    return data;
}

async function fetchArray(url, options) {
    try {
        const res = await fetch(url, options);
        if (!res.ok) return [];
        const data = await res.json().catch(() => []);
        return Array.isArray(data) ? data : (data.content || data.value || []);
    } catch { return []; }
}

function showMessage(text, isError) {
    const el = document.querySelector('#global-message');
    el.hidden = false;
    el.textContent = text;
    el.className = isError ? 'error' : 'success';
    clearTimeout(showMessage._t);
    showMessage._t = setTimeout(() => { el.hidden = true; }, 4000);
}

function showModalError(text) {
    const el = document.querySelector('#modal-error');
    el.hidden = false;
    el.textContent = text;
}

function clearModalError() {
    const el = document.querySelector('#modal-error');
    el.hidden = true;
    el.textContent = '';
}

function showModal() {
    const m = document.querySelector('#campaign-modal');
    m.classList.remove('hidden');
    m.classList.add('open');
}

function hideModal() {
    const m = document.querySelector('#campaign-modal');
    m.classList.add('hidden');
    m.classList.remove('open');
    clearModalError();
}

// ── Store selector ─────────────────────────────────────────────────────────

async function loadStoreFilters() {
    const opts = { headers: { Authorization: 'Bearer ' + getToken() } };
    const [chains, zones, localities] = await Promise.all([
        fetchArray(API_BASE + '/api/chains', opts),
        fetchArray(API_BASE + '/api/zones', opts),
        fetchArray(API_BASE + '/api/localities', opts)
    ]);

    const chainSel = document.querySelector('#store-filter-chain');
    chainSel.innerHTML = '';
    const chainDefault = document.createElement('option');
    chainDefault.value = '';
    chainDefault.textContent = 'Todas las cadenas';
    chainSel.appendChild(chainDefault);
    chains.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = escapeHtml(c.name);
        chainSel.appendChild(opt);
    });

    const zoneSel = document.querySelector('#store-filter-zone');
    zoneSel.innerHTML = '';
    const zoneDefault = document.createElement('option');
    zoneDefault.value = '';
    zoneDefault.textContent = 'Todas las zonas';
    zoneSel.appendChild(zoneDefault);
    zones.forEach(z => {
        const opt = document.createElement('option');
        opt.value = z.id;
        opt.textContent = escapeHtml(z.name);
        zoneSel.appendChild(opt);
    });

    const localSel = document.querySelector('#store-filter-locality');
    localSel.innerHTML = '';
    const localDefault = document.createElement('option');
    localDefault.value = '';
    localDefault.textContent = 'Todas las localidades';
    localSel.appendChild(localDefault);
    localities.forEach(l => {
        const opt = document.createElement('option');
        opt.value = l.id;
        opt.textContent = escapeHtml(l.name);
        localSel.appendChild(opt);
    });
}

async function loadAvailableStores() {
    const chainId    = document.querySelector('#store-filter-chain').value;
    const zoneId     = document.querySelector('#store-filter-zone').value;
    const localityId = document.querySelector('#store-filter-locality').value;

    const params = new URLSearchParams();
    if (chainId)    params.append('chainId',    chainId);
    if (zoneId)     params.append('zoneId',     zoneId);
    if (localityId) params.append('localityId', localityId);

    params.append('size', '100');
    const url = API_BASE + '/api/stores?' + params.toString();
    allFilteredStores = await fetchArray(url, { headers: { Authorization: 'Bearer ' + getToken() } });
    renderAvailableList();
}

function renderAvailableList() {
    const ul = document.querySelector('#available-stores');
    const available = allFilteredStores.filter(s => !selectedStores.has(Number(s.id)));

    if (!available.length) {
        ul.innerHTML = '';
        const li = document.createElement('li');
        li.className = 'store-list-empty';
        li.textContent = 'Sin tiendas disponibles con estos filtros.';
        ul.appendChild(li);
        document.querySelector('#available-count').textContent = '0';
        return;
    }

    ul.innerHTML = '';
    available.forEach(s => {
        const chainName    = s.chainName ? escapeHtml(s.chainName) : '-';
        const localityName = s.locality  ? escapeHtml(s.locality)  : '-';
        const li = document.createElement('li');
        li.dataset.storeid = String(s.id);
        const span = document.createElement('span');
        span.textContent = escapeHtml(s.name) + ' — ' + chainName + ' — ' + localityName;
        li.appendChild(span);
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn-add-store btn btn-sm';
        btn.textContent = '+';
        li.appendChild(btn);
        ul.appendChild(li);
    });
    document.querySelector('#available-count').textContent = String(available.length);
}

function renderSelectedList() {
    const ul = document.querySelector('#selected-stores');
    const items = [...selectedStores.values()];

    if (!items.length) {
        ul.innerHTML = '';
        const li = document.createElement('li');
        li.className = 'store-list-empty';
        li.textContent = 'Sin tiendas seleccionadas.';
        ul.appendChild(li);
        document.querySelector('#selected-count').textContent = '0';
        return;
    }

    ul.innerHTML = '';
    items.forEach(s => {
        const chainName = s.chainName ? escapeHtml(s.chainName) : '-';
        const li = document.createElement('li');
        li.dataset.storeid = String(s.id);
        const span = document.createElement('span');
        span.textContent = escapeHtml(s.name) + ' — ' + chainName;
        li.appendChild(span);
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn-remove-store btn btn-sm btn-danger';
        btn.textContent = '×';
        li.appendChild(btn);
        ul.appendChild(li);
    });
    document.querySelector('#selected-count').textContent = String(items.length);
}

// ── Campaign table ──────────────────────────────────────────────────────────

async function loadCampaigns() {
    const data = await fetchJson(API_BASE + '/api/campaigns?size=200&sort=startDate,desc', {
        headers: authHeaders()
    });
    const list = Array.isArray(data) ? data : (data.content || []);
    renderTable(list);
}

function renderTable(campaigns) {
    const tbody = document.querySelector('#campaigns-tbody');
    tbody.innerHTML = '';
    if (!campaigns.length) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 5;
        td.className = 'table-empty';
        td.textContent = 'No hay campañas registradas.';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }
    campaigns.forEach(c => {
        const tr = document.createElement('tr');

        const td1 = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = escapeHtml(c.name || '');
        td1.appendChild(strong);
        tr.appendChild(td1);

        const td2 = document.createElement('td');
        td2.textContent = escapeHtml((c.type && c.type.name) ? c.type.name : '-');
        tr.appendChild(td2);

        const td3 = document.createElement('td');
        td3.textContent = formatDate(c.startDate);
        tr.appendChild(td3);

        const td4 = document.createElement('td');
        td4.textContent = formatDate(c.endDate);
        tr.appendChild(td4);

        const td5 = document.createElement('td');
        const div = document.createElement('div');
        div.className = 'td-actions';

        const editButton = document.createElement('button');
        editButton.className = 'btn btn-edit btn-sm';
        editButton.textContent = 'Editar';
        editButton.setAttribute('data-action', 'edit');
        editButton.setAttribute('data-id', c.id);
        div.appendChild(editButton);

        const deleteButton = document.createElement('button');
        deleteButton.className = 'btn btn-danger btn-sm';
        deleteButton.textContent = 'Eliminar';
        deleteButton.setAttribute('data-action', 'delete');
        deleteButton.setAttribute('data-id', c.id);
        div.appendChild(deleteButton);

        td5.appendChild(div);
        tr.appendChild(td5);

        tbody.appendChild(tr);
    });
}

function resetModal() {
    currentCampaignId = null;
    document.querySelector('#campaign-name').value = '';
    document.querySelector('#campaign-type').value = '';
    document.querySelector('#campaign-start').value = '';
    document.querySelector('#campaign-end').value = '';
    selectedStores.clear();
    renderSelectedList();
    renderAvailableList();
    clearModalError();
}

function mapCampaignPayload() {
    const name = document.querySelector('#campaign-name').value.trim();
    const type = document.querySelector('#campaign-type').value;
    const startDate = document.querySelector('#campaign-start').value;
    const endDate = document.querySelector('#campaign-end').value;
    const storeIds = [...selectedStores.keys()];

    if (!name) throw new Error('El nombre es obligatorio.');
    if (!type) throw new Error('El tipo es obligatorio.');
    if (!startDate) throw new Error('La fecha de inicio es obligatoria.');
    if (!endDate) throw new Error('La fecha de fin es obligatoria.');
    if (new Date(startDate) > new Date(endDate)) throw new Error('La fecha de inicio no puede ser posterior a la fecha de fin.');
    if (!storeIds.length) throw new Error('Debes seleccionar al menos una tienda.');

    return { name, typeId: Number(type), startDate, endDate, storeIds };
}

async function saveCampaign() {
    try {
        const payload = mapCampaignPayload();
        const method = currentCampaignId ? 'PUT' : 'POST';
        const url = currentCampaignId
            ? API_BASE + '/api/campaigns/' + currentCampaignId
            : API_BASE + '/api/campaigns';

        const data = await fetchJson(url, {
            method,
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });

        showMessage(data.message || 'Campaña guardada correctamente.');
        hideModal();
        resetModal();
        loadCampaigns();
    } catch (err) {
        showModalError(err.message || 'No se pudo guardar la campaña.');
    }
}

async function editCampaign(id) {
    try {
        const data = await fetchJson(API_BASE + '/api/campaigns/' + id, { headers: authHeaders() });
        currentCampaignId = data.id;
        document.querySelector('#campaign-name').value = data.name || '';
        document.querySelector('#campaign-type').value = data.type?.id || '';
        document.querySelector('#campaign-start').value = data.startDate || '';
        document.querySelector('#campaign-end').value = data.endDate || '';
        selectedStores.clear();
        (data.stores || []).forEach(s => selectedStores.set(Number(s.id), s));
        renderSelectedList();
        renderAvailableList();
        showModal();
    } catch (err) {
        showMessage(err.message || 'No se pudo cargar la campaña.', true);
    }
}

async function deleteCampaign(id) {
    if (!confirm('¿Eliminar esta campaña? Esta acción no se puede deshacer.')) return;
    try {
        await fetchJson(API_BASE + '/api/campaigns/' + id, {
            method: 'DELETE',
            headers: authHeaders()
        });
        showMessage('Campaña eliminada correctamente.');
        loadCampaigns();
    } catch (err) {
        showMessage(err.message || 'No se pudo eliminar la campaña.', true);
    }
}

function attachModalEvents() {
    document.querySelector('#btn-new').addEventListener('click', () => {
        resetModal();
        showModal();
    });
    document.querySelector('#btn-cancel-modal').addEventListener('click', () => {
        hideModal();
        resetModal();
    });
    document.querySelector('#btn-save').addEventListener('click', saveCampaign);

    document.querySelector('#btn-store-filter').addEventListener('click', loadAvailableStores);
    document.querySelector('#btn-store-clear').addEventListener('click', () => {
        document.querySelector('#store-filter-chain').value = '';
        document.querySelector('#store-filter-zone').value = '';
        document.querySelector('#store-filter-locality').value = '';
        loadAvailableStores();
    });
}

function attachTableEvents() {
    document.querySelector('#campaigns-tbody').addEventListener('click', event => {
        const btn = event.target.closest('button');
        if (!btn) return;
        const action = btn.getAttribute('data-action');
        const id = btn.getAttribute('data-id');
        if (action === 'edit') editCampaign(id);
        if (action === 'delete') deleteCampaign(id);
    });
}

async function loadTypes() {
    cachedTypes = await fetchArray(API_BASE + '/api/campaign-types', { headers: authHeaders() });
    const select = document.querySelector('#campaign-type');
    select.innerHTML = '<option value="">Selecciona un tipo...</option>';
    cachedTypes.forEach(t => {
        const opt = document.createElement('option');
        opt.value = t.id;
        opt.textContent = escapeHtml(t.name);
        select.appendChild(opt);
    });
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!getToken() || localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }

    try {
        await loadTypes();
        await loadStoreFilters();
        await loadAvailableStores();
        attachModalEvents();
        attachTableEvents();
        loadCampaigns();
    } catch (err) {
        showMessage(err.message || 'No se pudo cargar la pantalla.', true);
    }
});
