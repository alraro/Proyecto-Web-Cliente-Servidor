const API_BASE = 'http://localhost:8080';

let currentCampaignId = null;
let selectedStores = new Map();
let allFilteredStores = [];
let cachedTypes = [];

function getToken() { return sessionStorage.getItem('token'); }


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
    clearTimeout(showMessage._t);                   // _t para establecer timeout asociado
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

// Store selector 

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

// Campaign table 

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
        td2.textContent = escapeHtml(c.typeName ? c.typeName : '-');
        tr.appendChild(td2);

        const td3 = document.createElement('td');
        td3.textContent = formatDate(c.startDate);
        tr.appendChild(td3);

        const td4 = document.createElement('td');
        td4.textContent = formatDate(c.endDate);
        tr.appendChild(td4);

        const td5 = document.createElement('td');
        td5.className = 'actions-cell';

        const btnEdit = document.createElement('button');
        btnEdit.className = 'btn btn-sm btn-secondary';
        btnEdit.setAttribute('data-action', 'edit');
        btnEdit.setAttribute('data-campaign-id', c.id);
        btnEdit.textContent = 'Editar';
        td5.appendChild(btnEdit);

        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-sm btn-danger';
        btnDelete.setAttribute('data-action', 'delete');
        btnDelete.setAttribute('data-campaign-id', c.id);
        btnDelete.setAttribute('data-campaign-name', escapeJs(c.name || ''));
        btnDelete.textContent = 'Eliminar';
        td5.appendChild(btnDelete);

        tr.appendChild(td5);
        tbody.appendChild(tr);
    });
}

// Modal open/save

async function openCreateModal() {
    currentCampaignId = null;
    document.querySelector('#modal-title').textContent = 'Nueva campaña';
    document.querySelector('#campaign-name').value  = '';
    document.querySelector('#campaign-type').value  = '';
    document.querySelector('#campaign-start').value = '';
    document.querySelector('#campaign-end').value   = '';
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
        document.querySelector('#modal-title').textContent = 'Editar campaña';
        document.querySelector('#campaign-name').value  = c.name  || '';
        document.querySelector('#campaign-type').value  = (c.typeId != null) ? String(c.typeId) : '';
        document.querySelector('#campaign-start').value = c.startDate || '';
        document.querySelector('#campaign-end').value   = c.endDate   || '';
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
    const name      = (document.querySelector('#campaign-name').value  || '').trim();
    const typeIdRaw = (document.querySelector('#campaign-type').value  || '').trim();
    const startDate = (document.querySelector('#campaign-start').value || '').trim();
    const endDate   = (document.querySelector('#campaign-end').value   || '').trim();
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
    const sel = document.querySelector('#campaign-type');
    sel.innerHTML = '';
    const defaultOpt = document.createElement('option');
    defaultOpt.value = '';
    defaultOpt.textContent = 'Selecciona un tipo...';
    sel.appendChild(defaultOpt);
    (cachedTypes || []).forEach(t => {
        const opt = document.createElement('option');
        opt.value = t.id;
        opt.textContent = escapeHtml(t.name);
        sel.appendChild(opt);
    });
}

// Boot 

document.addEventListener('DOMContentLoaded', async () => {
    if (!getToken() || sessionStorage.getItem('role') !== 'ADMINISTRADOR') { window.location.href = 'login.html'; return; }


    document.querySelector('#btn-new').addEventListener('click', openCreateModal);
    document.querySelector('#btn-cancel-modal').addEventListener('click', hideModal);
    document.querySelector('#btn-save').addEventListener('click', saveCampaign);

    document.querySelector('#btn-store-filter').addEventListener('click', loadAvailableStores);
    document.querySelector('#btn-store-clear').addEventListener('click', () => {
        ['store-filter-chain','store-filter-zone','store-filter-locality']
            .forEach(id => document.querySelector(`#${id}`).value = '');
        loadAvailableStores();
    });

    document.querySelector('#available-stores').addEventListener('click', e => {
        const btn = e.target.closest('.btn-add-store');
        if (!btn) return;
        const storeId = Number(btn.closest('li').dataset.storeid);
        const store = allFilteredStores.find(s => Number(s.id) === storeId);
        if (store) selectedStores.set(storeId, store);
        renderAvailableList(); renderSelectedList();
    });

    document.querySelector('#selected-stores').addEventListener('click', e => {
        const btn = e.target.closest('.btn-remove-store');
        if (!btn) return;
        selectedStores.delete(Number(btn.closest('li').dataset.storeid));
        renderAvailableList(); renderSelectedList();
    });

    document.querySelector('#campaign-modal').addEventListener('click', e => {
        if (e.target === document.querySelector('#campaign-modal')) hideModal();
    });

    // Export button
    document.querySelector('#btn-export-campaigns').addEventListener('click', function () {
        exportarExcel('campaigns');
    });

    // Event delegation for table action buttons (edit / delete)
    document.querySelector('#campaigns-table').addEventListener('click', function (e) {
        const button = e.target.closest('button');
        if (!button) return;
        const action = button.getAttribute('data-action');
        const campaignId = button.getAttribute('data-campaign-id');
        if (action === 'edit') {
            openEditModal(parseInt(campaignId));
        } else if (action === 'delete') {
            const campaignName = button.getAttribute('data-campaign-name');
            deleteCampaign(parseInt(campaignId), campaignName);
        }
    });

    try {
        await loadCampaignTypes();
        await loadCampaigns();
        await loadStoreFilters();
    } catch (e) {
        showMessage(e.message || 'No se pudieron cargar los datos.', true);
    }
});