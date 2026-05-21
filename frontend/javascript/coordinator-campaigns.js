const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }


    const tbody = document.getElementById('campaigns-tbody');

    try {
        const campaigns = await fetchJson(API_BASE + '/api/coordinator/my-campaigns', {
            headers: authHeaders(token)
        });
        renderTable(Array.isArray(campaigns) ? campaigns : []);
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar las campañas', true);
        tbody.innerHTML = '';
        const errorRow = document.createElement('tr');
        const errorCell = document.createElement('td');
        errorCell.setAttribute('colspan', '4');
        errorCell.className = 'table-empty';
        errorCell.textContent = 'Error al cargar.';
        errorRow.appendChild(errorCell);
        tbody.appendChild(errorRow);
    }

    function renderTable(campaigns) {
        tbody.innerHTML = '';
        if (!campaigns.length) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.setAttribute('colspan', '4');
            cell.className = 'table-empty';
            cell.textContent = 'No tienes campañas asignadas.';
            row.appendChild(cell);
            tbody.appendChild(row);
            return;
        }
        campaigns.forEach(c => {
            const tr = document.createElement('tr');
            const tdName = document.createElement('td');
            tdName.textContent = escapeHtml(c.name || '');
            tr.appendChild(tdName);
            const tdType = document.createElement('td');
            tdType.textContent = escapeHtml(c.typeName || '-');
            tr.appendChild(tdType);
            const tdStart = document.createElement('td');
            tdStart.textContent = escapeHtml(c.startDate || '');
            tr.appendChild(tdStart);
            const tdEnd = document.createElement('td');
            tdEnd.textContent = escapeHtml(c.endDate || '');
            tr.appendChild(tdEnd);
            tbody.appendChild(tr);
        });
    }

    function authHeaders(t) {
        return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + t };
    }

    async function fetchJson(url, options) {
        const res = await fetch(url, options);
        const data = await res.json().catch(() => ({}));
        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            window.location.href = 'login.html';
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
});
