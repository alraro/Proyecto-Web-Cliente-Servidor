<%--
  Vista de solo lectura del listado de campañas (admin).

  Autores:
  - Fernando Luis Pinilla Molina: 75%
  - IA Generativa: 25%
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%@ page import="java.util.List, java.util.Collections, java.time.format.DateTimeFormatter, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String role = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }

    // Las campañas llegan por el model del controlador (SSR)
    List<CampaignDTO> campaigns = (List<CampaignDTO>) request.getAttribute("campaigns");
    if (campaigns == null) campaigns = Collections.emptyList();

    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    int activeCount = 0, futureCount = 0, pastCount = 0;
    for (CampaignDTO c : campaigns) {
        if ("ACTIVE".equals(c.getStatus()))      activeCount++;
        else if ("FUTURE".equals(c.getStatus())) futureCount++;
        else if ("PAST".equals(c.getStatus()))   pastCount++;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Ver Campañas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/campaigns.css">
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
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Campañas de recogida</h1>
        <p>Consulta el estado de todas las campañas de Bancosol.</p>
    </div>

    <div class="chips-bar">
        <button class="chip chip-selected chip-status-all" data-status="">Todas</button>
        <button class="chip chip-status-active"  data-status="ACTIVE">Activas: <%= activeCount %></button>
        <button class="chip chip-status-future"  data-status="FUTURE">Futuras: <%= futureCount %></button>
        <button class="chip chip-status-past"    data-status="PAST">Pasadas: <%= pastCount %></button>
    </div>

    <div class="filter-bar">
        <label for="sort-select">Ordenar por:</label>
        <select id="sort-select">
            <option value="startDate,desc">Inicio (más reciente)</option>
            <option value="startDate,asc">Inicio (más antiguo)</option>
            <option value="name,asc">Nombre (A–Z)</option>
            <option value="name,desc">Nombre (Z–A)</option>
        </select>
    </div>

    <div class="card">
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Tipo</th>
                        <th>Inicio</th>
                        <th>Fin</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody id="campaigns-tbody">
                <%
                    for (CampaignDTO c : campaigns) {
                        String status = c.getStatus();
                        String label = "ACTIVE".equals(status) ? "Activa"
                                     : "FUTURE".equals(status) ? "Futura"
                                     : "PAST".equals(status)   ? "Pasada" : "—";
                        String badge = "ACTIVE".equals(status) ? "badge-active"
                                     : "FUTURE".equals(status) ? "badge-future"
                                     : "badge-past";
                        String startIso = c.getStartDate() != null ? c.getStartDate().toString() : "";
                        String startTxt = c.getStartDate() != null ? c.getStartDate().format(dateFmt) : "—";
                        String endTxt   = c.getEndDate()   != null ? c.getEndDate().format(dateFmt)   : "—";
                        String name     = c.getName() != null ? c.getName() : "—";
                        String typeName = c.getTypeName() != null && !c.getTypeName().isEmpty() ? c.getTypeName() : "—";
                %>
                    <tr data-status="<%= status %>" data-name="<%= name.toLowerCase() %>" data-start="<%= startIso %>">
                        <td><strong><%= name %></strong></td>
                        <td><%= typeName %></td>
                        <td><%= startTxt %></td>
                        <td><%= endTxt %></td>
                        <td><span class="<%= badge %>"><%= label %></span></td>
                    </tr>
                <%  } %>
                    <tr id="empty-row" style="display:none">
                        <td colspan="5" class="table-empty">No hay campañas que mostrar.</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
    // El JS solo filtra y ordena las filas ya pintadas, sin fetch
    const tbody = document.getElementById('campaigns-tbody');
    const emptyRow = document.getElementById('empty-row');
    const rows = Array.from(tbody.querySelectorAll('tr[data-status]'));
    let currentStatus = '';

    function applyFilter() {
        let visible = 0;
        rows.forEach(row => {
            const show = !currentStatus || row.dataset.status === currentStatus;
            row.style.display = show ? '' : 'none';
            if (show) visible++;
        });
        emptyRow.style.display = visible === 0 ? '' : 'none';
    }

    document.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('.chip').forEach(c => c.classList.remove('chip-selected'));
            chip.classList.add('chip-selected');
            currentStatus = chip.dataset.status || '';
            applyFilter();
        });
    });

    document.getElementById('sort-select').addEventListener('change', event => {
        const value = event.target.value;
        const sorted = rows.slice().sort((a, b) => {
            switch (value) {
                case 'name,asc':  return a.dataset.name.localeCompare(b.dataset.name);
                case 'name,desc': return b.dataset.name.localeCompare(a.dataset.name);
                case 'startDate,asc': return a.dataset.start.localeCompare(b.dataset.start);
                default:          return b.dataset.start.localeCompare(a.dataset.start);
            }
        });
        sorted.forEach(row => tbody.insertBefore(row, emptyRow));
    });

    // si no hay campañas, que salga el mensaje de tabla vacia
    applyFilter();
</script>
</body>
</html>
