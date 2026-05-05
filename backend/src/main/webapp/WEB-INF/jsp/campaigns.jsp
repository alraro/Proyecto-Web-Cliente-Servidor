<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Campañas</title>
    <link rel="stylesheet" href="/css/administrador.css">
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

<main class="admin-page">
    <section class="page-header">
        <a href="/admin" class="back-link">&larr; Volver al menú</a>
        <h1>Campañas de recogida</h1>
        <p class="page-subtitle">Consulta el estado de todas las campañas de Bancosol.</p>
    </section>

    <div class="chips-bar" id="chips-bar">
        <button class="chip chip-all chip-selected" data-status="" id="chip-all">Todas</button>
        <button class="chip chip-active" data-status="ACTIVE" id="chip-active">Activas: —</button>
        <button class="chip chip-future" data-status="FUTURE" id="chip-future">Futuras: —</button>
        <button class="chip chip-past" data-status="PAST" id="chip-past">Pasadas: —</button>
    </div>

    <div class="filter-bar">
        <label for="sort-select">Ordenar por:</label>
        <select id="sort-select">
            <option value="startDate,desc">Inicio (más reciente)</option>
            <option value="startDate,asc">Inicio (más antiguo)</option>
            <option value="name,asc">Nombre (A-Z)</option>
            <option value="name,desc">Nombre (Z-A)</option>
        </select>
    </div>

    <section class="card">
        <table id="campaigns-table">
            <thead>
            <tr>
                <th>Nombre</th>
                <th>Tipo</th>
                <th>Inicio</th>
                <th>Fin</th>
                <th>Estado</th>
            </tr>
            </thead>
            <tbody id="campaigns-tbody"></tbody>
        </table>
    </section>

    <div class="pagination" id="pagination">
        <button id="btn-prev" disabled>&larr; Anterior</button>
        <span id="page-info"></span>
        <button id="btn-next" disabled>Siguiente &rarr;</button>
    </div>
</main>

<script>
    const STATUS_CONFIG = {
        ACTIVE: { label: "Activa", cssClass: "badge-active" },
        FUTURE: { label: "Futura", cssClass: "badge-future" },
        PAST: { label: "Pasada", cssClass: "badge-past" }
    };

    let currentPage = 0;
    let currentStatus = "";
    let currentSort = "startDate,desc";
    const PAGE_SIZE = 10;

    document.addEventListener("DOMContentLoaded", () => {
        const token = localStorage.getItem("token");
        if (!token) {
            window.location.href = "/login";
            return;
        }

        document.getElementById("user-name").textContent = localStorage.getItem("nombre") || "Admin";

        document.getElementById("btn-logout").addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "/login";
        });

        document.querySelectorAll(".chip").forEach((chip) => {
            chip.addEventListener("click", () => {
                currentStatus = chip.dataset.status;
                currentPage = 0;
                updateChipSelection();
                loadCampaigns();
            });
        });

        document.getElementById("sort-select").addEventListener("change", (event) => {
            currentSort = event.target.value;
            currentPage = 0;
            loadCampaigns();
        });

        document.getElementById("btn-prev").addEventListener("click", () => {
            const btnPrev = document.getElementById("btn-prev");
            if (!btnPrev.disabled) {
                currentPage -= 1;
                loadCampaigns();
            }
        });

        document.getElementById("btn-next").addEventListener("click", () => {
            const btnNext = document.getElementById("btn-next");
            if (!btnNext.disabled) {
                currentPage += 1;
                loadCampaigns();
            }
        });

        loadCampaigns();
    });

    async function loadCampaigns() {
        const token = localStorage.getItem("token");
        const tbody = document.getElementById("campaigns-tbody");

        try {
            let url = "/api/campaigns?page=" + currentPage + "&size=" + PAGE_SIZE + "&sort=" + encodeURIComponent(currentSort);
            if (currentStatus) {
                url += "&status=" + encodeURIComponent(currentStatus);
            }

            const response = await fetch(url, {
                headers: {
                    Authorization: "Bearer " + token
                }
            });

            if (response.status === 401) {
                window.location.href = "/login";
                return;
            }

            if (!response.ok) {
                throw new Error("No se pudieron cargar las campañas.");
            }

            const data = await response.json();
            renderTable(data.content || []);
            renderPagination(data.pagination || { page: 0, totalPages: 0, totalElements: 0, isFirst: true, isLast: true });
            renderSummary(data.summary || { totalActive: 0, totalPast: 0, totalFuture: 0 });
        } catch (error) {
            tbody.innerHTML = "<tr><td colspan='5' class='table-empty'>Error al cargar campañas. Inténtalo de nuevo.</td></tr>";
        }
    }

    function renderTable(campaigns) {
        const tbody = document.getElementById("campaigns-tbody");

        if (!Array.isArray(campaigns) || campaigns.length === 0) {
            tbody.innerHTML = "<tr><td colspan='5' class='table-empty'>No hay campañas con los filtros seleccionados.</td></tr>";
            return;
        }

        tbody.innerHTML = campaigns.map((campaign) => {
            const safeStatus = STATUS_CONFIG[campaign.status] || STATUS_CONFIG.ACTIVE;
            return "<tr>"
                + "<td>" + (campaign.name || "—") + "</td>"
                + "<td>" + ((campaign.type && campaign.type.name) ? campaign.type.name : "—") + "</td>"
                + "<td>" + formatDate(campaign.startDate) + "</td>"
                + "<td>" + formatDate(campaign.endDate) + "</td>"
                + "<td><span class='badge " + safeStatus.cssClass + "'>" + safeStatus.label + "</span></td>"
                + "</tr>";
        }).join("");
    }

    function renderPagination(pagination) {
        const page = Number.isInteger(pagination.page) ? pagination.page : 0;
        const totalPages = Number.isInteger(pagination.totalPages) ? pagination.totalPages : 0;
        const totalElements = Number.isInteger(pagination.totalElements) ? pagination.totalElements : 0;

        document.getElementById("page-info").textContent = "Página " + (page + 1) + " de " + totalPages
            + " (" + totalElements + " resultados)";
        document.getElementById("btn-prev").disabled = Boolean(pagination.isFirst);
        document.getElementById("btn-next").disabled = Boolean(pagination.isLast);
    }

    function renderSummary(summary) {
        document.getElementById("chip-active").textContent = "Activas: " + (summary.totalActive ?? 0);
        document.getElementById("chip-future").textContent = "Futuras: " + (summary.totalFuture ?? 0);
        document.getElementById("chip-past").textContent = "Pasadas: " + (summary.totalPast ?? 0);
    }

    function updateChipSelection() {
        document.querySelectorAll(".chip").forEach((chip) => {
            chip.classList.remove("chip-selected");
        });

        const selected = Array.from(document.querySelectorAll(".chip"))
            .find((chip) => chip.dataset.status === currentStatus);
        if (selected) {
            selected.classList.add("chip-selected");
        }
    }

    function formatDate(isoString) {
        if (!isoString) {
            return "—";
        }
        const parts = isoString.split("-");
        if (parts.length !== 3) {
            return isoString;
        }
        return parts[2] + "/" + parts[1] + "/" + parts[0];
    }
</script>
</body>
</html>
