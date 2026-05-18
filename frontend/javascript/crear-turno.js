const BACKEND = 'http://localhost:8080';

let selectedCampaign = null;

function getToken() { return localStorage.getItem('token'); }
function authHeaders() {
    return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() };
}
function logout() { localStorage.clear(); window.location.href = 'login.html'; }


document.addEventListener('DOMContentLoaded', () => {
    if (!getToken()) window.location.href = 'login.html';


    const userNameEl = document.getElementById('user-name');
	
    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Coordinador';
    }

    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
            
        } else if(e.target.id === 'btn-logout'){
            logout();
        }
    })

});

// ── Campañas ──────────────────────────────────────────────────────────────────

async function loadCampaigns() {
    try {
        const res = await fetch(BACKEND + '/api/coordinator/my-campaigns', { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (!res.ok) { showMessage('No se pudieron cargar las campañas.', 'error'); return; }
        const data = await res.json();
        const campaigns = Array.isArray(data) ? data : (data.content || []);
        const sel = document.getElementById('campaign-select');
        campaigns.forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.id;
            opt.textContent = `${c.name} (${c.startDate} – ${c.endDate})`;
            opt.dataset.start = c.startDate;
            opt.dataset.end   = c.endDate;
            sel.appendChild(opt);
        });
    } catch {
        showMessage('Error al conectar con el servidor.', 'error');
    }
}

