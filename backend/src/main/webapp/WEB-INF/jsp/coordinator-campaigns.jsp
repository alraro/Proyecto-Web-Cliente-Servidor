<%--
  Vista de campañas asignadas (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 80%
  - Hugo Herrero González: 5%
  - IA Generativa: 15%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"COORDINADOR".equals(role)) {
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
    <title>Bancosol | Mis Campañas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/coordinator-dashboard" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Coordinador" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/coordinator-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Mis Campañas</h1>
                <p>Campañas en las que participas como coordinador.</p>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-head">
            <h2>Campañas asignadas</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Tipo</th>
                        <th>Fecha inicio</th>
                        <th>Fecha fin</th>
                    </tr>
                </thead>
                <tbody>
                <% if (campaigns == null || campaigns.isEmpty()) { %>
                    <tr><td colspan="4" class="table-empty">No tienes campañas asignadas.</td></tr>
                <% } else {
                    for (CampaignDTO camp : campaigns) { %>
                    <tr>
                        <td><%= camp.getName() %></td>
                        <td><%= camp.getTypeName() %></td>
                        <td><%= camp.getStartDate() %></td>
                        <td><%= camp.getEndDate() %></td>
                    </tr>
                <%  }
                   } %>
                </tbody>
            </table>
        </div>
    </div>
</main>
</body>
</html>
