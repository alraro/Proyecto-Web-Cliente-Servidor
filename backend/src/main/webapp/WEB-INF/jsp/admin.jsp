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
    <a class="brand" href="/index" aria-label="Bancosol admin home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name"><%= nombre == null ? "Admin" : nombre %></span>
        <a href="/edit" class="edit-link">Editar perfil</a>
        <a href="/login" class="logout-link">Cerrar sesión</a>    
    </div>
</header>

<main class="admin-page">
    <section class="page-header">
        <h1>Panel de administración</h1>
        <p>Selecciona una sección para continuar.</p>
    </section>

    <section class="card-grid">
        <a href="/admin-coordinators">Coordinadores</a>
        <a href="/admin-captains">Capitanes</a>
        <a href="/admin-campaigns">Campañas</a>
        <a href="/admin-stores">Tiendas</a>
        <a href="/admin-chains">Cadenas</a>
        <a href="/admin-validate-users">Validar usuarios</a>
        <a href="/campaigns">Ver campañas</a>
        <a href="/admin-dashboard">Dashboard de Cobertura</a>
    </section>
</main>
</body>
</html>