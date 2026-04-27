const API_BASE = 'http://localhost:8080';

let currentCampaignId = null;
let hideMessageTimer = null;
let selectedStores = new Map(); // Map<storeId(Number), storeObject>
let allFilteredStores = [];

function getToken() {
    return localStorage.getItem('token');
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getToken()
    };
}

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

function formatDate(isoString) {
    if (!isoString) {
        return '-';
    }
    const parts = String(isoString).split('-');
    if (parts.length !== 3) {
        return String(isoString);
    }
    return parts[2] + '/' + parts[1] + '/' + parts[0];
}

function showMessage(text, isError) {
    const globalMessage = document.getElementById('global-message');
    globalMessage.hidden = false;
    globalMessage.textContent = text;
    globalMessage.classList.remove('success', 'error');
    globalMessage.classList.add(isError ? 'error' : 'success');

    window.clearTimeout(hideMessageTimer);
    hideMessageTimer = window.setTimeout(() => {
        globalMessage.hidden = true;
    }, 4000);
}

function showModalError(text) {
    const modalError = document.getElementById('modal-error');
    modalError.hidden = false;
    modalError.textContent = text;
}

function clearModalError() {
    const modalError = document.getElementById('modal-error');
    modalError.hidden = true;
    modalError.textContent = '';
}

function showModal() {
    const modal = document.getElementById('campaign-modal');
    modal.classList.remove('hidden');
    modal.classList.add('open');
}

function hideModal() {
    const modal = document.getElementById('campaign-modal');
    modal.classList.add('hidden');
    modal.classList.remove('open');
    clearModalError();
}

async function fetchJson(url, options) {
    const response = await fetch(url, options);
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        const error = new Error(data.message || ('Error ' + response.status));
        error.status = response.status;
        throw error;
    }

    return data;
}

async function loadCampaignTypes() {
    const types = await fetchJson(API_BASE + '/api/campaign-types', {
        method: 'GET',
        headers: authHeaders()
    });

    const typeSelect = document.getElementById('campaign-type');
    typeSelect.innerHTML = '<option value="">Selecciona un tipo...</option>';

    (types || []).forEach((type) => {
        const option = document.createElement('option');
        option.value = String(type.id);
        option.textContent = type.name;
        typeSelect.appendChild(option);
    });
}

