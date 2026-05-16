const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'login.html';
    });

    const campaignSelect = document.getElementById('campaign-select');
    const btnLoad        = document.getElementById('btn-load');
    const tbody          = document.getElementById('stores-tbody');

    try {
        const campaigns = await fetchJson(API_BASE + '/api/coordinator/my-campaigns', {
            headers: authHeaders(token)
        });
        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + ' (' + c.startDate + ' - ' + c.endDate + ')';
            campaignSelect.appendChild(opt);
        });
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar las campañas', true);
    }

    btnLoad.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        if (!campaignId) { showMessage('Selecciona una campaña', true); return; }
        tbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Cargando...</td></tr>";
        try {
            const stores = await fetchJson(
                API_BASE + '/api/coordinator/my-stores?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(stores) ? stores : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las tiendas', true);
            tbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Error al cargar.</td></tr>";
        }
    });

    function renderTable(stores) {
        tbody.innerHTML = '';
        if (!stores.length) {
            tbody.innerHTML = "<tr><td colspan='4' class='table-empty'>No hay tiendas en esta campaña.</td></tr>";
            return;
        }
        stores.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(s.name || '')}</td>
                <td>${escapeHtml(s.chainName || '-')}</td>
                <td>${escapeHtml(s.locality || '-')}</td>
                <td>${escapeHtml(s.address || '-')}</td>
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
});
