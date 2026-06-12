<%--
  Vista de asignación de voluntarios a turnos (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 70%
  - Hugo Herrero González: 5%
  - IA Generativa: 25%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.StoreSimpleDto, es.grupo8.backend.dto.ShiftResponseDto, es.grupo8.backend.dto.VoluntarioResponseDto" %>
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
    @SuppressWarnings("unchecked")
    List<StoreSimpleDto> stores = (List<StoreSimpleDto>) request.getAttribute("stores");
    @SuppressWarnings("unchecked")
    List<ShiftResponseDto> shifts = (List<ShiftResponseDto>) request.getAttribute("shifts");
    if (campaigns == null) campaigns = List.of();
    if (volunteers == null) volunteers = List.of();
    if (stores == null) stores = List.of();
    if (shifts == null) shifts = List.of();

    Integer selectedCampaignId = (Integer) request.getAttribute("selectedCampaignId");
    Integer selectedStoreId = (Integer) request.getAttribute("selectedStoreId");
    Integer selectedShiftId = (Integer) request.getAttribute("selectedShiftId");

    ShiftResponseDto selectedShift = null;
    if (selectedShiftId != null) {
        for (ShiftResponseDto s : shifts) {
            if (selectedShiftId.equals(s.getShiftId())) { selectedShift = s; break; }
        }
    }

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
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

<header class="topbar">
    <a class="brand" href="/coordinator-dashboard">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Coordinador" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil &#9998;</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesion &times;</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/coordinator-dashboard" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Asignación de Voluntarios</h1>
        <p>Asigna voluntarios a los turnos de tus tiendas.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <div class="card">
        <div class="card-body">
            <form method="GET" action="/coordinator-volunteers">
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
                    <button type="submit" class="btn btn-secondary">Cargar tiendas</button>
                </div>
            </form>
            <% if (selectedCampaignId != null) { %>
            <form method="GET" action="/coordinator-volunteers">
                <input type="hidden" name="campaignId" value="<%= selectedCampaignId %>">
                <label for="store-select">Tienda</label>
                <div class="selector-row">
                    <select id="store-select" name="storeId">
                        <option value="">Selecciona una tienda...</option>
                        <% for (StoreSimpleDto s : stores) { %>
                        <option value="<%= s.getId() %>"
                            <%= selectedStoreId != null && selectedStoreId.equals(s.getId()) ? "selected" : "" %>>
                            <%= s.getName() %>
                        </option>
                        <% } %>
                    </select>
                    <button type="submit" class="btn btn-secondary">Cargar turnos</button>
                </div>
            </form>
            <% } %>
        </div>
    </div>

    <% if (selectedStoreId != null) { %>
    <div class="card">
        <div class="card-header">
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
                <tbody>
                    <% if (shifts.isEmpty()) { %>
                    <tr>
                        <td colspan="5" class="table-empty">No hay turnos para esta selección.</td>
                    </tr>
                    <% } else { %>
                        <% for (ShiftResponseDto s : shifts) { %>
                        <tr>
                            <td><%= s.getDay() != null ? s.getDay() : "-" %></td>
                            <td><%= s.getStartTime() != null ? s.getStartTime() : "-" %></td>
                            <td><%= s.getEndTime() != null ? s.getEndTime() : "-" %></td>
                            <td><%= s.getVolunteersNeeded() != null ? s.getVolunteersNeeded() : "-" %></td>
                            <td>
                                <a href="/coordinator-volunteers?campaignId=<%= selectedCampaignId %>&amp;storeId=<%= selectedStoreId %>&amp;shiftId=<%= s.getShiftId() %>"
                                   class="btn btn-secondary btn-sm">Asignar</a>
                            </td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
    <% } %>

    <% if (selectedShift != null) { %>
    <div class="card">
        <div class="card-header">
            <h2>Asignar voluntario al turno — <%= selectedShift.getDay() %> <%= selectedShift.getStartTime() %>-<%= selectedShift.getEndTime() %></h2>
        </div>
        <form method="POST" action="/coordinator-volunteers/asignar">
            <input type="hidden" name="campaignId" value="<%= selectedCampaignId %>">
            <input type="hidden" name="storeId" value="<%= selectedStoreId %>">
            <div class="card-body">
                <div class="form-group">
                    <label for="volunteer-select">Voluntario</label>
                    <select id="volunteer-select" name="volunteerId" required>
                        <option value="">Selecciona un voluntario...</option>
                        <% for (VoluntarioResponseDto vol : volunteers) { %>
                        <option value="<%= vol.id() %>"><%= vol.name() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="shift-day-input">Día del turno</label>
                    <input type="date" id="shift-day-input" name="shiftDay" required
                           value="<%= selectedShift.getDay() != null ? selectedShift.getDay() : "" %>">
                </div>
                <div class="form-group">
                    <label for="start-time-input">Hora de inicio</label>
                    <input type="time" id="start-time-input" name="startTime" required
                           value="<%= selectedShift.getStartTime() != null ? selectedShift.getStartTime() : "" %>">
                </div>
                <div class="form-group">
                    <label for="end-time-input">Hora de fin</label>
                    <input type="time" id="end-time-input" name="endTime" required
                           value="<%= selectedShift.getEndTime() != null ? selectedShift.getEndTime() : "" %>">
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Guardar asignación</button>
                    <a href="/coordinator-volunteers?campaignId=<%= selectedCampaignId %>&amp;storeId=<%= selectedStoreId %>"
                       class="btn btn-secondary">Cancelar</a>
                </div>
            </div>
        </form>
    </div>
    <% } %>
</main>

<script>
    (function () {
        var msg = document.getElementById("flash-message");
        if (msg) {
            setTimeout(function () { msg.style.opacity = "0"; }, 3000);
            setTimeout(function () { msg.remove(); }, 3500);
        }
    }());
</script>
</body>
</html>
