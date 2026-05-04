const BACKEND = 'http://localhost:8080';

function getToken()    { return localStorage.getItem('token'); }
function authHeaders() { return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() }; }
function logout()      { localStorage.clear(); window.location.href = 'login.html'; }

if (!getToken()) { window.location.href = 'login.html'; }
document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';
document.getElementById('btn-logout').addEventListener('click', logout);

function showToast(msg, type = 'success') {
    const c = document.getElementById('toast-container');
    const t = document.createElement('div');
    t.className = 'toast toast-' + type;
    t.textContent = msg;
    c.appendChild(t);
    setTimeout(() => t.remove(), 3500);
}

function escHtml(v) {
    return String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
function escAttr(v) { return String(v ?? '').replace(/'/g, "\\'"); }

// Datos auxiliares
let allChains     = [];
let allLocalities = [];
let allZones      = [];

async function loadAuxData() {
    try {
        const [resChains, resLoc, resZones] = await Promise.all([
            fetch(BACKEND + '/api/chains',     { headers: authHeaders() }),
            fetch(BACKEND + '/api/localities', { headers: authHeaders() }),
            fetch(BACKEND + '/api/zones',      { headers: authHeaders() })
        ]);
        if (resChains.ok)  allChains     = await resChains.json();
        if (resLoc.ok)     allLocalities = await resLoc.json();
        if (resZones.ok)   allZones      = await resZones.json();
    } catch { /* los selects quedan vacíos pero no rompe nada */ }

    const fz = document.getElementById('filter-zone');
    allZones.forEach(z => {
        const o = document.createElement('option');
        o.value = z.id; o.textContent = z.name;
        fz.appendChild(o);
    });

    populateLocalities('');

    const fc  = document.getElementById('filter-chain');
    const fch = document.getElementById('input-chain');
    allChains.forEach(c => {
        [fc, fch].forEach(sel => {
            const o = document.createElement('option');
            o.value = c.id; o.textContent = c.name;
            sel.appendChild(o);
        });
    });
}

document.getElementById('filter-zone').addEventListener('change', function () {
    document.getElementById('filter-locality').value = '';
    populateLocalities(this.value);
});

function populateLocalities(zoneId) {
    const sel = document.getElementById('filter-locality');
    const valorActual = sel.value;
    sel.innerHTML = '<option value="">Todas las localidades</option>';
    const lista = zoneId
        ? allLocalities.filter(l => String(l.zoneId) === String(zoneId))
        : allLocalities;
    lista.forEach(l => {
        const o = document.createElement('option');
        o.value = l.id; o.textContent = l.name;
        sel.appendChild(o);
    });
    if (valorActual && lista.some(l => String(l.id) === String(valorActual))) {
        sel.value = valorActual;
    }
}

// Paginación
let currentPage = 0;
let pageSize    = 20;
let totalPages  = 1;

function renderTable(stores) {
    const tbody = document.getElementById('stores-tbody');
    if (!stores.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="table-empty">No hay tiendas que coincidan con los filtros.</td></tr>';
        return;
    }
    tbody.innerHTML = stores.map(s => `
        <tr>
            <td>${s.id}</td>
            <td><strong>${escHtml(s.name)}</strong></td>
            <td>${escHtml(s.address || '—')}</td>
            <td>${escHtml(s.locality || '—')}</td>
            <td>${escHtml(s.postalCode || '—')}</td>
            <td>${escHtml(s.zone || '—')}</td>
            <td>${escHtml(s.chainName || '—')}</td>
            <td>
                <div class="td-actions">
                    <button class="btn btn-edit btn-sm" onclick="openEdit(${s.id})">Editar</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteStore(${s.id}, '${escAttr(s.name)}')">Eliminar</button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function loadStores(page = 0) {
    const chainId    = document.getElementById('filter-chain').value;
    const localityId = document.getElementById('filter-locality').value;
    const zoneId     = document.getElementById('filter-zone').value;

    const params = new URLSearchParams();
    if (chainId)    params.append('chainId',    chainId);
    if (localityId) params.append('localityId', localityId);
    if (zoneId)     params.append('zoneId',     zoneId);
    params.append('page', page);
    params.append('size', pageSize);

    try {
        const res = await fetch(BACKEND + '/api/stores?' + params, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        const data = await res.json();

        currentPage = page;
        totalPages  = data.totalPages || 1;

        document.getElementById('current-page').textContent = currentPage + 1;
        document.getElementById('total-pages').textContent  = totalPages;
        document.getElementById('btn-prev-page').disabled   = currentPage === 0;
        document.getElementById('btn-next-page').disabled   = currentPage >= totalPages - 1;

        renderTable(data.content || []);
    } catch {
        document.getElementById('stores-tbody').innerHTML =
            '<tr><td colspan="8" class="table-empty">No se puede conectar con el servidor.</td></tr>';
    }
}

function previousPage() {
    if (currentPage > 0) loadStores(currentPage - 1);
}
function nextPage() {
    if (currentPage < totalPages - 1) loadStores(currentPage + 1);
}
function changePageSize() {
    pageSize = parseInt(document.getElementById('page-size-select').value);
    loadStores(0);
}

// Filtros
document.getElementById('btn-apply-filters').addEventListener('click', () => loadStores(0));
document.getElementById('btn-clear-filters').addEventListener('click', () => {
    document.getElementById('filter-zone').value     = '';
    document.getElementById('filter-locality').value = '';
    document.getElementById('filter-chain').value    = '';
    populateLocalities('');
    loadStores(0);
});

let editingId = null;

function openModal(titulo) {
    document.getElementById('modal-title').textContent = titulo;
    document.getElementById('modal-error').textContent = '';
    document.getElementById('modal-backdrop').classList.add('open');
    document.getElementById('input-nombre').focus();
}
function closeModal() {
    document.getElementById('modal-backdrop').classList.remove('open');
    document.getElementById('input-nombre').value    = '';
    document.getElementById('input-domicilio').value = '';
    document.getElementById('input-cp').value        = '';
    document.getElementById('input-chain').value     = '';
    document.getElementById('modal-error').textContent = '';
    editingId = null;
}

document.getElementById('btn-nueva-tienda').addEventListener('click', () => {
    editingId = null;
    openModal('Nueva tienda');
});
document.getElementById('btn-modal-cancel').addEventListener('click', closeModal);
document.getElementById('modal-backdrop').addEventListener('click', e => {
    if (e.target === document.getElementById('modal-backdrop')) closeModal();
});

async function openEdit(id) {
    try {
        const res = await fetch(BACKEND + '/api/stores/' + id, { headers: authHeaders() });
        if (!res.ok) { showToast('Error al cargar la tienda.', 'error'); return; }
        const s = await res.json();
        editingId = s.id;
        document.getElementById('input-nombre').value    = s.name       || '';
        document.getElementById('input-domicilio').value = s.address    || '';
        document.getElementById('input-cp').value        = s.postalCode || '';
        document.getElementById('input-chain').value     = s.chainId    || '';
        openModal('Editar tienda');
    } catch { showToast('Error de conexión.', 'error'); }
}

// Guardar
document.getElementById('btn-modal-save').addEventListener('click', async () => {
    const nombre    = document.getElementById('input-nombre').value.trim();
    const domicilio = document.getElementById('input-domicilio').value.trim();
    const cp        = document.getElementById('input-cp').value.trim();
    const chainId   = document.getElementById('input-chain').value;
    const errorEl   = document.getElementById('modal-error');

    if (!nombre) { errorEl.textContent = 'El nombre es obligatorio.'; return; }
    if (nombre.length > 255) { errorEl.textContent = 'El nombre no puede superar 255 caracteres.'; return; }
    if (cp && !/^\d{5}$/.test(cp)) { errorEl.textContent = 'El código postal debe tener exactamente 5 dígitos.'; return; }

    const body   = JSON.stringify({ name: nombre, address: domicilio || null, postalCode: cp || null, chainId: chainId ? parseInt(chainId) : null });
    const url    = editingId ? `${BACKEND}/api/stores/${editingId}` : `${BACKEND}/api/stores`;
    const method = editingId ? 'PUT' : 'POST';

    try {
        const res  = await fetch(url, { method, headers: authHeaders(), body });
        const data = await res.json();
        if (!res.ok) { errorEl.textContent = data.message || 'Error al guardar.'; return; }
        closeModal();
        showToast(editingId ? 'Tienda actualizada.' : 'Tienda creada.');
        loadStores(currentPage);
    } catch { errorEl.textContent = 'Error de conexión con el servidor.'; }
});

async function deleteStore(id, nombre) {
    if (!confirm(`¿Eliminar la tienda "${nombre}"?\nEsta acción no se puede deshacer.`)) return;
    try {
        const res = await fetch(`${BACKEND}/api/stores/${id}`, { method: 'DELETE', headers: authHeaders() });
        if (!res.ok) { const d = await res.json(); showToast(d.message || 'Error al eliminar.', 'error'); return; }
        showToast('Tienda eliminada.');
        loadStores(currentPage);
    } catch { showToast('Error de conexión.', 'error'); }
}

loadAuxData().then(() => loadStores(0));