<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <title>Bancosol | Capitanes</title>
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
        <button class="btn-edit" id="btn-edit">Editar perfil 🖉</button>
        <button class="btn-logout" id="btn-logout">Cerrar sesión ×</button>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/coordinator-dashboard" class="back-link-inline">← Volver al panel</a>
        <div class="page-header-row">
            <div>
                <h1>Capitanes</h1>
                <p>Consulta y registra capitanes para tus tiendas.</p>
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
                    <button type="button" id="btn-load" class="btn btn-secondary">Cargar capitanes</button>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-head">
            <h2>Capitanes de la campaña</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Email</th>
                    </tr>
                </thead>
                <tbody id="captains-tbody">
                    <tr><td colspan="2" class="table-empty">Selecciona una campaña para ver los capitanes.</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card">
        <div class="card-head">
            <h2>Registrar nuevo capitán</h2>
        </div>
        <div class="card-body">
            <p class="form-description">
                El nuevo capitán quedará pendiente de validación por el administrador.
            </p>
            <div class="form-group">
                <label for="new-name">Nombre completo</label>
                <input type="text" id="new-name" placeholder="Nombre del capitán">
            </div>
            <div class="form-group">
                <label for="new-email">Email</label>
                <input type="email" id="new-email" placeholder="email@ejemplo.com">
            </div>
            <div class="form-group">
                <label for="new-password">Contraseña provisional</label>
                <input type="password" id="new-password" placeholder="Mínimo 6 caracteres">
            </div>
            <button type="button" id="btn-register" class="btn btn-primary">Registrar capitán</button>
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

    document.getElementById('btn-load').addEventListener('click', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        if (!campaignId) { showMsg('Selecciona una campaña primero.', 'error'); return; }
        const tbody = document.getElementById('captains-tbody');
        tbody.innerHTML = '<tr><td colspan="2" class="table-empty">Cargando...</td></tr>';
        try {
            const res = await fetch('/api/coordinator/captains?campaignId=' + campaignId, {
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
            });
            if (!res.ok) { showMsg('Error al cargar los capitanes.', 'error'); return; }
            const captains = await res.json();
            tbody.innerHTML = captains.length
                ? captains.map(c => `<tr><td>${c.name || '-'}</td><td>${c.email || '-'}</td></tr>`).join('')
                : '<tr><td colspan="2" class="table-empty">No hay capitanes para esta campaña.</td></tr>';
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    });

    document.getElementById('btn-register').addEventListener('click', async () => {
        const campaignId = document.getElementById('campaign-select').value;
        const name     = document.getElementById('new-name').value.trim();
        const email    = document.getElementById('new-email').value.trim();
        const password = document.getElementById('new-password').value;
        if (!campaignId || !name || !email || !password) {
            showMsg('Rellena todos los campos y selecciona una campaña.', 'error');
            return;
        }
        try {
            const res = await fetch('/api/coordinator/captains/register', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN, 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password, campaignId: parseInt(campaignId) })
            });
            if (res.ok) {
                showMsg('Capitán registrado. Pendiente de aprobación del administrador.', 'success');
                document.getElementById('new-name').value = '';
                document.getElementById('new-email').value = '';
                document.getElementById('new-password').value = '';
            } else {
                const err = await res.json().catch(() => ({}));
                showMsg(err.message || 'Error al registrar el capitán.', 'error');
            }
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    });
</script>
</body>
</html>
