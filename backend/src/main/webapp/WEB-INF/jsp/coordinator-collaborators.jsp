<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Colaboradores</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
    <script src="/javascript/coordinator-collaborators.js" defer></script>
    <script src="/javascript/includeHTML.js" defer></script>
    <script src="/javascript/header.js" defer></script>

</head>
<body>
    <include-html src="header.html"></include-html>

    <main class="page-wrapper">
        <div class="page-header">
            <a href="/coordinator-dashboard" class="back-link-inline">← Volver al panel</a>
            <div class="page-header-row">
                <div>
                    <h1>Colaboradores</h1>
                    <p>Gestión de voluntarios y colaboradores de campaña.</p>
                </div>
            </div>
        </div>

        <div id="global-message" hidden></div>

        <!-- Filtro campaña -->
        <div class="card">
            <div class="card-body">
                <div class="form-group mb-0">
                    <label for="campaign-select">Campaña</label>
                    <div class="selector-row">
                        <select id="campaign-select">
                            <option value="">Selecciona una campaña...</option>
                        </select>
                        <button type="button" id="btn-load" class="btn btn-secondary">Cargar colaboradores</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Tabla de colaboradores -->
        <div class="card">
            <div class="card-head card-head-flex">
                <h2>Colaboradores y voluntarios</h2>
                <button type="button" id="btn-new" class="btn btn-primary m-0">+ Nuevo colaborador</button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Teléfono</th>
                            <th>Email</th>
                            <th>Entidad</th>
                            <th>Estado</th>
                            <th>Acción</th>
                        </tr>
                    </thead>
                    <tbody id="collaborators-tbody">
                        <tr><td colspan="6" class="table-empty">Selecciona una campaña para ver los colaboradores.</td></tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Formulario inline para editar / crear -->
        <div class="card" id="form-card" hidden>
            <div class="card-head">
                <h2 id="form-title">Nuevo colaborador</h2>
            </div>
            <div class="card-body">
                <input type="hidden" id="edit-id">
                <div class="form-group">
                    <label for="edit-name">Nombre</label>
                    <input type="text" id="edit-name" placeholder="Nombre completo">
                </div>
                <div class="form-group">
                    <label for="edit-phone">Teléfono</label>
                    <input type="text" id="edit-phone" placeholder="Teléfono de contacto">
                </div>
                <div class="form-group">
                    <label for="edit-email">Email</label>
                    <input type="email" id="edit-email" placeholder="email@ejemplo.com">
                </div>
                <div class="form-group">
                    <label for="edit-address">Dirección</label>
                    <input type="text" id="edit-address" placeholder="Dirección (opcional)">
                </div>
                <div class="form-group mb-0">
                    <label for="edit-partner-entity">Entidad colaboradora (opcional)</label>
                    <select id="edit-partner-entity">
                        <option value="">Sin entidad (voluntario independiente)</option>
                    </select>
                </div>
            </div>
            <div class="card-body card-actions-row">
                <button type="button" id="btn-save" class="btn btn-primary">Guardar</button>
                <button type="button" id="btn-cancel-form" class="btn btn-secondary">Cancelar</button>
            </div>
        </div>
    </main>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
