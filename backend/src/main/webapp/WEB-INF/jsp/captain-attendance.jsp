<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Asistencia del Equipo</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/captain-attendance.css">
    <script src="/javascript/common.js" defer></script>
    <script src="/javascript/captain-attendance.js" defer></script>
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
                    <h1>Asistencia del Equipo</h1>
                    <p>Marca la asistencia de los voluntarios en tus turnos.</p>
                </div>
            </div>
        </div>

        <div id="global-message" hidden></div>


        <div class="card">
            <div class="card-head">
                <h2>Selecciona una campaña</h2>
            </div>
            <div class="card-body">
                <div class="form-group">
                    <label for="campaign-select">Campaña</label>
                    <select id="campaign-select">
                        <option value="">Cargando campañas...</option>
                    </select>
                </div>
            </div>
        </div>


        <div id="shifts-container">

        </div>
    </main>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
