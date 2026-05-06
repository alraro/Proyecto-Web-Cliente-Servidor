const BACKEND = 'http://localhost:8080';

let selectedCampaign = null;

function getToken() { return localStorage.getItem('token'); }
function authHeaders() {
    return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() };
}
function logout() { localStorage.clear(); window.location.href = 'login.html'; }

if (!getToken()) window.location.href = 'login.html';
document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
document.getElementById('btn-logout').addEventListener('click', logout);

// ── Campañas ──────────────────────────────────────────────────────────────────

async function loadCampaigns() {
    try {
        const res = await fetch(BACKEND + '/api/campaigns?size=200&sort=startDate,desc', { headers: authHeaders() });
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

    storeSel.innerHTML  = '<option value="">Cargando tiendas...</option>';
    storeSel.disabled   = true;
    document.getElementById('shift-form-card').style.display = 'none';
    document.getElementById('shifts-card').style.display     = 'none';
    selectedCampaign = null;

    if (!campaignId) {
        storeSel.innerHTML = '<option value="">Selecciona primero una campaña...</option>';
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
            storeSel.innerHTML = '<option value="">Error al cargar tiendas</option>';
            return;
        }
        const stores = await res.json();
        storeSel.innerHTML = '<option value="">Selecciona una tienda...</option>';
        if (!stores.length) {
            storeSel.innerHTML = '<option value="">Sin tiendas asignadas a esta campaña</option>';
            return;
        }
        stores.forEach(s => {
            const opt = document.createElement('option');
            opt.value       = s.id;
            opt.textContent = s.name;
            storeSel.appendChild(opt);
        });
        storeSel.disabled = false;

        document.getElementById('shift-form-card').style.display = 'block';
        await loadShifts(campaignId);
        document.getElementById('shifts-card').style.display = 'block';
    } catch {
        storeSel.innerHTML = '<option value="">Error al cargar tiendas</option>';
    }
});

// ── Turnos ────────────────────────────────────────────────────────────────────

async function loadShifts(campaignId) {
    const container = document.getElementById('shifts-container');
    container.innerHTML = '<p class="empty-message">Cargando...</p>';
    try {
        const res = await fetch(`${BACKEND}/api/shifts?campaignId=${campaignId}`, { headers: authHeaders() });
        if (!res.ok) { container.innerHTML = '<p class="empty-message">Error al cargar los turnos.</p>'; return; }
        renderShifts(await res.json());
    } catch {
        container.innerHTML = '<p class="empty-message">Error al conectar con el servidor.</p>';
    }
}

function renderShifts(shifts) {
    const container = document.getElementById('shifts-container');
    if (!shifts.length) {
        container.innerHTML = '<p class="empty-message">No hay turnos creados para esta campaña.</p>';
        return;
    }
    container.innerHTML = shifts.map(s => `
        <div class="shift-item">
            <div class="shift-info">
                <h4>${escHtml(s.storeName)}</h4>
                <p>${escHtml(s.day)} &nbsp;·&nbsp; ${escHtml(s.startTime)} – ${escHtml(s.endTime)}</p>
                <div class="shift-details">
                    <span class="shift-detail">👤 ${s.volunteersNeeded} voluntarios</span>
                    ${s.location     ? `<span class="shift-detail">📍 ${escHtml(s.location)}</span>`     : ''}
                    ${s.observations ? `<span class="shift-detail">📝 ${escHtml(s.observations)}</span>` : ''}
                </div>
            </div>
            <button class="btn-edit" onclick="openAssignModal(${s.shiftId})" style="white-space:nowrap;">Asignar →</button>
        </div>
    `).join('');
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
        populateSelect('volunteer-select', availVol, 'volunteerId', 'name', 'Selecciona un voluntario...');
        populateSelect('captain-select',   availCap, 'userId',     'name', 'Selecciona un capitán...');
    } catch (e) {
        document.getElementById('modal-shift-info').textContent = 'Error al cargar los datos del turno.';
    }
}

function renderModalVolunteers(volunteers) {
    const el = document.getElementById('modal-volunteers');
    if (!volunteers.length) {
        el.innerHTML = '<p style="font-size:.88rem;color:var(--text-light);">Sin voluntarios asignados.</p>';
        return;
    }
    el.innerHTML = volunteers.map(v => `
        <div style="display:flex;justify-content:space-between;align-items:center;padding:.4rem 0;border-bottom:1px solid var(--border-color);">
            <span style="font-size:.9rem;">${escHtml(v.name)} <span style="color:var(--text-light)">(${escHtml(v.email || '')})</span></span>
            <button class="btn-secondary" style="padding:.25rem .75rem;font-size:.8rem;" onclick="unassignVolunteer(${v.volunteerId})">Quitar</button>
        </div>
    `).join('');
}

function renderModalCaptains(captains) {
    const el = document.getElementById('modal-captains');
    if (!captains.length) {
        el.innerHTML = '<p style="font-size:.88rem;color:var(--text-light);">Sin capitanes asignados.</p>';
        return;
    }
    el.innerHTML = captains.map(c => `
        <div style="display:flex;justify-content:space-between;align-items:center;padding:.4rem 0;border-bottom:1px solid var(--border-color);">
            <span style="font-size:.9rem;">${escHtml(c.name)} <span style="color:var(--text-light)">(${escHtml(c.email || '')})</span></span>
            <button class="btn-secondary" style="padding:.25rem .75rem;font-size:.8rem;" onclick="unassignCaptain(${c.userId})">Quitar</button>
        </div>
    `).join('');
}

function populateSelect(selectId, items, valueKey, labelKey, placeholder) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = `<option value="">${escHtml(placeholder)}</option>`;
    items.forEach(item => {
        const opt = document.createElement('option');
        opt.value       = item[valueKey];
        opt.textContent = item[labelKey];
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
    el.style.display = 'block';
    el.className = 'form-message ' + (level === 'warning' ? 'error' : level);
    // 'warning' usa estilo error (naranja/rojo) para que sea visualmente llamativo (RF-28)
}

function clearModalFeedback() {
    ['volunteer-feedback', 'captain-feedback'].forEach(id => {
        const el = document.getElementById(id);
        el.style.display = 'none';
        el.textContent = '';
    });
}

async function apiFetch(url) {
    const res = await fetch(url, { headers: authHeaders() });
    if (res.status === 401 || res.status === 403) { logout(); return; }
    return res.json();
}

loadCampaigns();
