const BACKEND = 'http://localhost:8080';

function getToken()    { return localStorage.getItem('token'); }
function authHeaders() { return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() }; }
function logout()      { localStorage.clear(); window.location.href = 'login.html'; }

document.addEventListener('DOMContentLoaded', () => {
    if (!getToken() || localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';
    document.getElementById('btn-logout').addEventListener('click', logout);

    loadPending();
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


// ── Usuarios pendientes ────────────────────────────────────────────────────
async function loadPending() {
    const tbody = document.getElementById('pending-tbody');
    try {
        const res = await fetch(BACKEND + '/api/users/pending', { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        const data = await res.json();

        const badge = document.getElementById('badge-pending');
        if (data.length > 0) {
            badge.textContent = data.length;
            badge.style.display = '';
        } else {
            badge.style.display = 'none';
        }

        if (!data.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay usuarios pendientes de aprobación.</td></tr>';
            return;
        }

        tbody.innerHTML = data.map(u => `
            <tr>
                <td>${u.id}</td>
                <td><strong>${escHtml(u.name)}</strong></td>
                <td>${escHtml(u.email)}</td>
                <td>${escHtml(u.phone || '—')}</td>
                <td>
                    <select id="role-${u.id}" style="padding:.35rem .6rem;border-radius:8px;border:1.5px solid #d1d5db;font-family:inherit;font-size:.85rem;">
                        <option value="">Seleccionar rol...</option>
                        <option value="ADMINISTRADOR">Administrador</option>
                        <option value="COORDINADOR">Coordinador</option>
                        <option value="CAPITAN">Capitán</option>
                        <option value="COLABORADOR">Colaborador</option>
                        <option value="RESPONSABLE_TIENDA">Responsable de Tienda</option>
                    </select>
                </td>
                <td>
                    <div class="td-actions">
                        <button class="btn btn-primary btn-sm" onclick="approveUser(${u.id})">✓ Aprobar</button>
                        <button class="btn btn-danger btn-sm" onclick="rejectUser(${u.id}, '${escHtml(u.name)}')">✗ Rechazar</button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch {
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">Error al conectar con el servidor.</td></tr>';
    }
}

async function approveUser(id) {
    const role = document.getElementById('role-' + id)?.value;
    if (!role) {
        showToast('Selecciona un rol antes de aprobar.', 'error');
        return;
    }

    try {
        const res = await fetch(`${BACKEND}/api/users/${id}/role`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ role })
        });
        const data = await res.json();
        if (!res.ok) { showToast(data.message || 'Error al asignar rol.', 'error'); return; }
        showToast(`Usuario aprobado como ${role}.`);
        loadPending();
    } catch {
        showToast('Error de conexión.', 'error');
    }
}

async function rejectUser(id, name) {
    if (!confirm(`¿Rechazar y eliminar la cuenta de "${name}"? Esta acción no se puede deshacer.`)) return;
    try {
        const res = await fetch(`${BACKEND}/api/users/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        if (!res.ok) { const d = await res.json(); showToast(d.message || 'Error al eliminar.', 'error'); return; }
        showToast('Usuario rechazado y eliminado.');
        loadPending();
    } catch {
        showToast('Error de conexión.', 'error');
    }
}

