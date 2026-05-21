const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }


    const campaignSelect = document.querySelector('#campaign-select');
    const btnLoad        = document.querySelector('#btn-load');
    const tbody          = document.querySelector('#stores-tbody');

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
        tbody.innerHTML = '';
        const loadingRow = document.createElement('tr');
        const loadingTd = document.createElement('td');
        loadingTd.colSpan = 4;
        loadingTd.className = 'table-empty';
        loadingTd.textContent = 'Cargando...';
        loadingRow.appendChild(loadingTd);
        tbody.appendChild(loadingRow);
        try {
            const stores = await fetchJson(
                API_BASE + '/api/coordinator/my-stores?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(stores) ? stores : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las tiendas', true);
            tbody.innerHTML = '';
            const errorRow = document.createElement('tr');
            const errorTd = document.createElement('td');
            errorTd.colSpan = 4;
            errorTd.className = 'table-empty';
            errorTd.textContent = 'Error al cargar.';
            errorRow.appendChild(errorTd);
            tbody.appendChild(errorRow);
        }
    });

    function renderTable(stores) {
        tbody.innerHTML = '';
        if (!stores.length) {
            const emptyRow = document.createElement('tr');
            const emptyTd = document.createElement('td');
            emptyTd.colSpan = 4;
            emptyTd.className = 'table-empty';
            emptyTd.textContent = 'No hay tiendas en esta campaña.';
            emptyRow.appendChild(emptyTd);
            tbody.appendChild(emptyRow);
            return;
        }
        stores.forEach(s => {
            const tr = document.createElement('tr');
            const tdName = document.createElement('td');
            tdName.textContent = escapeHtml(s.name || '');
            const tdChain = document.createElement('td');
            tdChain.textContent = escapeHtml(s.chainName || '-');
            const tdLocality = document.createElement('td');
            tdLocality.textContent = escapeHtml(s.locality || '-');
            const tdAddress = document.createElement('td');
            tdAddress.textContent = escapeHtml(s.address || '-');
            tr.appendChild(tdName);
            tr.appendChild(tdChain);
            tr.appendChild(tdLocality);
            tr.appendChild(tdAddress);
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
});
