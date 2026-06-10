<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"CAPITAN".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Asistencia del Equipo</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/captain-attendance.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/captain-dashboard" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Capitán" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/captain-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Asistencia del Equipo</h1>
                <p>Consulta los turnos de tu equipo de voluntarios.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card">
        <div class="card-head">
            <h2>Selecciona una campaña</h2>
        </div>
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
            <button type="button" id="btn-load" class="btn btn-secondary">Ver equipo</button>
        </div>
    </div>

    <div class="card">
        <div class="card-head">
            <h2>Turnos y asistencia</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Tienda</th>
                        <th>Día</th>
                        <th>Inicio</th>
                        <th>Fin</th>
                        <th>Voluntario</th>
                        <th>Asistencia</th>
                    </tr>
                </thead>
                <tbody id="shifts-tbody">
                    <tr><td colspan="6" class="table-empty">Selecciona una campaña para ver el equipo.</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</main>
<script>
    const BEARER_TOKEN = '<%= session.getAttribute("token") != null ? session.getAttribute("token") : "" %>';

    function showMsg(text, type) {
        const el = document.getElementById('global-message');
        el.textContent = text;
        el.className = 'global-message ' + type;
        el.removeAttribute('hidden');
        setTimeout(() => el.setAttribute('hidden', ''), 4000);
    }

    document.getElementById('btn-load').addEventListener('click', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        if (!campaignId) { showMsg('Selecciona una campaña primero.', 'error'); return; }
        const tbody = document.getElementById('shifts-tbody');
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">Cargando...</td></tr>';
        try {
            const res = await fetch('/api/shifts/my-team?campaignId=' + campaignId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { showMsg('Error al cargar los turnos.', 'error'); return; }
            const shifts = await res.json();
            if (!shifts.length) {
                tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay turnos asignados para esta campaña.</td></tr>';
                return;
            }
            const rows = [];
            shifts.forEach(function(shift) {
                const volunteers = Array.isArray(shift.volunteers) ? shift.volunteers : [];
                if (!volunteers.length) {
                    rows.push(
                        '<tr>' +
                        '<td>' + (shift.storeName || '-') + '</td>' +
                        '<td>' + (shift.day || '-') + '</td>' +
                        '<td>' + (shift.startTime || '-') + '</td>' +
                        '<td>' + (shift.endTime || '-') + '</td>' +
                        '<td><em>Sin voluntarios</em></td>' +
                        '<td>—</td>' +
                        '</tr>'
                    );
                } else {
                    volunteers.forEach(function(v) {
                        rows.push(
                            '<tr>' +
                            '<td>' + (shift.storeName || '-') + '</td>' +
                            '<td>' + (shift.day || '-') + '</td>' +
                            '<td>' + (shift.startTime || '-') + '</td>' +
                            '<td>' + (shift.endTime || '-') + '</td>' +
                            '<td>' + (v.volunteerName || '-') + '</td>' +
                            '<td><input type="checkbox"' + (v.attendance ? ' checked' : '') + ' disabled></td>' +
                            '</tr>'
                        );
                    });
                }
            });
            tbody.innerHTML = rows.join('');
        } catch (err) {
            showMsg('Error de conexión.', 'error');
        }
    });
</script>
</body>
</html>
