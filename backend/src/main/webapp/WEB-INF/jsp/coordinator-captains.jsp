<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Capitanes</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
    <script src="/javascript/coordinator-captains.js" defer></script>
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
                    <h1>Capitanes</h1>
                    <p>Consulta y registra capitanes para tus tiendas.</p>
                </div>
            </div>
        </div>

        <div id="global-message" hidden></div>

        <!-- Filtros -->
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
                        <button type="button" id="btn-load" class="btn btn-secondary" disabled>Cargar capitanes</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Capitanes asignados -->
        <div class="card">
            <div class="card-head">
                <h2>Capitanes de la campaña</h2>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Email</th>
                        </tr>
                    </thead>
                    <tbody id="captains-tbody">
                        <tr><td colspan="2" class="table-empty">Selecciona campaña y tienda para ver los capitanes.</td></tr>
                    </tbody>
                </table>
            </div>
            <div id="captains-info" hidden class="info-note">
                Para añadir un capitán, rellena el formulario de abajo. Tu solicitud quedará pendiente de aprobación por el administrador.
            </div>
        </div>

        <!-- Registrar nuevo capitán -->
        <div class="card">
            <div class="card-head">
                <h2>Registrar nuevo capitán</h2>
            </div>
            <div class="card-body">
                <p class="form-description">
                    El nuevo capitán quedará pendiente de validación por el administrador.
                </p>
                <div class="form-group">
                    <label for="new-name">Nombre completo</label>
                    <input type="text" id="new-name" placeholder="Nombre del capitán">
                </div>
                <div class="form-group">
                    <label for="new-email">Email</label>
                    <input type="email" id="new-email" placeholder="email@ejemplo.com">
                </div>
                <div class="form-group">
                    <label for="new-password">Contraseña provisional</label>
                    <input type="password" id="new-password" placeholder="Mínimo 6 caracteres">
                </div>
                <button type="button" id="btn-register" class="btn btn-primary">Registrar capitán</button>
            </div>
        </div>
    </main>
<% String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); %>
<!-- Page generated on: <%= today %> -->
</body>
</html>
