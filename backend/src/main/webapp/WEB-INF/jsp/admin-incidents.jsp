<!--
-
- Autores:
-	- Hugo Herrero González: 100%
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.IncidentDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role) || token == null) {
        response.sendRedirect("/login");
        return;
    }

    List<IncidentDTO> incidents = (List<IncidentDTO>) request.getAttribute("incidents");

%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Campañas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">

    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin-incidents.css">
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
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

    <main class="page-wrapper">
        <div class="page-header">
            <a href="/admin" class="back-link-inline">← Volver al panel</a>
            <h1>Historial de Incidencias</h1>
            <p>Visualiza todas las incidencias reportadas por capitanes</p>
        </div>

        <section class="card">
            <div class="campaigns-table-wrap">
                <table id="incident-table">
                    <thead>
                        <tr>
                            <th>Fecha</th>
                            <th>Campaña</th>
                            <th>Tienda</th>
                            <th>Reportado por</th>
                            <th>Descripción</th>
                        </tr>
                    </thead>

                    <tbody>
                        <%
                        if (incidents == null || incidents.isEmpty()) {
                        %>
                        <tr>
                            <td>No hay incidencias registradas</td>
                        </tr>
                        <%
                        } else {
                            for (IncidentDTO i : incidents) {
                        %>
                        <tr>
                            <td><%= i.getCreatedAt() != null ? i.getCreatedAt() : "-" %></td>
                            <td><%= i.getCampaignName() != null ? i.getCampaignName() : "-" %></td>
                            <td><%= i.getStoreName() != null ? i.getStoreName() : "-" %></td>
                            <td><%= i.getCaptainName() != null ? i.getCaptainName() : "-" %></td>
                            <td><%= i.getDescription() != null ? i.getDescription() : "-" %></td>
                        </tr>
                        <%
                        }}%>
                    </tbody>
                </table>
            </div>
        </section>


    </main>


</body>