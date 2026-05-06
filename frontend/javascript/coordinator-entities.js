const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

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

        entitiesTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Cargando...</td></tr>";
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
            entitiesTbody.innerHTML = "<tr><td colspan='4' class='table-empty'>Error al cargar.</td></tr>";
        }
    });

    function renderEntities(entities, campaignId) {
        entitiesTbody.innerHTML = '';

        if (!entities.length) {
            entitiesTbody.innerHTML = `
                <tr><td colspan="4" class="table-empty">
                    No hay entidades colaboradoras con voluntarios asignados en esta campaña.<br>
                    <span style="font-size:.85rem;">Puedes asignar voluntarios desde la sección
                    <a href="coordinator-volunteers.html" style="color:var(--blue-700)">Asignación de Voluntarios</a>.</span>
                </td></tr>`;
            return;
        }

        entities.forEach(entity => {
            const rowId    = 'detail-' + entity.id;
            const entityVolunteers = volunteersCache.filter(v => v.partnerEntityId === entity.id);

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(entity.name || '')}</td>
                <td>${escapeHtml(entity.phone || '-')}</td>
                <td>${entity.volunteerCount}</td>
                <td>
                    <button type="button" class="btn btn-sm btn-secondary" data-target="${rowId}">
                        Ver voluntarios
                    </button>
                </td>
            `;
            entitiesTbody.appendChild(tr);

            // Fila expandible con voluntarios
            const detailRow = document.createElement('tr');
            detailRow.id = rowId;
            detailRow.hidden = true;
            detailRow.innerHTML = `
                <td colspan="4" style="background:#f8fafc;padding:.75rem 1.25rem;">
                    ${buildVolunteerDetail(entityVolunteers)}
                </td>
            `;
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
            return '<p style="margin:0;font-size:.88rem;color:var(--text-secondary,#6b7280)">No se encontraron voluntarios de esta entidad en el registro local.</p>';
        }
        const rows = volunteers.map(v =>
            `<tr>
                <td style="padding:.35rem .75rem">${escapeHtml(v.name || '')}</td>
                <td style="padding:.35rem .75rem">${escapeHtml(v.phone || '-')}</td>
                <td style="padding:.35rem .75rem">${escapeHtml(v.email || '-')}</td>
            </tr>`
        ).join('');
        return `
            <table style="width:100%;font-size:.88rem;border-collapse:collapse">
                <thead>
                    <tr style="color:var(--text-secondary,#6b7280)">
                        <th style="padding:.35rem .75rem;text-align:left;font-weight:600">Nombre</th>
                        <th style="padding:.35rem .75rem;text-align:left;font-weight:600">Teléfono</th>
                        <th style="padding:.35rem .75rem;text-align:left;font-weight:600">Email</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>`;
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
