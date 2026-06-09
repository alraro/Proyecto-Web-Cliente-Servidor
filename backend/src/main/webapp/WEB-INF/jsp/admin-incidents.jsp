<!--
-
- Autores:
-	- Hugo Herrero González: 100%
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role) || token == null) {
        response.sendRedirect("/login");
        return;
    }

    List<Map<String, Object>> incidents = (List<Map<String, Object>>) request.getAttribute("incidents");

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
                            for (Map<String, Object> i : incidents) {
                        %>
                        <tr>
                            <td><%= i.get("createdAt") != null ? i.get("createdAt") : "-" %></td>
                            <td><%= i.get("campaignName") != null ? i.get("campaignName") : "-" %></td>
                            <td><%= i.get("storeName") != null ? i.get("storeName") : "-" %></td>
                            <td><%= i.get("captainName") != null ? i.get("captainName") : "-" %></td>
                            <td><%= i.get("description") != null ? i.get("description") : "-" %></td>
                        </tr>
                        <%
                        }}%>
                    </tbody>
                </table>
            </div>
        </section>


    </main>


</body>