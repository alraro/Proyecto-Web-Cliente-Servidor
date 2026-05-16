const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'login.html';
    });

    const tbody = document.getElementById('campaigns-tbody');

    try {
        const campaigns = await fetchJson(API_BASE + '/api/coordinator/my-campaigns', {
            headers: authHeaders(token)
        });
        renderTable(Array.isArray(campaigns) ? campaigns : []);
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar las campañas', true);
        tbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Error al cargar.</td></tr>";
    }

    function renderTable(campaigns) {
        tbody.innerHTML = '';
        if (!campaigns.length) {
            tbody.innerHTML = "<tr><td colspan='4' class='table-empty'>No tienes campañas asignadas.</td></tr>";
            return;
        }
        campaigns.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(c.name || '')}</td>
                <td>${escapeHtml(c.typeName || '-')}</td>
                <td>${escapeHtml(c.startDate || '')}</td>
                <td>${escapeHtml(c.endDate || '')}</td>
            `;
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
