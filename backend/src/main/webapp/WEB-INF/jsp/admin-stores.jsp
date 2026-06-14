<%--
    Pagina de administracion de tiendas (SSR).

    Autores:
    - Alejandra Ortiz: 80%
    - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="es.grupo8.backend.dto.StoreResponseDto, es.grupo8.backend.dto.ChainResponseDto, es.grupo8.backend.entity.GeographicZone, es.grupo8.backend.entity.Locality, java.util.List" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }

    List<StoreResponseDto> stores = (List<StoreResponseDto>) request.getAttribute("stores");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer currentSize = (Integer) request.getAttribute("currentSize");
    Integer selectedChainId = (Integer) request.getAttribute("selectedChainId");
    Integer selectedLocalityId = (Integer) request.getAttribute("selectedLocalityId");
    Integer selectedZoneId = (Integer) request.getAttribute("selectedZoneId");

    List<ChainResponseDto> chains = (List<ChainResponseDto>) request.getAttribute("chains");
    List<GeographicZone> zones = (List<GeographicZone>) request.getAttribute("zones");
    List<Locality> localities = (List<Locality>) request.getAttribute("localities");

    Boolean showForm = (Boolean) request.getAttribute("showForm");
    Boolean isCreating = (Boolean) request.getAttribute("isCreating");
    StoreResponseDto editEntity = (StoreResponseDto) request.getAttribute("editEntity");

    if (stores == null) stores = List.of();
    if (currentPage == null) currentPage = 0;
    if (totalPages == null) totalPages = 1;
    if (currentSize == null) currentSize = 20;
    if (chains == null) chains = List.of();
    if (zones == null) zones = List.of();
    if (localities == null) localities = List.of();
    if (showForm == null) showForm = false;
    if (isCreating == null) isCreating = false;

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");

    String currentSearch = (String) request.getAttribute("currentSearch");
    String currentSort = (String) request.getAttribute("currentSort");
    if (currentSearch == null) currentSearch = "";
    if (currentSort == null) currentSort = "id,asc";

    String baseFilterUrl = "/admin-stores?size=" + currentSize + "&sort=" + java.net.URLEncoder.encode(currentSort, "UTF-8");
    if (!currentSearch.isEmpty()) baseFilterUrl += "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8");
    if (selectedChainId != null) baseFilterUrl += "&chainId=" + selectedChainId;
    if (selectedLocalityId != null) baseFilterUrl += "&localityId=" + selectedLocalityId;
    if (selectedZoneId != null) baseFilterUrl += "&zoneId=" + selectedZoneId;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Tiendas</title>
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

