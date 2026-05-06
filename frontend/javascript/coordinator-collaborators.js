const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

    const campaignSelect    = document.getElementById('campaign-select');
    const btnLoad           = document.getElementById('btn-load');
    const tbody             = document.getElementById('collaborators-tbody');
    const formCard          = document.getElementById('form-card');
    const formTitle         = document.getElementById('form-title');
    const editId            = document.getElementById('edit-id');
    const editName          = document.getElementById('edit-name');
    const editPhone         = document.getElementById('edit-phone');
    const editEmail         = document.getElementById('edit-email');
    const editAddress       = document.getElementById('edit-address');
    const editPartnerEntity = document.getElementById('edit-partner-entity');
    const btnNew            = document.getElementById('btn-new');
    const btnSave           = document.getElementById('btn-save');
    const btnCancelForm     = document.getElementById('btn-cancel-form');

    const pendingIds = new Set();
    let partnerEntities = [];

    // Carga campañas y entidades colaboradoras en paralelo
    try {
        const [campaigns, entities] = await Promise.all([
            fetchJson(API_BASE + '/api/coordinator/my-campaigns', { headers: authHeaders(token) }),
            fetchJson(API_BASE + '/api/coordinator/partner-entities', { headers: authHeaders(token) })
        ]);

        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + ' (' + c.startDate + ' - ' + c.endDate + ')';
            campaignSelect.appendChild(opt);
        });

        partnerEntities = Array.isArray(entities) ? entities : [];
        partnerEntities.forEach(pe => {
            const opt = document.createElement('option');
            opt.value = String(pe.id);
            opt.textContent = pe.name;
            editPartnerEntity.appendChild(opt);
        });
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar los datos iniciales', true);
    }

    btnLoad.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        if (!campaignId) { showMessage('Selecciona una campaña', true); return; }
        tbody.innerHTML = "<tr><td colspan='6' class='table-empty'>Cargando...</td></tr>";
        try {
            const volunteers = await fetchJson(
                API_BASE + '/api/coordinator/volunteers?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(volunteers) ? volunteers : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar los colaboradores', true);
            tbody.innerHTML = "<tr><td colspan='6' class='table-empty'>Error al cargar.</td></tr>";
        }
    });

    function renderTable(volunteers) {
        tbody.innerHTML = '';
        if (!volunteers.length) {
            tbody.innerHTML = "<tr><td colspan='6' class='table-empty'>No hay colaboradores registrados.</td></tr>";
            return;
        }
        volunteers.forEach(v => {
            const isPending   = pendingIds.has(v.id);
            const entityLabel = v.partnerEntityName || 'Independiente';
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(v.name || '')}</td>
                <td>${escapeHtml(v.phone || '-')}</td>
                <td>${escapeHtml(v.email || '-')}</td>
                <td>${escapeHtml(entityLabel)}</td>
                <td>${isPending
                    ? '<span class="badge-soon">Pendiente validación</span>'
                    : '<span style="color:#2e7d32;font-weight:600">Activo</span>'}</td>
                <td><button type="button" class="btn btn-sm btn-secondary" data-edit='${JSON.stringify(v)}'>Editar</button></td>
            `;
            tbody.appendChild(tr);
        });
    }

    tbody.addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-edit]');
        if (!btn) return;
        openForm(false, JSON.parse(btn.dataset.edit));
    });

    btnNew.addEventListener('click', () => openForm(true, null));

    function openForm(isNew, v) {
        formCard.hidden = false;
        formTitle.textContent = isNew ? 'Nuevo colaborador' : 'Editar colaborador';
        editId.value      = v ? String(v.id) : '';
        editName.value    = v ? (v.name    || '') : '';
        editPhone.value   = v ? (v.phone   || '') : '';
        editEmail.value   = v ? (v.email   || '') : '';
        editAddress.value = v ? (v.address || '') : '';
        editPartnerEntity.value = v && v.partnerEntityId ? String(v.partnerEntityId) : '';
        formCard.scrollIntoView({ behavior: 'smooth' });
    }

    btnCancelForm.addEventListener('click', () => { formCard.hidden = true; });

    btnSave.addEventListener('click', async () => {
        const id              = editId.value;
        const name            = editName.value.trim();
        const phone           = editPhone.value.trim();
        const email           = editEmail.value.trim();
        const address         = editAddress.value.trim();
        const partnerEntityId = editPartnerEntity.value ? Number(editPartnerEntity.value) : null;

        if (!name) { showMessage('El nombre es obligatorio', true); return; }

        const body = {
            name,
            phone:           phone   || null,
            email:           email   || null,
            address:         address || null,
            partnerEntityId: partnerEntityId
        };

        try {
            if (id) {
                await fetchJson(API_BASE + '/api/coordinator/volunteers/' + id, {
                    method: 'PUT',
                    headers: authHeaders(token),
                    body: JSON.stringify(body)
                });
                showMessage('Colaborador actualizado correctamente', false);
            } else {
                const created = await fetchJson(API_BASE + '/api/coordinator/volunteers', {
                    method: 'POST',
                    headers: authHeaders(token),
                    body: JSON.stringify(body)
                });
                if (created && created.id) pendingIds.add(created.id);
                showMessage('Colaborador creado. Quedará pendiente de validación.', false);
            }
            formCard.hidden = true;
            if (campaignSelect.value) btnLoad.click();
        } catch (err) {
            showMessage(err.message || 'No se pudo guardar', true);
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
        showMessage._t = setTimeout(() => { el.hidden = true; }, 5000);
    }

    function escapeHtml(v) {
        return String(v).replace(/&/g,'&amp;').replace(/</g,'&lt;')
            .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
    }
});
