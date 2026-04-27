const BACKEND = 'http://localhost:8080';

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

if (!getToken()) {
    window.location.href = 'login.html';
}

const role = (localStorage.getItem('role') || '').toUpperCase();
if (role !== 'ADMINISTRADOR') {
    window.location.href = 'login.html';
}

document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';
document.getElementById('btn-logout').addEventListener('click', logout);

function showToast(msg, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

function escHtml(v) {
    return String(v ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
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

function isValidEmail(email) {
    return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email);
}

let currentPage = 0;
let pageSize = 20;
let totalPages = 1;
let searchQuery = '';
let sortBy = 'id';
let sortOrder = 'asc';
let editingUserId = null;

function renderTable(managers) {
    const tbody = document.getElementById('managers-tbody');

    if (!managers.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay responsables de entidad colaboradora registrados.</td></tr>';
        return;
    }

    tbody.innerHTML = managers.map(m => `
        <tr>
            <td>${m.userId}</td>
            <td><strong>${escHtml(m.name || '—')}</strong></td>
            <td>${escHtml(m.email || '—')}</td>
            <td>${escHtml(m.phone || '—')}</td>
            <td>${escHtml(m.partnerEntityName || (m.partnerEntityId ? 'ID ' + m.partnerEntityId : 'Sin asignar'))}</td>
            <td>
                <div class="td-actions">
                    <button class="btn btn-edit btn-sm" onclick="abrirEditar(${m.userId})">Editar</button>
                    <button class="btn btn-danger btn-sm" onclick="quitarRol(${m.userId}, '${escAttr(m.name || '')}')">Quitar rol</button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function cargarManagers(page = 0) {
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

        const res = await fetch(BACKEND + '/api/partner-entity-managers?' + params, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) {
            logout();
            return;
        }

        if (!res.ok) {
            document.getElementById('managers-tbody').innerHTML =
                '<tr><td colspan="6" class="table-empty">Error al cargar los responsables.</td></tr>';
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
        document.getElementById('managers-tbody').innerHTML =
            '<tr><td colspan="6" class="table-empty">No se puede conectar con el servidor. ¿Está el backend en marcha?</td></tr>';
    }
}

function previousPage() {
    if (currentPage > 0) {
        cargarManagers(currentPage - 1);
    }
}

function nextPage() {
    if (currentPage < totalPages - 1) {
        cargarManagers(currentPage + 1);
    }
}

function changePageSize() {
    pageSize = parseInt(document.getElementById('page-size-select').value, 10);
    currentPage = 0;
    cargarManagers(0);
}

function applyFilters() {
    searchQuery = document.getElementById('filter-search').value.trim();
    sortBy = document.getElementById('filter-sort-by').value;
    sortOrder = document.getElementById('filter-sort-order').value;
    currentPage = 0;
    cargarManagers(0);
}

function clearFilters() {
    document.getElementById('filter-search').value = '';
    document.getElementById('filter-sort-by').value = 'id';
    document.getElementById('filter-sort-order').value = 'asc';

    searchQuery = '';
    sortBy = 'id';
    sortOrder = 'asc';
    currentPage = 0;
    cargarManagers(0);
}

document.getElementById('btn-apply-filters').addEventListener('click', applyFilters);
document.getElementById('btn-clear-filters').addEventListener('click', clearFilters);
document.getElementById('filter-search').addEventListener('keypress', e => {
    if (e.key === 'Enter') {
        applyFilters();
    }
});

const convertBackdrop = document.getElementById('convert-modal-backdrop');
const editBackdrop = document.getElementById('edit-modal-backdrop');

function abrirConvertir() {
    document.getElementById('convert-modal-error').textContent = '';
    document.getElementById('input-convert-user-id').value = '';
    document.getElementById('input-convert-partner-entity-id').value = '';
    convertBackdrop.classList.add('open');
    document.getElementById('input-convert-user-id').focus();
}

function cerrarConvertir() {
    convertBackdrop.classList.remove('open');
    document.getElementById('convert-modal-error').textContent = '';
}

async function guardarConvertir() {
    const userIdRaw = document.getElementById('input-convert-user-id').value.trim();
    const partnerEntityIdRaw = document.getElementById('input-convert-partner-entity-id').value.trim();
    const errorEl = document.getElementById('convert-modal-error');

    const userId = Number(userIdRaw);
    if (!userIdRaw || Number.isNaN(userId) || userId <= 0) {
        errorEl.textContent = 'Debes indicar un ID de usuario válido.';
        return;
    }

    let partnerEntityId = null;
    if (partnerEntityIdRaw) {
        partnerEntityId = Number(partnerEntityIdRaw);
        if (Number.isNaN(partnerEntityId) || partnerEntityId <= 0) {
            errorEl.textContent = 'El ID de entidad socia debe ser un número positivo.';
            return;
        }
    }

    try {
        const res = await fetch(`${BACKEND}/api/partner-entity-managers/${userId}`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ partnerEntityId })
        });

        const data = await parseResponseJson(res);
        if (!res.ok) {
            errorEl.textContent = data.message || 'Error al convertir el usuario.';
            return;
        }

        cerrarConvertir();
        showToast('Usuario convertido a responsable correctamente.');
        cargarManagers(0);
    } catch {
        errorEl.textContent = 'Error de conexión con el servidor.';
    }
}

async function abrirEditar(userId) {
    try {
        const res = await fetch(`${BACKEND}/api/partner-entity-managers/${userId}`, {
            headers: authHeaders()
        });

        if (!res.ok) {
            showToast('No se pudieron cargar los datos del responsable.', 'error');
            return;
        }

        const manager = await res.json();
        editingUserId = manager.userId;

        document.getElementById('input-name').value = manager.name || '';
        document.getElementById('input-email').value = manager.email || '';
        document.getElementById('input-phone').value = manager.phone || '';
        document.getElementById('input-address').value = manager.address || '';
        document.getElementById('input-postal-code').value = manager.postalCode || '';
        document.getElementById('input-partner-entity-id').value = manager.partnerEntityId || '';
        document.getElementById('edit-modal-error').textContent = '';

        editBackdrop.classList.add('open');
        document.getElementById('input-name').focus();
    } catch {
        showToast('No se pudieron cargar los datos del responsable.', 'error');
    }
}

function cerrarEditar() {
    editBackdrop.classList.remove('open');
    document.getElementById('edit-modal-error').textContent = '';
    editingUserId = null;
}

async function guardarEditar() {
    const name = document.getElementById('input-name').value.trim();
    const email = document.getElementById('input-email').value.trim();
    const phone = document.getElementById('input-phone').value.trim();
    const address = document.getElementById('input-address').value.trim();
    const postalCode = document.getElementById('input-postal-code').value.trim();
    const partnerEntityIdRaw = document.getElementById('input-partner-entity-id').value.trim();
    const errorEl = document.getElementById('edit-modal-error');

    if (!editingUserId) {
        errorEl.textContent = 'No se ha identificado el usuario a editar.';
        return;
    }

    if (!name) {
        errorEl.textContent = 'El nombre es obligatorio.';
        return;
    }

    if (name.length > 255) {
        errorEl.textContent = 'El nombre no puede superar 255 caracteres.';
        return;
    }

    if (!email) {
        errorEl.textContent = 'El email es obligatorio.';
        return;
    }

    if (email.length > 255 || !isValidEmail(email)) {
        errorEl.textContent = 'El email tiene un formato inválido.';
        return;
    }

    if (phone.length > 20) {
        errorEl.textContent = 'El teléfono no puede superar 20 caracteres.';
        return;
    }

    if (!isValidPhone(phone)) {
        errorEl.textContent = 'El teléfono debe tener entre 7 y 15 dígitos y solo puede incluir +, espacios, paréntesis o guiones.';
        return;
    }

    if (address.length > 1000) {
        errorEl.textContent = 'La dirección no puede superar 1000 caracteres.';
        return;
    }

    if (postalCode.length > 10) {
        errorEl.textContent = 'El código postal no puede superar 10 caracteres.';
        return;
    }

    let partnerEntityId = null;
    if (partnerEntityIdRaw) {
        partnerEntityId = Number(partnerEntityIdRaw);
        if (Number.isNaN(partnerEntityId) || partnerEntityId <= 0) {
            errorEl.textContent = 'El ID de entidad socia debe ser un número positivo.';
            return;
        }
    }

    const body = {
        name,
        email,
        phone: phone || null,
        address: address || null,
        postalCode: postalCode || null,
        partnerEntityId
    };

    try {
        const res = await fetch(`${BACKEND}/api/partner-entity-managers/${editingUserId}`, {
            method: 'PUT',
            headers: authHeaders(),
            body: JSON.stringify(body)
        });

        const data = await parseResponseJson(res);
        if (!res.ok) {
            errorEl.textContent = data.message || 'Error al guardar los cambios.';
            return;
        }

        cerrarEditar();
        showToast('Responsable actualizado correctamente.');
        cargarManagers(currentPage);
    } catch {
        errorEl.textContent = 'Error de conexión con el servidor.';
    }
}

async function quitarRol(userId, name) {
    const label = name || `ID ${userId}`;
    if (!confirm(`¿Quitar el rol de responsable a "${label}"?`)) return;

    try {
        const res = await fetch(`${BACKEND}/api/partner-entity-managers/${userId}`, {
            method: 'DELETE',
            headers: authHeaders()
        });

        if (!res.ok) {
            const data = await parseResponseJson(res);
            showToast(data.message || 'Error al quitar el rol.', 'error');
            return;
        }

        showToast('Rol eliminado correctamente.');
        cargarManagers(0);
    } catch {
        showToast('Error de conexión con el servidor.', 'error');
    }
}

async function parseResponseJson(res) {
    try {
        return await res.json();
    } catch {
        return {};
    }
}

document.getElementById('btn-convertir').addEventListener('click', abrirConvertir);
document.getElementById('btn-convert-cancel').addEventListener('click', cerrarConvertir);
document.getElementById('btn-convert-save').addEventListener('click', guardarConvertir);

document.getElementById('btn-edit-cancel').addEventListener('click', cerrarEditar);
document.getElementById('btn-edit-save').addEventListener('click', guardarEditar);

convertBackdrop.addEventListener('click', e => {
    if (e.target === convertBackdrop) {
        cerrarConvertir();
    }
});

editBackdrop.addEventListener('click', e => {
    if (e.target === editBackdrop) {
        cerrarEditar();
    }
});

cargarManagers(0);
