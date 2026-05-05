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
function renderTable(users) {
    const tbody = document.getElementById('users-tbody');
    if (!users.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay usuarios registrados.</td></tr>';
        return;
    }
    tbody.innerHTML = users.map(u => `
        <tr>
            <td>${u.idUser}</td>
            <td><strong>${escHtml(u.name)}</strong></td>
            <td>${escHtml(u.email || '—')}</td>
            <td>${escHtml(u.phone || '—')}</td>
            <td>${renderRoles(u.roles)}</td>
            <td>
                <div class="td-actions">
                    <button class="btn btn-edit btn-sm" onclick="abrirEditar(${u.idUser}, '${escAttr(u.name)}')">Editar</button>
                    <button class="btn btn-danger btn-sm" onclick="eliminarUsuario(${u.idUser}, '${escAttr(u.name)}')">Eliminar</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderRoles(roles) {
    if (!roles || !roles.length) return '—';
    return roles.map(r => `<span class="role-badge ${r.toLowerCase()}">${getRoleDisplayName(r)}</span>`).join('');
}

function getRoleDisplayName(role) {
    const names = {
        'ADMIN': 'Admin',
        'COORDINATOR': 'Coord.',
        'CAPTAIN': 'Cap.',
        'PARTNER_ENTITY_MANAGER': 'Resp.'
    };
    return names[role] || role;
}

function escHtml(v) {
    return String(v ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
function escAttr(v) {
    return String(v ?? '').replace(/'/g, "\\'");
}

// ── Paginación ───────────────────────────────────────────────────────────
let currentPage = 0;
let pageSize = 20;
let totalPages = 1;
let searchQuery = '';
let sortBy = 'id';
let sortOrder = 'asc';
let roleFilter = '';

async function cargarUsuarios(page = 0) {
    try {
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', pageSize);

        if (searchQuery) {
            params.append('search', searchQuery);
        }

        if (sortBy && sortOrder) {
            params.append('sort', `${sortBy},${sortOrder}`);
        }

        if (roleFilter) {
            params.append('role', roleFilter);
        }

        const res = await fetch(BACKEND + '/api/users?' + params, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (!res.ok) {
            document.getElementById('users-tbody').innerHTML =
                '<tr><td colspan="6" class="table-empty">Error al cargar los usuarios.</td></tr>';
            return;
        }
        const data = await res.json();
        currentPage = page;
        totalPages = data.totalPages || 1;

        document.getElementById('current-page').textContent = currentPage + 1;
        document.getElementById('total-pages').textContent = totalPages;
        document.getElementById('btn-prev-page').disabled = currentPage === 0;
        document.getElementById('btn-next-page').disabled = currentPage >= totalPages - 1;

        const items = data.content || [];
        renderTable(Array.isArray(items) ? items : []);
    } catch {
        document.getElementById('users-tbody').innerHTML =
            '<tr><td colspan="6" class="table-empty">No se puede conectar con el servidor. ¿Está el backend en marcha?</td></tr>';
    }
}

function previousPage() {
    if (currentPage > 0) {
        cargarUsuarios(currentPage - 1);
    }
}

function nextPage() {
    if (currentPage < totalPages - 1) {
        cargarUsuarios(currentPage + 1);
    }
}

function changePageSize() {
    pageSize = parseInt(document.getElementById('page-size-select').value);
    currentPage = 0;
    cargarUsuarios(0);
}

// ── Filtros y búsqueda ────────────────────────────────────────────────
function applyFilters() {
    searchQuery = document.getElementById('filter-search').value.trim();
    sortBy = document.getElementById('filter-sort-by').value;
    sortOrder = document.getElementById('filter-sort-order').value;
    roleFilter = document.getElementById('filter-role').value;
    currentPage = 0;
    cargarUsuarios(0);
}

function clearFilters() {
    document.getElementById('filter-search').value = '';
    document.getElementById('filter-sort-by').value = 'id';
    document.getElementById('filter-sort-order').value = 'asc';
    document.getElementById('filter-role').value = '';
    searchQuery = '';
    sortBy = 'id';
    sortOrder = 'asc';
    roleFilter = '';
    currentPage = 0;
    cargarUsuarios(0);
}

document.getElementById('btn-apply-filters').addEventListener('click', applyFilters);
document.getElementById('btn-clear-filters').addEventListener('click', clearFilters);

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
    document.getElementById('input-email').value = '';
    document.getElementById('input-password').value = '';
    document.getElementById('input-telefono').value = '';
    document.getElementById('input-direccion').value = '';
    document.getElementById('input-codigo-postal').value = '';
    document.getElementById('input-role').value = '';
    document.getElementById('modal-error').textContent = '';
    editandoId = null;
    document.getElementById('form-group-role').style.display = 'block';
    document.getElementById('input-password').required = true;
}

function abrirCrear() {
    editandoId = null;
    abrirModal('Nuevo usuario');
    document.getElementById('input-password').required = true;
    document.getElementById('form-group-role').style.display = 'block';
}

async function abrirEditar(id, nombre) {
    try {
        const res = await fetch(BACKEND + '/api/users/' + id, { headers: authHeaders() });
        if (!res.ok) { showToast('Error al cargar el usuario.', 'error'); return; }
        const usuario = await res.json();

        editandoId = usuario.idUser;
        document.getElementById('input-nombre').value = usuario.name || '';
        document.getElementById('input-email').value = usuario.email || '';
        document.getElementById('input-password').value = '';
        document.getElementById('input-telefono').value = usuario.phone || '';
        document.getElementById('input-direccion').value = usuario.address || '';
        document.getElementById('input-codigo-postal').value = usuario.postalCode || '';
        document.getElementById('input-role').value = '';
        document.getElementById('form-group-role').style.display = 'none';
        document.getElementById('input-password').required = false;
        abrirModal('Editar usuario: ' + nombre);
    } catch {
        showToast('Error al cargar el usuario.', 'error');
    }
}

document.getElementById('btn-nuevo').addEventListener('click', abrirCrear);
document.getElementById('btn-cancelar').addEventListener('click', cerrarModal);
document.getElementById('modal-backdrop').addEventListener('click', e => {
    if (e.target === document.getElementById('modal-backdrop')) cerrarModal();
});

// ── Guardar ───────────────────────────────────────────────────────────
document.getElementById('btn-guardar').addEventListener('click', async () => {
    const nombre    = document.getElementById('input-nombre').value.trim();
    const email     = document.getElementById('input-email').value.trim();
    const password  = document.getElementById('input-password').value;
    const telefono  = document.getElementById('input-telefono').value.trim();
    const direccion = document.getElementById('input-direccion').value.trim();
    const postalCode = document.getElementById('input-codigo-postal').value.trim();
    const rol       = document.getElementById('input-role').value;
    const errorEl   = document.getElementById('modal-error');

    // Validación cliente
    if (!nombre) { errorEl.textContent = 'El nombre es obligatorio.'; return; }
    if (nombre.length > 255) { errorEl.textContent = 'El nombre no puede superar 255 caracteres.'; return; }
    if (!email) { errorEl.textContent = 'El email es obligatorio.'; return; }
    if (email.length > 255) { errorEl.textContent = 'El email no puede superar 255 caracteres.'; return; }

    if (!editandoId) {
        if (!password) { errorEl.textContent = 'La contraseña es obligatoria.'; return; }
        if (!rol) { errorEl.textContent = 'El rol es obligatorio.'; return; }
    }

    if (telefono.length > 20) { errorEl.textContent = 'El teléfono no puede superar 20 caracteres.'; return; }
    if (postalCode.length > 10) { errorEl.textContent = 'El código postal no puede superar 10 caracteres.'; return; }

    let body;
    let url;
    let method;

    if (editandoId) {
        const updateData = {
            name: nombre,
            email: email
        };
        if (telefono) updateData.phone = telefono;
        if (direccion) updateData.address = direccion;
        if (postalCode) updateData.postalCode = postalCode;
        if (password) updateData.password = password;

        body = JSON.stringify(updateData);
        url = `${BACKEND}/api/users/${editandoId}`;
        method = 'PUT';
    } else {
        body = JSON.stringify({
            name: nombre,
            email: email,
            password: password,
            phone: telefono || null,
            address: direccion || null,
            postalCode: postalCode || null,
            role: rol
        });
        url = `${BACKEND}/api/users`;
        method = 'POST';
    }

    try {
        const res  = await fetch(url, { method, headers: authHeaders(), body });
        const data = await res.json();

        if (!res.ok) {
            errorEl.textContent = data.message || 'Error al guardar.';
            return;
        }

        cerrarModal();
        showToast(editandoId ? 'Usuario actualizado correctamente.' : 'Usuario creado correctamente.');
        cargarUsuarios(0);
    } catch {
        errorEl.textContent = 'Error de conexión con el servidor.';
    }
});

// ── Eliminar ──────────────────────────────────────────────────────────
async function eliminarUsuario(id, nombre) {
    if (!confirm(`¿Eliminar el usuario "${nombre}"?\nEsta acción no se puede deshacer.`)) return;
    try {
        const res = await fetch(`${BACKEND}/api/users/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        if (!res.ok) {
            const data = await res.json();
            showToast(data.message || 'Error al eliminar.', 'error');
            return;
        }
        showToast('Usuario eliminado.');
        cargarUsuarios(0);
    } catch {
        showToast('Error de conexión con el servidor.', 'error');
    }
}

// ── Init ──────────────────────────────────────────────────────────────
cargarUsuarios(0);
