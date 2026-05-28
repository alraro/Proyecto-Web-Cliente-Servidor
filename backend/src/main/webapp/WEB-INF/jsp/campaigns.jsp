<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Ver Campañas</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/campaigns.css">

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
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Campañas de recogida</h1>
        <p>Consulta el estado de todas las campañas de Bancosol.</p>
    </div>

    <div class="chips-bar">
        <button class="chip chip-selected chip-status-all" id="chip-all">Todas</button>
        <button class="chip chip-status-active"  id="chip-active">Activas: —</button>
        <button class="chip chip-status-future"  id="chip-future">Futuras: —</button>
        <button class="chip chip-status-past"    id="chip-past">Pasadas: —</button>
    </div>

    <div class="filter-bar">
        <label for="sort-select">Ordenar por:</label>
        <select id="sort-select">
            <option value="startDate,desc">Inicio (más reciente)</option>
            <option value="startDate,asc">Inicio (más antiguo)</option>
            <option value="name,asc">Nombre (A–Z)</option>
            <option value="name,desc">Nombre (Z–A)</option>
        </select>
    </div>

    <div class="card">
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Tipo</th>
                        <th>Inicio</th>
                        <th>Fin</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody id="campaigns-tbody">
                    <tr><td colspan="5" class="table-empty">Cargando campañas...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="pagination">
        <button id="btn-prev" disabled>← Anterior</button>
        <span id="page-info"></span>
        <button id="btn-next" disabled>Siguiente →</button>
    </div>
</main>

<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
