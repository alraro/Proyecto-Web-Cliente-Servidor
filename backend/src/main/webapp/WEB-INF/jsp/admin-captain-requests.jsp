<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, es.grupo8.backend.entity.CaptainRequest" %>
<%
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");
    if (token == null || !"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CaptainRequest> pendingRequests = (List<CaptainRequest>) request.getAttribute("pendingRequests");
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
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/admin" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name"><%= nombre != null ? nombre : "Admin" %></span>
        <a class="btn" href="/edit">Editar perfil</a>
        <a href="/logout" class="btn">Cerrar sesión</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al menú</a>
        <div class="page-header-row">
            <div>
                <h1>Solicitudes de Capitanes</h1>
                <p>Aprueba o rechaza las solicitudes de alta de nuevos capitanes enviadas por coordinadores.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card">
        <div class="card-head">
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
                <tbody id="requests-tbody">
                <% if (pendingRequests == null || pendingRequests.isEmpty()) { %>
                    <tr><td colspan="4" class="table-empty">No hay solicitudes pendientes.</td></tr>
                <% } else {
                    for (CaptainRequest req : pendingRequests) { %>
                    <tr id="row-<%= req.getId() %>">
                        <td><%= req.getName() != null ? req.getName() : "-" %></td>
                        <td><%= req.getEmail() != null ? req.getEmail() : "-" %></td>
                        <td><%= req.getCreatedAt() != null ? req.getCreatedAt().toString().substring(0, 10) : "-" %></td>
                        <td>
                            <button type="button" class="btn btn-primary"
                                onclick="handleRequest(<%= req.getId() %>, 'approve')">Aprobar</button>
                            <button type="button" class="btn btn-secondary"
                                onclick="handleRequest(<%= req.getId() %>, 'reject')">Rechazar</button>
                        </td>
                    </tr>
                <%  }
                   } %>
                </tbody>
            </table>
        </div>
    </div>
</main>
<script>
    const BEARER_TOKEN = '<%= token %>';

    function showMsg(text, type) {
        const el = document.getElementById('global-message');
        el.textContent = text;
        el.className = 'global-message ' + type;
        el.removeAttribute('hidden');
        setTimeout(() => el.setAttribute('hidden', ''), 5000);
    }

    async function handleRequest(id, action) {
        try {
            const res = await fetch('/api/admin/captain-requests/' + id + '/' + action, {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN, 'Content-Type': 'application/json' }
            });
            if (res.ok) {
                const row = document.getElementById('row-' + id);
                if (row) row.remove();
                const msg = action === 'approve' ? 'Solicitud aprobada. El capitán ha sido creado.' : 'Solicitud rechazada.';
                showMsg(msg, 'success');
                if (document.getElementById('requests-tbody').children.length === 0) {
                    document.getElementById('requests-tbody').innerHTML =
                        '<tr><td colspan="4" class="table-empty">No hay solicitudes pendientes.</td></tr>';
                }
            } else {
                const err = await res.json().catch(() => ({}));
                showMsg(err.message || 'Error al procesar la solicitud.', 'error');
            }
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    }
</script>
</body>
</html>
