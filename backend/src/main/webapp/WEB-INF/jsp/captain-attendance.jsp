<%--
  Vista de asistencia del equipo de voluntarios (capitán).

  Autores:
  - Alejandro Calvo Aguilar: 40%
  - Fernando Luis Pinilla Molina: 40%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role  = (String) session.getAttribute("role");
    String token = (String) session.getAttribute("token");
    if (!"CAPITAN".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    if (campaigns == null) campaigns = List.of();

    Integer selectedCampaignId = (Integer) request.getAttribute("selectedCampaignId");
    List<Map<String, Object>> teamShifts = (List<Map<String, Object>>) request.getAttribute("teamShifts");
    if (teamShifts == null) teamShifts = List.of();
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

<header class="topbar">
    <a class="brand" href="/captain-dashboard">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Capitán" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil &#9998;</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesion &times;</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/captain-dashboard" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Asistencia del Equipo</h1>
        <p>Registra la asistencia de los voluntarios de tu equipo.</p>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Selecciona una campaña</h2>
        </div>
        <div class="card-body">
            <form method="GET" action="/captain-attendance">
                <label for="campaign-select">Campaña</label>
                <div class="selector-row">
                    <select id="campaign-select" name="campaignId">
                        <option value="">Selecciona una campaña...</option>
                        <% for (CampaignDTO camp : campaigns) { %>
                        <option value="<%= camp.getId() %>"
                            <%= selectedCampaignId != null && selectedCampaignId.equals(camp.getId()) ? "selected" : "" %>>
                            <%= camp.getName() %>
                        </option>
                        <% } %>
                    </select>
                    <button type="submit" class="btn btn-secondary">Ver equipo</button>
                </div>
            </form>
        </div>
    </div>

    <div id="attendance-message" hidden></div>

    <% if (selectedCampaignId != null) { %>
        <% if (teamShifts.isEmpty()) { %>
        <p class="loading-msg">No hay turnos asignados para esta campaña.</p>
        <% } else { %>
            <% for (Map<String, Object> shift : teamShifts) {
                String storeName = shift.get("storeName") != null ? shift.get("storeName").toString() : "-";
                String day       = shift.get("day")       != null ? shift.get("day").toString()       : "-";
                String startTime = shift.get("startTime") != null ? shift.get("startTime").toString() : "-";
                String endTime   = shift.get("endTime")   != null ? shift.get("endTime").toString()   : "-";
                String shiftId   = shift.get("shiftId")   != null ? shift.get("shiftId").toString()   : "";
                String obs       = shift.get("observations") != null ? shift.get("observations").toString() : "";
                List<Map<String, Object>> volunteers = (List<Map<String, Object>>) shift.get("volunteers");
                if (volunteers == null) volunteers = List.of();

                int presentCount = 0;
                for (Map<String, Object> v : volunteers) {
                    if (Boolean.TRUE.equals(v.get("attendance"))) presentCount++;
                }
                int totalCount = volunteers.size();
                boolean allPresent = totalCount > 0 && presentCount == totalCount;
            %>
            <div class="shift-card">
                <div class="shift-card-header">
                    <div class="shift-meta">
                        <div class="shift-store"><%= storeName %></div>
                        <div class="shift-date"><%= day %></div>
                        <div class="shift-time"><%= startTime %> &ndash; <%= endTime %></div>
                    </div>
                    <div class="attendance-counter <%= allPresent ? "all-present" : "" %>">
                        <%= presentCount %>/<%= totalCount %>
                    </div>
                </div>

                <% if (!obs.isEmpty()) { %>
                <p class="shift-obs"><%= obs %></p>
                <% } %>

                <% if (volunteers.isEmpty()) { %>
                <p class="no-volunteers">Sin voluntarios asignados a este turno.</p>
                <% } else { %>
                <div class="volunteer-list">
                    <% for (Map<String, Object> v : volunteers) {
                        boolean attended = Boolean.TRUE.equals(v.get("attendance"));
                        String volunteerId = v.get("volunteerId") != null ? v.get("volunteerId").toString() : "";
                        String volunteerName = v.get("volunteerName") != null ? v.get("volunteerName").toString() : "-";
                        String phone = v.get("phone") != null ? v.get("phone").toString() : "";
                    %>
                    <div class="volunteer-row">
                        <div class="volunteer-info">
                            <div class="volunteer-name"><%= volunteerName %></div>
                            <% if (!phone.isEmpty()) { %>
                            <div class="volunteer-phone"><%= phone %></div>
                            <% } %>
                        </div>
                        <div class="attendance-controls">
                            <button class="btn-attendance btn-present <%= attended ? "active" : "" %>"
                                    data-shift-id="<%= shiftId %>"
                                    data-volunteer-id="<%= volunteerId %>"
                                    data-value="true">Presente</button>
                            <button class="btn-attendance btn-absent <%= !attended ? "active" : "" %>"
                                    data-shift-id="<%= shiftId %>"
                                    data-volunteer-id="<%= volunteerId %>"
                                    data-value="false">Ausente</button>
                        </div>
                    </div>
                    <% } %>
                </div>
                <% } %>
            </div>
            <% } %>
        <% } %>
    <% } %>
</main>

<script>
    const TOKEN = '<%= token != null ? token : "" %>';

    document.querySelectorAll('.btn-attendance').forEach(function(btn) {
        btn.addEventListener('click', async function() {
            const shiftId     = this.dataset.shiftId;
            const volunteerId = parseInt(this.dataset.volunteerId);
            const attended    = this.dataset.value === 'true';

            const controls = this.closest('.attendance-controls');
            const buttons  = controls.querySelectorAll('.btn-attendance');
            buttons.forEach(function(b) { b.disabled = true; b.classList.add('loading'); });

            try {
                const res = await fetch('/api/shifts/' + shiftId + '/attendance', {
                    method: 'PUT',
                    headers: {
                        'Authorization': 'Bearer ' + TOKEN,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ volunteerId: volunteerId, attendance: attended })
                });

                if (!res.ok) {
                    showFeedback('Error al registrar la asistencia', true);
                    return;
                }

                buttons.forEach(function(b) { b.classList.remove('active'); });
                this.classList.add('active');
                updateCounter(this.closest('.shift-card'));
                showFeedback('Asistencia actualizada correctamente', false);
            } catch (e) {
                showFeedback('Error al conectar con el servidor', true);
            } finally {
                buttons.forEach(function(b) { b.disabled = false; b.classList.remove('loading'); });
            }
        });
    });

    function updateCounter(card) {
        const counter = card.querySelector('.attendance-counter');
        if (!counter) return;
        const total   = card.querySelectorAll('.btn-present').length;
        const present = card.querySelectorAll('.btn-present.active').length;
        counter.textContent = present + '/' + total;
        if (present === total && total > 0) {
            counter.classList.add('all-present');
        } else {
            counter.classList.remove('all-present');
        }
    }

    function showFeedback(text, isError) {
        const el = document.querySelector('#attendance-message');
        el.textContent = text;
        el.className   = 'form-message ' + (isError ? 'error' : 'success');
        el.hidden      = false;
        setTimeout(function() { el.hidden = true; }, 3000);
    }
</script>

</body>
</html>
