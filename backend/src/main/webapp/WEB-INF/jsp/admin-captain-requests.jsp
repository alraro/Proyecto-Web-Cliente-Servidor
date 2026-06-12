<%--
  Vista de aprobación de solicitudes de capitán (admin).

  Autores:
  - Fernando Luis Pinilla Molina: 80%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, es.grupo8.backend.entity.CaptainRequest" %>
<%
    String role   = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");
    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CaptainRequest> pendingRequests = (List<CaptainRequest>) request.getAttribute("pendingRequests");
    if (pendingRequests == null) pendingRequests = List.of();

    String flashSuccess = (String) request.getAttribute("success");
    String flashError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Solicitudes de Capitanes</title>
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

<main class="page-wrapper">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al panel</a>
        <h1>Solicitudes de Capitanes</h1>
        <p>Aprueba o rechaza las solicitudes de alta de nuevos capitanes enviadas por coordinadores.</p>
    </div>

    <% if (flashSuccess != null) { %>
    <div class="toast toast-success" id="flash-message"><%= flashSuccess %></div>
    <% } else if (flashError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= flashError %></div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <h2>Solicitudes pendientes</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Fecha de solicitud</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (pendingRequests.isEmpty()) { %>
                    <tr>
                        <td colspan="4" class="table-empty">No hay solicitudes pendientes.</td>
                    </tr>
                    <% } else { %>
                        <% for (CaptainRequest req : pendingRequests) { %>
                        <tr>
                            <td><%= req.getName() != null ? req.getName() : "-" %></td>
                            <td><%= req.getEmail() != null ? req.getEmail() : "-" %></td>
                            <td><%= req.getCreatedAt() != null ? req.getCreatedAt().toString().substring(0, 10) : "-" %></td>
                            <td>
                                <div class="td-actions">
                                    <form method="POST" action="/admin-captain-requests/<%= req.getId() %>/aprobar" style="display:inline">
                                        <button type="submit" class="btn btn-primary btn-sm">Aprobar</button>
                                    </form>
                                    <form method="POST" action="/admin-captain-requests/<%= req.getId() %>/rechazar" style="display:inline"
                                          onsubmit="return confirm('&iquest;Rechazar la solicitud de &quot;<%= req.getName() %>&quot;?')">
                                        <button type="submit" class="btn btn-danger btn-sm">Rechazar</button>
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
