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
        return Array.isArray(data) ? data : (data.value || []);
    } catch { return []; }
}

function showMessage(text, isError) {
    const el = document.getElementById('global-message');
    el.hidden = false;
    el.textContent = text;
    el.className = isError ? 'error' : 'success';
    clearTimeout(showMessage._t);
    showMessage._t = setTimeout(() => { el.hidden = true; }, 4000);
}

function showModalError(text) {
    const el = document.getElementById('modal-error');
    el.hidden = false;
    el.textContent = text;
}

function clearModalError() {
    const el = document.getElementById('modal-error');
    el.hidden = true;
    el.textContent = '';
}

function showModal() {
    const m = document.getElementById('campaign-modal');
    m.classList.remove('hidden');
    m.classList.add('open');
}

function hideModal() {
    const m = document.getElementById('campaign-modal');
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

    const chainSel = document.getElementById('store-filter-chain');
    chainSel.innerHTML = '<option value="">Todas las cadenas</option>';
    chains.forEach(c => { chainSel.insertAdjacentHTML('beforeend', `<option value="${c.id}">${escapeHtml(c.name)}</option>`); });

    const zoneSel = document.getElementById('store-filter-zone');
    zoneSel.innerHTML = '<option value="">Todas las zonas</option>';
    zones.forEach(z => { zoneSel.insertAdjacentHTML('beforeend', `<option value="${z.id}">${escapeHtml(z.name)}</option>`); });

    const localSel = document.getElementById('store-filter-locality');
    localSel.innerHTML = '<option value="">Todas las localidades</option>';
    localities.forEach(l => { localSel.insertAdjacentHTML('beforeend', `<option value="${l.id}">${escapeHtml(l.name)}</option>`); });
}

async function loadAvailableStores() {
    const chainId    = document.getElementById('store-filter-chain').value;
    const zoneId     = document.getElementById('store-filter-zone').value;
    const localityId = document.getElementById('store-filter-locality').value;

    const params = new URLSearchParams();
    if (chainId)    params.append('chainId',    chainId);
    if (zoneId)     params.append('zoneId',     zoneId);
    if (localityId) params.append('localityId', localityId);

    const url = API_BASE + '/api/stores' + (params.toString() ? '?' + params.toString() : '');
    allFilteredStores = await fetchArray(url, { headers: { Authorization: 'Bearer ' + getToken() } });
    renderAvailableList();
}

function renderAvailableList() {
    const ul = document.getElementById('available-stores');
    const available = allFilteredStores.filter(s => !selectedStores.has(Number(s.id)));

    if (!available.length) {
        ul.innerHTML = '<li class="store-list-empty">Sin tiendas disponibles con estos filtros.</li>';
        document.getElementById('available-count').textContent = '0';
        return;
    }

    ul.innerHTML = '';
    available.forEach(s => {
        const chainName    = s.chainName ? escapeHtml(s.chainName) : '-';
        const localityName = s.locality  ? escapeHtml(s.locality)  : '-';
        const li = document.createElement('li');
        li.dataset.storeid = String(s.id);
        li.innerHTML = `<span>${escapeHtml(s.name)} — ${chainName} — ${localityName}</span>
            <button type="button" class="btn-add-store btn btn-sm">+</button>`;
        ul.appendChild(li);
    });
    document.getElementById('available-count').textContent = String(available.length);
}

function renderSelectedList() {
    const ul = document.getElementById('selected-stores');
    const items = [...selectedStores.values()];

    if (!items.length) {
        ul.innerHTML = '<li class="store-list-empty">Sin tiendas seleccionadas.</li>';
        document.getElementById('selected-count').textContent = '0';
        return;
    }

    ul.innerHTML = '';
    items.forEach(s => {
        const chainName = s.chainName ? escapeHtml(s.chainName) : '-';
        const li = document.createElement('li');
        li.dataset.storeid = String(s.id);
        li.innerHTML = `<span>${escapeHtml(s.name)} — ${chainName}</span>
            <button type="button" class="btn-remove-store btn btn-sm btn-danger">×</button>`;
        ul.appendChild(li);
    });
    document.getElementById('selected-count').textContent = String(items.length);
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
    const tbody = document.getElementById('campaigns-tbody');
    if (!campaigns.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay campañas registradas.</td></tr>';
        return;
    }
    tbody.innerHTML = campaigns.map(c => `
        <tr>
            <td><strong>${escapeHtml(c.name || '')}</strong></td>
            <td>${escapeHtml((c.type && c.type.name) ? c.type.name : '-')}</td>
            <td>${formatDate(c.startDate)}</td>
            <td>${formatDate(c.endDate)}</td>
            <td class="actions-cell">
                <button class="btn btn-sm btn-secondary" onclick="openEditModal(${c.id})">Editar</button>
                <button class="btn btn-sm btn-danger" onclick="deleteCampaign(${c.id},'${escapeJs(c.name || '')}')">Eliminar</button>
            </td>
        </tr>`).join('');
}

// ── Modal open/save ─────────────────────────────────────────────────────────

async function openCreateModal() {
    currentCampaignId = null;
    document.getElementById('modal-title').textContent = 'Nueva campaña';
    document.getElementById('campaign-name').value  = '';
    document.getElementById('campaign-type').value  = '';
    document.getElementById('campaign-start').value = '';
    document.getElementById('campaign-end').value   = '';
    selectedStores = new Map();
    clearModalError();
    showModal();
    await loadAvailableStores();
    renderSelectedList();
}

