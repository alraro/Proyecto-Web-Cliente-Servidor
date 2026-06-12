<%--
  Vista de tiendas asignadas (capitán).

  Autores:
  - Fernando Luis Pinilla Molina: 75%
  - Hugo Herrero González: 5%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.StoreResponseDto, es.grupo8.backend.dto.VolunteerShiftDTO" %>
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
    Integer selectedStoreId = (Integer) request.getAttribute("selectedStoreId");
    @SuppressWarnings("unchecked")
    List<StoreResponseDto> stores = (List<StoreResponseDto>) request.getAttribute("stores");
    @SuppressWarnings("unchecked")
    List<VolunteerShiftDTO> shifts = (List<VolunteerShiftDTO>) request.getAttribute("shifts");
    if (stores == null) stores = List.of();
    if (shifts == null) shifts = List.of();

    String selectedStoreName = "";
    if (selectedStoreId != null) {
        for (StoreResponseDto s : stores) {
            if (selectedStoreId.equals(s.id())) { selectedStoreName = s.name(); break; }
        }
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mis Tiendas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
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
        <h1>Mis Tiendas</h1>
        <p>Vista de solo lectura: tiendas y voluntarios asignados.</p>
    </div>

    <div class="card">
        <div class="card-body">
            <form method="GET" action="/captain-stores">
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
        </div>
    </div>

    <% if (selectedCampaignId != null) { %>
    <div class="card">
        <div class="card-header">
            <h2>Tiendas asignadas</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Cadena</th>
                        <th>Dirección</th>
                        <th>Detalle</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (stores.isEmpty()) { %>
                    <tr>
                        <td colspan="4" class="table-empty">No hay tiendas para esta campaña.</td>
                    </tr>
                    <% } else { %>
                        <% for (StoreResponseDto s : stores) { %>
                        <tr>
                            <td><%= s.name() != null ? s.name() : "-" %></td>
                            <td><%= s.chainName() != null ? s.chainName() : "-" %></td>
                            <td><%= s.address() != null ? s.address() : "-" %></td>
                            <td>
                                <a href="/captain-stores?campaignId=<%= selectedCampaignId %>&amp;storeId=<%= s.id() %>"
                                   class="btn btn-secondary btn-sm">Ver turnos</a>
                            </td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
    <% } %>

    <% if (selectedStoreId != null) { %>
    <div class="card">
        <div class="card-header">
            <h2>Turnos — <%= selectedStoreName %></h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Día</th>
                        <th>Hora inicio</th>
                        <th>Hora fin</th>
                        <th>Voluntario</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (shifts.isEmpty()) { %>
                    <tr>
                        <td colspan="4" class="table-empty">No hay turnos registrados para esta tienda.</td>
                    </tr>
                    <% } else { %>
                        <% for (VolunteerShiftDTO vs : shifts) { %>
                        <tr>
                            <td><%= vs.shiftDay() != null ? vs.shiftDay() : "-" %></td>
                            <td><%= vs.startTime() != null ? vs.startTime() : "-" %></td>
                            <td><%= vs.endTime() != null ? vs.endTime() : "-" %></td>
                            <td><%= vs.volunteerName() != null ? vs.volunteerName() : "Sin asignar" %></td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
    <% } %>
</main>
</body>
</html>
