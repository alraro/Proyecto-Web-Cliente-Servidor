const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }


    const campaignSelect = document.getElementById('campaign-select');
    const btnLoad        = document.getElementById('btn-load');
    const entitiesTbody  = document.getElementById('entities-tbody');

    // Cache de voluntarios por campaña para los paneles expandibles
    let volunteersCache = [];

    // Carga campañas
    try {
        const campaigns = await fetchJson(
            API_BASE + '/api/coordinator/my-campaigns',
            { headers: authHeaders(token) }
        );
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

        const loadingRow = document.createElement('tr');
        const loadingTd = document.createElement('td');
        loadingTd.colSpan = 4;
        loadingTd.className = 'table-empty';
        loadingTd.textContent = 'Cargando...';
        loadingRow.appendChild(loadingTd);
        entitiesTbody.innerHTML = '';
        entitiesTbody.appendChild(loadingRow);
        volunteersCache = [];

        try {
            const [entities, volunteers] = await Promise.all([
                fetchJson(
                    API_BASE + '/api/coordinator/campaign-entities?campaignId=' + campaignId,
                    { headers: authHeaders(token) }
                ),
                fetchJson(
                    API_BASE + '/api/coordinator/volunteers?campaignId=' + campaignId,
                    { headers: authHeaders(token) }
                )
            ]);

            volunteersCache = Array.isArray(volunteers) ? volunteers : [];
            renderEntities(Array.isArray(entities) ? entities : [], campaignId);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las entidades', true);
            entitiesTbody.innerHTML = '';
            const errorRow = document.createElement('tr');
            const errorTd = document.createElement('td');
            errorTd.colSpan = 4;
            errorTd.className = 'table-empty';
            errorTd.textContent = 'Error al cargar.';
            errorRow.appendChild(errorTd);
            entitiesTbody.appendChild(errorRow);
        }
    });

    function renderEntities(entities, campaignId) {
        entitiesTbody.innerHTML = '';

        if (!entities.length) {
            const emptyRow = document.createElement('tr');
            const emptyTd = document.createElement('td');
            emptyTd.colSpan = 4;
            emptyTd.className = 'table-empty';
            const emptyText = document.createTextNode('No hay entidades colaboradoras con voluntarios asignados en esta campaña.');
            const emptyBr = document.createElement('br');
            const emptySpan = document.createElement('span');
            emptySpan.style.fontSize = '.85rem';
            const emptySpanText = document.createTextNode('Puedes asignar voluntarios desde la sección ');
            const emptyLink = document.createElement('a');
            emptyLink.href = 'coordinator-volunteers.html';
            emptyLink.style.color = 'var(--blue-700)';
            emptyLink.textContent = 'Asignación de Voluntarios';
            const emptySpanEnd = document.createTextNode('.');
            emptySpan.appendChild(emptySpanText);
            emptySpan.appendChild(emptyLink);
            emptySpan.appendChild(emptySpanEnd);
            emptyTd.appendChild(emptyText);
            emptyTd.appendChild(emptyBr);
            emptyTd.appendChild(emptySpan);
            emptyRow.appendChild(emptyTd);
            entitiesTbody.appendChild(emptyRow);
            return;
        }

        entities.forEach(entity => {
            const rowId    = 'detail-' + entity.id;
            const entityVolunteers = volunteersCache.filter(v => v.partnerEntityId === entity.id);

            const tr = document.createElement('tr');
            const tdName = document.createElement('td');
            tdName.textContent = escapeHtml(entity.name || '');
            const tdPhone = document.createElement('td');
            tdPhone.textContent = escapeHtml(entity.phone || '-');
            const tdCount = document.createElement('td');
            tdCount.textContent = entity.volunteerCount;
            const tdActions = document.createElement('td');
            const viewBtn = document.createElement('button');
            viewBtn.type = 'button';
            viewBtn.className = 'btn btn-sm btn-secondary';
            viewBtn.dataset.target = rowId;
            viewBtn.textContent = 'Ver voluntarios';
            tdActions.appendChild(viewBtn);
            tr.appendChild(tdName);
            tr.appendChild(tdPhone);
            tr.appendChild(tdCount);
            tr.appendChild(tdActions);
            entitiesTbody.appendChild(tr);

            // Fila expandible con voluntarios
            const detailRow = document.createElement('tr');
            detailRow.id = rowId;
            detailRow.hidden = true;
            const detailTd = document.createElement('td');
            detailTd.colSpan = 4;
            detailTd.style.background = '#f8fafc';
            detailTd.style.padding = '.75rem 1.25rem';
            detailTd.appendChild(buildVolunteerDetail(entityVolunteers));
            detailRow.appendChild(detailTd);
            entitiesTbody.appendChild(detailRow);
        });

        // Toggle expand/collapse
        entitiesTbody.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-target]');
            if (!btn) return;
            const target = document.getElementById(btn.dataset.target);
            if (!target) return;
            target.hidden = !target.hidden;
            btn.textContent = target.hidden ? 'Ver voluntarios' : 'Ocultar';
        });
    }

    function buildVolunteerDetail(volunteers) {
        if (!volunteers.length) {
            const p = document.createElement('p');
            p.style.margin = '0';
            p.style.fontSize = '.88rem';
            p.style.color = 'var(--text-secondary,#6b7280)';
            p.textContent = 'No se encontraron voluntarios de esta entidad en el registro local.';
            return p;
        }
        const table = document.createElement('table');
        table.style.width = '100%';
        table.style.fontSize = '.88rem';
        table.style.borderCollapse = 'collapse';
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        headerRow.style.color = 'var(--text-secondary,#6b7280)';
        const thName = document.createElement('th');
        thName.style.padding = '.35rem .75rem';
        thName.style.textAlign = 'left';
        thName.style.fontWeight = '600';
        thName.textContent = 'Nombre';
        const thPhone = document.createElement('th');
        thPhone.style.padding = '.35rem .75rem';
        thPhone.style.textAlign = 'left';
        thPhone.style.fontWeight = '600';
        thPhone.textContent = 'Teléfono';
        const thEmail = document.createElement('th');
        thEmail.style.padding = '.35rem .75rem';
        thEmail.style.textAlign = 'left';
        thEmail.style.fontWeight = '600';
        thEmail.textContent = 'Email';
        headerRow.appendChild(thName);
        headerRow.appendChild(thPhone);
        headerRow.appendChild(thEmail);
        thead.appendChild(headerRow);
        table.appendChild(thead);
        const tbody = document.createElement('tbody');
        volunteers.forEach(v => {
            const row = document.createElement('tr');
            const tdName = document.createElement('td');
            tdName.style.padding = '.35rem .75rem';
            tdName.textContent = escapeHtml(v.name || '');
            const tdPhone = document.createElement('td');
            tdPhone.style.padding = '.35rem .75rem';
            tdPhone.textContent = escapeHtml(v.phone || '-');
            const tdEmail = document.createElement('td');
            tdEmail.style.padding = '.35rem .75rem';
            tdEmail.textContent = escapeHtml(v.email || '-');
            row.appendChild(tdName);
            row.appendChild(tdPhone);
            row.appendChild(tdEmail);
            tbody.appendChild(row);
        });
        table.appendChild(tbody);
        return table;
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
