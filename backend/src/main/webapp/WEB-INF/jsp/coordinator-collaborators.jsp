<%--
  Vista de gestión de colaboradores y voluntarios (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 70%
  - Hugo Herrero González: 5%
  - IA Generativa: 25%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.VoluntarioResponseDto, es.grupo8.backend.dto.PartnerEntityResponseDto" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"COORDINADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    List<VoluntarioResponseDto> volunteers = (List<VoluntarioResponseDto>) request.getAttribute("volunteers");
    List<PartnerEntityResponseDto> partnerEntities = (List<PartnerEntityResponseDto>) request.getAttribute("partnerEntities");
    if (volunteers == null) volunteers = List.of();
    if (partnerEntities == null) partnerEntities = List.of();

    Boolean showForm = (Boolean) request.getAttribute("showForm");
    Boolean isCreating = (Boolean) request.getAttribute("isCreating");
    VoluntarioResponseDto editEntity = (VoluntarioResponseDto) request.getAttribute("editEntity");
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
    <title>Bancosol | Colaboradores</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/coordinator-dashboard">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Coordinador" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil &#9998;</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesion &times;</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/coordinator-dashboard" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Colaboradores</h1>
        <p>Gestión de voluntarios y colaboradores de campaña.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <% if (showForm) { %>
    <div class="card">
        <div class="card-header">
            <h2><%= isCreating ? "Nuevo colaborador" : "Editar colaborador" %></h2>
        </div>
        <form method="POST" action="/coordinator-collaborators/guardar">
            <% if (!isCreating && editEntity != null) { %>
            <input type="hidden" name="id" value="<%= editEntity.id() %>">
            <% } %>
            <div class="card-body">
                <div class="form-group">
                    <label for="edit-name">Nombre <span class="required-asterisk">*</span></label>
                    <input type="text" id="edit-name" name="name" required placeholder="Nombre completo"
                           value="<%= editEntity != null && editEntity.name() != null ? editEntity.name() : "" %>">
                </div>
                <div class="form-group">
                    <label for="edit-phone">Teléfono</label>
                    <input type="text" id="edit-phone" name="phone" placeholder="Teléfono de contacto"
                           value="<%= editEntity != null && editEntity.phone() != null ? editEntity.phone() : "" %>">
                </div>
                <div class="form-group">
                    <label for="edit-email">Email</label>
                    <input type="email" id="edit-email" name="email" placeholder="email@ejemplo.com"
                           value="<%= editEntity != null && editEntity.email() != null ? editEntity.email() : "" %>">
                </div>
                <div class="form-group">
                    <label for="edit-address">Dirección</label>
                    <input type="text" id="edit-address" name="address" placeholder="Dirección (opcional)"
                           value="<%= editEntity != null && editEntity.address() != null ? editEntity.address() : "" %>">
                </div>
                <div class="form-group">
                    <label for="edit-partner-entity">Entidad colaboradora (opcional)</label>
                    <select id="edit-partner-entity" name="partnerEntityId">
                        <option value="">Sin entidad (voluntario independiente)</option>
                        <% for (PartnerEntityResponseDto pe : partnerEntities) { %>
                        <option value="<%= pe.id() %>"
                            <%= editEntity != null && pe.id().equals(editEntity.partnerEntityId()) ? "selected" : "" %>>
                            <%= pe.name() %>
                        </option>
                        <% } %>
                    </select>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Guardar</button>
                    <a href="/coordinator-collaborators" class="btn btn-secondary">Cancelar</a>
                </div>
            </div>
        </form>
    </div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Colaboradores y voluntarios</h2>
            <div class="card-actions">
                <a href="/coordinator-collaborators?crear=1" class="btn btn-primary">+ Nuevo colaborador</a>
            </div>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Teléfono</th>
                        <th>Email</th>
                        <th>Entidad</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (volunteers.isEmpty()) { %>
                    <tr>
                        <td colspan="5" class="table-empty">No hay colaboradores.</td>
                    </tr>
                    <% } else { %>
                        <% for (VoluntarioResponseDto v : volunteers) { %>
                        <tr>
                            <td><%= v.name() != null ? v.name() : "-" %></td>
                            <td><%= v.phone() != null ? v.phone() : "-" %></td>
                            <td><%= v.email() != null ? v.email() : "-" %></td>
                            <td><%= v.partnerEntityName() != null ? v.partnerEntityName() : "Independiente" %></td>
                            <td>
                                <a href="/coordinator-collaborators?editar=<%= v.id() %>" class="btn btn-secondary btn-sm">Editar</a>
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
