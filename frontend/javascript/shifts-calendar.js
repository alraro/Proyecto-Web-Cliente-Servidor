/* Extract token from URL params (used when navigating from the SSR coordinator portal). */
handleUrlTokenParams();

document.addEventListener('DOMContentLoaded', async () => {
    if (!getToken()) { window.location.href = 'login.html'; return; }


    const campaignSelect     = document.querySelector('#campaign-select');
    const calendarContainer  = document.querySelector('#calendar-container');
    const calLegend          = document.querySelector('#cal-legend');


    try {
        const campaigns = await apiFetch('/api/coordinator/my-campaigns');
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
            const data = await apiFetch('/api/shifts/calendar?campaignId=' + campaignId);
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
            h2.textContent = store.storeName;
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

        const pct100   = parseInt(pct * 100 + 0.5);
        const barWidth = pct100 > 100 ? 100 : pct100;

        const timeDiv = document.createElement('div');
        timeDiv.className = 'shift-time';
        timeDiv.textContent = (shift.startTime || '') + ' – ' + (shift.endTime || '');
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
            obsDiv.textContent = shift.observations;
            card.appendChild(obsDiv);
        }

        return card;
    }


    function formatDate(dateStr) {
        if (!dateStr) return '';
        const parts = dateStr.split('-');
        const y = Number(parts[0]);
        const m = Number(parts[1]);
        const d = Number(parts[2]);
        const names = ['dom','lun','mar','mié','jue','vie','sáb'];
        const t = [0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4];
        const yr  = m < 3 ? y - 1 : y;
        const dow = (yr + parseInt(yr / 4) - parseInt(yr / 100) + parseInt(yr / 400) + t[m - 1] + d) % 7;
        return names[dow] + ' ' + parts[2] + '/' + parts[1];
    }
});
