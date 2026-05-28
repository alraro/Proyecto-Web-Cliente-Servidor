<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Panel de Capitán</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/captain-dashboard.css">


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

    <main class="page-wrapper">
        <div class="welcome-bar">
            <div>
                <h2>Bienvenido, <span id="welcome-name">Capitán</span> 👋</h2>
                <p>Desde aquí puedes consultar tus tiendas y reportar incidencias.</p>
            </div>
            <span class="role-pill">⚓ Capitán</span>
        </div>

        <p class="section-title">Mi área de trabajo</p>
        <div class="menu-grid">
            <a class="menu-card" href="/captain-stores">
                <div class="menu-card-icon icon-green">🏬</div>
                <h3>Mis Tiendas</h3>
                <p>Consulta las tiendas que tienes asignadas y los voluntarios de cada turno.</p>
                <span class="menu-card-arrow">Ver tiendas →</span>
            </a>
            <a class="menu-card" href="/captain-incidents">
                <div class="menu-card-icon icon-orange">⚠️</div>
                <h3>Registrar Incidencia</h3>
                <p>Notifica incidencias ocurridas durante la campaña en tus tiendas.</p>
                <span class="menu-card-arrow">Reportar incidencia →</span>
            </a>
            <a class="menu-card" href="/captain-attendance">
                <div class="menu-card-icon icon-blue">✅</div>
                <h3>Asistencia del Equipo</h3>
                <p>Consulta los turnos de tu equipo y marca la asistencia de los voluntarios.</p>
                <span class="menu-card-arrow">Ver turnos →</span>
            </a>
        </div>
    </main>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
