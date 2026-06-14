<%--
    Pagina de administracion de usuarios (SSR).

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

    List<UserResponseDto> users = (List<UserResponseDto>) request.getAttribute("users");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer currentSize = (Integer) request.getAttribute("currentSize");
    String currentSearch = (String) request.getAttribute("currentSearch");
    String currentRole = (String) request.getAttribute("currentRole");
    String sortField = (String) request.getAttribute("sortField");
    String sortOrder = (String) request.getAttribute("sortOrder");

    if (users == null) users = List.of();
    if (currentPage == null) currentPage = 0;
    if (totalPages == null) totalPages = 1;
    if (currentSize == null) currentSize = 20;
    if (sortField == null) sortField = "id";
    if (sortOrder == null) sortOrder = "asc";

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");

    String baseFilterUrl = "/admin-users?size=" + currentSize;
    if (currentSearch != null && !currentSearch.isEmpty()) {
        baseFilterUrl += "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8");
    }
    if (currentRole != null && !currentRole.isEmpty()) {
        baseFilterUrl += "&role=" + java.net.URLEncoder.encode(currentRole, "UTF-8");
    }
    baseFilterUrl += "&sort=" + sortField + "," + sortOrder;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Usuarios</title>
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

<main class="page-wrapper" aria-label="Users management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Usuarios</h1>
        <p>Gestiona los usuarios registrados en la plataforma.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Listado de usuarios</h2>
            <div class="card-actions">
                <a href="/api/export/users" class="btn btn-secondary">Exportar datos</a>              
                <a href="/admin-users" class="btn btn-edit btn-sm">&#8635; Actualizar</a>
            </div>
        </div>

        <form method="GET" action="/admin-users" class="filters-bar">
            <input type="text" name="search" placeholder="Buscar por nombre o email..."
                   value="<%= currentSearch != null ? currentSearch : "" %>" class="filter-search">
            <select name="role">
                <option value="">Todos los roles</option>
                <option value="ADMINISTRADOR" <%= "ADMINISTRADOR".equals(currentRole) ? "selected" : "" %>>Administrador</option>
                <option value="COORDINADOR" <%= "COORDINADOR".equals(currentRole) ? "selected" : "" %>>Coordinador</option>
                <option value="CAPITAN" <%= "CAPITAN".equals(currentRole) ? "selected" : "" %>>Capitan</option>
                <option value="COLABORADOR" <%= "COLABORADOR".equals(currentRole) ? "selected" : "" %>>Colaborador</option>
                <option value="RESPONSABLE_TIENDA" <%= "RESPONSABLE_TIENDA".equals(currentRole) ? "selected" : "" %>>Responsable de Tienda</option>
            </select>
            <select name="sort">
                <option value="id,asc" <%= "id".equals(sortField) && "asc".equals(sortOrder) ? "selected" : "" %>>ID ascendente</option>
                <option value="id,desc" <%= "id".equals(sortField) && "desc".equals(sortOrder) ? "selected" : "" %>>ID descendente</option>
                <option value="name,asc" <%= "name".equals(sortField) && "asc".equals(sortOrder) ? "selected" : "" %>>Nombre A-Z</option>
                <option value="name,desc" <%= "name".equals(sortField) && "desc".equals(sortOrder) ? "selected" : "" %>>Nombre Z-A</option>
            </select>
            <button type="submit" class="btn">Filtrar</button>
            <a href="/admin-users" class="btn-clear">Limpiar</a>
        </form>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Telefono</th>
                        <th>Rol actual</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (users.isEmpty()) { %>
                    <tr>
                        <td colspan="6" class="table-empty">
                            <%= (currentSearch != null && !currentSearch.isEmpty()) ? "No hay usuarios que coincidan con la busqueda." : "No hay usuarios registrados." %>
                        </td>
                    </tr>
                    <% } else { %>
                        <% for (UserResponseDto u : users) { %>
                        <tr>
                            <td><%= u.idUser() %></td>
                            <td><strong><%= u.name() %></strong></td>
                            <td><%= u.email() != null ? u.email() : "&mdash;" %></td>
                            <td><%= u.phone() != null ? u.phone() : "&mdash;" %></td>
                            <td>
                                <% if (u.roles() != null && !u.roles().isEmpty()) { %>
                                    <span class="badge badge-yes"><%= String.join(", ", u.roles()) %></span>
                                <% } else { %>
                                    <span class="badge badge-no">PENDIENTE</span>
                                <% } %>
                            </td>
                            <td>
                                <div class="td-actions" style="gap:0.25rem; flex-wrap:nowrap;">
                                    <button type="button" class="btn btn-primary btn-sm"
                                            onclick="abrirModal(<%= u.idUser() %>, '<%= u.roles().get(0) %>')">Editar</button>
                                    <form method="POST" action="/admin-users/eliminar/<%= u.idUser() %>"
                                          style="display:inline"
                                          onsubmit="return confirm('&iquest;Eliminar al usuario &quot;<%= u.name() %>&quot;? Esta acci&oacute;n no se puede deshacer.')">
                                        <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="pagination">
            <% if (currentPage > 0) { %>
            <a href="<%= baseFilterUrl %>&page=<%= currentPage - 1 %>" class="btn btn-secondary">&larr; Anterior</a>
            <% } else { %>
            <button class="btn btn-secondary" disabled>&larr; Anterior</button>
            <% } %>
            <span class="pagination-info">
                Pagina <%= currentPage + 1 %> de <%= totalPages %>
            </span>
            <select class="pagination-select" id="page-size-select"
                    onchange="window.location.href='<%= baseFilterUrl %>&page=0&size=' + this.value">
                <option value="20" <%= currentSize == 20 ? "selected" : "" %>>20 por pagina</option>
                <option value="50" <%= currentSize == 50 ? "selected" : "" %>>50 por pagina</option>
                <option value="100" <%= currentSize == 100 ? "selected" : "" %>>100 por pagina</option>
            </select>
            <% if (currentPage + 1 < totalPages) { %>
            <a href="<%= baseFilterUrl %>&page=<%= currentPage + 1 %>" class="btn btn-secondary">Siguiente &rarr;</a>
            <% } else { %>
            <button class="btn btn-secondary" disabled>Siguiente &rarr;</button>
            <% } %>
        </div>
    </div>
</main>

<div class="modal-backdrop" id="modal-backdrop">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h3 id="modal-title">Cambiar rol de usuario</h3>
        <form method="POST" action="/admin-users/asignar-rol">
            <input type="hidden" name="userId" id="modal-userId">
            <div class="form-group">
                <label for="modal-role">Nuevo rol <span class="required-asterisk">*</span></label>
                <select name="role" id="modal-role" required>
                    <option value="ADMINISTRADOR">Administrador</option>
                    <option value="COORDINADOR">Coordinador</option>
                    <option value="CAPITAN">Capitan</option>
                    <option value="COLABORADOR">Colaborador</option>
                    <option value="RESPONSABLE_TIENDA">Resp. Tienda</option>
                </select>
            </div>
            <p class="form-message" id="modal-error"></p>
            <div class="modal-footer">
                <button type="button" class="btn-cancel" onclick="cerrarModal()">Cancelar</button>
                <button type="submit" class="btn btn-primary">Guardar</button>
            </div>
        </form>
    </div>
</div>

<div class="toast-container" id="toast-container"></div>

<script>
    var currentRole = null;

    function abrirModal(userId, role) {
        currentRole = role;
        document.getElementById("modal-userId").value = userId;
        var select = document.getElementById("modal-role");
        Array.from(select.options).forEach(function(opt) {
            opt.hidden = opt.value === currentRole;
        });
        document.getElementById("modal-backdrop").classList.add("open");
        document.getElementById("modal-error").textContent = "";
    }

    function cerrarModal() {
        document.getElementById("modal-backdrop").classList.remove("open");
    }

    document.getElementById("modal-backdrop").addEventListener("click", function(e) {
        if (e.target === this) cerrarModal();
    });

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