document.getElementById('campaign-select').addEventListener('change', async function () {
    const campaignId = this.value;
    const selOpt     = this.options[this.selectedIndex];
    const storeSel   = document.getElementById('store-select');

        const opt0 = document.createElement('option');
        opt0.value = '';
        opt0.textContent = 'Cargando tiendas...';
        storeSel.innerHTML = '';
        storeSel.appendChild(opt0);
    storeSel.disabled   = true;
    document.getElementById('shift-form-card').classList.add('hidden');
    document.getElementById('shifts-card').classList.add('hidden');
    selectedCampaign = null;

    if (!campaignId) {
        const opt0 = document.createElement('option');
        opt0.value = '';
        opt0.textContent = 'Selecciona primero una campaña...';
        storeSel.innerHTML = '';
        storeSel.appendChild(opt0);
        return;
    }

    selectedCampaign = { id: campaignId, start: selOpt.dataset.start, end: selOpt.dataset.end };

    // Restringir el selector de fecha al rango de la campaña
    const dayInput = document.getElementById('shift-day');
    dayInput.min = selectedCampaign.start;
    dayInput.max = selectedCampaign.end;

    try {
        const res = await fetch(`${BACKEND}/api/shifts/campaign/${campaignId}/stores`, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (!res.ok) {
            const opt0 = document.createElement('option');
            opt0.value = '';
            opt0.textContent = 'Error al cargar tiendas';
            storeSel.innerHTML = '';
            storeSel.appendChild(opt0);
            return;
        }
        const stores = await res.json();
        const opt0 = document.createElement('option');
        opt0.value = '';
        opt0.textContent = 'Selecciona una tienda...';
        storeSel.innerHTML = '';
        storeSel.appendChild(opt0);
        if (!stores.length) {
            const opt0 = document.createElement('option');
            opt0.value = '';
            opt0.textContent = 'Sin tiendas asignadas a esta campaña';
            storeSel.innerHTML = '';
            storeSel.appendChild(opt0);
            return;
        }
        stores.forEach(s => {
            const opt = document.createElement('option');
            opt.value       = s.id;
            opt.textContent = s.name;
            storeSel.appendChild(opt);
        });
        storeSel.disabled = false;

        document.getElementById('shift-form-card').classList.remove('hidden');
        await loadShifts(campaignId);
        document.getElementById('shifts-card').classList.remove('hidden');
    } catch {
        const opt0 = document.createElement('option');
        opt0.value = '';
        opt0.textContent = 'Error al cargar tiendas';
        storeSel.innerHTML = '';
        storeSel.appendChild(opt0);
    }
});

// ── Turnos ────────────────────────────────────────────────────────────────────

async function loadShifts(campaignId) {
    const container = document.getElementById('shifts-container');
    container.innerHTML = '';
    const p = document.createElement('p');
    p.className = 'empty-message';
    p.textContent = 'Cargando...';
    container.appendChild(p);
    try {
        const res = await fetch(`${BACKEND}/api/shifts?campaignId=${campaignId}`, { headers: authHeaders() });
        if (!res.ok) {
            container.innerHTML = '';
            const pErr = document.createElement('p');
            pErr.className = 'empty-message';
            pErr.textContent = 'Error al cargar los turnos.';
            container.appendChild(pErr);
            return;
        }
        renderShifts(await res.json());
    } catch {
        container.innerHTML = '';
        const pErr = document.createElement('p');
        pErr.className = 'empty-message';
        pErr.textContent = 'Error al conectar con el servidor.';
        container.appendChild(pErr);
    }
}

function renderShifts(shifts) {
    const container = document.getElementById('shifts-container');
    if (!shifts.length) {
        container.innerHTML = '';
        const p = document.createElement('p');
        p.className = 'empty-message';
        p.textContent = 'No hay turnos creados para esta campaña.';
        container.appendChild(p);
        return;
    }
    container.innerHTML = '';
    shifts.forEach(s => {
        const div = document.createElement('div');
        div.className = 'shift-item';

        const shiftInfo = document.createElement('div');
        shiftInfo.className = 'shift-info';

        const h4 = document.createElement('h4');
        h4.textContent = escHtml(s.storeName);
        shiftInfo.appendChild(h4);

        const p = document.createElement('p');
        p.textContent = s.day + ' \u00a0\u00b7\u00a0 ' + s.startTime + ' \u2013 ' + s.endTime;
        shiftInfo.appendChild(p);

        const shiftDetails = document.createElement('div');
        shiftDetails.className = 'shift-details';

        const spanVol = document.createElement('span');
        spanVol.className = 'shift-detail';
        spanVol.textContent = '\ud83d\udc64 ' + s.volunteersNeeded + ' voluntarios';
        shiftDetails.appendChild(spanVol);

        if (s.location) {
            const spanLoc = document.createElement('span');
            spanLoc.className = 'shift-detail';
            spanLoc.textContent = '\ud83d\udccd ' + escHtml(s.location);
            shiftDetails.appendChild(spanLoc);
        }

        if (s.observations) {
            const spanObs = document.createElement('span');
            spanObs.className = 'shift-detail';
            spanObs.textContent = '\ud83d\udcdd ' + escHtml(s.observations);
            shiftDetails.appendChild(spanObs);
        }

        shiftInfo.appendChild(shiftDetails);
        div.appendChild(shiftInfo);

        const btn = document.createElement('button');
        btn.className = 'btn-edit';
        btn.style.whiteSpace = 'nowrap';
        btn.textContent = 'Asignar \u2192';
        btn.setAttribute('onclick', 'openAssignModal(' + s.shiftId + ')');
        div.appendChild(btn);

        container.appendChild(div);
    });
}

document.getElementById('btn-refresh').addEventListener('click', () => {
    if (selectedCampaign) loadShifts(selectedCampaign.id);
});

// ── Envío del formulario ──────────────────────────────────────────────────────

document.getElementById('btn-submit').addEventListener('click', async () => {
    const campaignId       = document.getElementById('campaign-select').value;
    const storeId          = document.getElementById('store-select').value;
    const day              = document.getElementById('shift-day').value;
    const startTime        = document.getElementById('start-time').value;
    const endTime          = document.getElementById('end-time').value;
    const volunteersRaw    = document.getElementById('volunteers-needed').value;
    const location         = document.getElementById('location').value.trim();
    const observations     = document.getElementById('observations').value.trim();

    // Validación cliente
    if (!campaignId)    { showMessage('Selecciona una campaña.',  'error'); return; }
    if (!storeId)       { showMessage('Selecciona una tienda.',   'error'); return; }
    if (!day)           { showMessage('El día es obligatorio.',   'error'); return; }
    if (!startTime)     { showMessage('La hora de inicio es obligatoria.', 'error'); return; }
    if (!endTime)       { showMessage('La hora de fin es obligatoria.',    'error'); return; }

    const volunteers = parseInt(volunteersRaw, 10);
    if (!volunteersRaw || isNaN(volunteers) || volunteers <= 0) {
        showMessage('El número de voluntarios debe ser mayor que 0.', 'error'); return;
    }
    if (startTime >= endTime) {
        showMessage('La hora de inicio debe ser anterior a la hora de fin.', 'error'); return;
    }
    if (selectedCampaign && (day < selectedCampaign.start || day > selectedCampaign.end)) {
        showMessage(
            `El día debe estar dentro del rango de la campaña (${selectedCampaign.start} – ${selectedCampaign.end}).`,
            'error'
        );
        return;
    }

    try {
        const res = await fetch(`${BACKEND}/api/shifts`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({
                campaignId:       parseInt(campaignId, 10),
                storeId:          parseInt(storeId, 10),
                day,
                startTime,
                endTime,
                volunteersNeeded: volunteers,
                location:         location     || null,
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

document.getElementById('btn-reset').addEventListener('click', resetForm);

function resetForm() {
    document.getElementById('shift-day').value         = '';
    document.getElementById('start-time').value        = '';
    document.getElementById('end-time').value          = '';
    document.getElementById('volunteers-needed').value = '';
    document.getElementById('location').value          = '';
    document.getElementById('observations').value      = '';
    const msg = document.getElementById('form-message');
    msg.className   = 'form-message';
    msg.textContent = '';
}

function showMessage(text, type) {
    const el = document.getElementById('form-message');
    el.textContent = text;
    el.className   = 'form-message ' + type;
}

function escHtml(v) {
    return String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// ── Modal de asignación ───────────────────────────────────────────────────────

let currentShiftId = null;

document.getElementById('modal-close').addEventListener('click', closeAssignModal);
document.getElementById('assignment-modal').addEventListener('click', e => {
    if (e.target === document.getElementById('assignment-modal')) closeAssignModal();
});

function closeAssignModal() {
    document.getElementById('assignment-modal').classList.add('hidden');
    currentShiftId = null;
}

async function openAssignModal(shiftId) {
    currentShiftId = shiftId;
    document.getElementById('assignment-modal').classList.remove('hidden');
    clearModalFeedback();
    await loadModalData(shiftId);
}

async function loadModalData(shiftId) {
    try {
        const [volData, capData, availVol, availCap] = await Promise.all([
            apiFetch(`${BACKEND}/api/shifts/${shiftId}/volunteers`),
            apiFetch(`${BACKEND}/api/shifts/${shiftId}/captains`),
            apiFetch(`${BACKEND}/api/shifts/${shiftId}/available-volunteers`),
            apiFetch(`${BACKEND}/api/shifts/${shiftId}/available-captains`)
        ]);

        // Info del turno
        document.getElementById('modal-title').textContent = `Asignaciones — Turno #${shiftId}`;
        document.getElementById('modal-shift-info').textContent =
            `Voluntarios: ${volData.volunteersAssigned} / ${volData.volunteersNeeded} asignados`;

        // Indicador de aforo
        const capEl = document.getElementById('capacity-indicator');
        const full = volData.volunteersAssigned >= volData.volunteersNeeded;
        capEl.textContent = full
            ? '⚠️ Aforo completo'
            : `${volData.volunteersAssigned} de ${volData.volunteersNeeded} voluntarios asignados`;
        capEl.style.color = full ? 'var(--error-color)' : 'var(--success-color)';

        renderModalVolunteers(volData.volunteers || []);
        renderModalCaptains(capData.captains || []);
        populateSelect('volunteer-select', availVol, 'volunteerId', 'name', 'Selecciona un voluntario...',
            v => v.name + (v.phone ? ' · ' + v.phone : '') +
                 (v.partnerEntityName ? ' (' + v.partnerEntityName + ')' : ' (Independiente)'));
        populateSelect('captain-select',   availCap, 'userId',     'name', 'Selecciona un capitán...');
    } catch (e) {
        document.getElementById('modal-shift-info').textContent = 'Error al cargar los datos del turno.';
    }
}

function renderModalVolunteers(volunteers) {
    const el = document.getElementById('modal-volunteers');
    if (!volunteers.length) {
        el.innerHTML = '';
        const p = document.createElement('p');
        p.style.fontSize = '.88rem';
        p.style.color = 'var(--text-light)';
        p.textContent = 'Sin voluntarios asignados.';
        el.appendChild(p);
        return;
    }
    el.innerHTML = '';
    volunteers.forEach(v => {
        const div = document.createElement('div');
        div.style.display = 'flex';
        div.style.justifyContent = 'space-between';
        div.style.alignItems = 'center';
        div.style.padding = '.4rem 0';
        div.style.borderBottom = '1px solid var(--border-color)';

        const span = document.createElement('span');
        span.style.fontSize = '.9rem';
        span.textContent = escHtml(v.name) + ' (' + escHtml(v.email || '') + ')';
        div.appendChild(span);

        const btn = document.createElement('button');
        btn.className = 'btn-secondary';
        btn.style.padding = '.25rem .75rem';
        btn.style.fontSize = '.8rem';
        btn.textContent = 'Quitar';
        btn.setAttribute('onclick', 'unassignVolunteer(' + v.volunteerId + ')');
        div.appendChild(btn);

        el.appendChild(div);
    });
}

function renderModalCaptains(captains) {
    const el = document.getElementById('modal-captains');
    if (!captains.length) {
        el.innerHTML = '';
        const p = document.createElement('p');
        p.style.fontSize = '.88rem';
        p.style.color = 'var(--text-light)';
        p.textContent = 'Sin capitanes asignados.';
        el.appendChild(p);
        return;
    }
    el.innerHTML = '';
    captains.forEach(c => {
        const div = document.createElement('div');
        div.style.display = 'flex';
        div.style.justifyContent = 'space-between';
        div.style.alignItems = 'center';
        div.style.padding = '.4rem 0';
        div.style.borderBottom = '1px solid var(--border-color)';

        const span = document.createElement('span');
        span.style.fontSize = '.9rem';
        span.textContent = escHtml(c.name) + ' (' + escHtml(c.email || '') + ')';
        div.appendChild(span);

        const btn = document.createElement('button');
        btn.className = 'btn-secondary';
        btn.style.padding = '.25rem .75rem';
        btn.style.fontSize = '.8rem';
        btn.textContent = 'Quitar';
        btn.setAttribute('onclick', 'unassignCaptain(' + c.userId + ')');
        div.appendChild(btn);

        el.appendChild(div);
    });
}

function populateSelect(selectId, items, valueKey, labelKey, placeholder, labelFn) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = '';
    const defaultOpt = document.createElement('option');
    defaultOpt.value = '';
    defaultOpt.textContent = escHtml(placeholder);
    sel.appendChild(defaultOpt);
    items.forEach(item => {
        const opt = document.createElement('option');
        opt.value       = item[valueKey];
        opt.textContent = labelFn ? labelFn(item) : item[labelKey];
        sel.appendChild(opt);
    });
}

document.getElementById('btn-assign-volunteer').addEventListener('click', async () => {
    const volunteerId = document.getElementById('volunteer-select').value;
    if (!volunteerId) { showFeedback('volunteer', 'Selecciona un voluntario.', 'error'); return; }
    try {
        const res = await fetch(`${BACKEND}/api/shifts/${currentShiftId}/volunteers`, {
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

document.getElementById('btn-assign-captain').addEventListener('click', async () => {
    const userId = document.getElementById('captain-select').value;
    if (!userId) { showFeedback('captain', 'Selecciona un capitán.', 'error'); return; }
    try {
        const res = await fetch(`${BACKEND}/api/shifts/${currentShiftId}/captains`, {
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
        const res = await fetch(`${BACKEND}/api/shifts/${currentShiftId}/volunteers/${volunteerId}`, {
            method: 'DELETE', headers: authHeaders()
        });
        if (!res.ok) { const d = await res.json(); showFeedback('volunteer', d.message, 'error'); return; }
        await loadModalData(currentShiftId);
    } catch { showFeedback('volunteer', 'Error de conexión.', 'error'); }
}

async function unassignCaptain(userId) {
    try {
        const res = await fetch(`${BACKEND}/api/shifts/${currentShiftId}/captains/${userId}`, {
            method: 'DELETE', headers: authHeaders()
        });
        if (!res.ok) { const d = await res.json(); showFeedback('captain', d.message, 'error'); return; }
        await loadModalData(currentShiftId);
    } catch { showFeedback('captain', 'Error de conexión.', 'error'); }
}

function showFeedback(type, text, level) {
    const el = document.getElementById(`${type}-feedback`);
    el.textContent = text;
    el.classList.remove('feedback-hidden');
    el.className = 'form-message ' + (level === 'warning' ? 'error' : level);
    // 'warning' usa estilo error (naranja/rojo) para que sea visualmente llamativo (RF-28)
}

function clearModalFeedback() {
    ['volunteer-feedback', 'captain-feedback'].forEach(id => {
        const el = document.getElementById(id);
        el.classList.add('feedback-hidden');
        el.textContent = '';
    });
}

async function apiFetch(url) {
    const res = await fetch(url, { headers: authHeaders() });
    if (res.status === 401 || res.status === 403) { logout(); return; }
    return res.json();
}

loadCampaigns();
