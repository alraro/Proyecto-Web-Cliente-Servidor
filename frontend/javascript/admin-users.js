// Referencias DOM en cache
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
const ROLE_OPTIONS = [
    'ADMINISTRADOR',
    'COORDINADOR',
    'CAPITAN',
    'COLABORADOR',
    'RESPONSABLE_TIENDA'
];

// Escapar HTML para uso seguro en atributos
function escHtml(value) {
    return String(value ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// Crear un badge de rol
function createRoleBadge(role) {
    const span = document.createElement('span');
    const roleValue = role || 'PENDIENTE';
    const cls = roleValue === 'PENDIENTE' ? 'badge badge-no' : 'badge badge-yes';
    span.className = cls;
    span.textContent = roleValue;
    return span;
}

// Renderizar una fila de usuario con DOM
function renderUserRow(user, displayId) {
    const tr = document.createElement('tr');

    const tdId = document.createElement('td');
    tdId.textContent = displayId;
    tr.appendChild(tdId);

    const tdName = document.createElement('td');
    const strong = document.createElement('strong');
    strong.textContent = user.name;
    tdName.appendChild(strong);
    tr.appendChild(tdName);

    const tdEmail = document.createElement('td');
    tdEmail.textContent = user.email;
    tr.appendChild(tdEmail);

    const tdPhone = document.createElement('td');
    tdPhone.textContent = user.phone || '—';
    tr.appendChild(tdPhone);

    const tdRole = document.createElement('td');
    const roleActual = (user.roles && user.roles.length > 0) ? user.roles[0] : null;
    tdRole.appendChild(createRoleBadge(roleActual));
    tr.appendChild(tdRole);

    const tdActions = document.createElement('td');
    tdActions.className = 'td-actions';

    const btnEdit = document.createElement('button');
    btnEdit.className = 'btn btn-primary btn-sm';
    btnEdit.textContent = 'Editar';
    btnEdit.setAttribute('data-action', 'edit');
    btnEdit.setAttribute('data-userid', user.idUser);
    tdActions.appendChild(btnEdit);

    const btnDelete = document.createElement('button');
    btnDelete.className = 'btn btn-danger btn-sm';
    btnDelete.textContent = 'Eliminar';
    btnDelete.setAttribute('data-action', 'delete');
    btnDelete.setAttribute('data-userid', user.idUser);
    tdActions.appendChild(btnDelete);

    tr.appendChild(tdActions);
    return tr;
}

// Cargar usuarios desde la API
async function loadUsers() {
    if (!usersTbody) return;

    // Limpiar filas actuales
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

        data.forEach(function (user, index) {
            usersTbody.appendChild(renderUserRow(user, index + 1));
        });
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

function findUserById(userId) {
    return usersCache.find(u => String(u.idUser) === String(userId)) || null;
}

function populateRoleOptions(user) {
    const currentRoles = (user.roles || []).filter(role => role && role !== 'PENDIENTE');

    inputRole.innerHTML = '';

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Seleccionar rol...';
    inputRole.appendChild(defaultOption);

    ROLE_OPTIONS
        .filter(role => !currentRoles.includes(role))
        .forEach(role => {
            const option = document.createElement('option');
            option.value = role;
            option.textContent = role;
            inputRole.appendChild(option);
        });
}

function openEditModal(userId) {
    const user = findUserById(userId);
    if (!user) return;

    currentUserId = userId;
    populateRoleOptions(user);
    inputRole.value = '';
    modalError.textContent = '';
    modalBackdrop.classList.add('open');
}

function closeModal() {
    modalBackdrop.classList.remove('open');
    currentUserId = null;
    inputRole.value = '';
    modalError.textContent = '';
}

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
        btnGuardar.disabled = false;
        await loadUsers();
    } catch (error) {
        modalError.textContent = 'Error de conexion.';
        modalError.className = 'form-message is-error';
        btnGuardar.disabled = false;
    }
}

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

// Inicializar al cargar el DOM
document.addEventListener('DOMContentLoaded', function () {
    usersTbody = document.querySelector('#users-tbody');
    modalBackdrop = document.querySelector('#modal-backdrop');
    inputRole = document.querySelector('#input-role');
    modalError = document.querySelector('#modal-error');
    btnGuardar = document.querySelector('#btn-guardar');
    btnCancelar = document.querySelector('#btn-cancelar');
    btnRefresh = document.querySelector('#btn-refresh');
    btnExport = document.querySelector('#btn-export');

    if (!getToken()) {
        window.location.href = 'login.html';
        return;
    }

    if (localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }

    btnRefresh.addEventListener('click', loadUsers);

    btnExport.addEventListener('click', function () {
        exportarExcel('users');
    });

    btnCancelar.addEventListener('click', closeModal);

    btnGuardar.addEventListener('click', saveUserRole);

    // Cerrar modal al hacer clic en el fondo
    modalBackdrop.addEventListener('click', function (e) {
        if (e.target === modalBackdrop) {
            closeModal();
        }
    });

    usersTbody.addEventListener('click', function (e) {
        const button = e.target.closest('button');
        if (!button) return;

        const userId = button.getAttribute('data-userid');
        const action = button.getAttribute('data-action');

        if (action === 'edit') {
            openEditModal(userId);
        } else if (action === 'delete') {
            deleteUser(userId);
        }
    });

    loadUsers();
});
