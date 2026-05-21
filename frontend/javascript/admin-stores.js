document.addEventListener('DOMContentLoaded', () => {
    if (!getToken() || localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }

    
});



function escHtml(v) {
    return String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
// Datos auxiliares
let allChains = [];
let allLocalities = [];
let allZones = [];

async function loadAuxData() {
    try {
        const [resChains, resLoc, resZones] = await Promise.all([
            fetch(API_BASE + '/api/chains', { headers: authHeaders() }),
            fetch(API_BASE + '/api/localities', { headers: authHeaders() }),
            fetch(API_BASE + '/api/zones', { headers: authHeaders() })
        ]);
        if (resChains.ok) allChains = await resChains.json();
        if (resLoc.ok) allLocalities = await resLoc.json();
        if (resZones.ok) allZones = await resZones.json();
    } catch { /* los selects quedan vacíos pero no rompe nada */ }

    const fz = document.querySelector('#filter-zone');
    allZones.forEach(z => {
        const o = document.createElement('option');
        o.value = z.id; o.textContent = z.name;
        fz.appendChild(o);
    });

    populateLocalities('');

    const fc = document.querySelector('#filter-chain');
    const fch = document.querySelector('#input-chain');
    allChains.forEach(c => {
        [fc, fch].forEach(sel => {
            const o = document.createElement('option');
            o.value = c.id; o.textContent = c.name;
            sel.appendChild(o);
        });
    });
}

document.querySelector('#filter-zone').addEventListener('change', function () {
    document.querySelector('#filter-locality').value = '';
    populateLocalities(this.value);
});

function populateLocalities(zoneId) {
    const sel = document.querySelector('#filter-locality');
    const valorActual = sel.value;
    sel.innerHTML = '';
    const o = document.createElement('option');
    o.value = '';
    o.textContent = 'Todas las localidades';
    sel.appendChild(o);
    const lista = zoneId
        ? allLocalities.filter(l => String(l.zoneId) === String(zoneId))
        : allLocalities;
    lista.forEach(l => {
        const opt = document.createElement('option');
        opt.value = l.id; opt.textContent = l.name;
        sel.appendChild(opt);
    });
    if (valorActual && lista.some(l => String(l.id) === String(valorActual))) {
        sel.value = valorActual;
    }
}

// Paginación
let currentPage = 0;
let pageSize = 20;
let totalPages = 1;

function renderTable(stores) {
    const tbody = document.querySelector('#stores-tbody');
    tbody.innerHTML = '';
    if (!stores.length) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 8;
        td.className = 'table-empty';
        td.textContent = 'No hay tiendas que coincidan con los filtros.';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }
    stores.forEach(s => {
        const tr = document.createElement('tr');

        const td1 = document.createElement('td');
        td1.textContent = s.id;
        tr.appendChild(td1);

        const td2 = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = escHtml(s.name);
        td2.appendChild(strong);
        tr.appendChild(td2);

        const td3 = document.createElement('td');
        td3.textContent = escHtml(s.address || '—');
        tr.appendChild(td3);

        const td4 = document.createElement('td');
        td4.textContent = escHtml(s.locality || '—');
        tr.appendChild(td4);

        const td5 = document.createElement('td');
        td5.textContent = escHtml(s.postalCode || '—');
        tr.appendChild(td5);

        const td6 = document.createElement('td');
        td6.textContent = escHtml(s.zone || '—');
        tr.appendChild(td6);

        const td7 = document.createElement('td');
        td7.textContent = escHtml(s.chainName || '—');
        tr.appendChild(td7);

        const td8 = document.createElement('td');
        const div = document.createElement('div');
        div.className = 'td-actions';

        const btnEdit = document.createElement('button');
        btnEdit.className = 'btn btn-edit btn-sm';
        btnEdit.setAttribute('data-action', 'edit');
        btnEdit.setAttribute('data-store-id', s.id);
        btnEdit.textContent = 'Editar';
        div.appendChild(btnEdit);

        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-danger btn-sm';
        btnDelete.setAttribute('data-action', 'delete');
        btnDelete.setAttribute('data-store-id', s.id);
        btnDelete.setAttribute('data-store-name', escapeAttr(s.name));
        btnDelete.textContent = 'Eliminar';
        div.appendChild(btnDelete);

        td8.appendChild(div);
        tr.appendChild(td8);

        tbody.appendChild(tr);
    });
}

