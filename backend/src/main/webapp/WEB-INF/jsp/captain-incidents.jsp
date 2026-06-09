<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");
    if (!"CAPITAN".equals(role)) {
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
    <title>Bancosol | Registrar Incidencia</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/captain-dashboard" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Capitán" : nombre %></span>
        </div>
        <button class="btn-edit" id="btn-edit">Editar perfil 🖉</button>
        <button class="btn-logout" id="btn-logout">Cerrar sesión ×</button>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/captain-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Registrar Incidencia</h1>
                <p>Notifica incidencias ocurridas en tus tiendas.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card">
        <div class="card-head">
            <h2>Nueva incidencia</h2>
        </div>
        <div class="card-body">
            <div class="form-group">
                <label for="campaign-select">Campaña</label>
                <select id="campaign-select">
                    <option value="">Selecciona una campaña...</option>
                    <% if (campaigns != null) {
                        for (CampaignDTO camp : campaigns) { %>
                    <option value="<%= camp.getId() %>"><%= camp.getName() %></option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label for="store-select">Tienda</label>
                <select id="store-select" disabled>
                    <option value="">Selecciona primero una campaña...</option>
                </select>
            </div>
            <div class="form-group mb-0">
                <label for="description">Descripción de la incidencia</label>
                <textarea id="description" rows="5" placeholder="Describe con detalle la incidencia ocurrida..."
                    class="textarea-field"></textarea>
            </div>
        </div>
        <div class="card-body pt-0">
            <button type="button" id="btn-submit" class="btn btn-primary">Enviar incidencia</button>
        </div>
    </div>

    <div class="card">
        <div class="card-head card-head-flex">
            <h2>Historial de incidencias</h2>
            <button type="button" id="btn-load-history" class="btn btn-secondary m-0">Cargar historial</button>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Campaña</th>
                        <th>Tienda</th>
                        <th>Descripción</th>
                    </tr>
                </thead>
                <tbody id="incidents-tbody">
                    <tr><td colspan="4" class="table-empty">Selecciona campaña y tienda para ver el historial.</td></tr>
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
        setTimeout(() => el.setAttribute('hidden', ''), 4000);
    }

    document.getElementById('campaign-select').addEventListener('change', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        const storeSelect = document.getElementById('store-select');
        storeSelect.innerHTML = '<option value="">Cargando tiendas...</option>';
        storeSelect.disabled = true;
        if (!campaignId) { storeSelect.innerHTML = '<option value="">Selecciona primero una campaña...</option>'; return; }
        try {
            const res = await fetch('/api/captain/my-stores?campaignId=' + campaignId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { storeSelect.innerHTML = '<option value="">Error al cargar</option>'; return; }
            const stores = await res.json();
            storeSelect.innerHTML = '<option value="">Selecciona una tienda...</option>' +
                stores.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
            storeSelect.disabled = false;
        } catch (e) {
            storeSelect.innerHTML = '<option value="">Error de conexión</option>';
        }
    });

    document.getElementById('btn-submit').addEventListener('click', async () => {
        const campaignId  = document.getElementById('campaign-select').value;
        const storeId     = document.getElementById('store-select').value;
        const description = document.getElementById('description').value.trim();
        if (!campaignId || !storeId || !description) {
            showMsg('Selecciona campaña, tienda y escribe una descripción.', 'error'); return;
        }
        try {
            const res = await fetch('/api/captain/incidents', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN, 'Content-Type': 'application/json' },
                body: JSON.stringify({ campaignId: parseInt(campaignId), storeId: parseInt(storeId), description })
            });
            if (res.ok) {
                showMsg('Incidencia enviada correctamente.', 'success');
                document.getElementById('description').value = '';
            } else {
                const err = await res.json().catch(() => ({}));
                showMsg(err.message || 'Error al enviar la incidencia.', 'error');
            }
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    });

    document.getElementById('btn-load-history').addEventListener('click', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        const storeId    = document.getElementById('store-select').value;
        if (!campaignId || !storeId) { showMsg('Selecciona campaña y tienda para ver el historial.', 'error'); return; }
        const tbody = document.getElementById('incidents-tbody');
        tbody.innerHTML = '<tr><td colspan="4" class="table-empty">Cargando...</td></tr>';
        try {
            const res = await fetch('/api/captain/incidents?campaignId=' + campaignId + '&storeId=' + storeId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { showMsg('Error al cargar el historial.', 'error'); return; }
            const incidents = await res.json();
            tbody.innerHTML = incidents.length
                ? incidents.map(i =>
                    `<tr>
                        <td>${i.createdAt || '-'}</td>
                        <td>${i.campaignName || '-'}</td>
                        <td>${i.storeName || '-'}</td>
                        <td>${i.description || '-'}</td>
                    </tr>`
                ).join('')
                : '<tr><td colspan="4" class="table-empty">No hay incidencias registradas.</td></tr>';
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    });
</script>
</body>
</html>