<main class="page-wrapper" aria-label="Stores management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Tiendas</h1>
        <p>Gestion de tiendas asociadas a cadenas de supermercados.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <% if (showForm) { %>
    <div class="card">
        <div class="card-header">
            <h2><%= isCreating ? "Nueva tienda" : "Editar tienda" %></h2>
        </div>
        <form method="POST" action="/admin-stores/guardar">
            <% if (!isCreating && editEntity != null) { %>
            <input type="hidden" name="id" value="<%= editEntity.id() %>">
            <% } %>
            <div class="form-group">
                <label for="input-nombre">Nombre <span class="required-asterisk">*</span></label>
                <input type="text" id="input-nombre" name="nombre" maxlength="255" required
                       placeholder="Nombre de la tienda"
                       value="<%= editEntity != null ? editEntity.name() : "" %>">
            </div>
            <div class="form-group">
                <label for="input-domicilio">Domicilio</label>
                <input type="text" id="input-domicilio" name="domicilio" maxlength="500"
                       placeholder="Calle, numero..."
                       value="<%= editEntity != null && editEntity.address() != null ? editEntity.address() : "" %>">
            </div>
            <div class="form-group">
                <label for="input-cp">Codigo postal</label>
                <input type="text" id="input-cp" name="cp" maxlength="5"
                       placeholder="Ej: 28001"
                       value="<%= editEntity != null && editEntity.postalCode() != null ? editEntity.postalCode() : "" %>">
            </div>
            <div class="form-group">
                <label for="input-chain">Cadena</label>
                <select id="input-chain" name="chainId">
                    <option value="">Sin cadena asignada</option>
                    <% for (ChainResponseDto c : chains) { %>
                    <option value="<%= c.id() %>"
                        <%= editEntity != null && editEntity.chainId() != null && editEntity.chainId().equals(c.id()) ? "selected" : "" %>>
                        <%= c.name() %>
                    </option>
                    <% } %>
                </select>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Guardar</button>
                <a href="/admin-stores" class="btn btn-secondary">Cancelar</a>
            </div>
        </form>
    </div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Listado de tiendas</h2>
            <div class="card-actions">
                <button id="btn-export-stores" class="btn btn-secondary">Exportar datos</button>
                <button class="btn btn-primary" id="btn-nueva-tienda">+ Nueva tienda</button>
            </div>
        </div>

        <form method="GET" action="/admin-stores" class="filters-bar">
            <input type="text" name="search" placeholder="Buscar por nombre..."
                   value="<%= currentSearch %>" class="filter-search">
            <select id="filter-zone" name="zoneId">
                <option value="">Todas las zonas</option>
                <% for (GeographicZone z : zones) { %>
                <option value="<%= z.getId() %>"
                    <%= selectedZoneId != null && selectedZoneId.equals(z.getId()) ? "selected" : "" %>>
                    <%= z.getName() %>
                </option>
                <% } %>
            </select>
            <select id="filter-locality" name="localityId">
                <option value="">Todas las localidades</option>
                <% for (Locality l : localities) { %>
                <option value="<%= l.getId() %>"
                    <%= selectedLocalityId != null && selectedLocalityId.equals(l.getId()) ? "selected" : "" %>
                    data-zone="<%= l.getIdZone() != null ? l.getIdZone().getId() : "" %>">
                    <%= l.getName() %>
                </option>
                <% } %>
            </select>
            <select name="chainId">
                <option value="">Todas las cadenas</option>
                <% for (ChainResponseDto c : chains) { %>
                <option value="<%= c.id() %>"
                    <%= selectedChainId != null && selectedChainId.equals(c.id()) ? "selected" : "" %>>
                    <%= c.name() %>
                </option>
                <% } %>
            </select>
            <button type="submit" class="btn">Filtrar</button>
            <a href="/admin-stores" class="btn-clear">Limpiar</a>
        </form>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Domicilio</th>
                        <th>Localidad</th>
                        <th>CP</th>
                        <th>Zona</th>
                        <th>Cadena</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (stores.isEmpty()) { %>
                    <tr>
                        <td colspan="8" class="table-empty">No hay tiendas que coincidan con los filtros.</td>
                    </tr>
                    <% } else { %>
                        <% for (StoreResponseDto s : stores) { %>
                        <tr>
                            <td><%= s.id() %></td>
                            <td><strong><%= s.name() %></strong></td>
                            <td><%= s.address() != null ? s.address() : "&mdash;" %></td>
                            <td><%= s.locality() != null ? s.locality() : "&mdash;" %></td>
                            <td><%= s.postalCode() != null ? s.postalCode() : "&mdash;" %></td>
                            <td><%= s.zone() != null ? s.zone() : "&mdash;" %></td>
                            <td><%= s.chainName() != null ? s.chainName() : "&mdash;" %></td>
                            <td>
                                <div class="td-actions">
                                    <a href="/admin-stores?editar=<%= s.id() %><%= selectedZoneId != null ? "&zoneId=" + selectedZoneId : "" %><%= selectedLocalityId != null ? "&localityId=" + selectedLocalityId : "" %><%= selectedChainId != null ? "&chainId=" + selectedChainId : "" %>" class="btn btn-edit btn-sm">Editar</a>
                                    <form method="POST" action="/admin-stores/eliminar/<%= s.id() %>"
                                          style="display:inline"
                                          onsubmit="return confirm('&iquest;Eliminar la tienda &quot;<%= s.name() %>&quot;? Esta acci&oacute;n no se puede deshacer.')">
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

<script>
    (function () {
        var msg = document.getElementById("flash-message");
        if (msg) {
            setTimeout(function () { msg.style.opacity = "0"; }, 3000);
            setTimeout(function () { msg.remove(); }, 3500);
        }

        var zoneSelect = document.getElementById("filter-zone");
        var locSelect = document.getElementById("filter-locality");
        if (zoneSelect && locSelect) {
            zoneSelect.addEventListener("change", function () {
                var zoneId = this.value;
                locSelect.value = "";
                Array.from(locSelect.options).forEach(function (o) {
                    if (o.value === "") return;
                    var oz = o.getAttribute("data-zone");
                    o.style.display = (!zoneId || oz === zoneId) ? "" : "none";
                });
            });
            zoneSelect.dispatchEvent(new Event("change"));
        }
    }());
</script>
</body>
</html>
