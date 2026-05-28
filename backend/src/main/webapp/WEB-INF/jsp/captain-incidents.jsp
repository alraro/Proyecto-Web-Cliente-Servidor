<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Registrar Incidencia</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">

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
            <a href="/captain-dashboard" class="back-link-inline">← Volver al panel</a>
            <div class="page-header-row">
                <div>
                    <h1>Registrar Incidencia</h1>
                    <p>Notifica incidencias ocurridas en tus tiendas.</p>
                </div>
            </div>
        </div>

        <div id="global-message" hidden></div>

        <!-- Formulario de incidencia -->
        <div class="card">
            <div class="card-head">
                <h2>Nueva incidencia</h2>
            </div>
            <div class="card-body">
                <div class="form-group">
                    <label for="campaign-select">Campaña</label>
                    <select id="campaign-select">
                        <option value="">Selecciona una campaña...</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="store-select">Tienda</label>
                    <select id="store-select" disabled>
                        <option value="">Selecciona primero una campaña...</option>
                    </select>
                </div>
                <div class="form-group mb-0">
                    <label for="description">Descripción de la incidencia</label>
                    <textarea id="description" rows="5" placeholder="Describe con detalle la incidencia ocurrida..."
                        class="textarea-field"></textarea>
                </div>
            </div>
            <div class="card-body pt-0">
                <button type="button" id="btn-submit" class="btn btn-primary">Enviar incidencia</button>
            </div>
        </div>

        <!-- Historial de incidencias -->
        <div class="card">
            <div class="card-head">
                <h2>Historial de incidencias</h2>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Fecha</th>
                            <th>Campaña</th>
                            <th>Tienda</th>
                            <th>Descripción</th>
                        </tr>
                    </thead>
                    <tbody id="incidents-tbody">
                        <tr><td colspan="4" class="table-empty">Selecciona campaña y tienda para ver el historial.</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
