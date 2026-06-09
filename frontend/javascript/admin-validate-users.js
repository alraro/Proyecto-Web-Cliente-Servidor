document.addEventListener('DOMContentLoaded', () => {
    if (!getToken() || localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }


    // Boton de actualizar
    document.querySelector('#btn-refresh-pending').addEventListener('click', loadPending);

    document.querySelector('#pending-tbody').addEventListener('click', function (e) {
        const button = e.target.closest('button');
        if (!button) return;
        const action = button.getAttribute('data-action');
        const userId = button.getAttribute('data-user-id');
        if (action === 'approve') {
            approveUser(parseInt(userId));
        } else if (action === 'reject') {
            const userName = button.getAttribute('data-user-name');
            rejectUser(parseInt(userId), userName);
        }
    });

    loadPending();
});

function escHtml(v) {
    return String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}


// Usuarios pendientes
async function loadPending() {
    const tbody = document.querySelector('#pending-tbody');
    try {
        const res = await fetch(API_BASE + '/api/users/pending', { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        const data = await res.json();

        const badge = document.querySelector('#badge-pending');
        if (data.length > 0) {
            badge.textContent = data.length;
            badge.classList.remove('hidden');
        } else {
            badge.classList.add('hidden');
        }

        if (!data.length) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 6;
            td.className = 'table-empty';
            td.textContent = 'No hay usuarios pendientes de aprobación.';
            tr.appendChild(td);
            tbody.innerHTML = '';
            tbody.appendChild(tr);
            return;
        }

        tbody.innerHTML = '';
        data.forEach(u => {
            const tr = document.createElement('tr');

            const td1 = document.createElement('td');
            td1.textContent = u.idUser;
            tr.appendChild(td1);

            const td2 = document.createElement('td');
            const strong = document.createElement('strong');
            strong.textContent = escHtml(u.name);
            td2.appendChild(strong);
            tr.appendChild(td2);

            const td3 = document.createElement('td');
            td3.textContent = escHtml(u.email);
            tr.appendChild(td3);

            const td4 = document.createElement('td');
            td4.textContent = escHtml(u.phone || '—');
            tr.appendChild(td4);

            const td5 = document.createElement('td');
            const select = document.createElement('select');
            select.id = 'role-' + u.idUser;
            select.style.cssText = 'padding:.35rem .6rem;border-radius:8px;border:1.5px solid #d1d5db;font-family:inherit;font-size:.85rem;';

            const optDefault = document.createElement('option');
            optDefault.value = '';
            optDefault.textContent = 'Seleccionar rol...';
            select.appendChild(optDefault);

            const roles = [
                { value: 'ADMINISTRADOR', label: 'Administrador' },
                { value: 'COORDINADOR', label: 'Coordinador' },
                { value: 'CAPITAN', label: 'Capitán' },
                { value: 'COLABORADOR', label: 'Colaborador' },
                { value: 'RESPONSABLE_TIENDA', label: 'Responsable de Tienda' }
            ];
            roles.forEach(r => {
                const opt = document.createElement('option');
                opt.value = r.value;
                opt.textContent = r.label;
                select.appendChild(opt);
            });

            td5.appendChild(select);
            tr.appendChild(td5);

            const td6 = document.createElement('td');
            const div = document.createElement('div');
            div.className = 'td-actions';

            const btnApprove = document.createElement('button');
            btnApprove.className = 'btn btn-primary btn-sm';
            btnApprove.setAttribute('data-action', 'approve');
            btnApprove.setAttribute('data-user-id', u.idUser);
            btnApprove.textContent = '✓ Aprobar';
            div.appendChild(btnApprove);

            const btnReject = document.createElement('button');
            btnReject.className = 'btn btn-danger btn-sm';
            btnReject.setAttribute('data-action', 'reject');
            btnReject.setAttribute('data-user-id', u.idUser);
            btnReject.setAttribute('data-user-name', escHtml(u.name));
            btnReject.textContent = '✗ Rechazar';
            div.appendChild(btnReject);

            td6.appendChild(div);
            tr.appendChild(td6);

            tbody.appendChild(tr);
        });
    } catch {
        const trErr = document.createElement('tr');
        const tdErr = document.createElement('td');
        tdErr.colSpan = 6;
        tdErr.className = 'table-empty';
        tdErr.textContent = 'Error al conectar con el servidor.';
        trErr.appendChild(tdErr);
        tbody.innerHTML = '';
        tbody.appendChild(trErr);
    }
}

async function approveUser(id) {
    const role = document.querySelector('#role-' + id)?.value;
    if (!role) {
        showToast('Selecciona un rol antes de aprobar.', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/users/${id}/role`, {
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
        const res = await fetch(`${API_BASE}/api/users/${id}`, {
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

