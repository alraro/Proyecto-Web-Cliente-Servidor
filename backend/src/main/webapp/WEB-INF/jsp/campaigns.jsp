<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");

    if (token == null || role == null || !"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
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
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/index" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name">Admin</span>
        <a class="btn" href="/edit">Editar perfil</a>
        <button type="button" id="btn-logout" class="btn">Cerrar sesión</button>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Campañas de recogida</h1>
        <p>Consulta el estado de todas las campañas de Bancosol.</p>
    </div>

    <div class="chips-bar">
        <button class="chip chip-selected chip-status-all" id="chip-all">Todas</button>
        <button class="chip chip-status-active"  id="chip-active">Activas: —</button>
        <button class="chip chip-status-future"  id="chip-future">Futuras: —</button>
        <button class="chip chip-status-past"    id="chip-past">Pasadas: —</button>
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
                    <tr><td colspan="5" class="table-empty">Cargando campañas...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="pagination">
        <button id="btn-prev" disabled>← Anterior</button>
        <span id="page-info"></span>
        <button id="btn-next" disabled>Siguiente →</button>
    </div>
</main>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const token = '<%= token %>';
        const nombre = '<%= nombre == null ? "Admin" : nombre %>';

        const userNameEl = document.getElementById('user-name');
        userNameEl.textContent = nombre || 'Admin';

        document.getElementById('btn-logout').addEventListener('click', () => {
            window.location.href = '/logout';
        });

        const STATUS = {
            ACTIVE: { label: 'Activa',  css: 'badge-active' },
            FUTURE: { label: 'Futura',  css: 'badge-future' },
            PAST:   { label: 'Pasada',  css: 'badge-past'   }
        };

        let currentPage   = 0;
        let currentStatus = '';
        let currentSort   = 'startDate,desc';
        const PAGE_SIZE   = 10;

        function formatDate(iso) {
            if (!iso) return '—';
            const p = String(iso).split('-');
            return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] : String(iso);
        }

        async function loadCampaigns() {
            const tbody = document.querySelector('#campaigns-tbody');
            try {
                let url = '/api/campaigns?page=' + currentPage + '&size=' + PAGE_SIZE
                          + '&sort=' + encodeURIComponent(currentSort);
                if (currentStatus) url += '&status=' + encodeURIComponent(currentStatus);

                const res = await fetch(url, { headers: { Authorization: 'Bearer ' + token } });
                if (res.status === 401) { window.location.href = '/login'; return; }
                if (!res.ok) throw new Error('No se pudieron cargar las campañas.');

                const data = await res.json();
                renderTable(data.content || []);
                renderPagination(data.pagination || { page:0, totalPages:0, totalElements:0, isFirst:true, isLast:true });
                renderSummary(data.summary   || { totalActive:0, totalFuture:0, totalPast:0 });
            } catch {
                tbody.replaceChildren();
                const tr = document.createElement('tr');
                const td = document.createElement('td');
                td.setAttribute('colspan', '5');
                td.className = 'table-empty';
                td.textContent = 'Error al cargar campañas. Inténtalo de nuevo.';
                tr.appendChild(td);
                tbody.appendChild(tr);
            }
        }

        function renderTable(campaigns) {
            const tbody = document.querySelector('#campaigns-tbody');
            if (!campaigns.length) {
                tbody.replaceChildren();
                const tr = document.createElement('tr');
                const td = document.createElement('td');
                td.setAttribute('colspan', '5');
                td.className = 'table-empty';
                td.textContent = 'No hay campañas con los filtros seleccionados.';
                tr.appendChild(td);
                tbody.appendChild(tr);
                return;
            }
            tbody.replaceChildren();
            campaigns.forEach(c => {
                const s = STATUS[c.status] || STATUS.ACTIVE;
                const tr = document.createElement('tr');

                const tdName = document.createElement('td');
                const strong = document.createElement('strong');
                strong.textContent = c.name || '—';
                tdName.appendChild(strong);
                tr.appendChild(tdName);

                const tdType = document.createElement('td');
                tdType.textContent = (c.type && c.type.name) ? c.type.name : '—';
                tr.appendChild(tdType);

                const tdStart = document.createElement('td');
                tdStart.textContent = formatDate(c.startDate);
                tr.appendChild(tdStart);

                const tdEnd = document.createElement('td');
                tdEnd.textContent = formatDate(c.endDate);
                tr.appendChild(tdEnd);

                const tdStatus = document.createElement('td');
                const span = document.createElement('span');
                span.className = s.css;
                span.textContent = s.label;
                tdStatus.appendChild(span);
                tr.appendChild(tdStatus);

                tbody.appendChild(tr);
            });
        }

        function renderPagination(p) {
            const page  = p.page        || 0;
            const total = p.totalPages  || 0;
            const count = p.totalElements || 0;
            document.querySelector('#page-info').textContent =
                'Página ' + (page + 1) + ' de ' + total + ' (' + count + ' resultados)';
            document.querySelector('#btn-prev').disabled = Boolean(p.isFirst);
            document.querySelector('#btn-next').disabled = Boolean(p.isLast);
        }

        function renderSummary(s) {
            document.querySelector('#chip-active').textContent = 'Activas: '  + (s.totalActive  ?? 0);
            document.querySelector('#chip-future').textContent = 'Futuras: '  + (s.totalFuture  ?? 0);
            document.querySelector('#chip-past').textContent   = 'Pasadas: '  + (s.totalPast    ?? 0);
        }

        function updateChips() {
            document.querySelectorAll('.chip').forEach(c => c.classList.remove('chip-selected'));
            const sel = [...document.querySelectorAll('.chip')].find(c => c.dataset.status === currentStatus);
            if (sel) sel.classList.add('chip-selected');
        }

        document.querySelectorAll('.chip').forEach(chip => {
            chip.addEventListener('click', () => {
                if (chip.classList.contains('chip-status-all')) {
                    currentStatus = '';
                } else if (chip.classList.contains('chip-status-active')) {
                    currentStatus = 'ACTIVE';
                } else if (chip.classList.contains('chip-status-future')) {
                    currentStatus = 'FUTURE';
                } else if (chip.classList.contains('chip-status-past')) {
                    currentStatus = 'PAST';
                }
                currentPage   = 0;
                updateChips();
                loadCampaigns();
            });
        });

        document.querySelector('#sort-select').addEventListener('change', e => {
            currentSort = e.target.value;
            currentPage = 0;
            loadCampaigns();
        });

        document.querySelector('#btn-prev').addEventListener('click', () => { currentPage--; loadCampaigns(); });
        document.querySelector('#btn-next').addEventListener('click', () => { currentPage++; loadCampaigns(); });

        loadCampaigns();
    });
</script>
</body>
</html>
