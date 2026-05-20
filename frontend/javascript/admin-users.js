// Cached DOM references
let usersTbody = null;
let modalBackdrop = null;
let inputRole = null;
let modalError = null;
let btnGuardar = null;
let btnCancelar = null;
let btnRefresh = null;
let btnExport = null;
let currentUserId = null;
let usersCache = [];

// Auth helpers
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

// Toast notification
function showToast(msg, type) {
    console.log("El toast se muestra con el mensaje: " + msg + " y el tipo: " + type);
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(function () {
        toast.remove();
    }, 3500);
}

// Escape HTML for safe attribute usage
function escHtml(value) {
    return String(value ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// Create a role badge element
function createRoleBadge(role) {
    const span = document.createElement('span');
    const roleValue = role || 'PENDIENTE';
    const cls = roleValue === 'PENDIENTE' ? 'badge badge-no' : 'badge badge-yes';
    span.className = cls;
    span.textContent = roleValue;
    return span;
}

// Render a single user table row using DOM API
function renderUserRow(user) {
    const tr = document.createElement('tr');

    // ID
    const tdId = document.createElement('td');
    tdId.textContent = user.id;
    tr.appendChild(tdId);

    // Name
    const tdName = document.createElement('td');
    const strong = document.createElement('strong');
    strong.textContent = user.name;
    tdName.appendChild(strong);
    tr.appendChild(tdName);

    // Email
    const tdEmail = document.createElement('td');
    tdEmail.textContent = user.email;
    tr.appendChild(tdEmail);

    // Phone
    const tdPhone = document.createElement('td');
    tdPhone.textContent = user.phone || '—';
    tr.appendChild(tdPhone);

    // Role badge
    const tdRole = document.createElement('td');
    tdRole.appendChild(createRoleBadge(user.role));
    tr.appendChild(tdRole);

    // Actions
    const tdActions = document.createElement('td');
    tdActions.className = 'td-actions';

    // Edit button
    const btnEdit = document.createElement('button');
    btnEdit.className = 'btn btn-primary btn-sm';
    btnEdit.textContent = 'Editar';
    btnEdit.setAttribute('data-userid', user.id);
    tdActions.appendChild(btnEdit);

    // Delete button
    const btnDelete = document.createElement('button');
    btnDelete.className = 'btn btn-danger btn-sm';
    btnDelete.textContent = 'Eliminar';
    btnDelete.setAttribute('data-userid', user.id);
    tdActions.appendChild(btnDelete);

    tr.appendChild(tdActions);

    return tr;
}

// Load all users from API
async function loadUsers() {
    if (!usersTbody) return;

    // Clear existing rows
    usersTbody.innerHTML = '';

    try {
        const response = await fetch(API_BASE + '/api/users', {
            headers: authHeaders()
        });

        if (response.status === 401 || response.status === 403) {
            logout();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(function () { return {}; });
            showToast(errorData.message || 'Error al cargar usuarios.', 'error');
            return;
        }

        const data = await response.json();

        if (!Array.isArray(data) || data.length === 0) {
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.colSpan = 6;
            emptyCell.className = 'table-empty';
            emptyCell.textContent = 'No hay usuarios.';
            emptyRow.appendChild(emptyCell);
            usersTbody.appendChild(emptyRow);
            return;
        }

        usersCache = data;

        for (let i = 0; i < data.length; i++) {
            usersTbody.appendChild(renderUserRow(data[i]));
        }
    } catch (error) {
        usersTbody.innerHTML = '';
        const errorRow = document.createElement('tr');
        const errorCell = document.createElement('td');
        errorCell.colSpan = 6;
        errorCell.className = 'table-empty';
        errorCell.textContent = 'Error al conectar con el servidor.';
        errorRow.appendChild(errorCell);
        usersTbody.appendChild(errorRow);
    }
}

// Find user in cache by ID
function findUserById(userId) {
    for (let i = 0; i < usersCache.length; i++) {
        if (String(usersCache[i].id) === String(userId)) {
            return usersCache[i];
        }
    }
    return null;
}

// Open edit modal for a user
function openEditModal(userId) {
    currentUserId = userId;
    inputRole.value = '';
    modalError.textContent = '';
    modalBackdrop.classList.add('open');
}

// Close modal
function closeModal() {
    modalBackdrop.classList.remove('open');
    currentUserId = null;
    inputRole.value = '';
    modalError.textContent = '';
}

// Save new role for user
async function saveUserRole() {
    if (!currentUserId) return;

    const role = inputRole.value;

    if (!role) {
        modalError.textContent = 'Selecciona un rol valido.';
        modalError.className = 'form-message is-error';
        return;
    }

    btnGuardar.disabled = true;
    modalError.textContent = '';

    try {
        const response = await fetch(API_BASE + '/api/users/' + currentUserId + '/role', {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ role: role })
        });

        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            modalError.textContent = data.message || 'Error al asignar rol.';
            modalError.className = 'form-message is-error';
            btnGuardar.disabled = false;
            return;
        }

        showToast('Rol actualizado correctamente.', 'success');
        closeModal();
        await loadUsers();
    } catch (error) {
        modalError.textContent = 'Error de conexion.';
        modalError.className = 'form-message is-error';
    }

    btnGuardar.disabled = false;
}

// Delete a user
async function deleteUser(userId) {
    const user = findUserById(userId);
    if (!user) return;

    const confirmDelete = confirm('¿Estas seguro de que quieres eliminar al usuario "' + user.name + '"?');
    if (!confirmDelete) return;

    try {
        const response = await fetch(API_BASE + '/api/users/' + userId, {
            method: 'DELETE',
            headers: authHeaders()
        });

        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            showToast(data.message || 'Error al eliminar usuario.', 'error');
            return;
        }

        showToast('Usuario eliminado correctamente.', 'success');
        await loadUsers();
    } catch (error) {
        showToast('Error de conexion.', 'error');
    }
}

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', function () {
    // Cache DOM elements
    usersTbody = document.getElementById('users-tbody');
    modalBackdrop = document.getElementById('modal-backdrop');
    inputRole = document.getElementById('input-role');
    modalError = document.getElementById('modal-error');
    btnGuardar = document.getElementById('btn-guardar');
    btnCancelar = document.getElementById('btn-cancelar');
    btnRefresh = document.getElementById('btn-refresh');
    btnExport = document.getElementById('btn-export');

    // Auth check
    if (!getToken()) {
        window.location.href = 'login.html';
        return;
    }

    if (localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }

    // Event listeners
    btnRefresh.addEventListener('click', loadUsers);

    btnExport.addEventListener('click', function () {
        exportarExcel('users');
    });

    btnCancelar.addEventListener('click', closeModal);

    btnGuardar.addEventListener('click', saveUserRole);

    // Close modal when clicking on backdrop
    modalBackdrop.addEventListener('click', function (e) {
        if (e.target === modalBackdrop) {
            closeModal();
        }
    });

    // Delegate table button clicks (edit / delete)
    usersTbody.addEventListener('click', function (e) {
        const button = e.target.closest('button');
        if (!button) return;

        const userId = button.getAttribute('data-userid');
        const action = button.textContent;

        if (action === 'Editar') {
            openEditModal(userId);
        } else if (action === 'Eliminar') {
            deleteUser(userId);
        }
    });

    // Load initial data
    loadUsers();
});
