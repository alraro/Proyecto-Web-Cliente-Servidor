const BACKEND = 'http://localhost:8080';

if (localStorage.getItem('role') !== 'ADMINISTRADOR') {
    window.location.href = 'login.html';
}

function getToken() { return localStorage.getItem('token'); }
function authHeaders() { return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() }; }
function logout() { localStorage.clear(); window.location.href = 'login.html'; }

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';
    document.getElementById('btn-logout').addEventListener('click', logout);
    document.getElementById('btn-refresh').addEventListener('click', loadUsers);
    loadUsers();
});

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

function roleBadge(role) {
    const cls = role === 'PENDIENTE' ? 'badge-no' : 'badge-yes';
    return `<span class="badge ${cls}">${escHtml(role)}</span>`;
}

function renderActions(userId) {
    return `
        <div class="td-actions" style="display:flex;gap:.5rem;align-items:center;flex-wrap:wrap;">
            <button class="btn btn-primary btn-sm" data-action="toggle-role" data-userid="${userId}">Cambiar rol</button>
            <div class="role-inline" data-userid="${userId}" style="display:none;gap:.35rem;align-items:center;">
                <select id="role-select-${userId}" style="padding:.35rem .6rem;border-radius:8px;border:1.5px solid #d1d5db;font-family:inherit;font-size:.85rem;">
                    <option value="">Seleccionar rol...</option>
                    <option value="ADMINISTRADOR">ADMINISTRADOR</option>
                    <option value="COORDINADOR">COORDINADOR</option>
                    <option value="CAPITAN">CAPITAN</option>
                    <option value="COLABORADOR">COLABORADOR</option>
                    <option value="RESPONSABLE_TIENDA">RESPONSABLE_TIENDA</option>
                </select>
                <button class="btn btn-primary btn-sm" data-action="confirm-role" data-userid="${userId}">Confirmar</button>
            </div>
        </div>
    `;
}

function toggleRoleInline(userId) {
    document.querySelectorAll('.role-inline').forEach((panel) => {
        if (panel.dataset.userid === String(userId)) {
            panel.style.display = panel.style.display === 'none' ? 'inline-flex' : 'none';
        } else {
            panel.style.display = 'none';
        }
    });
}

async function loadUsers() {
    const tbody = document.getElementById('users-tbody');
    try {
        const res = await fetch(BACKEND + '/api/users', { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        const data = await res.json();

        if (!Array.isArray(data) || !data.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay usuarios.</td></tr>';
            return;
        }

        tbody.innerHTML = data.map(u => `
            <tr>
                <td>${u.id}</td>
                <td><strong>${escHtml(u.name)}</strong></td>
                <td>${escHtml(u.email)}</td>
                <td>${escHtml(u.phone || '—')}</td>
                <td>${roleBadge(u.role || 'PENDIENTE')}</td>
                <td>${renderActions(u.id)}</td>
            </tr>
        `).join('');
    } catch {
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">Error al conectar con el servidor.</td></tr>';
    }
}

document.addEventListener('click', async (event) => {
    const target = event.target.closest('button[data-action]');
    if (!target) return;

    const action = target.dataset.action;
    const userId = target.dataset.userid;

    if (action === 'toggle-role') {
        toggleRoleInline(userId);
        return;
    }

    if (action === 'confirm-role') {
        const role = document.getElementById('role-select-' + userId)?.value;
        if (!role) {
            showToast('Selecciona un rol valido.', 'error');
            return;
        }
        try {
            const res = await fetch(`${BACKEND}/api/users/${userId}/role`, {
                method: 'POST',
                headers: authHeaders(),
                body: JSON.stringify({ role })
            });
            const data = await res.json().catch(() => ({}));
            if (!res.ok) {
                showToast(data.message || 'Error al asignar rol.', 'error');
                return;
            }
            showToast('Rol actualizado correctamente.');
            await loadUsers();
        } catch {
            showToast('Error de conexion.', 'error');
        }
    }
});
