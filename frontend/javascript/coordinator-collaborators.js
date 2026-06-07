/* Bootstrap auth from URL params (when navigating from the SSR portal). */
handleUrlTokenParams();

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }


    const campaignSelect    = document.querySelector('#campaign-select');
    const btnLoad           = document.querySelector('#btn-load');
    const tbody             = document.querySelector('#collaborators-tbody');
    const formCard          = document.querySelector('#form-card');
    const formTitle         = document.querySelector('#form-title');
    const editId            = document.querySelector('#edit-id');
    const editName          = document.querySelector('#edit-name');
    const editPhone         = document.querySelector('#edit-phone');
    const editEmail         = document.querySelector('#edit-email');
    const editAddress       = document.querySelector('#edit-address');
    const editPartnerEntity = document.querySelector('#edit-partner-entity');
    const btnNew            = document.querySelector('#btn-new');
    const btnSave           = document.querySelector('#btn-save');
    const btnCancelForm     = document.querySelector('#btn-cancel-form');

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
        tbody.innerHTML = '';
        const loadingRow = document.createElement('tr');
        const loadingCell = document.createElement('td');
        loadingCell.setAttribute('colspan', '6');
        loadingCell.className = 'table-empty';
        loadingCell.textContent = 'Cargando...';
        loadingRow.appendChild(loadingCell);
        tbody.appendChild(loadingRow);
        try {
            const volunteers = await fetchJson(
                API_BASE + '/api/coordinator/volunteers?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderTable(Array.isArray(volunteers) ? volunteers : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar los colaboradores', true);
            tbody.innerHTML = '';
            const errorRow = document.createElement('tr');
            const errorCell = document.createElement('td');
            errorCell.setAttribute('colspan', '6');
            errorCell.className = 'table-empty';
            errorCell.textContent = 'Error al cargar.';
            errorRow.appendChild(errorCell);
            tbody.appendChild(errorRow);
        }
    });

    function renderTable(volunteers) {
        tbody.innerHTML = '';
        if (!volunteers.length) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.setAttribute('colspan', '6');
            cell.className = 'table-empty';
            cell.textContent = 'No hay colaboradores registrados.';
            row.appendChild(cell);
            tbody.appendChild(row);
            return;
        }
        volunteers.forEach(v => {
            const isPending   = pendingIds.has(v.id);
            const entityLabel = v.partnerEntityName || 'Independiente';
            const tr = document.createElement('tr');
            const tdName = document.createElement('td');
            tdName.textContent = escapeHtml(v.name || '');
            tr.appendChild(tdName);
            const tdPhone = document.createElement('td');
            tdPhone.textContent = escapeHtml(v.phone || '-');
            tr.appendChild(tdPhone);
            const tdEmail = document.createElement('td');
            tdEmail.textContent = escapeHtml(v.email || '-');
            tr.appendChild(tdEmail);
            const tdEntity = document.createElement('td');
            tdEntity.textContent = escapeHtml(entityLabel);
            tr.appendChild(tdEntity);
            const tdStatus = document.createElement('td');
            if (isPending) {
                const badge = document.createElement('span');
                badge.className = 'badge-soon';
                badge.textContent = 'Pendiente validación';
                tdStatus.appendChild(badge);
            } else {
                const activeSpan = document.createElement('span');
                activeSpan.style.color = '#2e7d32';
                activeSpan.style.fontWeight = '600';
                activeSpan.textContent = 'Activo';
                tdStatus.appendChild(activeSpan);
            }
            tr.appendChild(tdStatus);
            const tdAction = document.createElement('td');
            const btnEdit = document.createElement('button');
            btnEdit.type = 'button';
            btnEdit.className = 'btn btn-sm btn-secondary';
            btnEdit.setAttribute('data-edit', JSON.stringify(v));
            btnEdit.textContent = 'Editar';
            tdAction.appendChild(btnEdit);
            tr.appendChild(tdAction);
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
        const el = document.querySelector('#global-message');
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
