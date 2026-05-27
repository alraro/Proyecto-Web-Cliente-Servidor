<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (token == null || !"CAPITAN".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Capitán</title>
    <link rel="stylesheet" href="/css/administrador.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="/captain" aria-label="Bancosol home">Bancosol</a>
    <div class="topbar-actions">
        <span id="user-name"><%= nombre == null ? "Capitán" : nombre %></span>
        <a id="btn-logout" class="btn" href="/login">Cerrar sesión</a>
    </div>
</header>

<main class="page-shell">
    <section class="card">
        <h1>Panel de capitán</h1>
        <p>Tu sesión se ha iniciado correctamente.</p>
    </section>
</main>
</body>
</html>