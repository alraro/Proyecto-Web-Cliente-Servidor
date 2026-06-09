<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
        </div>
    </div>

    <div id="shifts-container"></div>
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

    document.getElementById('campaign-select').addEventListener('change', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        const container = document.getElementById('shifts-container');
        container.innerHTML = '';
        if (!campaignId) return;
        container.innerHTML = '<p class="table-empty">Cargando turnos...</p>';
        try {
            const res = await fetch('/api/captain/volunteer-shifts?campaignId=' + campaignId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { container.innerHTML = '<p class="table-empty">Error al cargar los turnos.</p>'; return; }
            const shifts = await res.json();
            if (!shifts.length) {
                container.innerHTML = '<p class="table-empty">No hay turnos registrados para esta campaña.</p>';
                return;
            }
            container.innerHTML = `
                <div class="card">
                    <div class="card-head"><h2>Turnos de voluntarios</h2></div>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr><th>Voluntario</th><th>Día</th><th>Hora inicio</th><th>Hora fin</th><th>Asistencia</th></tr>
                            </thead>
                            <tbody>
                                ${shifts.map(s =>
                                    `<tr>
                                        <td>${s.volunteerName || '-'}</td>
                                        <td>${s.shiftDay || '-'}</td>
                                        <td>${s.startTime || '-'}</td>
                                        <td>${s.endTime || '-'}</td>
                                        <td>${s.attendance === true ? '✅' : s.attendance === false ? '❌' : '-'}</td>
                                    </tr>`
                                ).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>`;
        } catch (e) {
            container.innerHTML = '<p class="table-empty">Error de conexión.</p>';
        }
    });
</script>
</body>
</html>
