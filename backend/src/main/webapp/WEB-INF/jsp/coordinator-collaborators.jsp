<%--
  Vista de gestión de colaboradores y voluntarios (coordinador).

  Autores:
  - Fernando Luis Pinilla Molina: 70%
  - Hugo Herrero González: 5%
  - IA Generativa: 25%
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
    <title>Bancosol | Colaboradores</title>
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
                <h1>Colaboradores</h1>
                <p>Gestión de voluntarios y colaboradores de campaña.</p>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card">
        <div class="card-head card-head-flex">
            <h2>Colaboradores y voluntarios</h2>
            <button type="button" id="btn-new" class="btn btn-primary m-0">+ Nuevo colaborador</button>
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
                <tbody id="collaborators-tbody">
                    <tr><td colspan="5" class="table-empty">Cargando colaboradores...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card" id="form-card" hidden>
        <div class="card-head">
            <h2 id="form-title">Nuevo colaborador</h2>
        </div>
        <div class="card-body">
            <input type="hidden" id="edit-id">
            <div class="form-group">
                <label for="edit-name">Nombre</label>
                <input type="text" id="edit-name" placeholder="Nombre completo">
            </div>
            <div class="form-group">
                <label for="edit-phone">Teléfono</label>
                <input type="text" id="edit-phone" placeholder="Teléfono de contacto">
            </div>
            <div class="form-group">
                <label for="edit-email">Email</label>
                <input type="email" id="edit-email" placeholder="email@ejemplo.com">
            </div>
            <div class="form-group">
                <label for="edit-address">Dirección</label>
                <input type="text" id="edit-address" placeholder="Dirección (opcional)">
            </div>
            <div class="form-group mb-0">
                <label for="edit-partner-entity">Entidad colaboradora (opcional)</label>
                <select id="edit-partner-entity">
                    <option value="">Sin entidad (voluntario independiente)</option>
                </select>
            </div>
        </div>
        <div class="card-body card-actions-row">
            <button type="button" id="btn-save" class="btn btn-primary">Guardar</button>
            <button type="button" id="btn-cancel-form" class="btn btn-secondary">Cancelar</button>
        </div>
    </div>
</main>
<script>
    const BEARER_TOKEN = '<%= session.getAttribute("token") != null ? session.getAttribute("token") : "" %>';
    let editingId = null;

    function showMsg(text, type) {
        const el = document.getElementById('global-message');
        el.textContent = text;
        el.className = 'global-message ' + type;
        el.removeAttribute('hidden');
        setTimeout(() => el.setAttribute('hidden', ''), 4000);
    }

    function esc(s) { return (s || '').replace(/'/g, "\\'").replace(/"/g, '&quot;'); }

    async function loadCollaborators() {
        const res = await fetch('/api/coordinator/volunteers', {
            headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
        });
        if (!res.ok) return;
        const vols = await res.json();
        const tbody = document.getElementById('collaborators-tbody');
        tbody.innerHTML = vols.length
            ? vols.map(v =>
                `<tr>
                    <td>${v.name || '-'}</td>
                    <td>${v.phone || '-'}</td>
                    <td>${v.email || '-'}</td>
                    <td>${v.partnerEntityName || 'Independiente'}</td>
                    <td><button type="button" class="btn btn-secondary" onclick="startEdit(${v.id},'${esc(v.name)}','${esc(v.phone)}','${esc(v.email)}','${esc(v.address)}',${v.partnerEntityId || 'null'})">Editar</button></td>
                </tr>`
            ).join('')
            : '<tr><td colspan="5" class="table-empty">No hay colaboradores.</td></tr>';
    }

    async function loadEntities() {
        const res = await fetch('/api/coordinator/partner-entities', {
            headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN }
        });
        if (!res.ok) return;
        const entities = await res.json();
        const sel = document.getElementById('edit-partner-entity');
        entities.forEach(e => {
            const opt = document.createElement('option');
            opt.value = e.id;
            opt.textContent = e.name;
            sel.appendChild(opt);
        });
    }

    function startEdit(id, name, phone, email, address, partnerEntityId) {
        editingId = id;
        document.getElementById('form-title').textContent = 'Editar colaborador';
        document.getElementById('edit-id').value = id;
        document.getElementById('edit-name').value = name;
        document.getElementById('edit-phone').value = phone || '';
        document.getElementById('edit-email').value = email || '';
        document.getElementById('edit-address').value = address || '';
        document.getElementById('edit-partner-entity').value = partnerEntityId || '';
        document.getElementById('form-card').removeAttribute('hidden');
        document.getElementById('form-card').scrollIntoView({ behavior: 'smooth' });
    }

    function showNew() {
        editingId = null;
        document.getElementById('form-title').textContent = 'Nuevo colaborador';
        document.getElementById('edit-id').value = '';
        document.getElementById('edit-name').value = '';
        document.getElementById('edit-phone').value = '';
        document.getElementById('edit-email').value = '';
        document.getElementById('edit-address').value = '';
        document.getElementById('edit-partner-entity').value = '';
        document.getElementById('form-card').removeAttribute('hidden');
    }

    document.getElementById('btn-new').addEventListener('click', showNew);
    document.getElementById('btn-cancel-form').addEventListener('click', () => {
        document.getElementById('form-card').setAttribute('hidden', '');
    });

    document.getElementById('btn-save').addEventListener('click', async () => {
        const name     = document.getElementById('edit-name').value.trim();
        const phone    = document.getElementById('edit-phone').value.trim();
        const email    = document.getElementById('edit-email').value.trim();
        const address  = document.getElementById('edit-address').value.trim();
        const entityId = document.getElementById('edit-partner-entity').value;
        if (!name) { showMsg('El nombre es obligatorio.', 'error'); return; }
        const body = { name, phone: phone || null, email: email || null, address: address || null, partnerEntityId: entityId ? parseInt(entityId) : null };
        const url = editingId ? '/api/coordinator/volunteers/' + editingId : '/api/coordinator/volunteers';
        const method = editingId ? 'PUT' : 'POST';
        try {
            const res = await fetch(url, {
                method,
                headers: { 'Authorization': 'Bearer ' + BEARER_TOKEN, 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (res.ok) {
                showMsg(editingId ? 'Colaborador actualizado.' : 'Colaborador creado.', 'success');
                document.getElementById('form-card').setAttribute('hidden', '');
                loadCollaborators();
            } else {
                const err = await res.json().catch(() => ({}));
                showMsg(err.message || 'Error al guardar.', 'error');
            }
        } catch (e) {
            showMsg('Error de conexión.', 'error');
        }
    });

    loadCollaborators();
    loadEntities();
</script>
</body>
</html>
