const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Coordinador';
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'login.html';
    });

    const campaignSelect     = document.getElementById('campaign-select');
    const calendarContainer  = document.getElementById('calendar-container');
    const calLegend          = document.getElementById('cal-legend');

    // ── Carga campañas ────────────────────────────────────────────────────────

    try {
        const res = await fetch(API_BASE + '/api/coordinator/my-campaigns', {
            headers: authHeaders(token)
        });
        if (res.status === 401 || res.status === 403) { expiredSession(); return; }
        const campaigns = res.ok ? await res.json() : [];
        campaignSelect.innerHTML = '<option value="">Selecciona una campaña...</option>';
        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + (c.startDate ? ' (' + c.startDate + ' – ' + (c.endDate || '') + ')' : '');
            campaignSelect.appendChild(opt);
        });
    } catch (err) {
        showMessage('No se pudieron cargar las campañas', true);
        campaignSelect.innerHTML = '<option value="">Error al cargar</option>';
    }

    // ── Al cambiar campaña → cargar calendario ────────────────────────────────

    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        calendarContainer.innerHTML = '';
        calLegend.hidden = true;
        if (!campaignId) {
            calendarContainer.innerHTML = '<p class="cal-placeholder">Selecciona una campaña para ver el calendario de turnos.</p>';
            return;
        }

        calendarContainer.innerHTML = '<p class="cal-placeholder">Cargando calendario...</p>';

        try {
            const data = await fetchJson(
                API_BASE + '/api/shifts/calendar?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            const stores = Array.isArray(data) ? data : [];
            renderCalendar(stores);
            calLegend.hidden = stores.length === 0;
        } catch (err) {
            calendarContainer.innerHTML = '<p class="cal-placeholder cal-error">Error al cargar el calendario.</p>';
            showMessage(err.message || 'No se pudo cargar el calendario', true);
        }
    });

    // ── Renderizar panel de turnos ─────────────────────────────────────────────

    function renderCalendar(stores) {
        calendarContainer.innerHTML = '';
        if (!stores.length) {
            calendarContainer.innerHTML = '<p class="cal-placeholder">No hay turnos para esta campaña.</p>';
            return;
        }

        stores.forEach(store => {
            const section = document.createElement('section');
            section.className = 'store-section';

            const header = document.createElement('div');
            header.className = 'store-header';
            header.innerHTML = '<span class="store-icon">🏬</span><h2>' + escapeHtml(store.storeName) + '</h2>';
            section.appendChild(header);

            const daysGrid = document.createElement('div');
            daysGrid.className = 'days-grid';

            (store.days || []).forEach(day => {
                const dayCol = document.createElement('div');
                dayCol.className = 'day-col';

                const dayLabel = document.createElement('div');
                dayLabel.className = 'day-label';
                dayLabel.textContent = formatDate(day.date);
                dayCol.appendChild(dayLabel);

                (day.shifts || []).forEach(shift => {
                    const card = buildShiftCard(shift);
                    dayCol.appendChild(card);
                });

                daysGrid.appendChild(dayCol);
            });

            section.appendChild(daysGrid);
            calendarContainer.appendChild(section);
        });
    }

    function buildShiftCard(shift) {
        const needed   = shift.volunteersNeeded   || 0;
        const assigned = shift.volunteersAssigned || 0;
        const pct      = needed > 0 ? assigned / needed : 0;

        let statusClass;
        if (pct >= 1)       statusClass = 'shift-full';
        else if (pct > 0)   statusClass = 'shift-partial';
        else                statusClass = 'shift-empty';

        const card = document.createElement('div');
        card.className = 'shift-card ' + statusClass;

        const barWidth = Math.min(Math.round(pct * 100), 100);

        card.innerHTML =
            '<div class="shift-time">' + escapeHtml(shift.startTime || '') + ' – ' + escapeHtml(shift.endTime || '') + '</div>' +
            '<div class="shift-vol">' +
                '<span class="vol-count">' + assigned + '/' + needed + '</span>' +
                '<span class="vol-label"> voluntarios</span>' +
            '</div>' +
            '<div class="vol-bar"><div class="vol-bar-fill" style="width:' + barWidth + '%"></div></div>' +
            (shift.observations ? '<div class="shift-obs">' + escapeHtml(shift.observations) + '</div>' : '');

        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    function authHeaders(t) {
        return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + t };
    }

    async function fetchJson(url, options) {
        const res  = await fetch(url, options);
        const data = await res.json().catch(() => ({}));
        if (res.status === 401 || res.status === 403) { expiredSession(); throw new Error('Sesión expirada'); }
        if (!res.ok) throw new Error(data.message || 'Error ' + res.status);
        return data;
    }

    function expiredSession() {
        localStorage.clear(); window.location.href = 'login.html';
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
        const names = ['dom','lun','mar','mié','jue','vie','sáb'];
        const dow   = new Date(Number(y), Number(m) - 1, Number(d)).getDay();
        return names[dow] + ' ' + d + '/' + m;
    }
});
