const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

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
        storeSelect.innerHTML = '<option value="">Selecciona una tienda...</option>';
        storeSelect.disabled = true;
        btnLoad.disabled = true;
        captainsTbody.innerHTML = "<tr><td colspan='2' class='table-empty'>Selecciona campaña y tienda.</td></tr>";
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
        captainsTbody.innerHTML = "<tr><td colspan='2' class='table-empty'>Cargando...</td></tr>";
        try {
            const captains = await fetchJson(
                API_BASE + '/api/coordinator/captains?campaignId=' + campaignId + '&storeId=' + storeId,
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(captains) ? captains : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar los capitanes', true);
            captainsTbody.innerHTML = "<tr><td colspan='2' class='table-empty'>Error al cargar.</td></tr>";
        }
    });

    function renderTable(captains) {
        captainsTbody.innerHTML = '';
        if (!captains.length) {
            captainsTbody.innerHTML = "<tr><td colspan='2' class='table-empty'>No hay capitanes asignados a esta campaña.</td></tr>";
            return;
        }
        captains.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(c.name || '')}</td>
                <td>${escapeHtml(c.email || '')}</td>
            `;
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
            showMessage('Capitán registrado. Quedará pendiente de validación por el administrador.', false);
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
