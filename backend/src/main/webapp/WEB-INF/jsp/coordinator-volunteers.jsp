<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Asignación de Voluntarios</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
    <script src="/javascript/coordinator-volunteers.js" defer></script>
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
                    <h1>Asignación de Voluntarios</h1>
                    <p>Asigna voluntarios a los turnos de tus tiendas.</p>
                </div>
            </div>
        </div>

        <div id="global-message" hidden></div>

        <!-- Filtros campaña + tienda -->
        <div class="card">
            <div class="card-body">
                <div class="form-group">
                    <label for="campaign-select">Campaña</label>
                    <select id="campaign-select">
                        <option value="">Selecciona una campaña...</option>
                    </select>
                </div>
                <div class="form-group mb-0">
                    <label for="store-select">Tienda</label>
                    <div class="selector-row">
                        <select id="store-select" disabled>
                            <option value="">Selecciona primero una campaña...</option>
                        </select>
                        <button type="button" id="btn-load-shifts" class="btn btn-secondary" disabled>Cargar turnos</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Tabla de turnos -->
        <div class="card">
            <div class="card-head">
                <h2>Turnos</h2>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Día</th>
                            <th>Hora inicio</th>
                            <th>Hora fin</th>
                            <th>Voluntarios necesarios</th>
                            <th>Acción</th>
                        </tr>
                    </thead>
                    <tbody id="shifts-tbody">
                        <tr><td colspan="5" class="table-empty">Selecciona campaña y tienda para ver los turnos.</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <!-- Modal asignación de voluntario -->
    <div id="modal-overlay" class="modal-overlay" hidden>
        <div class="modal">
            <div class="modal-header">
                <h3>Asignar voluntario al turno</h3>
                <button type="button" id="modal-close" class="modal-close">✕</button>
            </div>
            <div class="modal-body">
                <p id="modal-shift-info" class="modal-shift-info"></p>
                <div class="form-group">
                    <label for="volunteer-select">Voluntario</label>
                    <select id="volunteer-select">
                        <option value="">Selecciona un voluntario...</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="shift-day-input">Día del turno</label>
                    <input type="date" id="shift-day-input">
                </div>
                <div class="form-group">
                    <label for="start-time-input">Hora de inicio</label>
                    <input type="time" id="start-time-input">
                </div>
                <div class="form-group mb-0">
                    <label for="end-time-input">Hora de fin</label>
                    <input type="time" id="end-time-input">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="modal-cancel" class="btn btn-secondary">Cancelar</button>
                <button type="button" id="modal-save" class="btn btn-primary">Guardar asignación</button>
            </div>
        </div>
    </div>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
