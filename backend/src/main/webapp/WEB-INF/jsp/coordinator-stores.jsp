<%--
  Vista de tiendas de la campaña (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 75%
  - Hugo Herrero González: 5%
  - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"COORDINADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
    @SuppressWarnings("unchecked")
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mis Tiendas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/coordinator-dashboard" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Coordinador" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/coordinator-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Mis Tiendas</h1>
                <p>Tiendas asignadas a tu campaña.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card">
        <div class="card-body">
            <div class="form-group mb-0">
                <label for="campaign-select">Selecciona una campaña</label>
                <div class="selector-row">
                    <select id="campaign-select">
                        <option value="">Selecciona una campaña...</option>
                        <% if (campaigns != null) {
                            for (CampaignDTO camp : campaigns) { %>
                        <option value="<%= camp.getId() %>"><%= camp.getName() %></option>
                        <%  }
                           } %>
                    </select>
                    <button type="button" id="btn-load" class="btn btn-secondary">Cargar tiendas</button>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-head">
            <h2>Tiendas de la campaña</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Cadena</th>
                        <th>Localidad</th>
                        <th>Dirección</th>
                    </tr>
                </thead>
                <tbody id="stores-tbody">
                    <tr><td colspan="4" class="table-empty">Selecciona una campaña para ver las tiendas.</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</main>
<script>
    const BEARER_TOKEN = '<%= session.getAttribute("token") != null ? session.getAttribute("token") : "" %>';

    function showMsg(text, type) {
        const el = document.getElementById('global-message');
        el.textContent = text;
        el.className = 'global-message ' + type;
        el.removeAttribute('hidden');
        setTimeout(() => el.setAttribute('hidden', ''), 4000);
    }

    document.getElementById('btn-load').addEventListener('click', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        if (!campaignId) { showMsg('Selecciona una campaña primero.', 'error'); return; }
        const tbody = document.getElementById('stores-tbody');
        tbody.innerHTML = '<tr><td colspan="4" class="table-empty">Cargando...</td></tr>';
        try {
            const res = await fetch('/api/coordinator/my-stores?campaignId=' + campaignId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { showMsg('Error al cargar las tiendas.', 'error'); return; }
            const stores = await res.json();
            if (!stores.length) {
                tbody.innerHTML = '<tr><td colspan="4" class="table-empty">No hay tiendas para esta campaña.</td></tr>';
                return;
            }
            tbody.innerHTML = stores.map(s =>
                `<tr>
                    <td>${s.name || '-'}</td>
                    <td>${s.chainName || '-'}</td>
                    <td>${s.locality || '-'}</td>
                    <td>${s.address || '-'}</td>
                </tr>`
            ).join('');
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    });
</script>
</body>
</html>
