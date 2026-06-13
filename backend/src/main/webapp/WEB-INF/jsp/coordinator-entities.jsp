<%--
  Vista de entidades colaboradoras por campaña (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 75%
  - Hugo Herrero González: 5%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.CampaignEntityDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"COORDINADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    if (campaigns == null) campaigns = List.of();

    Integer selectedCampaignId = (Integer) request.getAttribute("selectedCampaignId");
    List<CampaignEntityDTO> entities = (List<CampaignEntityDTO>) request.getAttribute("entities");
    if (entities == null) entities = List.of();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Entidades Colaboradoras</title>
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
        <h1>Entidades colaboradoras</h1>
        <p>Consulta qué entidades tienen voluntarios asignados en cada campaña.</p>
    </div>

    <div class="card">
        <div class="card-body">
            <form method="GET" action="/coordinator-entities">
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
                    <button type="submit" class="btn btn-secondary">Ver entidades</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Entidades con voluntarios asignados</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre de entidad</th>
                        <th>Teléfono</th>
                        <th>Nº voluntarios asignados</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (selectedCampaignId == null) { %>
                    <tr>
                        <td colspan="3" class="table-empty">Selecciona una campaña para ver las entidades.</td>
                    </tr>
                    <% } else if (entities.isEmpty()) { %>
                    <tr>
                        <td colspan="3" class="table-empty">No hay entidades con voluntarios en esta campaña.</td>
                    </tr>
                    <% } else { %>
                        <% for (CampaignEntityDTO e : entities) { %>
                        <tr>
                            <td><%= e.getName() != null ? e.getName() : "-" %></td>
                            <td><%= e.getPhone() != null ? e.getPhone() : "-" %></td>
                            <td><%= e.getVolunteerCount() != null ? e.getVolunteerCount() : 0 %></td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</main>
</body>
</html>
