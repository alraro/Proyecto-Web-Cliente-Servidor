<%--
  Vista CRUD de gestión de campañas (admin).

  Autores:
  - Fernando Luis Pinilla Molina: 65%
  - Hugo Herrero González: 5%
  - IA Generativa: 30%
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Set, es.grupo8.backend.dto.CampaignDTO, es.grupo8.backend.dto.CampaignTypeResponseDto, es.grupo8.backend.dto.StoreResponseDto" %>
<%
    String role   = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");
    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    @SuppressWarnings("unchecked")
    List<CampaignTypeResponseDto> campaignTypes = (List<CampaignTypeResponseDto>) request.getAttribute("campaignTypes");
    if (campaigns == null) campaigns = List.of();
    if (campaignTypes == null) campaignTypes = List.of();

    Boolean showForm = (Boolean) request.getAttribute("showForm");
    Boolean isCreating = (Boolean) request.getAttribute("isCreating");
    CampaignDTO editEntity = (CampaignDTO) request.getAttribute("editEntity");
    if (showForm == null) showForm = false;
    if (isCreating == null) isCreating = false;

    @SuppressWarnings("unchecked")
    List<StoreResponseDto> allStores = (List<StoreResponseDto>) request.getAttribute("allStores");
    @SuppressWarnings("unchecked")
    Set<Integer> assignedStoreIds = (Set<Integer>) request.getAttribute("assignedStoreIds");
    if (allStores == null) allStores = List.of();
    if (assignedStoreIds == null) assignedStoreIds = Set.of();

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Campañas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/admin-campaigns.css">
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

<main class="page-wrapper" aria-label="Campaign management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Gestión de Campañas</h1>
        <p>Crea, edita y elimina campañas de recogida de alimentos.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <% if (showForm) { %>
    <div class="card">
        <div class="card-header">
            <h2><%= isCreating ? "Nueva campaña" : "Editar campaña" %></h2>
        </div>
        <form method="POST" action="/admin-campaigns/guardar">
            <% if (!isCreating && editEntity != null) { %>
            <input type="hidden" name="id" value="<%= editEntity.getId() %>">
            <% } %>
            <div class="card-body">
                <div class="form-grid">
                    <div class="form-group full-width">
                        <label for="campaign-name">Nombre <span class="required-asterisk">*</span></label>
                        <input id="campaign-name" name="name" type="text" required maxlength="100"
                               placeholder="Nombre de la campaña"
                               value="<%= editEntity != null && editEntity.getName() != null ? editEntity.getName() : "" %>">
                    </div>
                    <div class="form-group full-width">
                        <label for="campaign-type">Tipo <span class="required-asterisk">*</span></label>
                        <select id="campaign-type" name="typeId" required>
                            <option value="">Selecciona un tipo...</option>
                            <% for (CampaignTypeResponseDto t : campaignTypes) { %>
                            <option value="<%= t.getId() %>"
                                <%= editEntity != null && t.getId().equals(editEntity.getTypeId()) ? "selected" : "" %>>
                                <%= t.getName() %>
                            </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="campaign-start">Fecha de inicio <span class="required-asterisk">*</span></label>
                        <input id="campaign-start" name="startDate" type="date" required
                               value="<%= editEntity != null && editEntity.getStartDate() != null ? editEntity.getStartDate() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="campaign-end">Fecha de fin <span class="required-asterisk">*</span></label>
                        <input id="campaign-end" name="endDate" type="date" required
                               value="<%= editEntity != null && editEntity.getEndDate() != null ? editEntity.getEndDate() : "" %>">
                    </div>
                </div>

                <div class="store-selector-section">
                    <h3 class="store-selector-title">Tiendas participantes</h3>
                    <ul class="store-list">
                        <% if (allStores.isEmpty()) { %>
                        <li class="store-list-empty">No hay tiendas registradas.</li>
                        <% } else { %>
                            <% for (StoreResponseDto s : allStores) { %>
                            <li>
                                <label>
                                    <input type="checkbox" name="storeIds" value="<%= s.id() %>"
                                        <%= assignedStoreIds.contains(s.id()) ? "checked" : "" %>>
                                    <%= s.name() != null ? s.name() : "-" %>
                                    &mdash; <%= s.chainName() != null ? s.chainName() : "-" %>
                                    &mdash; <%= s.locality() != null ? s.locality() : "-" %>
                                </label>
                            </li>
                            <% } %>
                        <% } %>
                    </ul>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Guardar</button>
                    <a href="/admin-campaigns" class="btn btn-secondary">Cancelar</a>
                </div>
            </div>
        </form>
    </div>
    <% } %>

    <div class="card" aria-label="Listado de campañas">
        <div class="card-header">
            <h2>Listado de campañas</h2>
            <div class="card-actions">
                <a href="/api/export/campaigns" class="btn btn-secondary">Exportar datos</a>
                <a href="/admin-campaigns?crear=1" class="btn btn-primary">+ Nueva campaña</a>
            </div>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Tipo</th>
                        <th>Fecha inicio</th>
                        <th>Fecha fin</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (campaigns.isEmpty()) { %>
                    <tr>
                        <td colspan="5" class="table-empty">No hay campañas registradas.</td>
                    </tr>
                    <% } else { %>
                        <% for (CampaignDTO c : campaigns) { %>
                        <tr>
                            <td><strong><%= c.getName() != null ? c.getName() : "-" %></strong></td>
                            <td><%= c.getTypeName() != null && !c.getTypeName().isEmpty() ? c.getTypeName() : "-" %></td>
                            <td><%= c.getStartDate() != null ? c.getStartDate() : "-" %></td>
                            <td><%= c.getEndDate() != null ? c.getEndDate() : "-" %></td>
                            <td>
                                <div class="td-actions">
                                    <a href="/admin-campaigns?editar=<%= c.getId() %>" class="btn btn-secondary btn-sm">Editar</a>
                                    <form method="POST" action="/admin-campaigns/eliminar/<%= c.getId() %>"
                                          style="display:inline"
                                          onsubmit="return confirm('&iquest;Eliminar la campa&ntilde;a &quot;<%= c.getName() %>&quot;? Se eliminar&aacute;n tambi&eacute;n todas sus asignaciones.')">
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
