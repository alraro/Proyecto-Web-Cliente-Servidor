<%--
  Vista de gestión y alta de capitanes (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 75%
  - Hugo Herrero González: 5%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.UserResponseDto" %>
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
    List<UserResponseDto> captains = (List<UserResponseDto>) request.getAttribute("captains");
    if (captains == null) captains = List.of();

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Capitanes</title>
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
        <h1>Capitanes</h1>
        <p>Consulta y registra capitanes para tus tiendas.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <div class="card">
        <div class="card-body">
            <form method="GET" action="/coordinator-captains">
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
                    <button type="submit" class="btn btn-secondary">Cargar capitanes</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Capitanes de la campaña</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Email</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (selectedCampaignId == null) { %>
                    <tr>
                        <td colspan="2" class="table-empty">Selecciona una campaña para ver los capitanes.</td>
                    </tr>
                    <% } else if (captains.isEmpty()) { %>
                    <tr>
                        <td colspan="2" class="table-empty">No hay capitanes para esta campaña.</td>
                    </tr>
                    <% } else { %>
                        <% for (UserResponseDto c : captains) { %>
                        <tr>
                            <td><%= c.name() != null ? c.name() : "-" %></td>
                            <td><%= c.email() != null ? c.email() : "-" %></td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <% if (selectedCampaignId != null) { %>
    <div class="card">
        <div class="card-header">
            <h2>Registrar nuevo capitán</h2>
        </div>
        <div class="card-body">
            <p class="form-description">
                El nuevo capitán quedará pendiente de validación por el administrador.
            </p>
            <form method="POST" action="/coordinator-captains/registrar">
                <input type="hidden" name="campaignId" value="<%= selectedCampaignId %>">
                <div class="form-group">
                    <label for="new-name">Nombre completo</label>
                    <input type="text" id="new-name" name="name" required placeholder="Nombre del capitán">
                </div>
                <div class="form-group">
                    <label for="new-email">Email</label>
                    <input type="email" id="new-email" name="email" required placeholder="email@ejemplo.com">
                </div>
                <div class="form-group">
                    <label for="new-password">Contraseña provisional</label>
                    <input type="password" id="new-password" name="password" required minlength="6"
                           placeholder="Mínimo 6 caracteres">
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Registrar capitán</button>
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
