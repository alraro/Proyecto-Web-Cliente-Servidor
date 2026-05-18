const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Capitán';
    
    const userNameEl = document.getElementById('user-name');
	
    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Capitán';
    }

    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
            
        } else if(e.target.id === 'btn-logout'){
            localStorage.clear();
            window.location.href = 'login.html';
        }
    })


    const campaignSelect   = document.getElementById('campaign-select');
    const shiftsContainer  = document.getElementById('shifts-container');

    // ── Carga campañas del capitán ────────────────────────────────────────────

    try {
        const campaigns = await fetchJson(API_BASE + '/api/captain/my-campaigns', {
            headers: authHeaders(token)
        });
        campaignSelect.innerHTML = '<option value="">Selecciona una campaña...</option>';
        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + ' (' + (c.startDate || '') + ' – ' + (c.endDate || '') + ')';
            campaignSelect.appendChild(opt);
        });
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar las campañas', true);
        campaignSelect.innerHTML = '<option value="">Error al cargar campañas</option>';
    }

    // ── Al cambiar campaña → cargar turnos del capitán ────────────────────────

    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        shiftsContainer.innerHTML = '';
        if (!campaignId) return;

        shiftsContainer.innerHTML = '<p class="loading-msg">Cargando turnos...</p>';

        try {
            const shifts = await fetchJson(
                API_BASE + '/api/shifts/my-team?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderShifts(Array.isArray(shifts) ? shifts : []);
        } catch (err) {
            shiftsContainer.innerHTML = '<p class="loading-msg error-msg">Error al cargar los turnos.</p>';
            showMessage(err.message || 'No se pudieron cargar los turnos', true);
        }
    });

    // ── Renderizar tarjetas de turnos ─────────────────────────────────────────

    function renderShifts(shifts) {
        shiftsContainer.innerHTML = '';
        if (!shifts.length) {
            shiftsContainer.innerHTML = '<p class="loading-msg">No tienes turnos asignados en esta campaña.</p>';
            return;
        }

        shifts.forEach(shift => {
            const card = document.createElement('div');
            card.className = 'shift-card';
            card.dataset.shiftId = shift.shiftId;

            const presentCount = (shift.volunteers || []).filter(v => v.attendance).length;
            const totalCount   = (shift.volunteers || []).length;

            card.innerHTML = `
                <div class="shift-card-header">
                    <div class="shift-meta">
                        <span class="shift-store">${escapeHtml(shift.storeName || '')}</span>
                        <span class="shift-date">${formatDate(shift.day)}</span>
                        <span class="shift-time">${shift.startTime || ''} – ${shift.endTime || ''}</span>
                    </div>
                    <div class="attendance-counter" id="counter-${shift.shiftId}">
                        ${presentCount}/${totalCount} presentes
                    </div>
                </div>
                ${shift.observations ? `<p class="shift-obs">${escapeHtml(shift.observations)}</p>` : ''}
                <div class="volunteer-list" id="volunteers-${shift.shiftId}">
                    ${renderVolunteerRows(shift.volunteers || [], shift.shiftId)}
                </div>
                ${!totalCount ? '<p class="no-volunteers">No hay voluntarios asignados a este turno.</p>' : ''}
            `;

            shiftsContainer.appendChild(card);
        });

        // Delegación de eventos para botones de asistencia
        shiftsContainer.addEventListener('click', handleAttendanceClick);
    }

    function renderVolunteerRows(volunteers, shiftId) {
        return volunteers.map(v => `
            <div class="volunteer-row" id="row-${shiftId}-${v.volunteerId}">
                <div class="volunteer-info">
                    <span class="volunteer-name">${escapeHtml(v.volunteerName || '')}</span>
                    ${v.phone ? `<span class="volunteer-phone">${escapeHtml(v.phone)}</span>` : ''}
                </div>
                <div class="attendance-controls">
                    <button
                        class="btn-attendance ${v.attendance ? 'btn-present active' : 'btn-present'}"
                        data-shift-id="${shiftId}"
                        data-volunteer-id="${v.volunteerId}"
                        data-attendance="true"
                        aria-label="Marcar presente"
                        ${v.attendance ? 'disabled' : ''}
                    >Presente</button>
                    <button
                        class="btn-attendance ${!v.attendance ? 'btn-absent active' : 'btn-absent'}"
                        data-shift-id="${shiftId}"
                        data-volunteer-id="${v.volunteerId}"
                        data-attendance="false"
                        aria-label="Marcar ausente"
                        ${!v.attendance ? 'disabled' : ''}
                    >Ausente</button>
                </div>
            </div>
        `).join('');
    }

    // ── Manejar click en botones de asistencia ────────────────────────────────

    async function handleAttendanceClick(e) {
        const btn = e.target.closest('.btn-attendance');
        if (!btn || btn.disabled) return;

        const shiftId     = Number(btn.dataset.shiftId);
        const volunteerId = Number(btn.dataset.volunteerId);
        const attendance  = btn.dataset.attendance === 'true';

        btn.disabled = true;
        btn.classList.add('loading');

        try {
            await fetchJson(API_BASE + '/api/shifts/' + shiftId + '/attendance', {
                method: 'PUT',
                headers: authHeaders(token),
                body: JSON.stringify({ volunteerId, attendance })
            });

            updateVolunteerRow(shiftId, volunteerId, attendance);
            showMessage(
                attendance ? 'Asistencia marcada como Presente' : 'Asistencia marcada como Ausente',
                false
            );
        } catch (err) {
            btn.disabled = false;
            btn.classList.remove('loading');
            showMessage(err.message || 'Error al actualizar la asistencia', true);
        }
    }

    function updateVolunteerRow(shiftId, volunteerId, attendance) {
        const row = document.getElementById('row-' + shiftId + '-' + volunteerId);
        if (!row) return;

        const btnPresent = row.querySelector('[data-attendance="true"]');
        const btnAbsent  = row.querySelector('[data-attendance="false"]');

        if (btnPresent) {
            btnPresent.classList.toggle('active', attendance);
            btnPresent.disabled = attendance;
        }
        if (btnAbsent) {
            btnAbsent.classList.toggle('active', !attendance);
            btnAbsent.disabled = !attendance;
        }

        // Actualizar contador del turno
        const card    = row.closest('.shift-card');
        const counter = document.getElementById('counter-' + shiftId);
        if (card && counter) {
            const rows   = card.querySelectorAll('.volunteer-row');
            const present = card.querySelectorAll('.btn-present.active').length;
            counter.textContent = present + '/' + rows.length + ' presentes';
            counter.className = 'attendance-counter' + (present === rows.length ? ' all-present' : '');
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    function authHeaders(t) {
        return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + t };
    }

    async function fetchJson(url, options) {
        const res  = await fetch(url, options);
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
        return String(v)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;')
            .replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function formatDate(dateStr) {
        if (!dateStr) return '';
        const [y, m, d] = dateStr.split('-');
        return d + '/' + m + '/' + y;
    }
});
