const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    

    const campaignSelect  = document.querySelector('#campaign-select');
    const btnLoad         = document.querySelector('#btn-load');
    const storesTbody     = document.querySelector('#stores-tbody');
    const detailPanel     = document.querySelector('#detail-panel');
    const detailTitle     = document.querySelector('#detail-title');
    const shiftsContainer = document.querySelector('#shifts-container');

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
        storesTbody.innerHTML = '';
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 4;
        td.className = 'table-empty';
        td.textContent = 'Cargando...';
        tr.appendChild(td);
        storesTbody.appendChild(tr);
        try {
            const stores = await fetchJson(
                API_BASE + '/api/captain/my-stores?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderStores(Array.isArray(stores) ? stores : [], campaignId);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las tiendas', true);
            storesTbody.innerHTML = '';
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 4;
            td.className = 'table-empty';
            td.textContent = 'Error al cargar.';
            tr.appendChild(td);
            storesTbody.appendChild(tr);
        }
    });

    function renderStores(stores, campaignId) {
        storesTbody.innerHTML = '';
        if (!stores.length) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 4;
            td.className = 'table-empty';
            td.textContent = 'No hay tiendas en esta campaña.';
            tr.appendChild(td);
            storesTbody.appendChild(tr);
            return;
        }
        stores.forEach(s => {
            const tr = document.createElement('tr');
            const td0 = document.createElement('td');
            td0.textContent = escapeHtml(s.name || '');
            tr.appendChild(td0);
            const td1 = document.createElement('td');
            td1.textContent = escapeHtml(s.chainName || '-');
            tr.appendChild(td1);
            const td2 = document.createElement('td');
            td2.textContent = escapeHtml(s.address || '-');
            tr.appendChild(td2);
            const td3 = document.createElement('td');
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn btn-sm btn-secondary';
            btn.dataset.storeId = s.id;
            btn.dataset.storeName = escapeHtml(s.name || '');
            btn.dataset.campaignId = campaignId;
            btn.textContent = 'Ver detalle';
            td3.appendChild(btn);
            tr.appendChild(td3);
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
        shiftsContainer.innerHTML = '';
        const p = document.createElement('p');
        p.style.color = 'var(--text-secondary)';
        p.textContent = 'Cargando...';
        shiftsContainer.appendChild(p);
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
            shiftsContainer.innerHTML = '';
            const p = document.createElement('p');
            p.style.color = '#c62828';
            p.textContent = 'Error al cargar el detalle.';
            shiftsContainer.appendChild(p);
            showMessage(err.message || 'Error al cargar el detalle', true);
        }
    });

    function renderShifts(shifts, volunteerShifts) {
        if (!shifts.length) {
            shiftsContainer.innerHTML = '';
            const p = document.createElement('p');
            p.style.color = 'var(--text-secondary)';
            p.textContent = 'No hay turnos registrados para esta tienda.';
            shiftsContainer.appendChild(p);
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

            const h4 = document.createElement('h4');
            h4.textContent = escapeHtml(s.day || s.shiftDay || '') + ' · ' + escapeHtml(s.startTime || '') + ' - ' + escapeHtml(s.endTime || '');
            div.appendChild(h4);

            const p1 = document.createElement('p');
            p1.textContent = 'Voluntarios necesarios: ';
            const strong = document.createElement('strong');
            strong.textContent = escapeHtml(String(s.volunteersNeeded || 0));
            p1.appendChild(strong);
            div.appendChild(p1);

            if (s.observations) {
                const p2 = document.createElement('p');
                p2.textContent = 'Observaciones: ' + escapeHtml(s.observations);
                div.appendChild(p2);
            }

            const volDiv = document.createElement('div');
            volDiv.style.marginTop = '.5rem';
            if (vols.length) {
                vols.forEach(v => {
                    const span = document.createElement('span');
                    span.className = 'volunteer-pill';
                    span.textContent = escapeHtml(v.volunteerName || v.name || 'Voluntario') + (v.phone ? ' · ' + escapeHtml(v.phone) : '');
                    volDiv.appendChild(span);
                });
            } else {
                const em = document.createElement('em');
                em.style.color = 'var(--text-secondary)';
                em.style.fontSize = '.85rem';
                em.textContent = 'Sin voluntarios asignados';
                volDiv.appendChild(em);
            }
            div.appendChild(volDiv);

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
