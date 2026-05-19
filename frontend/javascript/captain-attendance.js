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
        campaignSelect.innerHTML = '';
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = 'Selecciona una campaña...';
        campaignSelect.appendChild(defaultOpt);
        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + ' (' + (c.startDate || '') + ' – ' + (c.endDate || '') + ')';
            campaignSelect.appendChild(opt);
        });
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar las campañas', true);
        campaignSelect.innerHTML = '';
        const errorOpt = document.createElement('option');
        errorOpt.value = '';
        errorOpt.textContent = 'Error al cargar campañas';
        campaignSelect.appendChild(errorOpt);
    }

    // ── Al cambiar campaña → cargar turnos del capitán ────────────────────────

    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        shiftsContainer.innerHTML = '';
        if (!campaignId) return;

        const loadingP = document.createElement('p');
        loadingP.className = 'loading-msg';
        loadingP.textContent = 'Cargando turnos...';
        shiftsContainer.appendChild(loadingP);

        try {
            const shifts = await fetchJson(
                API_BASE + '/api/shifts/my-team?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            renderShifts(Array.isArray(shifts) ? shifts : []);
        } catch (err) {
            const errorP = document.createElement('p');
            errorP.className = 'loading-msg error-msg';
            errorP.textContent = 'Error al cargar los turnos.';
            shiftsContainer.innerHTML = '';
            shiftsContainer.appendChild(errorP);
            showMessage(err.message || 'No se pudieron cargar los turnos', true);
        }
    });

    // ── Renderizar tarjetas de turnos ─────────────────────────────────────────

    function renderShifts(shifts) {
        shiftsContainer.innerHTML = '';
        if (!shifts.length) {
            const emptyP = document.createElement('p');
            emptyP.className = 'loading-msg';
            emptyP.textContent = 'No tienes turnos asignados en esta campaña.';
            shiftsContainer.appendChild(emptyP);
            return;
        }

        shifts.forEach(shift => {
            const card = document.createElement('div');
            card.className = 'shift-card';
            card.dataset.shiftId = shift.shiftId;

            const presentCount = (shift.volunteers || []).filter(v => v.attendance).length;
            const totalCount   = (shift.volunteers || []).length;

            const headerDiv = document.createElement('div');
            headerDiv.className = 'shift-card-header';
            const metaDiv = document.createElement('div');
            metaDiv.className = 'shift-meta';
            const storeSpan = document.createElement('span');
            storeSpan.className = 'shift-store';
            storeSpan.textContent = escapeHtml(shift.storeName || '');
            const dateSpan = document.createElement('span');
            dateSpan.className = 'shift-date';
            dateSpan.textContent = formatDate(shift.day);
            const timeSpan = document.createElement('span');
            timeSpan.className = 'shift-time';
            timeSpan.textContent = (shift.startTime || '') + ' – ' + (shift.endTime || '');
            metaDiv.appendChild(storeSpan);
            metaDiv.appendChild(dateSpan);
            metaDiv.appendChild(timeSpan);
            const counterDiv = document.createElement('div');
            counterDiv.className = 'attendance-counter';
            counterDiv.id = 'counter-' + shift.shiftId;
            counterDiv.textContent = presentCount + '/' + totalCount + ' presentes';
            headerDiv.appendChild(metaDiv);
            headerDiv.appendChild(counterDiv);
            card.appendChild(headerDiv);

            if (shift.observations) {
                const obsP = document.createElement('p');
                obsP.className = 'shift-obs';
                obsP.textContent = escapeHtml(shift.observations);
                card.appendChild(obsP);
            }

            const volunteerListDiv = document.createElement('div');
            volunteerListDiv.className = 'volunteer-list';
            volunteerListDiv.id = 'volunteers-' + shift.shiftId;
            const volunteerRows = renderVolunteerRows(shift.volunteers || [], shift.shiftId);
            volunteerRows.forEach(row => volunteerListDiv.appendChild(row));
            card.appendChild(volunteerListDiv);

            if (!totalCount) {
                const noVolP = document.createElement('p');
                noVolP.className = 'no-volunteers';
                noVolP.textContent = 'No hay voluntarios asignados a este turno.';
                card.appendChild(noVolP);
            }

            shiftsContainer.appendChild(card);
        });

        // Delegación de eventos para botones de asistencia
        shiftsContainer.addEventListener('click', handleAttendanceClick);
    }

    function renderVolunteerRows(volunteers, shiftId) {
        return volunteers.map(v => {
            const rowDiv = document.createElement('div');
            rowDiv.className = 'volunteer-row';
            rowDiv.id = 'row-' + shiftId + '-' + v.volunteerId;
            const infoDiv = document.createElement('div');
            infoDiv.className = 'volunteer-info';
            const nameSpan = document.createElement('span');
            nameSpan.className = 'volunteer-name';
            nameSpan.textContent = escapeHtml(v.volunteerName || '');
            infoDiv.appendChild(nameSpan);
            if (v.phone) {
                const phoneSpan = document.createElement('span');
                phoneSpan.className = 'volunteer-phone';
                phoneSpan.textContent = escapeHtml(v.phone);
                infoDiv.appendChild(phoneSpan);
            }
            rowDiv.appendChild(infoDiv);
            const controlsDiv = document.createElement('div');
            controlsDiv.className = 'attendance-controls';
            const btnPresent = document.createElement('button');
            btnPresent.className = 'btn-attendance ' + (v.attendance ? 'btn-present active' : 'btn-present');
            btnPresent.dataset.shiftId = shiftId;
            btnPresent.dataset.volunteerId = v.volunteerId;
            btnPresent.dataset.attendance = 'true';
            btnPresent.setAttribute('aria-label', 'Marcar presente');
            btnPresent.textContent = 'Presente';
            if (v.attendance) {
                btnPresent.disabled = true;
            }
            const btnAbsent = document.createElement('button');
            btnAbsent.className = 'btn-attendance ' + (!v.attendance ? 'btn-absent active' : 'btn-absent');
            btnAbsent.dataset.shiftId = shiftId;
            btnAbsent.dataset.volunteerId = v.volunteerId;
            btnAbsent.dataset.attendance = 'false';
            btnAbsent.setAttribute('aria-label', 'Marcar ausente');
            btnAbsent.textContent = 'Ausente';
            if (!v.attendance) {
                btnAbsent.disabled = true;
            }
            controlsDiv.appendChild(btnPresent);
            controlsDiv.appendChild(btnAbsent);
            rowDiv.appendChild(controlsDiv);
            return rowDiv;
        });
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
