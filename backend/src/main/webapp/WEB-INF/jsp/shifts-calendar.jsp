<%--
  Vista de calendario de turnos.

  Autores:
  - Alejandro Calvo Aguilar: 85%
  - IA Generativa: 15%
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");

    if (token == null || !"COORDINADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Calendario de Turnos</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/shifts-calendar.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Coordinador" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">

    <div class="page-header">
        <a href="/coordinator-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Calendario de Turnos</h1>
                <p>Vista de turnos por tienda, día y franja horaria.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="cal-filter-bar">
        <div class="form-group">
            <label for="campaign-select">Campaña</label>
            <select id="campaign-select">
                <option value="">Cargando campañas...</option>
            </select>
        </div>
    </div>

    <div class="cal-legend" id="cal-legend" hidden>
        <span class="legend-item legend-full">Completo</span>
        <span class="legend-item legend-partial">Parcial</span>
        <span class="legend-item legend-empty">Sin voluntarios</span>
    </div>

    <div id="calendar-container">
        <p class="cal-placeholder">Selecciona una campaña para ver el calendario de turnos.</p>
    </div>

</main>

<script>
    const TOKEN = '<%= token %>';

    function authHeaders() {
        return { 'Authorization': 'Bearer ' + TOKEN, 'Content-Type': 'application/json' };
    }

    async function apiFetch(url) {
        const res = await fetch(url, { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { window.location.href = '/logout'; return; }
        if (!res.ok) throw new Error('Error ' + res.status);
        return res.json();
    }

    function showMessage(text, isError) {
        const el = document.querySelector('#global-message');
        el.textContent = text;
        el.className   = isError ? 'form-message error' : 'form-message success';
        el.hidden      = false;
    }

    const campaignSelect    = document.querySelector('#campaign-select');
    const calendarContainer = document.querySelector('#calendar-container');
    const calLegend         = document.querySelector('#cal-legend');

    async function loadCampaigns() {
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
        } catch {
            showMessage('No se pudieron cargar las campañas', true);
            campaignSelect.replaceChildren();
            const errOpt = document.createElement('option');
            errOpt.value = '';
            errOpt.textContent = 'Error al cargar';
            campaignSelect.appendChild(errOpt);
        }
    }

    campaignSelect.addEventListener('change', async () => {
        const campaignId = campaignSelect.value;
        calendarContainer.replaceChildren();
        calLegend.hidden = true;

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
            const data   = await apiFetch('/api/shifts/calendar?campaignId=' + campaignId);
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
                    dayCol.appendChild(buildShiftCard(shift));
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
        if (pct >= 1)      statusClass = 'shift-full';
        else if (pct > 0)  statusClass = 'shift-partial';
        else               statusClass = 'shift-empty';

        const card = document.createElement('div');
        card.className = 'shift-card ' + statusClass;

        const pct100   = Math.min(Math.round(pct * 100), 100);

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
        barFill.style.width = pct100 + '%';
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
        const [y, m, d] = dateStr.split('-').map(Number);
        const date = new Date(y, m - 1, d);
        const dayNames = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'];
        return dayNames[date.getDay()] + ' ' + String(d).padStart(2, '0') + '/' + String(m).padStart(2, '0');
    }

    loadCampaigns();
</script>

</body>
</html>
