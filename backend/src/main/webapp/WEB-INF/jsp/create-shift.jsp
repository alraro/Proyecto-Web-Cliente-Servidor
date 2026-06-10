<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");

    if (token == null || !"COORDINADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Crear Turno</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/coordinator.css">
    <link rel="stylesheet" href="/css/create-shift.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Coordinador" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">

    <div class="page-header">
        <a href="/coordinator-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Crear turno de recogida</h1>
                <p>Define turnos de voluntarios para una campaña y tienda, y asigna participantes.</p>
            </div>
        </div>
    </div>

    <div class="form-card">
        <h3>Selecciona campaña y tienda</h3>
        <div class="form-row">
            <div class="form-group">
                <label for="campaign-select">Campaña <span class="required-asterisk">*</span></label>
                <select id="campaign-select">
                    <option value="">Selecciona una campaña...</option>
                </select>
            </div>
            <div class="form-group">
                <label for="store-select">Tienda <span class="required-asterisk">*</span></label>
                <select id="store-select" disabled>
                    <option value="">Selecciona primero una campaña...</option>
                </select>
            </div>
        </div>
    </div>

    <div class="form-card hidden" id="shift-form-card">
        <h3>Datos del turno</h3>

        <div class="form-row">
            <div class="form-group">
                <label for="shift-day">Día <span class="required-asterisk">*</span></label>
                <input type="date" id="shift-day">
            </div>
            <div class="form-group">
                <label for="volunteers-needed">Nº voluntarios necesarios <span class="required-asterisk">*</span></label>
                <input type="number" id="volunteers-needed" min="1" placeholder="Ej: 5">
            </div>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label for="start-time">Hora de inicio <span class="required-asterisk">*</span></label>
                <input type="time" id="start-time">
            </div>
            <div class="form-group">
                <label for="end-time">Hora de fin <span class="required-asterisk">*</span></label>
                <input type="time" id="end-time">
            </div>
        </div>

        <div class="form-group">
            <label for="location">Ubicación específica</label>
            <input type="text" id="location" placeholder="Ej: Entrada principal, junto a los carritos" maxlength="500">
        </div>

        <div class="form-group">
            <label for="observations">Observaciones</label>
            <textarea id="observations" rows="3" placeholder="Observaciones adicionales..." maxlength="1000"></textarea>
        </div>

        <div id="form-message" class="form-message"></div>

        <div class="form-actions">
            <button type="button" class="btn-primary" id="btn-submit">Crear turno</button>
            <button type="button" class="btn-secondary" id="btn-reset">Limpiar</button>
        </div>
    </div>

    <div class="shifts-list-card hidden" id="shifts-card">
        <div class="shifts-list-header">
            <h3>Turnos creados para esta campaña</h3>
            <button class="btn-refresh" id="btn-refresh">↻ Actualizar</button>
        </div>
        <div id="shifts-container">
            <p class="empty-message">Cargando turnos...</p>
        </div>
    </div>

</main>

<div class="coordinator-modal-overlay hidden" id="assignment-modal">
    <div class="coordinator-modal-content">
        <span class="coordinator-modal-close" id="modal-close" role="button" aria-label="Cerrar">×</span>
        <h3 id="modal-title">Asignaciones del turno</h3>

        <div id="modal-shift-info" class="shift-info-box"></div>

        <h4 class="modal-section-title">Voluntarios</h4>
        <div id="capacity-indicator" class="capacity-indicator"></div>
        <div id="modal-volunteers" class="modal-assignments-list"></div>
        <div class="form-group">
            <label>Añadir voluntario</label>
            <div class="modal-assign-row">
                <select id="volunteer-select" class="modal-select">
                    <option value="">Selecciona un voluntario...</option>
                </select>
                <button id="btn-assign-volunteer" class="btn-primary">Asignar</button>
            </div>
            <p id="volunteer-feedback" class="form-message feedback-hidden"></p>
        </div>

        <h4 class="modal-section-title modal-section-title-spaced">Capitanes</h4>
        <div id="modal-captains" class="modal-assignments-list"></div>
        <div class="form-group">
            <label>Añadir capitán</label>
            <div class="modal-assign-row">
                <select id="captain-select" class="modal-select">
                    <option value="">Selecciona un capitán...</option>
                </select>
                <button id="btn-assign-captain" class="btn-primary">Asignar</button>
            </div>
            <p id="captain-feedback" class="form-message feedback-hidden"></p>
        </div>
    </div>
</div>

<script>
    const TOKEN = '<%= token %>';

    function authHeaders() {
        return { 'Authorization': 'Bearer ' + TOKEN, 'Content-Type': 'application/json' };
    }

    let selectedCampaign = null;
    let currentShiftId   = null;

    async function loadCampaigns() {
        try {
            const res = await fetch('/api/coordinator/my-campaigns', { headers: authHeaders() });
            if (res.status === 401 || res.status === 403) { window.location.href = '/logout'; return; }
            if (!res.ok) { showMessage('No se pudieron cargar las campañas.', 'error'); return; }
            const data = await res.json();
            const campaigns = Array.isArray(data) ? data : (data.content || []);
            const sel = document.querySelector('#campaign-select');
            campaigns.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.name + ' (' + c.startDate + ' – ' + c.endDate + ')';
                opt.setAttribute('data-start', c.startDate);
                opt.setAttribute('data-end', c.endDate);
                sel.appendChild(opt);
            });
        } catch {
            showMessage('Error al conectar con el servidor.', 'error');
        }
    }

    document.querySelector('#campaign-select').addEventListener('change', async function () {
        const campaignId = this.value;
        const selOpt     = this.options[this.selectedIndex];
        const storeSel   = document.querySelector('#store-select');

        storeSel.innerHTML = '<option value="">Cargando tiendas...</option>';
        storeSel.disabled  = true;
        document.querySelector('#shift-form-card').className = 'form-card hidden';
        document.querySelector('#shifts-card').className = 'shifts-list-card hidden';
        selectedCampaign = null;

        if (!campaignId) {
            storeSel.innerHTML = '<option value="">Selecciona primero una campaña...</option>';
            return;
        }

        selectedCampaign = {
            id:    campaignId,
            start: selOpt.getAttribute('data-start'),
            end:   selOpt.getAttribute('data-end')
        };

        const dayInput = document.querySelector('#shift-day');
        dayInput.min = selectedCampaign.start;
        dayInput.max = selectedCampaign.end;

        try {
            const res = await fetch('/api/shifts/campaign/' + campaignId + '/stores', { headers: authHeaders() });
            if (res.status === 401 || res.status === 403) { window.location.href = '/logout'; return; }
            if (!res.ok) { storeSel.innerHTML = '<option value="">Error al cargar tiendas</option>'; return; }
            const stores = await res.json();
            if (!stores.length) {
                storeSel.innerHTML = '<option value="">Sin tiendas asignadas a esta campaña</option>';
                return;
            }
            storeSel.innerHTML = '<option value="">Selecciona una tienda...</option>';
            stores.forEach(s => {
                const opt = document.createElement('option');
                opt.value       = s.id;
                opt.textContent = s.name;
                storeSel.appendChild(opt);
            });
            storeSel.disabled = false;
            document.querySelector('#shift-form-card').className = 'form-card';
            await loadShifts(campaignId);
            document.querySelector('#shifts-card').className = 'shifts-list-card';
        } catch {
            storeSel.innerHTML = '<option value="">Error al cargar tiendas</option>';
        }
    });

    async function loadShifts(campaignId) {
        const container = document.querySelector('#shifts-container');
        container.innerHTML = '<p class="empty-message">Cargando...</p>';
        try {
            const res = await fetch('/api/shifts?campaignId=' + campaignId, { headers: authHeaders() });
            if (!res.ok) { container.innerHTML = '<p class="empty-message">Error al cargar los turnos.</p>'; return; }
            renderShifts(await res.json());
        } catch {
            container.innerHTML = '<p class="empty-message">Error al conectar con el servidor.</p>';
        }
    }

    function renderShifts(shifts) {
        const container = document.querySelector('#shifts-container');
        if (!shifts.length) {
            container.innerHTML = '<p class="empty-message">No hay turnos creados para esta campaña.</p>';
            return;
        }
        container.innerHTML = '';
        shifts.forEach(s => {
            const div = document.createElement('div');
            div.className = 'shift-item';

            const info = document.createElement('div');
            info.className = 'shift-info';

            const h4 = document.createElement('h4');
            h4.textContent = s.storeName;
            info.appendChild(h4);

            const p = document.createElement('p');
            p.textContent = s.day + '  ·  ' + s.startTime + ' – ' + s.endTime;
            info.appendChild(p);

            const details = document.createElement('div');
            details.className = 'shift-details';

            const spanVol = document.createElement('span');
            spanVol.className = 'shift-detail';
            spanVol.textContent = '👤 ' + s.volunteersNeeded + ' voluntarios';
            details.appendChild(spanVol);

            if (s.location) {
                const spanLoc = document.createElement('span');
                spanLoc.className = 'shift-detail';
                spanLoc.textContent = '📍 ' + s.location;
                details.appendChild(spanLoc);
            }
            if (s.observations) {
                const spanObs = document.createElement('span');
                spanObs.className = 'shift-detail';
                spanObs.textContent = '📝 ' + s.observations;
                details.appendChild(spanObs);
            }

            info.appendChild(details);
            div.appendChild(info);

            const btn = document.createElement('button');
            btn.className = 'btn-edit';
            btn.style.whiteSpace = 'nowrap';
            btn.textContent = 'Asignar →';
            btn.addEventListener('click', function () { openAssignModal(s.shiftId); });
            div.appendChild(btn);

            container.appendChild(div);
        });
    }

    document.querySelector('#btn-refresh').addEventListener('click', () => {
        if (selectedCampaign) loadShifts(selectedCampaign.id);
    });

    document.querySelector('#btn-submit').addEventListener('click', async () => {
        const campaignId    = document.querySelector('#campaign-select').value;
        const storeId       = document.querySelector('#store-select').value;
        const day           = document.querySelector('#shift-day').value;
        const startTime     = document.querySelector('#start-time').value;
        const endTime       = document.querySelector('#end-time').value;
        const volunteersRaw = document.querySelector('#volunteers-needed').value;
        const location      = document.querySelector('#location').value.trim();
        const observations  = document.querySelector('#observations').value.trim();

        if (!campaignId)  { showMessage('Selecciona una campaña.',  'error'); return; }
        if (!storeId)     { showMessage('Selecciona una tienda.',   'error'); return; }
        if (!day)         { showMessage('El día es obligatorio.',   'error'); return; }
        if (!startTime)   { showMessage('La hora de inicio es obligatoria.', 'error'); return; }
        if (!endTime)     { showMessage('La hora de fin es obligatoria.',    'error'); return; }

        const volunteers = parseInt(volunteersRaw, 10);
        if (!volunteersRaw || isNaN(volunteers) || volunteers <= 0) {
            showMessage('El número de voluntarios debe ser mayor que 0.', 'error'); return;
        }
        if (startTime >= endTime) {
            showMessage('La hora de inicio debe ser anterior a la hora de fin.', 'error'); return;
        }
        if (selectedCampaign && (day < selectedCampaign.start || day > selectedCampaign.end)) {
            showMessage('El día debe estar dentro del rango de la campaña (' + selectedCampaign.start + ' – ' + selectedCampaign.end + ').', 'error');
            return;
        }

        try {
            const res = await fetch('/api/shifts', {
                method:  'POST',
                headers: authHeaders(),
                body:    JSON.stringify({
                    campaignId:       parseInt(campaignId, 10),
                    storeId:          parseInt(storeId, 10),
                    day,
                    startTime,
                    endTime,
                    volunteersNeeded: volunteers,
                    location:         location    || null,
                    observations:     observations || null
                })
            });
            const data = await res.json();
            if (!res.ok) { showMessage(data.message || 'Error al crear el turno.', 'error'); return; }
            showMessage('Turno creado correctamente.', 'success');
            resetForm();
            loadShifts(campaignId);
        } catch {
            showMessage('Error de conexión con el servidor.', 'error');
        }
    });

    document.querySelector('#btn-reset').addEventListener('click', resetForm);

    function resetForm() {
        document.querySelector('#shift-day').value         = '';
        document.querySelector('#start-time').value        = '';
        document.querySelector('#end-time').value          = '';
        document.querySelector('#volunteers-needed').value = '';
        document.querySelector('#location').value          = '';
        document.querySelector('#observations').value      = '';
        const msg = document.querySelector('#form-message');
        msg.className   = 'form-message';
        msg.textContent = '';
    }

    function showMessage(text, type) {
        const el = document.querySelector('#form-message');
        el.textContent = text;
        el.className   = 'form-message ' + type;
    }

    // --- Assignment modal ---

    document.querySelector('#modal-close').addEventListener('click', closeAssignModal);
    document.querySelector('#assignment-modal').addEventListener('click', e => {
        if (e.target === document.querySelector('#assignment-modal')) closeAssignModal();
    });

    function closeAssignModal() {
        document.querySelector('#assignment-modal').className = 'coordinator-modal-overlay hidden';
        currentShiftId = null;
    }

    async function openAssignModal(shiftId) {
        currentShiftId = shiftId;
        document.querySelector('#assignment-modal').className = 'coordinator-modal-overlay';
        clearModalFeedback();
        await loadModalData(shiftId);
    }

    async function loadModalData(shiftId) {
        try {
            const results = await Promise.all([
                apiFetch('/api/shifts/' + shiftId + '/volunteers'),
                apiFetch('/api/shifts/' + shiftId + '/captains'),
                apiFetch('/api/shifts/' + shiftId + '/available-volunteers'),
                apiFetch('/api/shifts/' + shiftId + '/available-captains')
            ]);
            const volData  = results[0];
            const capData  = results[1];
            const availVol = results[2];
            const availCap = results[3];

            document.querySelector('#modal-title').textContent = 'Asignaciones — Turno #' + shiftId;
            document.querySelector('#modal-shift-info').textContent =
                'Voluntarios: ' + volData.volunteersAssigned + ' / ' + volData.volunteersNeeded + ' asignados';

            const capEl = document.querySelector('#capacity-indicator');
            const full  = volData.volunteersAssigned >= volData.volunteersNeeded;
            capEl.textContent = full
                ? '⚠️ Aforo completo'
                : volData.volunteersAssigned + ' de ' + volData.volunteersNeeded + ' voluntarios asignados';
            capEl.style.color = full ? 'var(--error-color)' : 'var(--success-color)';

            renderModalList('modal-volunteers', volData.volunteers || [], v =>
                v.name + ' (' + (v.email || '') + ')', v => unassignVolunteer(v.volunteerId));
            renderModalList('modal-captains', capData.captains || [], c =>
                c.name + ' (' + (c.email || '') + ')', c => unassignCaptain(c.userId));

            populateSelect('volunteer-select', availVol, 'volunteerId', 'Selecciona un voluntario...', v =>
                v.name + (v.phone ? ' · ' + v.phone : '') + (v.partnerEntityName ? ' (' + v.partnerEntityName + ')' : ' (Independiente)'));
            populateSelect('captain-select', availCap, 'userId', 'Selecciona un capitán...', c => c.name);
        } catch {
            document.querySelector('#modal-shift-info').textContent = 'Error al cargar los datos del turno.';
        }
    }

    function renderModalList(containerId, items, labelFn, onRemove) {
        const el = document.querySelector('#' + containerId);
        if (!items.length) {
            el.innerHTML = '<p style="font-size:.88rem;color:var(--text-light)">Sin asignaciones.</p>';
            return;
        }
        el.innerHTML = '';
        items.forEach(item => {
            const div = document.createElement('div');
            div.style.cssText = 'display:flex;justify-content:space-between;align-items:center;padding:.4rem 0;border-bottom:1px solid var(--border-color)';
            const span = document.createElement('span');
            span.style.fontSize = '.9rem';
            span.textContent = labelFn(item);
            div.appendChild(span);
            const btn = document.createElement('button');
            btn.className = 'btn-secondary';
            btn.style.cssText = 'padding:.25rem .75rem;font-size:.8rem';
            btn.textContent = 'Quitar';
            btn.addEventListener('click', function () { onRemove(item); });
            div.appendChild(btn);
            el.appendChild(div);
        });
    }

    function populateSelect(selectId, items, valueKey, placeholder, labelFn) {
        const sel = document.querySelector('#' + selectId);
        sel.innerHTML = '<option value="">' + placeholder + '</option>';
        items.forEach(item => {
            const opt = document.createElement('option');
            opt.value       = item[valueKey];
            opt.textContent = labelFn(item);
            sel.appendChild(opt);
        });
    }

    document.querySelector('#btn-assign-volunteer').addEventListener('click', async () => {
        const volunteerId = document.querySelector('#volunteer-select').value;
        if (!volunteerId) { showFeedback('volunteer', 'Selecciona un voluntario.', 'error'); return; }
        try {
            const res = await fetch('/api/shifts/' + currentShiftId + '/volunteers', {
                method: 'POST', headers: authHeaders(),
                body: JSON.stringify({ volunteerId: parseInt(volunteerId, 10) })
            });
            const data = await res.json();
            if (!res.ok) {
                const isAlert = data.conflict === 'OVERLAP' || data.conflict === 'CAPACITY_EXCEEDED';
                showFeedback('volunteer', data.message || 'Error al asignar.', isAlert ? 'warning' : 'error');
                return;
            }
            showFeedback('volunteer', data.message, 'success');
            await loadModalData(currentShiftId);
        } catch { showFeedback('volunteer', 'Error de conexión.', 'error'); }
    });

    document.querySelector('#btn-assign-captain').addEventListener('click', async () => {
        const userId = document.querySelector('#captain-select').value;
        if (!userId) { showFeedback('captain', 'Selecciona un capitán.', 'error'); return; }
        try {
            const res = await fetch('/api/shifts/' + currentShiftId + '/captains', {
                method: 'POST', headers: authHeaders(),
                body: JSON.stringify({ userId: parseInt(userId, 10) })
            });
            const data = await res.json();
            if (!res.ok) {
                const isAlert = data.conflict === 'OVERLAP';
                showFeedback('captain', data.message || 'Error al asignar.', isAlert ? 'warning' : 'error');
                return;
            }
            showFeedback('captain', data.message, 'success');
            await loadModalData(currentShiftId);
        } catch { showFeedback('captain', 'Error de conexión.', 'error'); }
    });

    async function unassignVolunteer(volunteerId) {
        try {
            const res = await fetch('/api/shifts/' + currentShiftId + '/volunteers/' + volunteerId, {
                method: 'DELETE', headers: authHeaders()
            });
            if (!res.ok) { const d = await res.json(); showFeedback('volunteer', d.message, 'error'); return; }
            await loadModalData(currentShiftId);
        } catch { showFeedback('volunteer', 'Error de conexión.', 'error'); }
    }

    async function unassignCaptain(userId) {
        try {
            const res = await fetch('/api/shifts/' + currentShiftId + '/captains/' + userId, {
                method: 'DELETE', headers: authHeaders()
            });
            if (!res.ok) { const d = await res.json(); showFeedback('captain', d.message, 'error'); return; }
            await loadModalData(currentShiftId);
        } catch { showFeedback('captain', 'Error de conexión.', 'error'); }
    }

    function showFeedback(type, text, level) {
        const el = document.querySelector('#' + type + '-feedback');
        el.textContent = text;
        el.className   = 'form-message ' + (level === 'warning' ? 'error' : level);
    }

    function clearModalFeedback() {
        ['volunteer-feedback', 'captain-feedback'].forEach(id => {
            const el = document.querySelector('#' + id);
            el.className   = 'form-message feedback-hidden';
            el.textContent = '';
        });
    }

    async function apiFetch(url) {
        const res = await fetch(url, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { window.location.href = '/logout'; return; }
        return res.json();
    }

    loadCampaigns();
</script>

</body>
</html>
