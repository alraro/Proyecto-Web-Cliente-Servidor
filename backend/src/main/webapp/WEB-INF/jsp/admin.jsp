<%--
    Pagina de administracion.

    Autores:
    - Alejandra Ortiz: 80%
    - IA Generativa: 20%
--%>
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
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="/index" aria-label="Bancosol admin home">
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
    <div class="welcome-bar">
        <div>
            <h2>Bienvenido, <span id="welcome-name"><%= nombre == null ? "Admin" : nombre %></span> 👋</h2>
            <p>Desde aquí puedes gestionar todos los aspectos de las campañas de Bancosol.</p>
        </div>
        <span class="role-pill">🔑 Administrador</span>
    </div>

    <p class="section-title">Gestión</p>
    <div class="menu-grid">
        <a class="menu-card" href="/admin-chains">
            <div class="menu-card-icon icon-blue">🏪</div>
            <h3>Cadenas de supermercados</h3>
            <p>Crear, editar y eliminar cadenas. Activar o desactivar su participación en campañas.</p>
            <span class="menu-card-arrow">Ir a cadenas →</span>
        </a>
        <a class="menu-card" href="/admin-campaigns">
            <div class="menu-card-icon icon-orange">📢</div>
            <h3>Campañas</h3>
            <p>Crear y gestionar campañas de recogida de alimentos, asignar fechas y zonas.</p>
            <span class="menu-card-arrow">Ir a campañas →</span>
        </a>
        <a class="menu-card" href="/campaigns">
            <div class="menu-card-icon icon-blue">📋</div>
            <h3>Ver campañas</h3>
            <p>Consulta el estado de todas las campañas: activas, pasadas y futuras.</p>
            <span class="menu-card-arrow">Ver listado →</span>
        </a>
        <a class="menu-card" href="/admin-stores">
            <div class="menu-card-icon icon-green">🏬</div>
            <h3>Tiendas</h3>
            <p>Gestionar las tiendas participantes, asignar cadenas y códigos postales.</p>
            <span class="menu-card-arrow">Ir a tiendas →</span>
        </a>
        <a class="menu-card" href="/admin-dashboard">
            <div class="menu-card-icon icon-teal">📊</div>
            <h3>Dashboard</h3>
            <p>Visualiza cobertura por cadena, localidad y zona para cada campaña.</p>
            <span class="menu-card-arrow">Ir a dashboard →</span>
        </a>
        <a class="menu-card" href="/admin-createusers">
            <div class="menu-card-icon icon-orange">👤</div>
            <h3>Crear usuarios</h3>
            <p>Crear nuevos usuarios con roles asignados.</p>
            <span class="menu-card-arrow">Ir a crear usuarios →</span>
        </a>
    </div>

    <p class="section-title">Usuarios y accesos</p>
    <div class="menu-grid">
        <a class="menu-card" href="/admin-validate-users">
            <div class="menu-card-icon icon-purple">✅</div>
            <h3>Validar registros</h3>
            <p>Revisar y aprobar o rechazar las solicitudes de nuevos usuarios.</p>
            <span class="menu-card-arrow">Ver solicitudes →</span>
        </a>
        <a class="menu-card" href="/admin-users">
            <div class="menu-card-icon icon-teal">👥</div>
            <h3>Usuarios</h3>
            <p>Ver todos los usuarios registrados, cambiar roles y gestionar permisos.</p>
            <span class="menu-card-arrow">Ir a usuarios →</span>
        </a>
        <a class="menu-card" href="/admin-partner-entities">
            <div class="menu-card-icon icon-orange">🤝</div>
            <h3>Entidades colaboradoras</h3>
            <p>Gestionar las asociaciones y entidades que colaboran en las campañas.</p>
            <span class="menu-card-arrow">Ir a entidades →</span>
        </a>
    </div>

    <p class="section-title">Operativa de campaña</p>
    <div class="menu-grid">
        <a class="menu-card" href="/admin-captains">
            <div class="menu-card-icon icon-red">⚓</div>
            <h3>Capitanes</h3>
            <p>Asignar capitanes a tiendas y campañas, ver su estado y actividad.</p>
            <span class="menu-card-arrow">Ir a capitanes →</span>
        </a>
        <a class="menu-card" href="/admin-coordinators">
            <div class="menu-card-icon icon-blue">🗺️</div>
            <h3>Coordinadores</h3>
            <p>Ver y asignar coordinadores por zona geográfica y campaña.</p>
            <span class="menu-card-arrow">Ir a coordinadores →</span>
        </a>
        <a class="menu-card" href="/admin-captain-requests">
            <div class="menu-card-icon icon-purple">📋</div>
            <h3>Solicitudes de Capitán</h3>
            <p>Revisa y aprueba o rechaza las solicitudes de nuevos capitanes.</p>
            <span class="menu-card-arrow">Ver solicitudes →</span>
        </a>
    </div>
</main>
</body>
</html>
