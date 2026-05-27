<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (token == null || !"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Administración</title>
    <link rel="stylesheet" href="/css/administrador.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="/admin" aria-label="Bancosol admin home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name"><%= nombre == null ? "Admin" : nombre %></span>
        <a id="btn-logout" class="btn" href="/login">Cerrar sesión</a>
    </div>
</header>

<main class="admin-page">
    <section class="page-header">
        <h1>Panel de administración</h1>
        <p>Selecciona una sección para continuar.</p>
    </section>

    <section class="card-grid">
        <a class="card" href="/admin-coordinators">Coordinadores</a>
        <a class="card" href="/admin-captains">Capitanes</a>
        <a class="card" href="/admin-campaigns">Campañas</a>
        <a class="card" href="/admin-stores">Tiendas</a>
        <a class="card" href="/admin-validate-users">Validar usuarios</a>
        <a class="card" href="/campaigns">Ver campañas</a>
    </section>
</main>
</body>
</html>