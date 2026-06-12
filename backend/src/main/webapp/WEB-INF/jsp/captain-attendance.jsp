<%--
  Vista de asistencia del equipo de voluntarios (capitán).

  Autores:
  - Fernando Luis Pinilla Molina: 75%
  - Hugo Herrero González: 5%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"CAPITAN".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    if (campaigns == null) campaigns = List.of();

    Integer selectedCampaignId = (Integer) request.getAttribute("selectedCampaignId");
    @SuppressWarnings("unchecked")
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
        <p>Consulta los turnos de tu equipo de voluntarios.</p>
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

    <% if (selectedCampaignId != null) { %>
    <div class="card">
        <div class="card-header">
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
                <tbody>
                    <% if (teamShifts.isEmpty()) { %>
                    <tr>
                        <td colspan="6" class="table-empty">No hay turnos asignados para esta campaña.</td>
                    </tr>
                    <% } else { %>
                        <% for (Map<String, Object> shift : teamShifts) {
                            String storeName = shift.get("storeName") != null ? shift.get("storeName").toString() : "-";
                            String day       = shift.get("day")       != null ? shift.get("day").toString()       : "-";
                            String startTime = shift.get("startTime") != null ? shift.get("startTime").toString() : "-";
                            String endTime   = shift.get("endTime")   != null ? shift.get("endTime").toString()   : "-";
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> volunteers = (List<Map<String, Object>>) shift.get("volunteers");
                            if (volunteers == null || volunteers.isEmpty()) { %>
                        <tr>
                            <td><%= storeName %></td>
                            <td><%= day %></td>
                            <td><%= startTime %></td>
                            <td><%= endTime %></td>
                            <td><em>Sin voluntarios</em></td>
                            <td>&mdash;</td>
                        </tr>
                        <%  } else {
                                for (Map<String, Object> v : volunteers) {
                                    boolean attended = Boolean.TRUE.equals(v.get("attendance")); %>
                        <tr>
                            <td><%= storeName %></td>
                            <td><%= day %></td>
                            <td><%= startTime %></td>
                            <td><%= endTime %></td>
                            <td><%= v.get("volunteerName") != null ? v.get("volunteerName") : "-" %></td>
                            <td><input type="checkbox" <%= attended ? "checked" : "" %> disabled></td>
                        </tr>
                        <%      }
                            }
                        } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
    <% } %>
</main>
</body>
</html>
