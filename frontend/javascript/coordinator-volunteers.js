/* Bootstrap auth from URL params (when navigating from the SSR portal). */
handleUrlTokenParams();

document.addEventListener('DOMContentLoaded', async () => {
    const token = sessionStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }


    const campaignSelect   = document.querySelector('#campaign-select');
    const storeSelect      = document.querySelector('#store-select');
    const btnLoadShifts    = document.querySelector('#btn-load-shifts');
    const shiftsTbody      = document.querySelector('#shifts-tbody');
    const modalOverlay     = document.querySelector('#modal-overlay');
    const modalClose       = document.querySelector('#modal-close');
    const modalCancel      = document.querySelector('#modal-cancel');
    const modalSave        = document.querySelector('#modal-save');
    const volunteerSelect  = document.querySelector('#volunteer-select');
    const modalShiftInfo   = document.querySelector('#modal-shift-info');
    const shiftDayInput    = document.querySelector('#shift-day-input');
    const startTimeInput   = document.querySelector('#start-time-input');
    const endTimeInput     = document.querySelector('#end-time-input');

    let currentShift = null;

    // Carga campañas del coordinador
    try {
        const campaigns = await fetchJson(API_BASE + '/api/coordinator/my-campaigns', { headers: authHeaders(token) });
        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + ' (' + c.startDate + ' - ' + c.endDate + ')';
            campaignSelect.appendChild(opt);
        });
    } catch (err) {
        showMessage(err.message || 'No se pudieron cargar las campañas', true);
    }

    // Al cambiar campaña → cargar tiendas
    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        storeSelect.innerHTML = '';
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = 'Selecciona una tienda...';
        storeSelect.appendChild(defaultOpt);
        storeSelect.disabled = true;
        btnLoadShifts.disabled = true;
        shiftsTbody.innerHTML = '';
        const initRow = document.createElement('tr');
        const initTd = document.createElement('td');
        initTd.colSpan = 5;
        initTd.className = 'table-empty';
        initTd.textContent = 'Selecciona campaña y tienda para ver los turnos.';
        initRow.appendChild(initTd);
        shiftsTbody.appendChild(initRow);
        if (!campaignId) return;
        try {
            const stores = await fetchJson(
                API_BASE + '/api/coordinator/my-stores?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            (Array.isArray(stores) ? stores : []).forEach(s => {
                const opt = document.createElement('option');
                opt.value = String(s.id);
                opt.textContent = s.name;
                storeSelect.appendChild(opt);
            });
            storeSelect.disabled = false;
            btnLoadShifts.disabled = false;
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar las tiendas', true);
        }
    });

    // Cargar turnos
    btnLoadShifts.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        const storeId    = storeSelect.value;
        if (!campaignId || !storeId) { showMessage('Selecciona campaña y tienda', true); return; }
        shiftsTbody.innerHTML = '';
        const loadingRow = document.createElement('tr');
        const loadingTd = document.createElement('td');
        loadingTd.colSpan = 5;
        loadingTd.className = 'table-empty';
        loadingTd.textContent = 'Cargando...';
        loadingRow.appendChild(loadingTd);
        shiftsTbody.appendChild(loadingRow);
        try {
            const shifts = await fetchJson(
                API_BASE + '/api/shifts?campaignId=' + campaignId + '&storeId=' + storeId,
                { headers: authHeaders(token) }
            );
            renderShifts(Array.isArray(shifts) ? shifts : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar los turnos', true);
            shiftsTbody.innerHTML = '';
            const errorRow = document.createElement('tr');
            const errorTd = document.createElement('td');
            errorTd.colSpan = 5;
            errorTd.className = 'table-empty';
            errorTd.textContent = 'Error al cargar turnos.';
            errorRow.appendChild(errorTd);
            shiftsTbody.appendChild(errorRow);
        }
    });

    function renderShifts(shifts) {
        shiftsTbody.innerHTML = '';
        if (!shifts.length) {
            const emptyRow = document.createElement('tr');
            const emptyTd = document.createElement('td');
            emptyTd.colSpan = 5;
            emptyTd.className = 'table-empty';
            emptyTd.textContent = 'No hay turnos para esta tienda y campaña.';
            emptyRow.appendChild(emptyTd);
            shiftsTbody.appendChild(emptyRow);
            return;
        }
        shifts.forEach(s => {
            const tr = document.createElement('tr');
            const tdDay = document.createElement('td');
            tdDay.textContent = escapeHtml(s.day || '');
            const tdStart = document.createElement('td');
            tdStart.textContent = escapeHtml(s.startTime || '');
            const tdEnd = document.createElement('td');
            tdEnd.textContent = escapeHtml(s.endTime || '');
            const tdNeeded = document.createElement('td');
            tdNeeded.textContent = escapeHtml(String(s.volunteersNeeded || 0));
            const tdActions = document.createElement('td');
            const assignBtn = document.createElement('button');
            assignBtn.type = 'button';
            assignBtn.className = 'btn btn-sm btn-primary';
            assignBtn.dataset.shift = JSON.stringify(s);
            assignBtn.textContent = 'Asignar voluntario';
            tdActions.appendChild(assignBtn);
            tr.appendChild(tdDay);
            tr.appendChild(tdStart);
            tr.appendChild(tdEnd);
            tr.appendChild(tdNeeded);
            tr.appendChild(tdActions);
            shiftsTbody.appendChild(tr);
        });
    }

    // Delegación para abrir modal al hacer click en "Asignar voluntario"
    shiftsTbody.addEventListener('click', async (event) => {
        const btn = event.target.closest('button[data-shift]');
        if (!btn) return;
        currentShift = JSON.parse(btn.dataset.shift);
        modalShiftInfo.textContent = 'Turno: ' + currentShift.day + ' · ' + currentShift.startTime + ' - ' + currentShift.endTime;
        shiftDayInput.value  = currentShift.day || '';
        startTimeInput.value = currentShift.startTime || '';
        endTimeInput.value   = currentShift.endTime || '';

        // Cargar voluntarios disponibles
        volunteerSelect.innerHTML = '';
        const loadingOpt = document.createElement('option');
        loadingOpt.value = '';
        loadingOpt.textContent = 'Cargando...';
        volunteerSelect.appendChild(loadingOpt);
        try {
            const volunteers = await fetchJson(
                API_BASE + '/api/coordinator/volunteers?campaignId=' + (currentShift.campaignId || campaignSelect.value),
                { headers: authHeaders(token) }
            );
            volunteerSelect.innerHTML = '';
            const defaultOpt = document.createElement('option');
            defaultOpt.value = '';
            defaultOpt.textContent = 'Selecciona un voluntario...';
            volunteerSelect.appendChild(defaultOpt);
            (Array.isArray(volunteers) ? volunteers : []).forEach(v => {
                const opt = document.createElement('option');
                opt.value = String(v.id);
                const entityPart = v.partnerEntityName
                    ? ' (' + v.partnerEntityName + ')'
                    : ' (Independiente)';
                opt.textContent = v.name + (v.phone ? ' · ' + v.phone : '') + entityPart;
                volunteerSelect.appendChild(opt);
            });
        } catch (err) {
            volunteerSelect.innerHTML = '';
            const errorOpt = document.createElement('option');
            errorOpt.value = '';
            errorOpt.textContent = 'Error al cargar voluntarios';
            volunteerSelect.appendChild(errorOpt);
        }

        modalOverlay.hidden = false;
    });

    function closeModal() { modalOverlay.hidden = true; currentShift = null; }
    modalClose.addEventListener('click', closeModal);
    modalCancel.addEventListener('click', closeModal);
    modalOverlay.addEventListener('click', (e) => { if (e.target === modalOverlay) closeModal(); });

    // Guardar asignación
    modalSave.addEventListener('click', async () => {
        const volunteerId = volunteerSelect.value;
        const shiftDay    = shiftDayInput.value;
        const startTime   = startTimeInput.value;
        const endTime     = endTimeInput.value;

        if (!volunteerId) { showMessage('Selecciona un voluntario', true); return; }
        if (!shiftDay || !startTime || !endTime) { showMessage('Completa día y horas', true); return; }

        const body = {
            volunteerId:  Number(volunteerId),
            campaignId:   currentShift?.campaignId || Number(campaignSelect.value),
            storeId:      currentShift?.storeId    || Number(storeSelect.value),
            shiftDay,
            startTime,
            endTime
        };

        try {
            await fetchJson(API_BASE + '/api/coordinator/volunteer-shifts', {
                method: 'POST',
                headers: authHeaders(token),
                body: JSON.stringify(body)
            });
            showMessage('Voluntario asignado correctamente', false);
            closeModal();
        } catch (err) {
            showMessage(err.message || 'No se pudo asignar el voluntario', true);
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
            sessionStorage.clear(); window.location.href = 'login.html';
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
        showMessage._t = setTimeout(() => { el.hidden = true; }, 4000);
    }

    function escapeHtml(v) {
        return String(v).replace(/&/g,'&amp;').replace(/</g,'&lt;')
            .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
    }
});
