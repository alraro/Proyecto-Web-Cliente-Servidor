<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");

    if (token == null || !"COLABORADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Colaborador</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="/collaborator" aria-label="Bancosol colaborador home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Colaborador" : nombre %></span>
        </div>
        <button class="btn-edit" id="btn-edit">Editar perfil &#x270F;</button>
        <button class="btn-logout" id="btn-logout">Cerrar sesión &times;</button>
    </div>
</header>

<main class="page-wrapper">
    <div class="welcome-bar">
        <div>
            <h2>Bienvenido, <span id="welcome-name"><%= nombre == null ? "Colaborador" : nombre %></span> &#x1F44B;</h2>
            <p>Bienvenido al portal de Bancosol. Tu cuenta está activa.</p>
        </div>
        <span class="role-pill">&#x1F91D; Colaborador</span>
    </div>
</main>

<script>
    document.getElementById("btn-edit").addEventListener("click", function () {
        window.location.href = "/edit";
    });
    document.getElementById("btn-logout").addEventListener("click", function () {
        window.location.href = "/logout";
    });
</script>
</body>
</html>
