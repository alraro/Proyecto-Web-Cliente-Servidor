/* Bootstrap auth from URL params (when navigating from the SSR portal). */
handleUrlTokenParams();

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    

    const campaignSelect = document.querySelector('#campaign-select');
    const storeSelect    = document.querySelector('#store-select');
    const description    = document.querySelector('#description');
    const btnSubmit      = document.querySelector('#btn-submit');
    const incidentsTbody = document.querySelector('#incidents-tbody');

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
        storeSelect.innerHTML = '';
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = 'Selecciona una tienda...';
        storeSelect.appendChild(defaultOpt);
        storeSelect.disabled = true;
        incidentsTbody.innerHTML = '';
        const initRow = document.createElement('tr');
        const initTd = document.createElement('td');
        initTd.colSpan = 4;
        initTd.className = 'table-empty';
        initTd.textContent = 'Selecciona campaña y tienda para ver el historial.';
        initRow.appendChild(initTd);
        incidentsTbody.appendChild(initRow);
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
        incidentsTbody.innerHTML = '';
        const loadingRow = document.createElement('tr');
        const loadingTd = document.createElement('td');
        loadingTd.colSpan = 4;
        loadingTd.className = 'table-empty';
        loadingTd.textContent = 'Cargando...';
        loadingRow.appendChild(loadingTd);
        incidentsTbody.appendChild(loadingRow);
        try {
            const incidents = await fetchJson(
                API_BASE + '/api/captain/incidents?campaignId=' + campaignId + '&storeId=' + storeId,
                { headers: authHeaders(token) }
            );
            renderIncidents(Array.isArray(incidents) ? incidents : []);
        } catch (err) {
            incidentsTbody.innerHTML = '';
            const errorRow = document.createElement('tr');
            const errorTd = document.createElement('td');
            errorTd.colSpan = 4;
            errorTd.className = 'table-empty';
            errorTd.textContent = 'Error al cargar el historial.';
            errorRow.appendChild(errorTd);
            incidentsTbody.appendChild(errorRow);
        }
    });

    function renderIncidents(incidents) {
        incidentsTbody.innerHTML = '';
        if (!incidents.length) {
            const emptyRow = document.createElement('tr');
            const emptyTd = document.createElement('td');
            emptyTd.colSpan = 4;
            emptyTd.className = 'table-empty';
            emptyTd.textContent = 'No hay incidencias registradas.';
            emptyRow.appendChild(emptyTd);
            incidentsTbody.appendChild(emptyRow);
            return;
        }
        incidents.forEach(i => {
            const tr = document.createElement('tr');
            const tdDate = document.createElement('td');
            tdDate.textContent = escapeHtml(i.createdAt || i.date || '-');
            const tdCampaign = document.createElement('td');
            tdCampaign.textContent = escapeHtml(i.campaignName || '-');
            const tdStore = document.createElement('td');
            tdStore.textContent = escapeHtml(i.storeName || '-');
            const tdDesc = document.createElement('td');
            tdDesc.textContent = escapeHtml(i.description || '');
            tr.appendChild(tdDate);
            tr.appendChild(tdCampaign);
            tr.appendChild(tdStore);
            tr.appendChild(tdDesc);
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
