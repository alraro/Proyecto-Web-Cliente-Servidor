const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'login.html'; return; }

    const userNameEl = document.getElementById('user-name');
	
    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Administrador';
    }

    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
            
        } else if(e.target.id === 'btn-logout'){
            localStorage.clear();
            window.location.href = 'login.html';
        }
    })

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
        campaignSelect.replaceChildren();
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = 'Selecciona una campaña...';
        campaignSelect.appendChild(defaultOpt);
        (Array.isArray(campaigns) ? campaigns : []).forEach(c => {
            const opt = document.createElement('option');
            opt.value = String(c.id);
            opt.textContent = c.name + (c.startDate ? ' (' + c.startDate + ' – ' + (c.endDate || '') + ')' : '');
            campaignSelect.appendChild(opt);
        });
    } catch (err) {
        showMessage('No se pudieron cargar las campañas', true);
        campaignSelect.replaceChildren();
        const errOpt = document.createElement('option');
        errOpt.value = '';
        errOpt.textContent = 'Error al cargar';
        campaignSelect.appendChild(errOpt);
    }

    // ── Al cambiar campaña → cargar calendario ────────────────────────────────

    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        calendarContainer.replaceChildren();
        if (!campaignId) {
            const p = document.createElement('p');
            p.className = 'cal-placeholder';
            p.textContent = 'Selecciona una campaña para ver el calendario de turnos.';
            calendarContainer.appendChild(p);
            return;
        }

        const loading = document.createElement('p');
        loading.className = 'cal-placeholder';
        loading.textContent = 'Cargando calendario...';
        calendarContainer.appendChild(loading);

        try {
            const data = await fetchJson(
                API_BASE + '/api/shifts/calendar?campaignId=' + campaignId,
                { headers: authHeaders(token) }
            );
            const stores = Array.isArray(data) ? data : [];
            renderCalendar(stores);
            calLegend.hidden = stores.length === 0;
        } catch (err) {
            calendarContainer.replaceChildren();
            const p = document.createElement('p');
            p.className = 'cal-placeholder cal-error';
            p.textContent = 'Error al cargar el calendario.';
            calendarContainer.appendChild(p);
            showMessage(err.message || 'No se pudo cargar el calendario', true);
        }
    });

    // ── Renderizar panel de turnos ─────────────────────────────────────────────

    function renderCalendar(stores) {
        calendarContainer.replaceChildren();
        if (!stores.length) {
            const p = document.createElement('p');
            p.className = 'cal-placeholder';
            p.textContent = 'No hay turnos para esta campaña.';
            calendarContainer.appendChild(p);
            return;
        }

        stores.forEach(store => {
            const section = document.createElement('section');
            section.className = 'store-section';

            const header = document.createElement('div');
            header.className = 'store-header';
            const icon = document.createElement('span');
            icon.className = 'store-icon';
            icon.textContent = '🏬';
            header.appendChild(icon);
            const h2 = document.createElement('h2');
            h2.textContent = escapeHtml(store.storeName);
            header.appendChild(h2);
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

        const timeDiv = document.createElement('div');
        timeDiv.className = 'shift-time';
        timeDiv.textContent = escapeHtml(shift.startTime || '') + ' – ' + escapeHtml(shift.endTime || '');
        card.appendChild(timeDiv);

        const volDiv = document.createElement('div');
        volDiv.className = 'shift-vol';
        const volCount = document.createElement('span');
        volCount.className = 'vol-count';
        volCount.textContent = assigned + '/' + needed;
        volDiv.appendChild(volCount);
        const volLabel = document.createElement('span');
        volLabel.className = 'vol-label';
        volLabel.textContent = ' voluntarios';
        volDiv.appendChild(volLabel);
        card.appendChild(volDiv);

        const barDiv = document.createElement('div');
        barDiv.className = 'vol-bar';
        const barFill = document.createElement('div');
        barFill.className = 'vol-bar-fill';
        barFill.style.width = barWidth + '%';
        barDiv.appendChild(barFill);
        card.appendChild(barDiv);

        if (shift.observations) {
            const obsDiv = document.createElement('div');
            obsDiv.className = 'shift-obs';
            obsDiv.textContent = escapeHtml(shift.observations);
            card.appendChild(obsDiv);
        }

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
