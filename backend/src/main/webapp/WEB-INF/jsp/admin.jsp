<%--
    Pagina de administracion.

    Autores:
    - Alejandra Ortiz: 80%
    - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Administracion</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
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
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil &#9998;</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesion &times;</a>
    </div>
</header>

<main class="page-wrapper" aria-label="Admin dashboard">
    <div class="welcome-bar">
        <div>
            <h2>Bienvenido, <span id="welcome-name"><%= nombre == null ? "Admin" : nombre %></span></h2>
            <p>Desde aqui puedes gestionar todos los aspectos de las campanas de Bancosol.</p>
        </div>
        <span class="role-pill">Administrador</span>
    </div>

    <p class="section-title">Gestion</p>
    <div class="menu-grid">
        <a class="menu-card" href="/admin-chains">
            <div class="menu-card-icon icon-blue">&#127978;</div>
            <h3>Cadenas de supermercados</h3>
            <p>Crear, editar y eliminar cadenas. Activar o desactivar su participacion en campanas.</p>
            <span class="menu-card-arrow">Ir a cadenas &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-campaigns">
            <div class="menu-card-icon icon-orange">&#128226;</div>
            <h3>Campanas</h3>
            <p>Crear y gestionar campanas de recogida de alimentos, asignar fechas y zonas.</p>
            <span class="menu-card-arrow">Ir a campanas &rarr;</span>
        </a>
        <a class="menu-card" href="/campaigns">
            <div class="menu-card-icon icon-blue">&#128203;</div>
            <h3>Ver campanas</h3>
            <p>Consulta el estado de todas las campanas: activas, pasadas y futuras.</p>
            <span class="menu-card-arrow">Ver listado &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-stores">
            <div class="menu-card-icon icon-green">&#127980;</div>
            <h3>Tiendas</h3>
            <p>Gestionar las tiendas participantes, asignar cadenas y codigos postales.</p>
            <span class="menu-card-arrow">Ir a tiendas &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-dashboard">
            <div class="menu-card-icon icon-teal">&#128202;</div>
            <h3>Dashboard</h3>
            <p>Visualiza cobertura por cadena, localidad y zona para cada campana.</p>
            <span class="menu-card-arrow">Ir a dashboard &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-createusers">
            <div class="menu-card-icon icon-orange">&#128100;</div>
            <h3>Crear usuarios</h3>
            <p>Crear nuevos usuarios con roles asignados.</p>
            <span class="menu-card-arrow">Ir a crear usuarios &rarr;</span>
        </a>
    </div>

    <p class="section-title">Usuarios y accesos</p>
    <div class="menu-grid">
        <a class="menu-card" href="/admin-validate-users">
            <div class="menu-card-icon icon-purple">&#10004;</div>
            <h3>Validar registros</h3>
            <p>Revisar y aprobar o rechazar las solicitudes de nuevos usuarios.</p>
            <span class="menu-card-arrow">Ver solicitudes &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-users">
            <div class="menu-card-icon icon-teal">&#128101;</div>
            <h3>Usuarios</h3>
            <p>Ver todos los usuarios registrados, cambiar roles y gestionar permisos.</p>
            <span class="menu-card-arrow">Ir a usuarios &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-partner-entities">
            <div class="menu-card-icon icon-orange">&#129309;</div>
            <h3>Entidades colaboradoras</h3>
            <p>Gestionar las asociaciones y entidades que colaboran en las campanas.</p>
            <span class="menu-card-arrow">Ir a entidades &rarr;</span>
        </a>
    </div>

    <p class="section-title">Operativa de campana</p>
    <div class="menu-grid">
        <a class="menu-card" href="/admin-captains">
            <div class="menu-card-icon icon-red">&#9875;</div>
            <h3>Capitanes</h3>
            <p>Asignar capitanes a tiendas y campanas, ver su estado y actividad.</p>
            <span class="menu-card-arrow">Ir a capitanes &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-coordinators">
            <div class="menu-card-icon icon-blue">&#128506;</div>
            <h3>Coordinadores</h3>
            <p>Ver y asignar coordinadores por zona geografica y campana.</p>
            <span class="menu-card-arrow">Ir a coordinadores &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-captain-requests">
            <div class="menu-card-icon icon-purple">&#128203;</div>
            <h3>Solicitudes de Capitan</h3>
            <p>Revisa y aprueba o rechaza las solicitudes de nuevos capitanes.</p>
            <span class="menu-card-arrow">Ver solicitudes &rarr;</span>
        </a>
        <a class="menu-card" href="/admin-incidents">
            <div class="menu-card-icon icon-red">&#128680;</div>
            <h3>Incidencias</h3>
            <p>Visualiza todas las incidencias reportadas por capitanes.</p>
            <span class="menu-card-arrow">Ver incidencias &rarr;</span>
        </a>
    </div>
</main>
</body>
</html>
