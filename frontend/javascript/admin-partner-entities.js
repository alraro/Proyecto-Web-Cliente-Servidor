const BACKEND = 'http://localhost:8080';

// ── Auth ──────────────────────────────────────────────────────────────
function getToken() { return localStorage.getItem('token'); }
function authHeaders() {
    return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() };
}
function logout() { localStorage.clear(); window.location.href = 'login.html'; }

if (!getToken()) { window.location.href = 'login.html'; }
const role = (localStorage.getItem('role') || '').toUpperCase();
if (role !== 'ADMINISTRADOR') { window.location.href = 'login.html'; }
document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';
document.getElementById('btn-logout').addEventListener('click', logout);

// ── Toast ─────────────────────────────────────────────────────────────
function showToast(msg, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

// ── Tabla ─────────────────────────────────────────────────────────────
function renderTable(partnerEntities) {
    const tbody = document.getElementById('partner-entities-tbody');
    if (!partnerEntities.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay entidades socias registradas.</td></tr>';
        return;
    }
    tbody.innerHTML = partnerEntities.map(pe => `
        <tr>
            <td>${pe.id}</td>
            <td><strong>${escHtml(pe.name)}</strong></td>
            <td>${escHtml(pe.address || '—')}</td>
            <td>${escHtml(pe.phone || '—')}</td>
            <td>
                <div class="td-actions">
                    <button class="btn btn-edit btn-sm" onclick="abrirEditar(${pe.id})">Editar</button>
                    <button class="btn btn-danger btn-sm" onclick="eliminarEntidad(${pe.id}, '${escAttr(pe.name)}')">Eliminar</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function escHtml(v) {
    return String(v ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
function escAttr(v) {
    return String(v ?? '').replace(/'/g, "\\'");
}

function isValidPhone(phone) {
    if (!phone) return true;

    const normalized = phone.replace(/\s+/g, ' ').trim();
    const phonePattern = /^\+?[0-9()\- ]{7,20}$/;
    const digitsOnly = normalized.replace(/\D/g, '');

    return phonePattern.test(normalized) && digitsOnly.length >= 7 && digitsOnly.length <= 15;
}

// ── Paginación ───────────────────────────────────────────────────────────
let currentPage = 0;
let pageSize = 20;
let totalPages = 1;
let searchQuery = '';
let sortBy = 'id';
let sortOrder = 'asc';

async function cargarEntidades(page = 0) {
    try {
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', pageSize);

        // Agregar búsqueda si existe
        if (searchQuery) {
            params.append('search', searchQuery);
        }

        // Agregar ordenamiento
        if (sortBy && sortOrder) {
            params.append('sort', `${sortBy},${sortOrder}`);
        }

        const res = await fetch(BACKEND + '/api/partner-entities?' + params, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (!res.ok) {
            document.getElementById('partner-entities-tbody').innerHTML =
                '<tr><td colspan="5" class="table-empty">Error al cargar las entidades socias.</td></tr>';
            return;
        }
        const data = await res.json();
        currentPage = page;
        totalPages = data.totalPages || 1;

        // Actualizar controles de paginación
        document.getElementById('current-page').textContent = currentPage + 1;
        document.getElementById('total-pages').textContent = totalPages;
        document.getElementById('btn-prev-page').disabled = currentPage === 0;
        document.getElementById('btn-next-page').disabled = currentPage >= totalPages - 1;

        const items = data.content || [];
        renderTable(Array.isArray(items) ? items : []);
    } catch {
        document.getElementById('partner-entities-tbody').innerHTML =
            '<tr><td colspan="5" class="table-empty">No se puede conectar con el servidor. ¿Está el backend en marcha?</td></tr>';
    }
}

function previousPage() {
    if (currentPage > 0) {
        cargarEntidades(currentPage - 1);
    }
}

function nextPage() {
    if (currentPage < totalPages - 1) {
        cargarEntidades(currentPage + 1);
    }
}

function changePageSize() {
    pageSize = parseInt(document.getElementById('page-size-select').value);
    currentPage = 0;
    cargarEntidades(0);
}

// ── Filtros y búsqueda ────────────────────────────────────────────────
function applyFilters() {
    searchQuery = document.getElementById('filter-search').value.trim();
    sortBy = document.getElementById('filter-sort-by').value;
    sortOrder = document.getElementById('filter-sort-order').value;
    currentPage = 0;
    cargarEntidades(0);
}

function clearFilters() {
    document.getElementById('filter-search').value = '';
    document.getElementById('filter-sort-by').value = 'id';
    document.getElementById('filter-sort-order').value = 'asc';
    searchQuery = '';
    sortBy = 'id';
    sortOrder = 'asc';
    currentPage = 0;
    cargarEntidades(0);
}

document.getElementById('btn-apply-filters').addEventListener('click', applyFilters);
document.getElementById('btn-clear-filters').addEventListener('click', clearFilters);

// Permitir buscar presionando Enter en el campo de búsqueda
document.getElementById('filter-search').addEventListener('keypress', e => {
    if (e.key === 'Enter') {
        applyFilters();
    }
});

// ── Modal ─────────────────────────────────────────────────────────────
let editandoId = null;

function abrirModal(titulo) {
    document.getElementById('modal-title').textContent = titulo;
    document.getElementById('modal-error').textContent = '';
    document.getElementById('modal-backdrop').classList.add('open');
    document.getElementById('input-nombre').focus();
}

function cerrarModal() {
    document.getElementById('modal-backdrop').classList.remove('open');
    document.getElementById('input-nombre').value = '';
    document.getElementById('input-direccion').value = '';
    document.getElementById('input-telefono').value = '';
    document.getElementById('modal-error').textContent = '';
    editandoId = null;
}

function abrirCrear() {
    editandoId = null;
    abrirModal('Nueva entidad socia');
}

async function abrirEditar(id) {
    try {
        const res = await fetch(BACKEND + '/api/partner-entities/' + id, { headers: authHeaders() });
        if (!res.ok) { showToast('Error al cargar la entidad socia.', 'error'); return; }
        const entidad = await res.json();

        editandoId = entidad.id;
        document.getElementById('input-nombre').value = entidad.name || '';
        document.getElementById('input-direccion').value = entidad.address || '';
        document.getElementById('input-telefono').value = entidad.phone || '';
        abrirModal('Editar entidad socia');
    } catch {
        showToast('Error al cargar la entidad socia.', 'error');
    }
}

document.getElementById('btn-nueva').addEventListener('click', abrirCrear);
document.getElementById('btn-cancelar').addEventListener('click', cerrarModal);
document.getElementById('modal-backdrop').addEventListener('click', e => {
    if (e.target === document.getElementById('modal-backdrop')) cerrarModal();
});

// ── Guardar ───────────────────────────────────────────────────────────
document.getElementById('btn-guardar').addEventListener('click', async () => {
    const nombre    = document.getElementById('input-nombre').value.trim();
    const direccion = document.getElementById('input-direccion').value.trim();
    const telefono  = document.getElementById('input-telefono').value.trim();
    const errorEl   = document.getElementById('modal-error');

    // Validación cliente
    if (!nombre) { errorEl.textContent = 'El nombre es obligatorio.'; return; }
    if (nombre.length > 255) { errorEl.textContent = 'El nombre no puede superar 255 caracteres.'; return; }
    if (direccion.length > 500) { errorEl.textContent = 'La dirección no puede superar 500 caracteres.'; return; }
    if (telefono.length > 20) { errorEl.textContent = 'El teléfono no puede superar 20 caracteres.'; return; }
    if (!isValidPhone(telefono)) {
        errorEl.textContent = 'El teléfono debe tener entre 7 y 15 dígitos y solo puede incluir +, espacios, paréntesis o guiones.';
        return;
    }

    const body   = JSON.stringify({
        name: nombre,
        address: direccion || null,
        phone: telefono || null
    });
    const url    = editandoId
        ? `${BACKEND}/api/partner-entities/${editandoId}`
        : `${BACKEND}/api/partner-entities`;
    const method = editandoId ? 'PUT' : 'POST';

    try {
        const res  = await fetch(url, { method, headers: authHeaders(), body });
        const data = await res.json();

        if (!res.ok) {
            errorEl.textContent = data.message || 'Error al guardar.';
            return;
        }

        cerrarModal();
        showToast(editandoId ? 'Entidad socia actualizada correctamente.' : 'Entidad socia creada correctamente.');
        cargarEntidades(0);
    } catch {
        errorEl.textContent = 'Error de conexión con el servidor.';
    }
});

// ── Eliminar ──────────────────────────────────────────────────────────
async function eliminarEntidad(id, nombre) {
    if (!confirm(`¿Eliminar la entidad socia "${nombre}"?\nEsta acción no se puede deshacer.`)) return;
    try {
        const res = await fetch(`${BACKEND}/api/partner-entities/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        if (!res.ok) {
            const data = await res.json();
            showToast(data.message || 'Error al eliminar.', 'error');
            return;
        }
        showToast('Entidad socia eliminada.');
        cargarEntidades(0);
    } catch {
        showToast('Error de conexión con el servidor.', 'error');
    }
}

// ── Init ──────────────────────────────────────────────────────────────
cargarEntidades(0);
