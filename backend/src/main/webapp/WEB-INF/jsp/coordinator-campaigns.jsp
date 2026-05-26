<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mis Campañas</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <script src="/javascript/coordinator-campaigns.js" defer></script>
    <script src="/javascript/includeHTML.js" defer></script>
    <script src="/javascript/header.js" defer></script>

</head>
<body>
    <include-html src="header.html"></include-html>

    <main class="page-wrapper">
        <div class="page-header">
            <a href="coordinator-dashboard.html" class="back-link-inline">← Volver al panel</a>
            <div class="page-header-row">
                <div>
                    <h1>Mis Campañas</h1>
                    <p>Campañas en las que participas como coordinador.</p>
                </div>
            </div>
        </div>

        <div id="global-message" hidden></div>

        <div class="card">
            <div class="card-head">
                <h2>Campañas asignadas</h2>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Tipo</th>
                            <th>Fecha inicio</th>
                            <th>Fecha fin</th>
                        </tr>
                    </thead>
                    <tbody id="campaigns-tbody">
                        <tr><td colspan="4" class="table-empty">Cargando campañas...</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
