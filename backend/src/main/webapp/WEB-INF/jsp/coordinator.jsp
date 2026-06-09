<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role   = (String) session.getAttribute("role");

    if (!"COORDINADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Coordinador</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/coordinator.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/coordinator" aria-label="Bancosol coordinador home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Coordinador" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil &#x270F;</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión &times;</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="welcome-bar">
        <div>
            <h2>Bienvenido, <span id="welcome-name"><%= nombre == null ? "Coordinador" : nombre %></span> &#x1F44B;</h2>
            <p>Desde aquí puedes gestionar tus campañas, turnos y voluntarios.</p>
        </div>
        <span class="role-pill">&#x1F5FA;&#xFE0F; Coordinador</span>
    </div>

    <p class="section-title">Mi área de trabajo</p>
    <div class="menu-grid">
        <a class="menu-card" href="/coordinator-campaigns">
            <div class="menu-card-icon icon-orange">&#x1F4E2;</div>
            <h3>Mis Campañas</h3>
            <p>Consulta las campañas en las que participas como coordinador.</p>
            <span class="menu-card-arrow">Ver campañas →</span>
        </a>
        <a class="menu-card" href="/coordinator-stores">
            <div class="menu-card-icon icon-green">&#x1F3EC;</div>
            <h3>Mis Tiendas</h3>
            <p>Ver las tiendas asignadas a ti en cada campaña.</p>
            <span class="menu-card-arrow">Ver tiendas →</span>
        </a>
        <a class="menu-card" href="/coordinator-captains">
            <div class="menu-card-icon icon-red">&#x2693;</div>
            <h3>Capitanes</h3>
            <p>Asigna capitanes a tus tiendas y registra nuevos si es necesario.</p>
            <span class="menu-card-arrow">Gestionar capitanes →</span>
        </a>
        <a class="menu-card" href="/coordinator-volunteers">
            <div class="menu-card-icon icon-blue">&#x1F64B;</div>
            <h3>Voluntarios</h3>
            <p>Asigna colaboradores y voluntarios a los turnos de tus tiendas.</p>
            <span class="menu-card-arrow">Gestionar voluntarios →</span>
        </a>
        <a class="menu-card" href="/coordinator-collaborators">
            <div class="menu-card-icon icon-teal">&#x1F91D;</div>
            <h3>Colaboradores</h3>
            <p>Actualiza datos de contacto y da de alta nuevos colaboradores.</p>
            <span class="menu-card-arrow">Gestionar colaboradores →</span>
        </a>
        <a class="menu-card" href="/coordinator-entities">
            <div class="menu-card-icon icon-teal">&#x1F3E2;</div>
            <h3>Entidades colaboradoras</h3>
            <p>Consulta qué entidades tienen voluntarios asignados en cada campaña.</p>
            <span class="menu-card-arrow">Ver entidades →</span>
        </a>
        <a class="menu-card" href="/coordinator-dashboard">
            <div class="menu-card-icon icon-blue">&#x1F4CA;</div>
            <h3>Dashboard</h3>
            <p>Panel de estadísticas y resumen de actividad de tus campañas.</p>
            <span class="menu-card-arrow">Ver dashboard →</span>
        </a>
    </div>

    <p class="section-title">Turnos</p>
    <div class="menu-grid">
        <a class="menu-card" href="/create-shift">
            <div class="menu-card-icon icon-blue">&#x1F550;</div>
            <h3>Crear turno</h3>
            <p>Crea turnos de recogida para tus tiendas y asigna voluntarios y capitanes.</p>
            <span class="menu-card-arrow">Ir a turnos →</span>
        </a>
        <a class="menu-card" href="/shifts-calendar">
            <div class="menu-card-icon icon-orange">&#x1F4C5;</div>
            <h3>Calendario de Turnos</h3>
            <p>Visualiza los turnos de tu campaña por tienda, día y franja horaria.</p>
            <span class="menu-card-arrow">Ver calendario →</span>
        </a>
    </div>
</main>
</body>
</html>
