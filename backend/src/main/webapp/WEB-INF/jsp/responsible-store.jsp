<%--
    Pagina de detalle de tienda del responsable (SSR).

    Autores:
    - Alejandra Ortiz: 100%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map, java.util.List" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");

    if (!"RESPONSABLE_TIENDA".equals(role)) {
        response.sendRedirect("/login");
        return;
    }

    Map<String, Object> store = (Map<String, Object>) request.getAttribute("store");
    List<Map<String, Object>> scheduledShifts = (List<Map<String, Object>>) request.getAttribute("scheduledShifts");
    String storeError = (String) request.getAttribute("storeError");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mi Tienda</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/responsible-store.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Responsable" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil &#9998;</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesion &times;</a>
    </div>
</header>

<main class="page-wrapper" aria-label="Responsible store page">
    <div class="page-header">
        <h1>Mi tienda</h1>
        <p>Informacion de tu tienda asignada y turnos programados.</p>
    </div>

    <% if (storeError != null) { %>
    <div class="toast toast-error" id="flash-message"><%= storeError %></div>
    <% } else if (store != null) { %>

    <div class="card">
        <div class="card-header">
            <h2><%= store.get("name") != null ? store.get("name") : "Tienda" %></h2>
        </div>
        <div class="info-grid">
            <div class="info-item">
                <label>Nombre</label>
                <span><%= store.get("name") != null ? store.get("name") : "&mdash;" %></span>
            </div>
            <div class="info-item">
                <label>Domicilio</label>
                <span><%= store.get("address") != null ? store.get("address") : "&mdash;" %></span>
            </div>
            <div class="info-item">
                <label>Codigo postal</label>
                <span><%= store.get("postalCode") != null ? store.get("postalCode") : "&mdash;" %></span>
            </div>
            <div class="info-item">
                <label>Localidad</label>
                <span><%= store.get("locality") != null ? store.get("locality") : "&mdash;" %></span>
            </div>
            <div class="info-item">
                <label>Zona geog.</label>
                <span><%= store.get("zone") != null ? store.get("zone") : "&mdash;" %></span>
            </div>
            <div class="info-item">
                <label>Cadena</label>
                <span><%= store.get("chainName") != null ? store.get("chainName") : "&mdash;" %></span>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Turnos programados</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Campana</th>
                        <th>Hora fin</th>
                        <th>Asistencia</th>
                        <th>Notas</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (scheduledShifts == null || scheduledShifts.isEmpty()) { %>
                    <tr>
                        <td colspan="4" class="table-empty">No hay turnos programados.</td>
                    </tr>
                    <% } else { %>
                        <% for (Map<String, Object> s : scheduledShifts) { %>
                        <tr>
                            <td><%= s.get("campaignName") != null ? s.get("campaignName") : "&mdash;" %></td>
                            <td><%= s.get("endTime") != null ? s.get("endTime") : "&mdash;" %></td>
                            <td>
                                <% Boolean attendance = (Boolean) s.get("attendance"); %>
                                <% if (Boolean.TRUE.equals(attendance)) { %>
                                <span class="badge-attendance badge-yes">&#10003; Si</span>
                                <% } else if (Boolean.FALSE.equals(attendance)) { %>
                                <span class="badge-attendance badge-no">&#10007; No</span>
                                <% } else { %>
                                <span class="badge-attendance badge-pending">Pendiente</span>
                                <% } %>
                            </td>
                            <td><%= s.get("notes") != null ? s.get("notes") : "&mdash;" %></td>
                        </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <% } %>
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