async function loadStores(page = 0) {
    const chainId = document.querySelector('#filter-chain').value;
    const localityId = document.querySelector('#filter-locality').value;
    const zoneId = document.querySelector('#filter-zone').value;

    const params = new URLSearchParams();
    if (chainId) params.append('chainId', chainId);
    if (localityId) params.append('localityId', localityId);
    if (zoneId) params.append('zoneId', zoneId);
    params.append('page', page);
    params.append('size', pageSize);

    try {
        const res = await fetch(API_BASE + '/api/stores?' + params, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        const data = await res.json();

        currentPage = page;
        totalPages = data.totalPages || 1;

        document.querySelector('#current-page').textContent = currentPage + 1;
        document.querySelector('#total-pages').textContent = totalPages;
        document.querySelector('#btn-prev-page').disabled = currentPage === 0;
        document.querySelector('#btn-next-page').disabled = currentPage >= totalPages - 1;

        renderTable(data.content || []);
    } catch {
        const tbodyErr = document.querySelector('#stores-tbody');
        tbodyErr.innerHTML = '';
        const trErr = document.createElement('tr');
        const tdErr = document.createElement('td');
        tdErr.colSpan = 8;
        tdErr.className = 'table-empty';
        tdErr.textContent = 'No se puede conectar con el servidor.';
        trErr.appendChild(tdErr);
        tbodyErr.appendChild(trErr);
    }
}

function previousPage() {
    if (currentPage > 0) loadStores(currentPage - 1);
}
function nextPage() {
    if (currentPage < totalPages - 1) loadStores(currentPage + 1);
}
function changePageSize() {
    pageSize = parseInt(document.querySelector('#page-size-select').value);
    loadStores(0);
}

// Filtros
document.querySelector('#btn-apply-filters').addEventListener('click', () => loadStores(0));
document.querySelector('#btn-clear-filters').addEventListener('click', () => {
    document.querySelector('#filter-zone').value = '';
    document.querySelector('#filter-locality').value = '';
    document.querySelector('#filter-chain').value = '';
    populateLocalities('');
    loadStores(0);
});

// Pagination and export buttons
document.querySelector('#btn-prev-page').addEventListener('click', previousPage);
document.querySelector('#btn-next-page').addEventListener('click', nextPage);
document.querySelector('#page-size-select').addEventListener('change', changePageSize);
document.querySelector('#btn-export-stores').addEventListener('click', function () {
    exportarExcel('stores');
});

// Event delegation for table action buttons (edit / delete)
document.querySelector('#stores-tbody').addEventListener('click', function (e) {
    const button = e.target.closest('button');
    if (!button) return;
    const action = button.getAttribute('data-action');
    const storeId = button.getAttribute('data-store-id');
    if (action === 'edit') {
        openEdit(parseInt(storeId));
    } else if (action === 'delete') {
        const storeName = button.getAttribute('data-store-name');
        deleteStore(parseInt(storeId), storeName);
    }
});

let editingId = null;

function openModal(titulo) {
    document.querySelector('#modal-title').textContent = titulo;
    document.querySelector('#modal-error').textContent = '';
    document.querySelector('#modal-backdrop').classList.add('open');
    document.querySelector('#input-nombre').focus();
}
function closeModal() {
    document.querySelector('#modal-backdrop').classList.remove('open');
    document.querySelector('#input-nombre').value = '';
    document.querySelector('#input-domicilio').value = '';
    document.querySelector('#input-cp').value = '';
    document.querySelector('#input-chain').value = '';
    document.querySelector('#modal-error').textContent = '';
    editingId = null;
}

document.querySelector('#btn-nueva-tienda').addEventListener('click', () => {
    editingId = null;
    openModal('Nueva tienda');
});
document.querySelector('#btn-modal-cancel').addEventListener('click', closeModal);
document.querySelector('#modal-backdrop').addEventListener('click', e => {
    if (e.target === document.querySelector('#modal-backdrop')) closeModal();
});

async function openEdit(id) {
    try {
        const res = await fetch(BACKEND + '/api/stores/' + id, { headers: authHeaders() });
        if (!res.ok) { showToast('Error al cargar la tienda.', 'error'); return; }
        const s = await res.json();
        editingId = s.id;
        document.querySelector('#input-nombre').value = s.name || '';
        document.querySelector('#input-domicilio').value = s.address || '';
        document.querySelector('#input-cp').value = s.postalCode || '';
        document.querySelector('#input-chain').value = s.chainId || '';
        openModal('Editar tienda');
    } catch { showToast('Error de conexión.', 'error'); }
}

// Guardar
document.querySelector('#btn-modal-save').addEventListener('click', async () => {
    const nombre = document.querySelector('#input-nombre').value.trim();
    const domicilio = document.querySelector('#input-domicilio').value.trim();
    const cp = document.querySelector('#input-cp').value.trim();
    const chainId = document.querySelector('#input-chain').value;
    const errorEl = document.querySelector('#modal-error');

    if (!nombre) { errorEl.textContent = 'El nombre es obligatorio.'; return; }
    if (nombre.length > 255) { errorEl.textContent = 'El nombre no puede superar 255 caracteres.'; return; }
    if (cp && !/^\d{5}$/.test(cp)) { errorEl.textContent = 'El código postal debe tener exactamente 5 dígitos.'; return; }

    const body = JSON.stringify({ name: nombre, address: domicilio || null, postalCode: cp || null, chainId: chainId ? parseInt(chainId) : null });
    const url = editingId ? `${BACKEND}/api/stores/${editingId}` : `${BACKEND}/api/stores`;
    const method = editingId ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, { method, headers: authHeaders(), body });
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