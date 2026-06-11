<%--
    Pagina de administracion de cadenas (SSR).

    Autores:
    - Alejandra Ortiz: 80%
    - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="es.grupo8.backend.dto.ChainResponseDto, java.util.List" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }

    List<ChainResponseDto> chains = (List<ChainResponseDto>) request.getAttribute("chains");
    Boolean showForm = (Boolean) request.getAttribute("showForm");
    Boolean isCreating = (Boolean) request.getAttribute("isCreating");
    ChainResponseDto editEntity = (ChainResponseDto) request.getAttribute("editEntity");

    if (chains == null) chains = List.of();
    if (showForm == null) showForm = false;
    if (isCreating == null) isCreating = false;

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Cadenas</title>
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

<main class="page-wrapper" aria-label="Chains management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Cadenas de supermercados</h1>
        <p>Gestiona las cadenas participantes en las campanas de Bancosol.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <% if (showForm) { %>
    <div class="card">
        <div class="card-header">
            <h2><%= isCreating ? "Nueva cadena" : "Editar cadena" %></h2>
        </div>
        <form method="POST" action="/admin-chains/guardar">
            <% if (!isCreating && editEntity != null) { %>
            <input type="hidden" name="id" value="<%= editEntity.id() %>">
            <% } %>
            <div class="form-group">
                <label for="input-nombre">Nombre <span class="required-asterisk">*</span></label>
                <input type="text" id="input-nombre" name="nombre" maxlength="255" required
                       placeholder="Ej: Mercadona"
                       value="<%= editEntity != null ? editEntity.name() : "" %>">
            </div>
            <div class="form-group">
                <label for="input-codigo">Codigo <span class="required-asterisk">*</span></label>
                <input type="text" id="input-codigo" name="codigo" maxlength="50" required
                       placeholder="Ej: MERC"
                       value="<%= editEntity != null ? editEntity.code() : "" %>">
                <p class="field-help">Solo letras, numeros, guiones y guiones bajos.</p>
            </div>
            <div class="checkbox-group">
                <input type="checkbox" id="input-participacion" name="participacion" value="true"
                    <%= editEntity != null && Boolean.TRUE.equals(editEntity.participation()) ? "checked" : "" %>>
                <label for="input-participacion">Participa en campanas activas</label>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Guardar</button>
                <a href="/admin-chains" class="btn btn-secondary">Cancelar</a>
            </div>
        </form>
    </div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Listado de cadenas</h2>
            <div class="card-actions">
                <a href="/api/export/chains" class="btn btn-secondary">Exportar datos</a>
                <a href="/admin-chains?crear=1" class="btn btn-primary">+ Nueva cadena</a>
            </div>
        </div>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Codigo</th>
                        <th>Participacion</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (chains.isEmpty()) { %>
                    <tr>
                        <td colspan="5" class="table-empty">No hay cadenas registradas.</td>
                    </tr>
                    <% } else { %>
                        <% for (ChainResponseDto c : chains) { %>
                        <tr>
                            <td><%= c.id() %></td>
                            <td><strong><%= c.name() %></strong></td>
                            <td><code class="inline-code"><%= c.code() %></code></td>
                            <td>
                                <% if (Boolean.TRUE.equals(c.participation())) { %>
                                <span class="badge badge-yes">&#10003; Si</span>
                                <% } else { %>
                                <span class="badge badge-no">&mdash; No</span>
                                <% } %>
                            </td>
                            <td>
                                <div class="td-actions">
                                    <a href="/admin-chains?editar=<%= c.id() %>" class="btn btn-edit btn-sm">Editar</a>
                                    <form method="POST" action="/admin-chains/eliminar/<%= c.id() %>"
                                          style="display:inline"
                                          onsubmit="return confirm('&iquest;Eliminar la cadena &quot;<%= c.name() %>&quot;? Esta acci&oacute;n no se puede deshacer.')">
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