async function openEditModal(id) {
    clearModalError();
    try {
        const c = await fetchJson(API_BASE + '/api/campaigns/' + id, { headers: authHeaders() });
        currentCampaignId = id;
        document.getElementById('modal-title').textContent = 'Editar campaña';
        document.getElementById('campaign-name').value  = c.name  || '';
        document.getElementById('campaign-type').value  = (c.type && c.type.id != null) ? String(c.type.id) : '';
        document.getElementById('campaign-start').value = c.startDate || '';
        document.getElementById('campaign-end').value   = c.endDate   || '';
        selectedStores = new Map();
        try {
            const sd = await fetchJson(API_BASE + '/api/campaigns/' + id + '/stores',
                { headers: { Authorization: 'Bearer ' + getToken() } });
            (sd.stores || []).forEach(s => selectedStores.set(Number(s.id), s));
        } catch (e) { console.error('Error cargando tiendas', e); }
        showModal();
        await loadAvailableStores();
        renderSelectedList();
    } catch (e) {
        showMessage(e.message || 'No se pudo cargar la campaña.', true);
    }
}

async function saveCampaign() {
    const name      = (document.getElementById('campaign-name').value  || '').trim();
    const typeIdRaw = (document.getElementById('campaign-type').value  || '').trim();
    const startDate = (document.getElementById('campaign-start').value || '').trim();
    const endDate   = (document.getElementById('campaign-end').value   || '').trim();
    clearModalError();

    if (!name)      return showModalError('El nombre de la campaña es obligatorio.');
    if (!typeIdRaw) return showModalError('El tipo de campaña es obligatorio.');
    if (!startDate) return showModalError('La fecha de inicio es obligatoria.');
    if (!endDate)   return showModalError('La fecha de fin es obligatoria.');
    if (endDate <= startDate) return showModalError('La fecha de fin debe ser posterior a la de inicio.');

    const body = { name, typeId: parseInt(typeIdRaw, 10), startDate, endDate };
    const isEditing = currentCampaignId != null;

    try {
        const resp = await fetchJson(
            API_BASE + '/api/campaigns' + (isEditing ? '/' + currentCampaignId : ''),
            { method: isEditing ? 'PUT' : 'POST', headers: authHeaders(), body: JSON.stringify(body) }
        );
        const campaignId = resp.campaign ? Number(resp.campaign.id) : currentCampaignId;
        let syncFailed = false;
        if (campaignId) {
            try {
                await fetchJson(API_BASE + '/api/campaigns/' + campaignId + '/stores', {
                    method: 'PUT',
                    headers: authHeaders(),
                    body: JSON.stringify({ storeIds: [...selectedStores.keys()] })
                });
            } catch { syncFailed = true; }
        }
        hideModal();
        showMessage(
            syncFailed ? 'Campaña guardada, pero error al sincronizar tiendas.' :
            isEditing   ? 'Campaña actualizada correctamente.' : 'Campaña creada correctamente.',
            syncFailed
        );
        await loadCampaigns();
    } catch (e) {
        if (e.message && e.message.includes('already exists'))
            return showModalError('Ya existe una campaña con ese nombre.');
        showModalError(e.message || 'No se pudo guardar la campaña.');
    }
}

async function deleteCampaign(id, name) {
    if (!confirm(`¿Seguro que quieres eliminar «${name}»?\nSe eliminarán también todas sus asignaciones.`)) return;
    try {
        await fetchJson(API_BASE + '/api/campaigns/' + id, { method: 'DELETE', headers: authHeaders() });
        showMessage('Campaña eliminada correctamente.', false);
        await loadCampaigns();
    } catch (e) {
        showMessage(e.message || 'No se pudo eliminar la campaña.', true);
    }
}

async function loadCampaignTypes() {
    cachedTypes = await fetchJson(API_BASE + '/api/campaign-types', { headers: authHeaders() });
    const sel = document.getElementById('campaign-type');
    sel.innerHTML = '<option value="">Selecciona un tipo...</option>';
    (cachedTypes || []).forEach(t => {
        sel.insertAdjacentHTML('beforeend', `<option value="${t.id}">${escapeHtml(t.name)}</option>`);
    });
}

// ── Boot ────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', async () => {
    if (!getToken()) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });
    document.getElementById('btn-new').addEventListener('click', openCreateModal);
    document.getElementById('btn-cancel-modal').addEventListener('click', hideModal);
    document.getElementById('btn-save').addEventListener('click', saveCampaign);

    document.getElementById('btn-store-filter').addEventListener('click', loadAvailableStores);
    document.getElementById('btn-store-clear').addEventListener('click', () => {
        ['store-filter-chain','store-filter-zone','store-filter-locality']
            .forEach(id => document.getElementById(id).value = '');
        loadAvailableStores();
    });

    document.getElementById('available-stores').addEventListener('click', e => {
        const btn = e.target.closest('.btn-add-store');
        if (!btn) return;
        const storeId = Number(btn.closest('li').dataset.storeid);
        const store = allFilteredStores.find(s => Number(s.id) === storeId);
        if (store) selectedStores.set(storeId, store);
        renderAvailableList(); renderSelectedList();
    });

    document.getElementById('selected-stores').addEventListener('click', e => {
        const btn = e.target.closest('.btn-remove-store');
        if (!btn) return;
        selectedStores.delete(Number(btn.closest('li').dataset.storeid));
        renderAvailableList(); renderSelectedList();
    });

    document.getElementById('campaign-modal').addEventListener('click', e => {
        if (e.target === document.getElementById('campaign-modal')) hideModal();
    });

    // Make functions available for inline onclick in table rows
    window.openEditModal  = openEditModal;
    window.deleteCampaign = deleteCampaign;

    try {
        await loadCampaignTypes();
        await loadCampaigns();
        await loadStoreFilters();
    } catch (e) {
        showMessage(e.message || 'No se pudieron cargar los datos.', true);
    }
});
