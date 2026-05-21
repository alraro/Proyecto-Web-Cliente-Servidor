
document.addEventListener('DOMContentLoaded', async () => {
    if (!getToken()) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Capitán';


    const campaignSelect   = document.getElementById('campaign-select');
    const shiftsContainer  = document.getElementById('shifts-container');


    try {
        const campaigns = await apiFetch('/api/captain/my-campaigns');
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


    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        shiftsContainer.innerHTML = '';
        if (!campaignId) return;

        const loadingP = document.createElement('p');
        loadingP.className = 'loading-msg';
        loadingP.textContent = 'Cargando turnos...';
        shiftsContainer.appendChild(loadingP);

        try {
            const shifts = await apiFetch('/api/shifts/my-team?campaignId=' + campaignId);
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
            card.setAttribute('data-shift-id', shift.shiftId);

            const presentCount = (shift.volunteers || []).filter(v => v.attendance).length;
            const totalCount   = (shift.volunteers || []).length;

            const headerDiv = document.createElement('div');
            headerDiv.className = 'shift-card-header';
            const metaDiv = document.createElement('div');
            metaDiv.className = 'shift-meta';
            const storeSpan = document.createElement('span');
            storeSpan.className = 'shift-store';
            storeSpan.textContent = shift.storeName || '';
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
                obsP.textContent = shift.observations;
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
            nameSpan.textContent = v.volunteerName || '';
            infoDiv.appendChild(nameSpan);
            if (v.phone) {
                const phoneSpan = document.createElement('span');
                phoneSpan.className = 'volunteer-phone';
                phoneSpan.textContent = v.phone;
                infoDiv.appendChild(phoneSpan);
            }
            rowDiv.appendChild(infoDiv);
            const controlsDiv = document.createElement('div');
            controlsDiv.className = 'attendance-controls';
            const btnPresent = document.createElement('button');
            btnPresent.className = 'btn-attendance ' + (v.attendance ? 'btn-present active' : 'btn-present');
            btnPresent.setAttribute('data-shift-id', shiftId);
            btnPresent.setAttribute('data-volunteer-id', v.volunteerId);
            btnPresent.setAttribute('data-attendance', 'true');
            btnPresent.setAttribute('aria-label', 'Marcar presente');
            btnPresent.textContent = 'Presente';
            if (v.attendance) {
                btnPresent.disabled = true;
            }
            const btnAbsent = document.createElement('button');
            btnAbsent.className = 'btn-attendance ' + (!v.attendance ? 'btn-absent active' : 'btn-absent');
            btnAbsent.setAttribute('data-shift-id', shiftId);
            btnAbsent.setAttribute('data-volunteer-id', v.volunteerId);
            btnAbsent.setAttribute('data-attendance', 'false');
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


    async function handleAttendanceClick(e) {
        let btn = e.target;
        while (btn && btn !== e.currentTarget && btn.className.indexOf('btn-attendance') === -1) {
            btn = btn.parentNode;
        }
        if (!btn || btn === e.currentTarget || btn.disabled) return;

        const shiftId     = Number(btn.getAttribute('data-shift-id'));
        const volunteerId = Number(btn.getAttribute('data-volunteer-id'));
        const attendance  = btn.getAttribute('data-attendance') === 'true';

        btn.disabled = true;
        btn.className = btn.className + ' loading';

        try {
            await apiFetch('/api/shifts/' + shiftId + '/attendance', {
                method: 'PUT',
                body: JSON.stringify({ volunteerId, attendance })
            });

            updateVolunteerRow(shiftId, volunteerId, attendance);
            showMessage(
                attendance ? 'Asistencia marcada como Presente' : 'Asistencia marcada como Ausente',
                false
            );
        } catch (err) {
            btn.disabled = false;
            btn.className = btn.className.replace(' loading', '');
            showMessage(err.message || 'Error al actualizar la asistencia', true);
        }
    }

    function updateVolunteerRow(shiftId, volunteerId, attendance) {
        const row = document.getElementById('row-' + shiftId + '-' + volunteerId);
        if (!row) return;

        const btnPresent = row.querySelector('[data-attendance="true"]');
        const btnAbsent  = row.querySelector('[data-attendance="false"]');

        if (btnPresent) {
            btnPresent.className = 'btn-attendance btn-present' + (attendance ? ' active' : '');
            btnPresent.disabled = attendance;
        }
        if (btnAbsent) {
            btnAbsent.className = 'btn-attendance btn-absent' + (!attendance ? ' active' : '');
            btnAbsent.disabled = !attendance;
        }

        let card = row.parentNode;
        while (card && card.className.indexOf('shift-card') === -1) { card = card.parentNode; }
        const counter = document.getElementById('counter-' + shiftId);
        if (card && counter) {
            const rows   = card.querySelectorAll('.volunteer-row');
            const present = card.querySelectorAll('.btn-present.active').length;
            counter.textContent = present + '/' + rows.length + ' presentes';
            counter.className = 'attendance-counter' + (present === rows.length ? ' all-present' : '');
        }
    }


    function formatDate(dateStr) {
        if (!dateStr) return '';
        const parts = dateStr.split('-');
        return parts[2] + '/' + parts[1] + '/' + parts[0];
    }
});