// NEW
async function loadStoreFilters() {
    try {
        const [chainsRes, zonesRes, localitiesRes] = await Promise.all([
            fetch(API_BASE + '/api/chains', { headers: authHeaders() }),
            fetch(API_BASE + '/api/zones', { headers: authHeaders() }),
            fetch(API_BASE + '/api/localities', { headers: authHeaders() })
        ]);

        const [chains, zones, localities] = await Promise.all([
            chainsRes.json(),
            zonesRes.json(),
            localitiesRes.json()
        ]);

        const chainSelect = document.getElementById('store-filter-chain');
        const zoneSelect = document.getElementById('store-filter-zone');
        const localitySelect = document.getElementById('store-filter-locality');

        chainSelect.innerHTML = '<option value="">Todas las cadenas</option>';
        (chains || []).forEach((item) => {
            const option = document.createElement('option');
            option.value = String(item.id);
            option.textContent = item.name;
            chainSelect.appendChild(option);
        });

        zoneSelect.innerHTML = '<option value="">Todas las zonas</option>';
        (zones || []).forEach((item) => {
            const option = document.createElement('option');
            option.value = String(item.id);
            option.textContent = item.name;
            zoneSelect.appendChild(option);
        });

        localitySelect.innerHTML = '<option value="">Todas las localidades</option>';
        (localities || []).forEach((item) => {
            const option = document.createElement('option');
            option.value = String(item.id);
            option.textContent = item.name;
            localitySelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading store filters', error);
    }
}

// NEW
async function loadAvailableStores() {
    const chainId = document.getElementById('store-filter-chain').value;
    const zoneId = document.getElementById('store-filter-zone').value;
    const localityId = document.getElementById('store-filter-locality').value;

    const params = new URLSearchParams();
    if (chainId) {
        params.append('chainId', chainId);
    }
    if (zoneId) {
        params.append('zoneId', zoneId);
    }
    if (localityId) {
        params.append('localityId', localityId);
    }

    const query = params.toString();
    const url = query ? (API_BASE + '/api/stores?' + query) : (API_BASE + '/api/stores');

    try {
        const res = await fetch(url, { headers: authHeaders() });
        const data = await res.json().catch(() => []);
        allFilteredStores = Array.isArray(data) ? data : [];
    } catch (error) {
        console.error('Error loading available stores', error);
        allFilteredStores = [];
    }
    renderAvailableList();
}

// NEW
function renderAvailableList() {
    const ul = document.getElementById('available-stores');
    const available = allFilteredStores.filter(s => !selectedStores.has(Number(s.id)));

    if (!available.length) {
        ul.innerHTML = '<li class="store-list-empty">Sin tiendas disponibles con estos filtros.</li>';
    } else {
        ul.innerHTML = available.map(s =>
            `<li data-storeid="${s.id}">${escapeHtml(s.name)} — ${escapeHtml(s.chainName || '-')} — ${escapeHtml(s.locality || '-')}
           <button type="button" class="btn-add-store btn btn-sm">+</button></li>`
        ).join('');
    }
    document.getElementById('available-count').textContent = available.length;
}

// NEW
function renderSelectedList() {
    const ul = document.getElementById('selected-stores');
    const items = [...selectedStores.values()];

    if (!items.length) {
        ul.innerHTML = '<li class="store-list-empty">Sin tiendas seleccionadas.</li>';
    } else {
        ul.innerHTML = items.map(s =>
            `<li data-storeid="${s.id}">${escapeHtml(s.name)} — ${escapeHtml(s.chainName || '-')}
           <button type="button" class="btn-remove-store btn btn-danger btn-sm">×</button></li>`
        ).join('');
    }
    document.getElementById('selected-count').textContent = items.length;
}

function renderTable(campaigns) {
    const tbody = document.getElementById('campaigns-tbody');

    if (!campaigns || !campaigns.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay campañas registradas.</td></tr>';
        return;
    }

    tbody.innerHTML = campaigns.map((campaign) => {
        const campaignId = Number(campaign.id);
        const campaignName = String(campaign.name || '');
        const campaignType = campaign.type && campaign.type.name ? campaign.type.name : '-';

        return `
            <tr>
                <td><strong>${escapeHtml(campaignName)}</strong></td>
                <td>${escapeHtml(campaignType)}</td>
                <td>${formatDate(campaign.startDate)}</td>
                <td>${formatDate(campaign.endDate)}</td>
                <td>
                    <div class="actions-cell">
                        <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${campaignId}">Editar</button>
                        <button class="btn btn-sm btn-danger" data-action="delete" data-id="${campaignId}" data-name="${escapeHtml(campaignName)}">Eliminar</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    tbody.querySelectorAll('button[data-action="edit"]').forEach((button) => {
        button.addEventListener('click', () => {
            const id = Number(button.dataset.id);
            openEditModal(id);
        });
    });

    tbody.querySelectorAll('button[data-action="delete"]').forEach((button) => {
        button.addEventListener('click', () => {
            const id = Number(button.dataset.id);
            const name = button.dataset.name || '';
            deleteCampaign(id, name);
        });
    });
}

async function loadCampaigns() {
    const campaigns = await fetchJson(API_BASE + '/api/campaigns', {
        method: 'GET',
        headers: authHeaders()
    });
    renderTable(campaigns);
}

// MODIFIED
async function openCreateModal() {
    currentCampaignId = null;
    document.getElementById('modal-title').textContent = 'Nueva campaña';
    document.getElementById('campaign-name').value = '';
    document.getElementById('campaign-type').value = '';
    document.getElementById('campaign-start').value = '';
    document.getElementById('campaign-end').value = '';
    selectedStores = new Map();
    await loadAvailableStores();
    renderSelectedList();
    clearModalError();
    showModal();
}

async function openEditModal(id) {
    try {
        const campaign = await fetchJson(API_BASE + '/api/campaigns/' + id, {
            method: 'GET',
            headers: authHeaders()
        });

        currentCampaignId = id;
        document.getElementById('modal-title').textContent = 'Editar campaña';
        document.getElementById('campaign-name').value = campaign.name || '';
        document.getElementById('campaign-type').value = campaign.type && campaign.type.id != null
            ? String(campaign.type.id)
            : '';
        document.getElementById('campaign-start').value = campaign.startDate || '';
        document.getElementById('campaign-end').value = campaign.endDate || '';

        selectedStores = new Map();
        try {
            const res = await fetch(API_BASE + '/api/campaigns/' + id + '/stores', { headers: authHeaders() });
            const data = await res.json();
            (data.stores || []).forEach(s => selectedStores.set(Number(s.id), s));
        } catch (e) {
            console.error('Error loading campaign stores', e);
        }
        await loadAvailableStores();
        renderSelectedList();

        clearModalError();
        showModal();
    } catch (error) {
        showMessage(error.message || 'No se pudo cargar la campaña.', true);
    }
}

async function saveCampaign() {
    const name = (document.getElementById('campaign-name').value || '').trim();
    const typeIdRaw = (document.getElementById('campaign-type').value || '').trim();
    const startDate = (document.getElementById('campaign-start').value || '').trim();
    const endDate = (document.getElementById('campaign-end').value || '').trim();

    clearModalError();

    if (!name) {
        showModalError('El nombre de la campaña es obligatorio.');
        return;
    }

    if (!typeIdRaw) {
        showModalError('El tipo de campaña es obligatorio.');
        return;
    }

    if (!startDate) {
        showModalError('La fecha de inicio es obligatoria.');
        return;
    }

    if (!endDate) {
        showModalError('La fecha de fin es obligatoria.');
        return;
    }

    if (endDate <= startDate) {
        showModalError('La fecha de fin debe ser posterior a la fecha de inicio.');
        return;
    }

    const body = {
        name,
        typeId: parseInt(typeIdRaw, 10),
        startDate,
        endDate
    };

    const isEditing = currentCampaignId != null;
    const url = isEditing
        ? API_BASE + '/api/campaigns/' + currentCampaignId
        : API_BASE + '/api/campaigns';
    const method = isEditing ? 'PUT' : 'POST';

    try {
        const data = await fetchJson(url, {
            method,
            headers: authHeaders(),
            body: JSON.stringify(body)
        });

        const campaignId = data.campaign ? data.campaign.id : currentCampaignId;
        await fetch(API_BASE + '/api/campaigns/' + campaignId + '/stores', {
            method: 'PUT',
            headers: authHeaders(),
            body: JSON.stringify({ storeIds: [...selectedStores.keys()] })
        }).catch(() => showMessage('La campaña se guardó pero hubo un error al sincronizar las tiendas.', true));

        hideModal();
        showMessage(isEditing ? 'Campaña actualizada correctamente.' : 'Campaña creada correctamente.', false);
        await loadCampaigns();
    } catch (error) {
        if (error.status === 409) {
            showModalError('Ya existe una campaña con ese nombre.');
            return;
        }
        showModalError(error.message || 'No se pudo guardar la campaña.');
    }
}

async function deleteCampaign(id, name) {
    const confirmed = window.confirm(
        '¿Seguro que quieres eliminar la campaña «' + name + '»?\nSe eliminarán también todas sus asignaciones.'
    );
    if (!confirmed) {
        return;
    }

    try {
        await fetchJson(API_BASE + '/api/campaigns/' + id, {
            method: 'DELETE',
            headers: authHeaders()
        });
        showMessage('Campaña eliminada correctamente.', false);
        await loadCampaigns();
    } catch (error) {
        showMessage(error.message || 'No se pudo eliminar la campaña.', true);
    }
}

function escapeHtml(value) {
    return String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

document.addEventListener('DOMContentLoaded', async () => {
    const token = getToken();
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const userNameEl = document.getElementById('user-name');
    userNameEl.textContent = localStorage.getItem('nombre') || 'Administrador';

    const btnLogout = document.getElementById('btn-logout');
    const btnNew = document.getElementById('btn-new');
    const btnSave = document.getElementById('btn-save');
    const btnCancelModal = document.getElementById('btn-cancel-modal');
    const modal = document.getElementById('campaign-modal');
    const btnStoreFilter = document.getElementById('btn-store-filter');
    const btnStoreClear = document.getElementById('btn-store-clear');
    const availableStores = document.getElementById('available-stores');
    const selectedStoresEl = document.getElementById('selected-stores');

    btnLogout.addEventListener('click', logout);
    btnNew.addEventListener('click', openCreateModal);
    btnSave.addEventListener('click', saveCampaign);
    btnCancelModal.addEventListener('click', hideModal);

    // NEW
    btnStoreFilter.addEventListener('click', loadAvailableStores);
    btnStoreClear.addEventListener('click', () => {
        ['store-filter-chain', 'store-filter-zone', 'store-filter-locality']
            .forEach(id => document.getElementById(id).value = '');
        loadAvailableStores();
    });
    availableStores.addEventListener('click', e => {
        const btn = e.target.closest('.btn-add-store');
        if (!btn) return;
        const storeId = Number(btn.closest('li').dataset.storeid);
        const store = allFilteredStores.find(s => Number(s.id) === storeId);
        if (store) selectedStores.set(storeId, store);
        renderAvailableList();
        renderSelectedList();
    });
    selectedStoresEl.addEventListener('click', e => {
        const btn = e.target.closest('.btn-remove-store');
        if (!btn) return;
        selectedStores.delete(Number(btn.closest('li').dataset.storeid));
        renderAvailableList();
        renderSelectedList();
    });

    modal.addEventListener('click', (event) => {
        if (event.target === modal) {
            hideModal();
        }
    });

    try {
        await loadCampaignTypes();
        await loadCampaigns();
        await loadStoreFilters();
    } catch (error) {
        showMessage(error.message || 'No se pudieron cargar los datos iniciales.', true);
    }
});
