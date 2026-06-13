<%--
  Vista de registro de incidencias (capitán).

  Autores:
  - Fernando Luis Pinilla Molina: 75%
  - Hugo Herrero González: 5%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.StoreResponseDto, es.grupo8.backend.dto.IncidentDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"CAPITAN".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    if (campaigns == null) campaigns = List.of();

    Integer selectedCampaignId = (Integer) request.getAttribute("selectedCampaignId");
    Integer selectedStoreId = (Integer) request.getAttribute("selectedStoreId");
    List<StoreResponseDto> stores = (List<StoreResponseDto>) request.getAttribute("stores");
    List<IncidentDTO> incidents = (List<IncidentDTO>) request.getAttribute("incidents");
    if (stores == null) stores = List.of();
    if (incidents == null) incidents = List.of();

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Registrar Incidencia</title>
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
        <h1>Registrar Incidencia</h1>
        <p>Notifica incidencias ocurridas en tus tiendas.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Selecciona campaña y tienda</h2>
        </div>
        <div class="card-body">
            <form method="GET" action="/captain-incidents">
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
            <form method="GET" action="/captain-incidents">
                <input type="hidden" name="campaignId" value="<%= selectedCampaignId %>">
                <label for="store-select">Tienda</label>
                <div class="selector-row">
                    <select id="store-select" name="storeId">
                        <option value="">Selecciona una tienda...</option>
                        <% for (StoreResponseDto s : stores) { %>
                        <option value="<%= s.id() %>"
                            <%= selectedStoreId != null && selectedStoreId.equals(s.id()) ? "selected" : "" %>>
                            <%= s.name() %>
                        </option>
                        <% } %>
                    </select>
                    <button type="submit" class="btn btn-secondary">Ver incidencias</button>
                </div>
            </form>
            <% } %>
        </div>
    </div>

    <% if (selectedCampaignId != null && selectedStoreId != null) { %>
    <div class="card">
        <div class="card-header">
            <h2>Nueva incidencia</h2>
        </div>
        <div class="card-body">
            <form method="POST" action="/captain-incidents/crear">
                <input type="hidden" name="campaignId" value="<%= selectedCampaignId %>">
                <input type="hidden" name="storeId" value="<%= selectedStoreId %>">
                <div class="form-group">
                    <label for="description">Descripción de la incidencia</label>
                    <textarea id="description" name="description" rows="5" required
                              class="textarea-field"
                              placeholder="Describe con detalle la incidencia ocurrida..."></textarea>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Enviar incidencia</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Historial de incidencias</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Campaña</th>
                        <th>Tienda</th>
                        <th>Descripción</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (incidents.isEmpty()) { %>
                    <tr>
                        <td colspan="4" class="table-empty">No hay incidencias registradas.</td>
                    </tr>
                    <% } else { %>
                        <% for (IncidentDTO i : incidents) { %>
                        <tr>
                            <td><%= i.getCreatedAt() != null ? i.getCreatedAt() : "-" %></td>
                            <td><%= i.getCampaignName() != null ? i.getCampaignName() : "-" %></td>
                            <td><%= i.getStoreName() != null ? i.getStoreName() : "-" %></td>
                            <td><%= i.getDescription() != null ? i.getDescription() : "-" %></td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
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
