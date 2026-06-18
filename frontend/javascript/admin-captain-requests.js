const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = sessionStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    const statusSelect  = document.querySelector('#status-select');
    const btnLoad       = document.querySelector('#btn-load');
    const requestsTbody = document.querySelector('#requests-tbody');

    async function loadRequests() {
        const status = statusSelect.value;
        const loadingRow = document.createElement('tr');
        const loadingCell = document.createElement('td');
        loadingCell.setAttribute('colspan', '7');
        loadingCell.className = 'table-empty';
        loadingCell.textContent = 'Cargando...';
        loadingRow.appendChild(loadingCell);
        requestsTbody.replaceChildren();
        try {
            const requests = await fetchJson(
                API_BASE + '/api/admin/captain-requests?status=' + encodeURIComponent(status),
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(requests) ? requests : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las solicitudes', true);
            requestsTbody.replaceChildren();
            const errorRow = document.createElement('tr');
            const errorCell = document.createElement('td');
            errorCell.setAttribute('colspan', '7');
            errorCell.className = 'table-empty';
            errorCell.textContent = 'Error al cargar.';
            errorRow.appendChild(errorCell);
            requestsTbody.appendChild(errorRow);
        }
    }

    btnLoad.addEventListener('click', loadRequests);

    function renderTable(requests) {
        requestsTbody.replaceChildren();
        if (!requests.length) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.setAttribute('colspan', '7');
            cell.className = 'table-empty';
            cell.textContent = 'No hay solicitudes con este estado.';
            row.appendChild(cell);
            requestsTbody.appendChild(row);
            return;
        }
        requests.forEach(r => {
            const tr = document.createElement('tr');
            const isPending = r.status === 'PENDIENTE';
            const fecha = r.createdAt ? new Date(r.createdAt).toLocaleDateString('es-ES') : '\u2014';
            const tdName = document.createElement('td');
            tdName.textContent = escapeHtml(r.name || '');
            tr.appendChild(tdName);
            const tdEmail = document.createElement('td');
            tdEmail.textContent = escapeHtml(r.email || '');
            tr.appendChild(tdEmail);
            const tdCampaign = document.createElement('td');
            tdCampaign.textContent = escapeHtml(r.campaignName || '');
            tr.appendChild(tdCampaign);
            const tdCoordinator = document.createElement('td');
            tdCoordinator.textContent = escapeHtml(r.coordinatorName || '');
            tr.appendChild(tdCoordinator);
            const tdDate = document.createElement('td');
            tdDate.textContent = fecha;
            tr.appendChild(tdDate);
            const tdStatus = document.createElement('td');
            tdStatus.appendChild(statusBadge(r.status));
            tr.appendChild(tdStatus);
            const tdAction = document.createElement('td');
            tdAction.className = 'action-cell';
            if (isPending) {
                const btnApprove = document.createElement('button');
                btnApprove.className = 'btn btn-sm btn-approve';
                btnApprove.setAttribute('data-id', r.id);
                btnApprove.textContent = 'Aprobar';
                tdAction.appendChild(btnApprove);
                const btnReject = document.createElement('button');
                btnReject.className = 'btn btn-sm btn-reject';
                btnReject.setAttribute('data-id', r.id);
                btnReject.textContent = 'Rechazar';
                tdAction.appendChild(btnReject);
            } else {
                tdAction.textContent = '\u2014';
            }
            tr.appendChild(tdAction);
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
            'PENDIENTE':  { text: 'Pendiente',  css: 'status-badge status-pending' },
            'APROBADA':   { text: 'Aprobada',   css: 'status-badge status-approved' },
            'RECHAZADA':  { text: 'Rechazada',  css: 'status-badge status-rejected' },
        };
        const entry = map[status];
        if (entry) {
            const span = document.createElement('span');
            span.className = entry.css;
            span.textContent = entry.text;
            return span;
        }
        const span = document.createElement('span');
        span.textContent = status;
        return span;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    function authHeaders(t) {
        return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + t };
    }

    async function fetchJson(url, options) {
        const res = await fetch(url, options);
        const data = await res.json().catch(() => ({}));
        if (res.status === 401 || res.status === 403) {
            sessionStorage.clear(); window.location.href = 'login.html';
            throw new Error('Sesión expirada');
        }
        if (!res.ok) throw new Error(data.message || 'Error ' + res.status);
        return data;
    }

    function showMessage(text, isError) {
        const el = document.querySelector('#global-message');
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
