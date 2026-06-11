<%--
    Pagina de administracion de entidades colaboradoras (SSR).

    Autores:
    - Grupo 8
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="es.grupo8.backend.dto.PartnerEntityResponseDto, java.util.List" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }

    List<PartnerEntityResponseDto> entities = (List<PartnerEntityResponseDto>) request.getAttribute("entities");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    String currentSearch = (String) request.getAttribute("currentSearch");
    String sortField = (String) request.getAttribute("sortField");
    String sortOrder = (String) request.getAttribute("sortOrder");
    Integer currentSize = (Integer) request.getAttribute("currentSize");
    Boolean showForm = (Boolean) request.getAttribute("showForm");
    Boolean isCreating = (Boolean) request.getAttribute("isCreating");
    PartnerEntityResponseDto editEntity = (PartnerEntityResponseDto) request.getAttribute("editEntity");

    if (entities == null) entities = List.of();
    if (currentPage == null) currentPage = 0;
    if (totalPages == null) totalPages = 1;
    if (currentSize == null) currentSize = 20;
    if (sortField == null) sortField = "id";
    if (sortOrder == null) sortOrder = "asc";
    if (showForm == null) showForm = false;
    if (isCreating == null) isCreating = false;

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");

    String baseFilterUrl = "/admin-partner-entities?size=" + currentSize
            + "&sort=" + sortField + "," + sortOrder;
    if (currentSearch != null && !currentSearch.isEmpty()) {
        baseFilterUrl += "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Entidades Colaboradoras</title>
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
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper" aria-label="Partner entities management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Entidades Colaboradoras</h1>
        <p>Gestiona las entidades colaboradoras participantes en las campañas de Bancosol.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <% if (showForm) { %>
    <div class="card">
        <div class="card-header">
            <h2><%= isCreating ? "Nueva entidad colaboradora" : "Editar entidad colaboradora" %></h2>
        </div>
        <form method="POST" action="/admin-partner-entities/guardar">
            <% if (!isCreating && editEntity != null) { %>
            <input type="hidden" name="id" value="<%= editEntity.id() %>">
            <% } %>
            <div class="form-group">
                <label for="input-nombre">Nombre <span class="required-asterisk">*</span></label>
                <input type="text" id="input-nombre" name="nombre" maxlength="255" required
                       placeholder="Nombre de la entidad"
                       value="<%= editEntity != null ? editEntity.name() : "" %>">
            </div>
            <div class="form-group">
                <label for="input-direccion">Dirección</label>
                <input type="text" id="input-direccion" name="direccion" maxlength="500"
                       placeholder="Calle, número..."
                       value="<%= editEntity != null && editEntity.address() != null ? editEntity.address() : "" %>">
            </div>
            <div class="form-group">
                <label for="input-telefono">Teléfono</label>
                <input type="text" id="input-telefono" name="telefono" maxlength="20"
                       placeholder="Ej: +34 912345678"
                       value="<%= editEntity != null && editEntity.phone() != null ? editEntity.phone() : "" %>">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Guardar</button>
                <a href="/admin-partner-entities" class="btn btn-secondary">Cancelar</a>
            </div>
        </form>
    </div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Listado de entidades colaboradoras</h2>
            <div class="card-actions">
                <a href="/api/export/partner-entities" class="btn btn-secondary">Exportar datos</a>
                <a href="/admin-partner-entities?crear=1" class="btn btn-primary">+ Nueva entidad</a>
            </div>
        </div>

        <form method="GET" action="/admin-partner-entities" class="filters-bar">
            <input type="text" name="search" placeholder="Buscar por nombre..."
                   value="<%= currentSearch != null ? currentSearch : "" %>" class="filter-search">
            <select name="sort">
                <option value="id,asc" <%= "id".equals(sortField) && "asc".equals(sortOrder) ? "selected" : "" %>>ID ascendente</option>
                <option value="id,desc" <%= "id".equals(sortField) && "desc".equals(sortOrder) ? "selected" : "" %>>ID descendente</option>
                <option value="name,asc" <%= "name".equals(sortField) && "asc".equals(sortOrder) ? "selected" : "" %>>Nombre A-Z</option>
                <option value="name,desc" <%= "name".equals(sortField) && "desc".equals(sortOrder) ? "selected" : "" %>>Nombre Z-A</option>
            </select>
            <button type="submit" class="btn">Filtrar</button>
            <a href="/admin-partner-entities" class="btn-clear">Limpiar</a>
        </form>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Dirección</th>
                        <th>Teléfono</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (entities.isEmpty()) { %>
                    <tr>
                        <td colspan="5" class="table-empty">
                            <%= (currentSearch != null && !currentSearch.isEmpty()) ? "No hay entidades que coincidan con la búsqueda." : "No hay entidades colaboradoras registradas." %>
                        </td>
                    </tr>
                    <% } else { %>
                        <% for (PartnerEntityResponseDto e : entities) { %>
                        <tr>
                            <td><%= e.id() %></td>
                            <td><strong><%= e.name() %></strong></td>
                            <td><%= e.address() != null ? e.address() : "—" %></td>
                            <td><%= e.phone() != null ? e.phone() : "—" %></td>
                            <td>
                                <div class="td-actions">
                                    <a href="/admin-partner-entities?editar=<%= e.id() %>" class="btn btn-edit btn-sm">Editar</a>
                                    <form method="POST" action="/admin-partner-entities/eliminar/<%= e.id() %>"
                                          style="display:inline"
                                          onsubmit="return confirm('¿Eliminar esta entidad colaboradora?')">
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
            <a href="<%= baseFilterUrl %>&page=<%= currentPage - 1 %>" class="btn btn-secondary">← Anterior</a>
            <% } else { %>
            <button class="btn btn-secondary" disabled>← Anterior</button>
            <% } %>
            <span class="pagination-info">
                Página <%= currentPage + 1 %> de <%= totalPages %>
            </span>
            <select class="pagination-select" id="page-size-select"
                    onchange="window.location.href='<%= baseFilterUrl %>&page=0&size=' + this.value">
                <option value="20" <%= currentSize == 20 ? "selected" : "" %>>20 por página</option>
                <option value="50" <%= currentSize == 50 ? "selected" : "" %>>50 por página</option>
                <option value="100" <%= currentSize == 100 ? "selected" : "" %>>100 por página</option>
            </select>
            <% if (currentPage + 1 < totalPages) { %>
            <a href="<%= baseFilterUrl %>&page=<%= currentPage + 1 %>" class="btn btn-secondary">Siguiente →</a>
            <% } else { %>
            <button class="btn btn-secondary" disabled>Siguiente →</button>
            <% } %>
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
