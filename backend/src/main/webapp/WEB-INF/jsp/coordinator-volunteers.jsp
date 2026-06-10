<%--
  Vista de asignación de voluntarios a turnos (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 70%
  - Hugo Herrero González: 5%
  - IA Generativa: 25%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.VoluntarioResponseDto" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"COORDINADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    @SuppressWarnings("unchecked")
    List<VoluntarioResponseDto> volunteers = (List<VoluntarioResponseDto>) request.getAttribute("volunteers");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Asignación de Voluntarios</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/coordinator-dashboard" aria-label="Bancosol home">
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
                <h1>Asignación de Voluntarios</h1>
                <p>Asigna voluntarios a los turnos de tus tiendas.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card">
        <div class="card-body">
            <div class="form-group">
                <label for="campaign-select">Campaña</label>
                <select id="campaign-select">
                    <option value="">Selecciona una campaña...</option>
                    <% if (campaigns != null) {
                        for (CampaignDTO camp : campaigns) { %>
                    <option value="<%= camp.getId() %>"><%= camp.getName() %></option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group mb-0">
                <label for="store-select">Tienda</label>
                <div class="selector-row">
                    <select id="store-select" disabled>
                        <option value="">Selecciona primero una campaña...</option>
                    </select>
                    <button type="button" id="btn-load-shifts" class="btn btn-secondary" disabled>Cargar turnos</button>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-head">
            <h2>Turnos</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Día</th>
                        <th>Hora inicio</th>
                        <th>Hora fin</th>
                        <th>Voluntarios necesarios</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody id="shifts-tbody">
                    <tr><td colspan="5" class="table-empty">Selecciona campaña y tienda para ver los turnos.</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</main>

<div id="modal-overlay" class="modal-overlay" hidden>
    <div class="modal">
        <div class="modal-header">
            <h3>Asignar voluntario al turno</h3>
            <button type="button" id="modal-close" class="modal-close">✕</button>
        </div>
        <div class="modal-body">
            <p id="modal-shift-info" class="modal-shift-info"></p>
            <div class="form-group">
                <label for="volunteer-select">Voluntario</label>
                <select id="volunteer-select">
                    <option value="">Selecciona un voluntario...</option>
                    <% if (volunteers != null) {
                        for (VoluntarioResponseDto vol : volunteers) { %>
                    <option value="<%= vol.id() %>"><%= vol.name() %></option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label for="shift-day-input">Día del turno</label>
                <input type="date" id="shift-day-input">
            </div>
            <div class="form-group">
                <label for="start-time-input">Hora de inicio</label>
                <input type="time" id="start-time-input">
            </div>
            <div class="form-group mb-0">
                <label for="end-time-input">Hora de fin</label>
                <input type="time" id="end-time-input">
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" id="modal-cancel" class="btn btn-secondary">Cancelar</button>
            <button type="button" id="modal-save" class="btn btn-primary">Guardar asignación</button>
        </div>
    </div>
</div>
<script>
    const BEARER_TOKEN = '<%= session.getAttribute("token") != null ? session.getAttribute("token") : "" %>';
    let selectedShiftId = null;

    function showMsg(text, type) {
        const el = document.getElementById('global-message');
        el.textContent = text;
        el.className = 'global-message ' + type;
        el.removeAttribute('hidden');
        setTimeout(() => el.setAttribute('hidden', ''), 4000);
    }

    async function onCampaignChange() {
        const campaignId = document.getElementById('campaign-select').value;
        const storeSelect = document.getElementById('store-select');
        const btnLoad = document.getElementById('btn-load-shifts');
        storeSelect.innerHTML = '<option value="">Selecciona primero una campaña...</option>';
        storeSelect.disabled = true;
        btnLoad.disabled = true;
        if (!campaignId) return;
        try {
            const res = await fetch('/api/shifts/campaign/' + campaignId + '/stores', {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) return;
            const stores = await res.json();
            storeSelect.innerHTML = '<option value="">Selecciona una tienda...</option>' +
                stores.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
            storeSelect.disabled = false;
            btnLoad.disabled = false;
        } catch (e) {
            showMsg('Error al cargar las tiendas.', 'error');
        }
    }

    async function loadShifts() {
        const campaignId = document.getElementById('campaign-select').value;
        const storeId = document.getElementById('store-select').value;
        if (!campaignId || !storeId) { showMsg('Selecciona campaña y tienda.', 'error'); return; }
        const tbody = document.getElementById('shifts-tbody');
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">Cargando...</td></tr>';
        try {
            const res = await fetch('/api/shifts?campaignId=' + campaignId + '&storeId=' + storeId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { showMsg('Error al cargar los turnos.', 'error'); return; }
            const shifts = await res.json();
            if (!shifts.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay turnos para esta selección.</td></tr>';
                return;
            }
            tbody.innerHTML = shifts.map(s =>
                `<tr>
                    <td>${s.day || '-'}</td>
                    <td>${s.startTime || '-'}</td>
                    <td>${s.endTime || '-'}</td>
                    <td>${s.volunteersNeeded || '-'}</td>
                    <td><button type="button" class="btn btn-secondary" onclick="openModal(${s.shiftId}, '${s.day} ${s.startTime}-${s.endTime}')">Asignar</button></td>
                </tr>`
            ).join('');
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    }

    function openModal(shiftId, info) {
        selectedShiftId = shiftId;
        document.getElementById('modal-shift-info').textContent = info;
        document.getElementById('modal-overlay').removeAttribute('hidden');
    }

    function closeModal() {
        selectedShiftId = null;
        document.getElementById('modal-overlay').setAttribute('hidden', '');
    }

    async function saveAssignment() {
        const volunteerId = parseInt(document.getElementById('volunteer-select').value);
        const shiftDay   = document.getElementById('shift-day-input').value;
        const startTime  = document.getElementById('start-time-input').value;
        const endTime    = document.getElementById('end-time-input').value;
        if (!volunteerId || !shiftDay || !startTime || !endTime) {
            alert('Rellena todos los campos del modal.'); return;
        }
        try {
            const res = await fetch('/api/coordinator/volunteer-shifts', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN, 'Content-Type': 'application/json' },
                body: JSON.stringify({ volunteerId, shiftId: selectedShiftId, shiftDay, startTime, endTime })
            });
            if (res.ok) { closeModal(); loadShifts(); showMsg('Voluntario asignado correctamente.', 'success'); }
            else { const err = await res.json().catch(() => ({})); showMsg(err.message || 'Error al guardar.', 'error'); }
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    }

    document.getElementById('campaign-select').addEventListener('change', onCampaignChange);
    document.getElementById('btn-load-shifts').addEventListener('click', loadShifts);
    document.getElementById('modal-close').addEventListener('click', closeModal);
    document.getElementById('modal-cancel').addEventListener('click', closeModal);
    document.getElementById('modal-save').addEventListener('click', saveAssignment);
</script>
</body>
</html>
