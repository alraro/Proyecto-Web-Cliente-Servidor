const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }


    const campaignSelect = document.getElementById('campaign-select');
    const storeSelect    = document.getElementById('store-select');
    const btnLoad        = document.getElementById('btn-load');
    const captainsTbody  = document.getElementById('captains-tbody');
    const btnRegister    = document.getElementById('btn-register');

    // Carga campañas
    try {
        const campaigns = await fetchJson(API_BASE + '/api/coordinator/my-campaigns', { headers: authHeaders(token) });
        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + ' (' + c.startDate + ' - ' + c.endDate + ')';
            campaignSelect.appendChild(opt);
        });
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar las campañas', true);
    }

    // Al cambiar campaña → cargar tiendas
    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        storeSelect.innerHTML = '';
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = 'Selecciona una tienda...';
        storeSelect.appendChild(defaultOption);
        storeSelect.disabled = true;
        btnLoad.disabled = true;
        captainsTbody.innerHTML = '';
        const infoRow = document.createElement('tr');
        const infoCell = document.createElement('td');
        infoCell.setAttribute('colspan', '2');
        infoCell.className = 'table-empty';
        infoCell.textContent = 'Selecciona campaña y tienda.';
        infoRow.appendChild(infoCell);
        captainsTbody.appendChild(infoRow);
        if (!campaignId) return;
        try {
            const stores = await fetchJson(
                API_BASE + '/api/coordinator/my-stores?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            (Array.isArray(stores) ? stores : []).forEach(s => {
                const opt = document.createElement('option');
                opt.value = String(s.id);
                opt.textContent = s.name;
                storeSelect.appendChild(opt);
            });
            storeSelect.disabled = false;
            btnLoad.disabled = false;
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las tiendas', true);
        }
    });

    // Cargar capitanes
    btnLoad.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        const storeId    = storeSelect.value;
        if (!campaignId || !storeId) { showMessage('Selecciona campaña y tienda', true); return; }
        captainsTbody.innerHTML = '';
        const loadingRow = document.createElement('tr');
        const loadingCell = document.createElement('td');
        loadingCell.setAttribute('colspan', '2');
        loadingCell.className = 'table-empty';
        loadingCell.textContent = 'Cargando...';
        loadingRow.appendChild(loadingCell);
        captainsTbody.appendChild(loadingRow);
        try {
            const captains = await fetchJson(
                API_BASE + '/api/coordinator/captains?campaignId=' + campaignId + '&storeId=' + storeId,
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(captains) ? captains : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar los capitanes', true);
            captainsTbody.innerHTML = '';
            const errorRow = document.createElement('tr');
            const errorCell = document.createElement('td');
            errorCell.setAttribute('colspan', '2');
            errorCell.className = 'table-empty';
            errorCell.textContent = 'Error al cargar.';
            errorRow.appendChild(errorCell);
            captainsTbody.appendChild(errorRow);
        }
    });

    function renderTable(captains) {
        captainsTbody.innerHTML = '';
        if (!captains.length) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.setAttribute('colspan', '2');
            cell.className = 'table-empty';
            cell.textContent = 'No hay capitanes asignados a esta campaña.';
            row.appendChild(cell);
            captainsTbody.appendChild(row);
            const info = document.getElementById('captains-info');
            if (info) info.hidden = false;
            return;
        }
        const info = document.getElementById('captains-info');
        if (info) info.hidden = true;
        captains.forEach(c => {
            const tr = document.createElement('tr');
            const tdName = document.createElement('td');
            tdName.textContent = escapeHtml(c.name || '');
            tr.appendChild(tdName);
            const tdEmail = document.createElement('td');
            tdEmail.textContent = escapeHtml(c.email || '');
            tr.appendChild(tdEmail);
            captainsTbody.appendChild(tr);
        });
    }

    // Registrar nuevo capitán
    btnRegister.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        const name       = document.getElementById('new-name').value.trim();
        const email      = document.getElementById('new-email').value.trim();
        const password   = document.getElementById('new-password').value;

        if (!campaignId) { showMessage('Selecciona una campaña antes de registrar', true); return; }
        if (!name || !email || !password) { showMessage('Nombre, email y contraseña son obligatorios', true); return; }
        if (password.length < 6) { showMessage('La contraseña debe tener al menos 6 caracteres', true); return; }

        try {
            await fetchJson(API_BASE + '/api/coordinator/captains/register', {
                method: 'POST',
                headers: authHeaders(token),
                body: JSON.stringify({ name, email, password, campaignId: Number(campaignId) })
            });
            showMessage('Solicitud enviada correctamente. El administrador recibirá tu petición y deberá aprobarla.', false);
            document.getElementById('new-name').value = '';
            document.getElementById('new-email').value = '';
            document.getElementById('new-password').value = '';
        } catch (err) {
            showMessage(err.message || 'No se pudo registrar el capitán', true);
        }
    });

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
});
