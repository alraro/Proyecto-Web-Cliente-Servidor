const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Capitán';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

    const campaignSelect = document.getElementById('campaign-select');
    const storeSelect    = document.getElementById('store-select');
    const description    = document.getElementById('description');
    const btnSubmit      = document.getElementById('btn-submit');
    const incidentsTbody = document.getElementById('incidents-tbody');

    // Carga campañas del capitán
    try {
        const campaigns = await fetchJson(API_BASE + '/api/captain/my-campaigns', { headers: authHeaders(token) });
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
        incidentsTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Selecciona campaña y tienda para ver el historial.</td></tr>";
        if (!campaignId) return;
        try {
            const stores = await fetchJson(
                API_BASE + '/api/captain/my-stores?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            (Array.isArray(stores) ? stores : []).forEach(s => {
                const opt = document.createElement('option');
                opt.value = String(s.id);
                opt.textContent = s.name;
                storeSelect.appendChild(opt);
            });
            storeSelect.disabled = false;
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las tiendas', true);
        }
    });

    // Al cambiar tienda → cargar historial
    storeSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        const storeId    = storeSelect.value;
        if (!campaignId || !storeId) return;
        incidentsTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Cargando...</td></tr>";
        try {
            const incidents = await fetchJson(
                API_BASE + '/api/captain/incidents?campaignId=' + campaignId + '&storeId=' + storeId,
                { headers: authHeaders(token) }
            );
            renderIncidents(Array.isArray(incidents) ? incidents : []);
        } catch (err) {
            incidentsTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Error al cargar el historial.</td></tr>";
        }
    });

    function renderIncidents(incidents) {
        incidentsTbody.innerHTML = '';
        if (!incidents.length) {
            incidentsTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>No hay incidencias registradas.</td></tr>";
            return;
        }
        incidents.forEach(i => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(i.createdAt || i.date || '-')}</td>
                <td>${escapeHtml(i.campaignName || '-')}</td>
                <td>${escapeHtml(i.storeName || '-')}</td>
                <td>${escapeHtml(i.description || '')}</td>
            `;
            incidentsTbody.appendChild(tr);
        });
    }

    // Enviar incidencia
    btnSubmit.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        const storeId    = storeSelect.value;
        const desc       = description.value.trim();

        if (!campaignId) { showMessage('Selecciona una campaña', true); return; }
        if (!storeId)    { showMessage('Selecciona una tienda', true); return; }
        if (!desc)       { showMessage('La descripción es obligatoria', true); return; }

        try {
            await fetchJson(API_BASE + '/api/captain/incidents', {
                method: 'POST',
                headers: authHeaders(token),
                body: JSON.stringify({
                    campaignId: Number(campaignId),
                    storeId:    Number(storeId),
                    description: desc
                })
            });
            showMessage('Incidencia registrada correctamente', false);
            description.value = '';
            // Refrescar historial
            storeSelect.dispatchEvent(new Event('change'));
        } catch (err) {
            showMessage(err.message || 'No se pudo registrar la incidencia', true);
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
