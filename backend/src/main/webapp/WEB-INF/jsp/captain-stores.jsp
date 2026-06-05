<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mis Tiendas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
    <script src="/javascript/captain-stores.js" defer></script>
    <script src="/javascript/includeHTML.js" defer></script>
    <script src="/javascript/header.js" defer></script>

</head>
<body>
    <include-html src="header.html"></include-html>


    <main class="page-wrapper">
        <div class="page-header">
            <a href="/captain-dashboard" class="back-link-inline">← Volver al panel</a>
            <div class="page-header-row">
                <div>
                    <h1>Mis Tiendas</h1>
                    <p>Vista de solo lectura: tiendas y voluntarios asignados.</p>
                </div>
            </div>
        </div>

        <div id="global-message" hidden></div>

        <div class="card">
            <div class="card-body">
                <div class="form-group mb-0">
                    <label for="campaign-select">Campaña</label>
                    <div class="selector-row">
                        <select id="campaign-select">
                            <option value="">Selecciona una campaña...</option>
                        </select>
                        <button type="button" id="btn-load" class="btn btn-secondary">Cargar tiendas</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Tabla de tiendas -->
        <div class="card">
            <div class="card-head">
                <h2>Tiendas asignadas</h2>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Cadena</th>
                            <th>Dirección</th>
                            <th>Detalle</th>
                        </tr>
                    </thead>
                    <tbody id="stores-tbody">
                        <tr><td colspan="4" class="table-empty">Selecciona una campaña para ver las tiendas.</td></tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Panel de detalle de tienda seleccionada -->
        <div id="detail-panel" class="detail-panel" hidden>
            <p class="section-title" id="detail-title">Turnos y voluntarios</p>
            <div id="shifts-container"></div>
        </div>
    </main>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
