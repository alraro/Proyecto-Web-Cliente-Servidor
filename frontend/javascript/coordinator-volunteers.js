const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

    const campaignSelect   = document.getElementById('campaign-select');
    const storeSelect      = document.getElementById('store-select');
    const btnLoadShifts    = document.getElementById('btn-load-shifts');
    const shiftsTbody      = document.getElementById('shifts-tbody');
    const modalOverlay     = document.getElementById('modal-overlay');
    const modalClose       = document.getElementById('modal-close');
    const modalCancel      = document.getElementById('modal-cancel');
    const modalSave        = document.getElementById('modal-save');
    const volunteerSelect  = document.getElementById('volunteer-select');
    const modalShiftInfo   = document.getElementById('modal-shift-info');
    const shiftDayInput    = document.getElementById('shift-day-input');
    const startTimeInput   = document.getElementById('start-time-input');
    const endTimeInput     = document.getElementById('end-time-input');

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
        storeSelect.innerHTML = '<option value="">Selecciona una tienda...</option>';
        storeSelect.disabled = true;
        btnLoadShifts.disabled = true;
        shiftsTbody.innerHTML = "<tr><td colspan='5' class='table-empty'>Selecciona campaña y tienda para ver los turnos.</td></tr>";
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
        shiftsTbody.innerHTML = "<tr><td colspan='5' class='table-empty'>Cargando...</td></tr>";
        try {
            const shifts = await fetchJson(
                API_BASE + '/api/shifts?campaignId=' + campaignId + '&storeId=' + storeId,
                { headers: authHeaders(token) }
            );
            renderShifts(Array.isArray(shifts) ? shifts : []);
        } catch (err) {
            showMessage(err.message || 'No se pudieron cargar los turnos', true);
            shiftsTbody.innerHTML = "<tr><td colspan='5' class='table-empty'>Error al cargar turnos.</td></tr>";
        }
    });

    function renderShifts(shifts) {
        shiftsTbody.innerHTML = '';
        if (!shifts.length) {
            shiftsTbody.innerHTML = "<tr><td colspan='5' class='table-empty'>No hay turnos para esta tienda y campaña.</td></tr>";
            return;
        }
        shifts.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(s.day || '')}</td>
                <td>${escapeHtml(s.startTime || '')}</td>
                <td>${escapeHtml(s.endTime || '')}</td>
                <td>${escapeHtml(String(s.volunteersNeeded || 0))}</td>
                <td><button type="button" class="btn btn-sm btn-primary" data-shift='${JSON.stringify(s)}'>Asignar voluntario</button></td>
            `;
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
        volunteerSelect.innerHTML = '<option value="">Cargando...</option>';
        try {
            const volunteers = await fetchJson(
                API_BASE + '/api/coordinator/volunteers?campaignId=' + (currentShift.campaignId || campaignSelect.value),
                { headers: authHeaders(token) }
            );
            volunteerSelect.innerHTML = '<option value="">Selecciona un voluntario...</option>';
            (Array.isArray(volunteers) ? volunteers : []).forEach(v => {
                const opt = document.createElement('option');
                opt.value = String(v.id);
                opt.textContent = v.name + (v.phone ? ' · ' + v.phone : '');
                volunteerSelect.appendChild(opt);
            });
        } catch (err) {
            volunteerSelect.innerHTML = '<option value="">Error al cargar voluntarios</option>';
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
