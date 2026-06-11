<%--
    Pagina de validacion de registros de usuarios (SSR).

    Autores:
    - Grupo 8
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="es.grupo8.backend.dto.UserResponseDto, java.util.List" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }

    List<UserResponseDto> pendingUsers = (List<UserResponseDto>) request.getAttribute("pendingUsers");
    if (pendingUsers == null) pendingUsers = List.of();

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Validar registros</title>
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

<main class="page-wrapper" aria-label="Pending users page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Validar registros</h1>
        <p>Revisa y aprueba o rechaza las solicitudes de nuevos usuarios.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Pendientes de aprobacion
                <% if (!pendingUsers.isEmpty()) { %>
                <span class="badge badge-no"><%= pendingUsers.size() %></span>
                <% } %>
            </h2>
            <div class="card-actions">
                <a href="/admin-validate-users" class="btn btn-edit btn-sm">&#8635; Actualizar</a>
            </div>
        </div>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Telefono</th>
                        <th>Asignar rol</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (pendingUsers.isEmpty()) { %>
                    <tr>
                        <td colspan="6" class="table-empty">No hay usuarios pendientes de aprobacion.</td>
                    </tr>
                    <% } else { %>
                        <% for (UserResponseDto u : pendingUsers) { %>
                        <tr>
                            <td><%= u.idUser() %></td>
                            <td><strong><%= u.name() %></strong></td>
                            <td><%= u.email() != null ? u.email() : "&mdash;" %></td>
                            <td><%= u.phone() != null ? u.phone() : "&mdash;" %></td>
                            <td>
                                <form method="POST" action="/admin-validate-users/aprobar/<%= u.idUser() %>"
                                      style="display:flex; gap:0.5rem; align-items:center;">
                                    <select name="role" required>
                                        <option value="">Seleccionar rol...</option>
                                        <option value="ADMINISTRADOR">Administrador</option>
                                        <option value="COORDINADOR">Coordinador</option>
                                        <option value="CAPITAN">Capitan</option>
                                        <option value="COLABORADOR">Colaborador</option>
                                        <option value="RESPONSABLE_TIENDA">Responsable de Tienda</option>
                                    </select>
                                    <button type="submit" class="btn btn-primary btn-sm">&#10003; Aprobar</button>
                                </form>
                            </td>
                            <td>
                                <form method="POST" action="/admin-validate-users/rechazar/<%= u.idUser() %>"
                                      style="display:inline"
                                      onsubmit="return confirm('&iquest;Rechazar y eliminar la cuenta de &quot;<%= u.name() %>&quot;? Esta acci&oacute;n no se puede deshacer.')">
                                    <button type="submit" class="btn btn-danger btn-sm">&#10007; Rechazar</button>
                                </form>
                            </td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
    (function () {
        var msg = document.getElementById("flash-message");
        if (msg) {
            setTimeout(function () { msg.style.opacity = "0"; }, 3000);
            setTimeout(function () { msg.remove(); }, 3500);
        }
    }());
</script>
</body>
</html>
