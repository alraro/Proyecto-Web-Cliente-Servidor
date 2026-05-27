<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (token == null || !"RESPONSABLE_TIENDA".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mi Tienda</title>
    <link rel="stylesheet" href="/css/administrador.css">
    <link rel="stylesheet" href="/css/admin-validate-responsible.css">
</head>
<body>

<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/index" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name"><%= nombre == null ? "Responsable" : nombre %></span>
        <a class="btn" href="/edit">Editar perfil</a>
        <a id="btn-logout" class="btn" href="/login">Cerrar sesión</a>
    </div>
</header>

<main class="admin-page" aria-label="Responsible store page">
    <section class="page-header">
        <h1>Mi tienda</h1>
        <p>Información de tu tienda asignada y turnos programados.</p>
    </section>

    <section class="card hidden" id="card-tienda" aria-label="Información de la tienda">
        <h2 id="store-title">Cargando...</h2>
        <div class="info-grid" id="info-grid"></div>
    </section>

    <section class="card hidden section-gap" id="card-turnos" aria-label="Turnos programados">
        <h2>Turnos programados</h2>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Campaña</th>
                        <th>Voluntario</th>
                        <th>Hora fin</th>
                        <th>Asistencia</th>
                        <th>Notas</th>
                    </tr>
                </thead>
                <tbody id="shifts-tbody">
                    <tr><td colspan="5" class="table-empty">Cargando turnos...</td></tr>
                </tbody>
            </table>
        </div>
    </section>

    <div id="error-msg" class="error-panel hidden"></div>
</main>
</body>
</html>
