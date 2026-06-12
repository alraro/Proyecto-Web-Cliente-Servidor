<%--
  Vista de gestión de coordinadores por campaña (admin).

  Autores:
  - Fernando Luis Pinilla Molina: 70%
  - Hugo Herrero González: 5%
  - IA Generativa: 25%
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.UserResponseDto" %>
<%
    String role   = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");
    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    if (campaigns == null) campaigns = List.of();

    Integer selectedCampaignId = (Integer) request.getAttribute("selectedCampaignId");
    @SuppressWarnings("unchecked")
    List<UserResponseDto> assignedCoordinators = (List<UserResponseDto>) request.getAttribute("assignedCoordinators");
    @SuppressWarnings("unchecked")
    List<UserResponseDto> availableCoordinators = (List<UserResponseDto>) request.getAttribute("availableCoordinators");
    if (assignedCoordinators == null) assignedCoordinators = List.of();
    if (availableCoordinators == null) availableCoordinators = List.of();

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Gestión de Coordinadores</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Admin" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil &#9998;</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesion &times;</a>
    </div>
</header>

<main class="page-wrapper" aria-label="Coordinators management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Gestión de Coordinadores</h1>
        <p>Asigna coordinadores a campañas de recogida.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <div class="card">
        <div class="card-body">
            <form method="GET" action="/admin-coordinators">
                <label for="campaign-select">Campaña</label>
                <div class="selector-row">
                    <select id="campaign-select" name="campaignId">
                        <option value="">Selecciona una campaña...</option>
                        <% for (CampaignDTO camp : campaigns) { %>
                        <option value="<%= camp.getId() %>"
                            <%= selectedCampaignId != null && selectedCampaignId.equals(camp.getId()) ? "selected" : "" %>>
                            <%= camp.getName() %> (<%= camp.getStartDate() %> - <%= camp.getEndDate() %>)
                        </option>
                        <% } %>
                    </select>
                    <button type="submit" class="btn btn-secondary">Cargar coordinadores</button>
                </div>
            </form>
        </div>
    </div>

    <% if (selectedCampaignId != null) { %>
    <div class="card">
        <div class="card-header">
            <h2>Coordinadores asignados</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (assignedCoordinators.isEmpty()) { %>
                    <tr>
                        <td colspan="3" class="table-empty">Sin coordinadores asignados.</td>
                    </tr>
                    <% } else { %>
                        <% for (UserResponseDto coordinator : assignedCoordinators) { %>
                        <tr>
                            <td><%= coordinator.name() != null ? coordinator.name() : "-" %></td>
                            <td><%= coordinator.email() != null ? coordinator.email() : "-" %></td>
                            <td>
                                <form method="POST" action="/admin-coordinators/eliminar" style="display:inline"
                                      onsubmit="return confirm('&iquest;Quitar a &quot;<%= coordinator.name() %>&quot; de la campa&ntilde;a?')">
                                    <input type="hidden" name="campaignId" value="<%= selectedCampaignId %>">
                                    <input type="hidden" name="userId" value="<%= coordinator.idUser() %>">
                                    <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                                </form>
                            </td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Añadir coordinador</h2>
        </div>
        <div class="card-body">
            <form method="POST" action="/admin-coordinators/asignar">
                <input type="hidden" name="campaignId" value="<%= selectedCampaignId %>">
                <div class="selector-row">
                    <select name="userId" required>
                        <option value="">Selecciona un coordinador...</option>
                        <% for (UserResponseDto user : availableCoordinators) { %>
                        <option value="<%= user.idUser() %>"><%= user.name() %> (<%= user.email() %>)</option>
                        <% } %>
                    </select>
                    <button type="submit" class="btn btn-primary">Asignar</button>
                </div>
            </form>
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
