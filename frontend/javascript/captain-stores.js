const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Capitán';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

    const campaignSelect  = document.getElementById('campaign-select');
    const btnLoad         = document.getElementById('btn-load');
    const storesTbody     = document.getElementById('stores-tbody');
    const detailPanel     = document.getElementById('detail-panel');
    const detailTitle     = document.getElementById('detail-title');
    const shiftsContainer = document.getElementById('shifts-container');

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

    btnLoad.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        if (!campaignId) { showMessage('Selecciona una campaña', true); return; }
        detailPanel.hidden = true;
        storesTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Cargando...</td></tr>";
        try {
            const stores = await fetchJson(
                API_BASE + '/api/captain/my-stores?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderStores(Array.isArray(stores) ? stores : [], campaignId);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las tiendas', true);
            storesTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Error al cargar.</td></tr>";
        }
    });

    function renderStores(stores, campaignId) {
        storesTbody.innerHTML = '';
        if (!stores.length) {
            storesTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>No hay tiendas en esta campaña.</td></tr>";
            return;
        }
        stores.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(s.name || '')}</td>
                <td>${escapeHtml(s.chainName || '-')}</td>
                <td>${escapeHtml(s.address || '-')}</td>
                <td><button type="button" class="btn btn-sm btn-secondary"
                    data-store-id="${s.id}" data-store-name="${escapeHtml(s.name || '')}"
                    data-campaign-id="${campaignId}">Ver detalle</button></td>
            `;
            storesTbody.appendChild(tr);
        });
    }

    // Delegar click en "Ver detalle"
    storesTbody.addEventListener('click', async (e) => {
        const btn = e.target.closest('button[data-store-id]');
        if (!btn) return;
        const storeId    = btn.dataset.storeId;
        const storeName  = btn.dataset.storeName;
        const campaignId = btn.dataset.campaignId;

        detailTitle.textContent = 'Turnos y voluntarios — ' + storeName;
        shiftsContainer.innerHTML = '<p style="color:var(--text-secondary)">Cargando...</p>';
        detailPanel.hidden = false;
        detailPanel.scrollIntoView({ behavior: 'smooth' });

        try {
            const [shifts, volunteerShifts] = await Promise.all([
                fetchJson(API_BASE + '/api/captain/shifts?campaignId=' + campaignId + '&storeId=' + storeId, { headers: authHeaders(token) }),
                fetchJson(API_BASE + '/api/captain/volunteer-shifts?campaignId=' + campaignId + '&storeId=' + storeId, { headers: authHeaders(token) })
            ]);
            renderShifts(
                Array.isArray(shifts) ? shifts : [],
                Array.isArray(volunteerShifts) ? volunteerShifts : []
            );
        } catch (err) {
            shiftsContainer.innerHTML = '<p style="color:#c62828">Error al cargar el detalle.</p>';
            showMessage(err.message || 'Error al cargar el detalle', true);
        }
    });

    function renderShifts(shifts, volunteerShifts) {
        if (!shifts.length) {
            shiftsContainer.innerHTML = '<p style="color:var(--text-secondary)">No hay turnos registrados para esta tienda.</p>';
            return;
        }

        // Agrupar voluntarios por (shiftDay + startTime)
        const volMap = {};
        volunteerShifts.forEach(vs => {
            const key = (vs.shiftDay || vs.day || '') + '_' + (vs.startTime || '');
            if (!volMap[key]) volMap[key] = [];
            volMap[key].push(vs);
        });

        shiftsContainer.innerHTML = '';
        shifts.forEach(s => {
            const key  = (s.day || s.shiftDay || '') + '_' + (s.startTime || '');
            const vols = volMap[key] || [];
            const div  = document.createElement('div');
            div.className = 'shift-block';

            const volHtml = vols.length
                ? vols.map(v => `<span class="volunteer-pill">${escapeHtml(v.volunteerName || v.name || 'Voluntario')}${v.phone ? ' · ' + escapeHtml(v.phone) : ''}</span>`).join('')
                : '<em style="color:var(--text-secondary);font-size:.85rem">Sin voluntarios asignados</em>';

            div.innerHTML = `
                <h4>${escapeHtml(s.day || s.shiftDay || '')} · ${escapeHtml(s.startTime || '')} - ${escapeHtml(s.endTime || '')}</h4>
                <p>Voluntarios necesarios: <strong>${escapeHtml(String(s.volunteersNeeded || 0))}</strong></p>
                ${s.observations ? '<p>Observaciones: ' + escapeHtml(s.observations) + '</p>' : ''}
                <div style="margin-top:.5rem">${volHtml}</div>
            `;
            shiftsContainer.appendChild(div);
        });
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
});
