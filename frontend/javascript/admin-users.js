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

let currentPage = 0;
let totalPages = 1;
let pageSize = 20;

const ROLE_OPTIONS = [
    'ADMINISTRADOR',
    'COORDINADOR',
    'CAPITAN',
    'COLABORADOR',
    'RESPONSABLE_TIENDA'
];

function escHtml(value) {
    return String(value ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function createRoleBadge(role) {
    const span = document.createElement('span');
    const roleValue = role || 'PENDIENTE';
    const cls = roleValue === 'PENDIENTE' ? 'badge badge-no' : 'badge badge-yes';
    span.className = cls;
    span.textContent = roleValue;
    return span;
}

function renderUserRow(user) {
    const tr = document.createElement('tr');

    const tdId = document.createElement('td');
    tdId.textContent = user.idUser;
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

function buildFilterParams() {
    var params = new URLSearchParams();
    params.append('page', currentPage);
    params.append('size', pageSize);

    var search = document.getElementById('filter-search').value.trim();
    if (search) params.append('search', search);

    var role = document.getElementById('filter-role').value;
    if (role) params.append('role', role);

    var sort = document.getElementById('filter-sort').value;
    if (sort) params.append('sort', sort);

    return params;
}

function renderPagination(page, total) {
    currentPage = page;
    totalPages = total;

    var prevBtn = document.getElementById('btn-prev-page');
    var nextBtn = document.getElementById('btn-next-page');
    var pageInfo = document.getElementById('page-info');

    if (prevBtn) prevBtn.disabled = currentPage === 0;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;
    if (pageInfo) pageInfo.textContent = 'Pagina ' + (currentPage + 1) + ' de ' + totalPages;
}

async function loadUsers(page) {
    if (!usersTbody) return;

    if (page !== undefined) currentPage = page;
    usersTbody.innerHTML = '';

    try {
        var params = buildFilterParams();
        var response = await fetch(API_BASE + '/api/users?' + params, { headers: authHeaders() });

        if (response.status === 401 || response.status === 403) {
            logout();
            return;
        }

        if (!response.ok) {
            var errorData = await response.json().catch(function () { return {}; });
            showToast(errorData.message || 'Error al cargar usuarios.', 'error');
            return;
        }

        var data = await response.json();
        var items = data.content || [];
        usersCache = items;

        if (items.length === 0) {
            var emptyRow = document.createElement('tr');
            var emptyCell = document.createElement('td');
            emptyCell.colSpan = 6;
            emptyCell.className = 'table-empty';
            emptyCell.textContent = 'No hay usuarios que coincidan con los filtros.';
            emptyRow.appendChild(emptyCell);
            usersTbody.appendChild(emptyRow);
        } else {
            items.forEach(function (user) {
                usersTbody.appendChild(renderUserRow(user));
            });
        }

        renderPagination(data.page || 0, data.totalPages || 1);
    } catch (error) {
        usersTbody.innerHTML = '';
        var errorRow = document.createElement('tr');
        var errorCell = document.createElement('td');
        errorCell.colSpan = 6;
        errorCell.className = 'table-empty';
        errorCell.textContent = 'Error al conectar con el servidor.';
        errorRow.appendChild(errorCell);
        usersTbody.appendChild(errorRow);
    }
}

function applyFilters() {
    currentPage = 0;
    loadUsers(0);
}

function clearFilters() {
    document.getElementById('filter-search').value = '';
    document.getElementById('filter-role').value = '';
    document.getElementById('filter-sort').value = 'id,asc';
    applyFilters();
}

function findUserById(userId) {
    return usersCache.find(u => String(u.idUser) === String(userId)) || null;
}

function populateRoleOptions(user) {
    var currentRoles = (user.roles || []).filter(function (r) { return r && r !== 'PENDIENTE'; });
    inputRole.innerHTML = '';

    var defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Seleccionar rol...';
    inputRole.appendChild(defaultOption);

    ROLE_OPTIONS
        .filter(function (role) { return !currentRoles.includes(role); })
        .forEach(function (role) {
            var option = document.createElement('option');
            option.value = role;
            option.textContent = role;
            inputRole.appendChild(option);
        });
}

function openEditModal(userId) {
    var user = findUserById(userId);
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
    var role = inputRole.value;

    if (!role) {
        modalError.textContent = 'Selecciona un rol valido.';
        modalError.className = 'form-message is-error';
        return;
    }

    btnGuardar.disabled = true;
    modalError.textContent = '';

    try {
        var response = await fetch(API_BASE + '/api/users/' + currentUserId + '/role', {
            method: 'POST',
            headers: authHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify({ role: role })
        });

        var data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            modalError.textContent = data.message || 'Error al asignar rol.';
            modalError.className = 'form-message is-error';
            btnGuardar.disabled = false;
            return;
        }

        showToast('Rol actualizado correctamente.', 'success');
        closeModal();
        btnGuardar.disabled = false;
        await loadUsers(currentPage);
    } catch (error) {
        modalError.textContent = 'Error de conexion.';
        modalError.className = 'form-message is-error';
        btnGuardar.disabled = false;
    }
}

async function deleteUser(userId) {
    var user = findUserById(userId);
    if (!user) return;

    if (!confirm('¿Estas seguro de que quieres eliminar al usuario "' + user.name + '"?')) return;

    try {
        var response = await fetch(API_BASE + '/api/users/' + userId, {
            method: 'DELETE',
            headers: authHeaders()
        });

        var data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            showToast(data.message || 'Error al eliminar usuario.', 'error');
            return;
        }

        showToast('Usuario eliminado correctamente.', 'success');
        await loadUsers(currentPage);
    } catch (error) {
        showToast('Error de conexion.', 'error');
    }
}

function createPaginationBar() {
    var card = document.querySelector('.card');
    var tableWrap = document.querySelector('.table-wrap');

    var paginationDiv = document.createElement('div');
    paginationDiv.className = 'pagination';

    var prevBtn = document.createElement('button');
    prevBtn.id = 'btn-prev-page';
    prevBtn.className = 'btn btn-secondary';
    prevBtn.textContent = '← Anterior';
    prevBtn.addEventListener('click', function () { if (currentPage > 0) loadUsers(currentPage - 1); });
    paginationDiv.appendChild(prevBtn);

    var pageInfo = document.createElement('span');
    pageInfo.className = 'pagination-info';
    pageInfo.id = 'page-info';
    pageInfo.textContent = 'Pagina 1 de 1';
    paginationDiv.appendChild(pageInfo);

    var nextBtn = document.createElement('button');
    nextBtn.id = 'btn-next-page';
    nextBtn.className = 'btn btn-secondary';
    nextBtn.textContent = 'Siguiente →';
    nextBtn.addEventListener('click', function () { if (currentPage < totalPages - 1) loadUsers(currentPage + 1); });
    paginationDiv.appendChild(nextBtn);

    var sizeSelect = document.createElement('select');
    sizeSelect.className = 'pagination-select';
    sizeSelect.id = 'page-size-select';
    [20, 50, 100].forEach(function (s) {
        var opt = document.createElement('option');
        opt.value = s;
        opt.textContent = s + ' por pagina';
        if (s === pageSize) opt.selected = true;
        sizeSelect.appendChild(opt);
    });
    sizeSelect.addEventListener('change', function () {
        pageSize = parseInt(this.value);
        currentPage = 0;
        loadUsers(0);
    });
    paginationDiv.appendChild(sizeSelect);

    if (tableWrap && tableWrap.nextSibling) {
        card.insertBefore(paginationDiv, tableWrap.nextSibling);
    } else {
        card.appendChild(paginationDiv);
    }
}

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

    if (sessionStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }

    createPaginationBar();

    btnRefresh.addEventListener('click', function () { loadUsers(0); });

    btnExport.addEventListener('click', function () {
        exportarExcel('users');
    });

    document.getElementById('btn-filter').addEventListener('click', applyFilters);
    document.getElementById('btn-clear-filters').addEventListener('click', clearFilters);
    document.getElementById('filter-search').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') applyFilters();
    });

    btnCancelar.addEventListener('click', closeModal);
    btnGuardar.addEventListener('click', saveUserRole);

    modalBackdrop.addEventListener('click', function (e) {
        if (e.target === modalBackdrop) closeModal();
    });

    usersTbody.addEventListener('click', function (e) {
        var button = e.target.closest('button');
        if (!button) return;

        var userId = button.getAttribute('data-userid');
        var action = button.getAttribute('data-action');

        if (action === 'edit') {
            openEditModal(userId);
        } else if (action === 'delete') {
            deleteUser(userId);
        }
    });

    loadUsers(0);
});
