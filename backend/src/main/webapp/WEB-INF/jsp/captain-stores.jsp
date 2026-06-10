<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
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
    <title>Bancosol | Mis Tiendas</title>
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
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/captain-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Mis Tiendas</h1>
                <p>Vista de solo lectura: tiendas y voluntarios asignados.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card">
        <div class="card-body">
            <div class="form-group mb-0">
                <label for="campaign-select">Campaña</label>
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
            <h2>Tiendas asignadas</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Cadena</th>
                        <th>Dirección</th>
                        <th>Detalle</th>
                    </tr>
                </thead>
                <tbody id="stores-tbody">
                    <tr><td colspan="4" class="table-empty">Selecciona una campaña para ver las tiendas.</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <div id="detail-panel" class="detail-panel" hidden>
        <p class="section-title" id="detail-title">Turnos y voluntarios</p>
        <div id="shifts-container"></div>
    </div>
</main>
<script>
    const BEARER_TOKEN = '<%= session.getAttribute("token") != null ? session.getAttribute("token") : "" %>';
    let currentCampaignId = null;

    function showMsg(text, type) {
        const el = document.getElementById('global-message');
        el.textContent = text;
        el.className = 'global-message ' + type;
        el.removeAttribute('hidden');
        setTimeout(() => el.setAttribute('hidden', ''), 4000);
    }

    async function loadStoreDetail(storeId, storeName) {
        document.getElementById('detail-title').textContent = 'Turnos — ' + storeName;
        const container = document.getElementById('shifts-container');
        container.innerHTML = '<p>Cargando turnos...</p>';
        document.getElementById('detail-panel').removeAttribute('hidden');
        try {
            const res = await fetch('/api/captain/volunteer-shifts?campaignId=' + currentCampaignId + '&storeId=' + storeId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { container.innerHTML = '<p>Error al cargar los turnos.</p>'; return; }
            const shifts = await res.json();
            container.innerHTML = shifts.length
                ? shifts.map(s =>
                    `<div class="shift-card">
                        <strong>${s.shiftDay || '-'} ${s.startTime || ''} - ${s.endTime || ''}</strong>
                        <span>${s.volunteerName || 'Sin asignar'}</span>
                    </div>`
                ).join('')
                : '<p class="table-empty">No hay turnos registrados para esta tienda.</p>';
        } catch (e) {
            container.innerHTML = '<p>Error de conexión.</p>';
        }
    }

    document.getElementById('btn-load').addEventListener('click', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        if (!campaignId) { showMsg('Selecciona una campaña primero.', 'error'); return; }
        currentCampaignId = campaignId;
        const tbody = document.getElementById('stores-tbody');
        tbody.innerHTML = '<tr><td colspan="4" class="table-empty">Cargando...</td></tr>';
        document.getElementById('detail-panel').setAttribute('hidden', '');
        try {
            const res = await fetch('/api/captain/my-stores?campaignId=' + campaignId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { showMsg('Error al cargar las tiendas.', 'error'); return; }
            const stores = await res.json();
            tbody.innerHTML = stores.length
                ? stores.map(s =>
                    `<tr>
                        <td>${s.name || '-'}</td>
                        <td>${s.chainName || '-'}</td>
                        <td>${s.address || '-'}</td>
                        <td><button type="button" class="btn btn-secondary" onclick="loadStoreDetail(${s.id},'${(s.name||'').replace(/'/g,"\\'")}')">Ver turnos</button></td>
                    </tr>`
                ).join('')
                : '<tr><td colspan="4" class="table-empty">No hay tiendas para esta campaña.</td></tr>';
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    });
</script>
</body>
</html>
