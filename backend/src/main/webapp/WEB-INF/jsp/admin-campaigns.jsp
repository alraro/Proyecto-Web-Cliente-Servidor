<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Campañas</title>
    <link rel="stylesheet" href="/css/administrador.css">
    <link rel="stylesheet" href="/css/admin-campaigns.css">
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

<main class="admin-page" aria-label="Campaign management page">
    <section class="page-header">
        <a href="/admin" class="back-link">&larr; Volver al menú</a>
        <div class="page-header-row">
            <div>
                <h1>Gestión de Campañas</h1>
                <p>Crea, edita y elimina campañas de recogida de alimentos.</p>
            </div>
            <button type="button" id="btn-new" class="btn btn-primary">Nueva campaña</button>
        </div>
    </section>

    <div id="global-message" hidden></div>

    <section class="card" aria-label="Listado de campañas">
        <div class="campaigns-table-wrap">
            <table id="campaigns-table">
                <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Tipo</th>
                    <th>Fecha inicio</th>
                    <th>Fecha fin</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody id="campaigns-tbody"></tbody>
            </table>
        </div>
    </section>
</main>

<div id="campaign-modal" class="modal-overlay" aria-hidden="true">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h2 id="modal-title">Nueva campaña</h2>

        <div class="form-grid">
            <div class="form-group full-width">
                <label for="campaign-name">Nombre</label>
                <input id="campaign-name" type="text" required maxlength="100" placeholder="Nombre de la campaña">
            </div>

            <div class="form-group full-width">
                <label for="campaign-type">Tipo</label>
                <select id="campaign-type"></select>
            </div>

            <div class="form-group">
                <label for="campaign-start">Fecha de inicio</label>
                <input id="campaign-start" type="date">
            </div>

            <div class="form-group">
                <label for="campaign-end">Fecha de fin</label>
                <input id="campaign-end" type="date">
            </div>
        </div>

        <div id="modal-error" hidden></div>

        <div class="modal-actions">
            <button type="button" id="btn-cancel-modal" class="btn btn-secondary">Cancelar</button>
            <button type="button" id="btn-save" class="btn btn-primary">Guardar</button>
        </div>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", async () => {
        const params = new URLSearchParams(window.location.search);
        const tokenFromQuery = params.get("token");
        const nameFromQuery = params.get("nombre");
        if (tokenFromQuery) { localStorage.setItem("token", tokenFromQuery); }
        if (nameFromQuery) { localStorage.setItem("nombre", nameFromQuery); }

        const token = localStorage.getItem("token");
        if (!token) {
            window.location.href = "/login";
            return;
        }

        const userNameEl = document.getElementById("user-name");
        userNameEl.textContent = localStorage.getItem("nombre") || "Admin";

        const btnLogout = document.getElementById("btn-logout");
        const btnNew = document.getElementById("btn-new");
        const campaignsTbody = document.getElementById("campaigns-tbody");
        const globalMessage = document.getElementById("global-message");

        const modalEl = document.getElementById("campaign-modal");
        const modalTitle = document.getElementById("modal-title");
        const modalError = document.getElementById("modal-error");
        const btnCancelModal = document.getElementById("btn-cancel-modal");
        const btnSave = document.getElementById("btn-save");

        const nameInput = document.getElementById("campaign-name");
        const typeSelect = document.getElementById("campaign-type");
        const startInput = document.getElementById("campaign-start");
        const endInput = document.getElementById("campaign-end");

        let currentCampaignId = null;
        let currentCampaigns = [];
        let cachedTypes = [];

        btnLogout.addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "/login";
        });

        btnNew.addEventListener("click", openCreateModal);
        btnCancelModal.addEventListener("click", hideModal);
        btnSave.addEventListener("click", saveCampaign);

        modalEl.addEventListener("click", (event) => {
            if (event.target === modalEl) {
                hideModal();
            }
        });

        window.openEditModal = openEditModal;
        window.deleteCampaign = deleteCampaign;

        try {
            await loadCampaignTypes();
            await loadCampaigns();
        } catch (error) {
            showMessage(error.message || "No se pudieron cargar los datos de campañas.", true);
        }

        async function loadCampaignTypes() {
            cachedTypes = await fetchJson("/api/campaign-types", {
                method: "GET",
                headers: authHeaders(token)
            });

            typeSelect.innerHTML = "<option value=''>Selecciona un tipo...</option>";
            (cachedTypes || []).forEach((type) => {
                const option = document.createElement("option");
                option.value = String(type.id);
                option.textContent = type.name || "Sin nombre";
                typeSelect.appendChild(option);
            });
        }

        async function loadCampaigns() {
            currentCampaigns = await fetchJson("/api/campaigns", {
                method: "GET",
                headers: authHeaders(token)
            });
            renderCampaignsTable(currentCampaigns || []);
        }

        function renderCampaignsTable(campaigns) {
            campaignsTbody.innerHTML = "";

            if (!campaigns.length) {
                const row = document.createElement("tr");
                row.innerHTML = "<td colspan='5'>No hay campañas registradas.</td>";
                campaignsTbody.appendChild(row);
                return;
            }

            campaigns.forEach((campaign) => {
                const row = document.createElement("tr");
                const campaignId = Number(campaign.id);
                const campaignName = escapeHtml(campaign.name || "");
                const campaignNameForJs = escapeJsString(campaign.name || "");
                const typeName = escapeHtml((campaign.type && campaign.type.name) ? campaign.type.name : "-");

                row.innerHTML = `
                    <td>${campaignName}</td>
                    <td>${typeName}</td>
                    <td>${formatDate(campaign.startDate)}</td>
                    <td>${formatDate(campaign.endDate)}</td>
                    <td class="actions-cell">
                        <button class="btn btn-sm btn-secondary" onclick="openEditModal(${campaignId})">Editar</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteCampaign(${campaignId}, '${campaignNameForJs}')">Eliminar</button>
                    </td>
                `;
                campaignsTbody.appendChild(row);
            });
        }

        function openCreateModal() {
            currentCampaignId = null;
            modalTitle.textContent = "Nueva campaña";
            clearModalError();
            nameInput.value = "";
            typeSelect.value = "";
            startInput.value = "";
            endInput.value = "";
            showModal();
        }

        async function openEditModal(campaignId) {
            clearModalError();
            try {
                const campaign = await fetchJson(`/api/campaigns/${campaignId}`, {
                    method: "GET",
                    headers: authHeaders(token)
                });

                currentCampaignId = campaignId;
                modalTitle.textContent = "Editar campaña";
                nameInput.value = campaign.name || "";
                typeSelect.value = (campaign.type && campaign.type.id != null) ? String(campaign.type.id) : "";
                startInput.value = campaign.startDate || "";
                endInput.value = campaign.endDate || "";
                showModal();
            } catch (error) {
                showMessage(error.message || "No se pudieron cargar los datos de la campaña.", true);
            }
        }

        async function saveCampaign() {
            const name = (nameInput.value || "").trim();
            const typeIdRaw = (typeSelect.value || "").trim();
            const startDate = (startInput.value || "").trim();
            const endDate = (endInput.value || "").trim();

            clearModalError();

            if (!name) {
                return showModalError("El nombre de la campaña es obligatorio.");
            }
            if (!typeIdRaw) {
                return showModalError("El tipo de campaña es obligatorio.");
            }
            if (!startDate) {
                return showModalError("La fecha de inicio es obligatoria.");
            }
            if (!endDate) {
                return showModalError("La fecha de fin es obligatoria.");
            }
            if (endDate <= startDate) {
                return showModalError("La fecha de fin debe ser posterior a la fecha de inicio.");
            }

            const body = {
                name,
                typeId: parseInt(typeIdRaw, 10),
                startDate,
                endDate
            };

            try {
                if (currentCampaignId == null) {
                    await fetchJson("/api/campaigns", {
                        method: "POST",
                        headers: authHeaders(token),
                        body: JSON.stringify(body)
                    });
                    hideModal();
                    showMessage("Campaña creada correctamente.", false);
                } else {
                    await fetchJson(`/api/campaigns/${currentCampaignId}`, {
                        method: "PUT",
                        headers: authHeaders(token),
                        body: JSON.stringify(body)
                    });
                    hideModal();
                    showMessage("Campaña actualizada correctamente.", false);
                }
                await loadCampaigns();
            } catch (error) {
                const message = error && error.message ? error.message : "No se pudo guardar la campaña.";
                if (message.includes("already exists")) {
                    showModalError("Ya existe una campaña con ese nombre.");
                    return;
                }
                showModalError(message);
            }
        }

        async function deleteCampaign(id, name) {
            const confirmText = `¿Seguro que quieres eliminar la campaña «${name}»?\nSe eliminarán también todas sus asignaciones.`;
            if (!window.confirm(confirmText)) {
                return;
            }

            try {
                await fetchJson(`/api/campaigns/${id}`, {
                    method: "DELETE",
                    headers: authHeaders(token)
                });
                showMessage("Campaña eliminada correctamente.", false);
                await loadCampaigns();
            } catch (error) {
                showMessage(error.message || "No se pudo eliminar la campaña.", true);
            }
        }

        function showModal() {
            modalEl.classList.add("open");
            modalEl.setAttribute("aria-hidden", "false");
        }

        function hideModal() {
            modalEl.classList.remove("open");
            modalEl.setAttribute("aria-hidden", "true");
            clearModalError();
        }

        function showModalError(text) {
            modalError.hidden = false;
            modalError.textContent = text;
        }

        function clearModalError() {
            modalError.hidden = true;
            modalError.textContent = "";
        }

        function authHeaders(jwtToken) {
            return {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${jwtToken}`
            };
        }

        async function fetchJson(url, options) {
            try {
                const response = await fetch(url, options);
                const data = await response.json().catch(() => ({}));

                if (response.status === 401 || response.status === 403) {
                    throw new Error("Tu sesión no es válida o ha expirado.");
                }

                if (!response.ok) {
                    throw new Error(data.message || `Error ${response.status}`);
                }
                return data;
            } catch (error) {
                if (error instanceof Error) {
                    throw error;
                }
                throw new Error("Error inesperado de red.");
            }
        }

        function showMessage(text, isError) {
            globalMessage.hidden = false;
            globalMessage.textContent = text;
            globalMessage.classList.remove("success", "error");
            globalMessage.classList.add(isError ? "error" : "success");

            window.clearTimeout(showMessage.hideTimer);
            showMessage.hideTimer = window.setTimeout(() => {
                globalMessage.hidden = true;
            }, 4000);
        }

        function formatDate(isoString) {
            if (!isoString) {
                return "-";
            }
            const parts = String(isoString).split("-");
            if (parts.length !== 3) {
                return String(isoString);
            }
            return `${parts[2]}/${parts[1]}/${parts[0]}`;
        }

        function escapeHtml(value) {
            return String(value)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/\"/g, "&quot;")
                .replace(/'/g, "&#39;");
        }

        function escapeJsString(value) {
            return String(value)
                .replace(/\\/g, "\\\\")
                .replace(/'/g, "\\'")
                .replace(/\r/g, " ")
                .replace(/\n/g, " ");
        }
    });
</script>
</body>
</html>
