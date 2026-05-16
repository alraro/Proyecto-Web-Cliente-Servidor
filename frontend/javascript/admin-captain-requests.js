const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

    const statusSelect  = document.getElementById('status-select');
    const btnLoad       = document.getElementById('btn-load');
    const requestsTbody = document.getElementById('requests-tbody');

    async function loadRequests() {
        const status = statusSelect.value;
        requestsTbody.innerHTML = "<tr><td colspan='7' class='table-empty'>Cargando...</td></tr>";
        try {
            const requests = await fetchJson(
                API_BASE + '/api/admin/captain-requests?status=' + encodeURIComponent(status),
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(requests) ? requests : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las solicitudes', true);
            requestsTbody.innerHTML = "<tr><td colspan='7' class='table-empty'>Error al cargar.</td></tr>";
        }
    }

    btnLoad.addEventListener('click', loadRequests);

    function renderTable(requests) {
        requestsTbody.innerHTML = '';
        if (!requests.length) {
            requestsTbody.innerHTML = "<tr><td colspan='7' class='table-empty'>No hay solicitudes con este estado.</td></tr>";
            return;
        }
        requests.forEach(r => {
            const tr = document.createElement('tr');
            const isPending = r.status === 'PENDIENTE';
            const fecha = r.createdAt ? new Date(r.createdAt).toLocaleDateString('es-ES') : '—';
            tr.innerHTML = `
                <td>${escapeHtml(r.name || '')}</td>
                <td>${escapeHtml(r.email || '')}</td>
                <td>${escapeHtml(r.campaignName || '')}</td>
                <td>${escapeHtml(r.coordinatorName || '')}</td>
                <td>${fecha}</td>
                <td>${statusBadge(r.status)}</td>
                <td class="action-cell">
                    ${isPending ? `
                    <button class="btn btn-sm btn-approve" data-id="${r.id}">Aprobar</button>
                    <button class="btn btn-sm btn-reject"  data-id="${r.id}">Rechazar</button>
                    ` : '—'}
                </td>
            `;
            requestsTbody.appendChild(tr);
        });

        requestsTbody.querySelectorAll('.btn-approve').forEach(btn => {
            btn.addEventListener('click', () => handleAction(Number(btn.dataset.id), 'approve'));
        });
        requestsTbody.querySelectorAll('.btn-reject').forEach(btn => {
            btn.addEventListener('click', () => handleAction(Number(btn.dataset.id), 'reject'));
        });
    }

    async function handleAction(id, action) {
        const label = action === 'approve' ? 'aprobar' : 'rechazar';
        if (!confirm('¿Seguro que quieres ' + label + ' esta solicitud?')) return;
        try {
            const data = await fetchJson(
                API_BASE + '/api/admin/captain-requests/' + id + '/' + action,
                { method: 'POST', headers: authHeaders(token) }
            );
            showMessage(data.message || 'Operación realizada', false);
            await loadRequests();
        } catch (err) {
            showMessage(err.message || 'Error al procesar la solicitud', true);
        }
    }

    function statusBadge(status) {
        const map = {
            'PENDIENTE':  '<span class="status-badge status-pending">Pendiente</span>',
            'APROBADA':   '<span class="status-badge status-approved">Aprobada</span>',
            'RECHAZADA':  '<span class="status-badge status-rejected">Rechazada</span>',
        };
        return map[status] || escapeHtml(status);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    function authHeaders(t) {
        return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + t };
    }

    async function fetchJson(url, options) {
        const res = await fetch(url, options);
        const data = await res.json().catch(() => ({}));
        if (res.status === 401 || res.status === 403) {
            localStorage.clear(); window.location.href = 'login.html';
            throw new Error('Sesión expirada');
        }
        if (!res.ok) throw new Error(data.message || 'Error ' + res.status);
        return data;
    }

    function showMessage(text, isError) {
        const el = document.getElementById('global-message');
        el.hidden = false;
        el.textContent = text;
        el.className = isError ? 'error' : 'success';
        clearTimeout(showMessage._t);
        showMessage._t = setTimeout(() => { el.hidden = true; }, 4000);
    }

    function escapeHtml(v) {
        return String(v).replace(/&/g,'&amp;').replace(/</g,'&lt;')
            .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
    }

    // Carga inicial
    await loadRequests();
});
