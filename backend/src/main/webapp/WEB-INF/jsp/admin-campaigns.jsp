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

        <!-- NEW: Store selector section -->
        <div class="store-selector-section">
            <h3 class="store-selector-title">Tiendas participantes</h3>

            <div class="store-filter-row">
                <select id="store-filter-chain">
                    <option value="">Todas las cadenas</option>
                </select>
                <select id="store-filter-zone">
                    <option value="">Todas las zonas</option>
                </select>
                <select id="store-filter-locality">
                    <option value="">Todas las localidades</option>
                </select>
                <button type="button" id="btn-store-filter" class="btn btn-secondary btn-sm">Filtrar</button>
                <button type="button" id="btn-store-clear" class="btn btn-secondary btn-sm">Limpiar</button>
            </div>

            <div class="store-dual-panel">
                <div class="store-panel">
                    <h4>Disponibles <span id="available-count" class="badge">0</span></h4>
                    <ul id="available-stores" class="store-list"></ul>
                </div>
                <div class="store-panel">
                    <h4>Seleccionadas <span id="selected-count" class="badge">0</span></h4>
                    <ul id="selected-stores" class="store-list"></ul>
                </div>
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

        // NEW: auth options for calls that only require Authorization header
        const authOpts = { headers: { Authorization: "Bearer " + token } };

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

        // NEW: store selector elements
        const storeFilterChain = document.getElementById("store-filter-chain");
        const storeFilterZone = document.getElementById("store-filter-zone");
        const storeFilterLocality = document.getElementById("store-filter-locality");
        const btnStoreFilter = document.getElementById("btn-store-filter");
        const btnStoreClear = document.getElementById("btn-store-clear");
        const availableStoresEl = document.getElementById("available-stores");
        const selectedStoresEl = document.getElementById("selected-stores");
        const availableCountEl = document.getElementById("available-count");
        const selectedCountEl = document.getElementById("selected-count");

        let currentCampaignId = null;
        // NEW
        let selectedStores = new Map(); // Map<storeId(Number), storeObject>
        let allFilteredStores = []; // result of last GET /api/stores call
        let currentCampaigns = [];
        let cachedTypes = [];

        btnLogout.addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "/login";
        });

        btnNew.addEventListener("click", openCreateModal);
        btnCancelModal.addEventListener("click", hideModal);
        btnSave.addEventListener("click", saveCampaign);

        // NEW
        btnStoreFilter.addEventListener("click", loadAvailableStores);
        btnStoreClear.addEventListener("click", () => {
            storeFilterChain.value = "";
            storeFilterZone.value = "";
            storeFilterLocality.value = "";
            loadAvailableStores();
        });

        availableStoresEl.addEventListener("click", e => {
            const btn = e.target.closest(".btn-add-store");
            if (!btn) return;
            const li = btn.closest("li");
            const storeId = Number(li.dataset.storeid);
            const store = allFilteredStores.find(s => Number(s.id) === storeId);
            if (store) {
                selectedStores.set(storeId, store);
            }
            renderAvailableList();
            renderSelectedList();
        });

        selectedStoresEl.addEventListener("click", e => {
            const btn = e.target.closest(".btn-remove-store");
            if (!btn) return;
            const li = btn.closest("li");
            selectedStores.delete(Number(li.dataset.storeid));
            renderAvailableList();
            renderSelectedList();
        });

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
            // NEW
            await loadStoreFilters();
        } catch (error) {
            showMessage(error.message || "No se pudieron cargar los datos de campañas.", true);
        }

        // NEW
        async function loadStoreFilters() {
            const [chains, zones, localities] = await Promise.all([
                fetchArray("/api/chains", authOpts),
                fetchArray("/api/zones", authOpts),
                fetchArray("/api/localities", authOpts)
            ]);

            storeFilterChain.innerHTML = '<option value="">Todas las cadenas</option>';
            chains.forEach((chain) => {
                const option = document.createElement("option");
                option.value = String(chain.id);
                option.textContent = chain.name || "Sin nombre";
                storeFilterChain.appendChild(option);
            });

            storeFilterZone.innerHTML = '<option value="">Todas las zonas</option>';
            zones.forEach((zone) => {
                const option = document.createElement("option");
                option.value = String(zone.id);
                option.textContent = zone.name || "Sin nombre";
                storeFilterZone.appendChild(option);
            });

            storeFilterLocality.innerHTML = '<option value="">Todas las localidades</option>';
            localities.forEach((locality) => {
                const option = document.createElement("option");
                option.value = String(locality.id);
                option.textContent = locality.name || "Sin nombre";
                storeFilterLocality.appendChild(option);
            });
        }

        // NEW
        async function loadAvailableStores() {
            const chainId = (storeFilterChain.value || "").trim();
            const zoneId = (storeFilterZone.value || "").trim();
            const localityId = (storeFilterLocality.value || "").trim();

            const params = new URLSearchParams();
            if (chainId) params.append("chainId", chainId);
            if (zoneId) params.append("zoneId", zoneId);
            if (localityId) params.append("localityId", localityId);

            const url = params.toString() ? `/api/stores?${params.toString()}` : "/api/stores";
            allFilteredStores = await fetchArray(url, authOpts);
            renderAvailableList();
        }

        // NEW
        function renderAvailableList() {
            const availableStores = (allFilteredStores || []).filter((store) => !selectedStores.has(Number(store.id)));
            availableStoresEl.innerHTML = "";

            if (!availableStores.length) {
                availableStoresEl.innerHTML = '<li class="store-list-empty">Sin tiendas disponibles con estos filtros.</li>';
                availableCountEl.textContent = "0";
                return;
            }

            availableStores.forEach((store) => {
                const li = document.createElement("li");
                li.dataset.storeid = String(store.id);
                li.innerHTML = `${escapeHtml(store.name || "-")} — ${escapeHtml(store.chainName || "-")} — ${escapeHtml(store.locality || "-")} <button type="button" class="btn-add-store btn btn-sm btn-primary">+</button>`;
                availableStoresEl.appendChild(li);
            });

            availableCountEl.textContent = String(availableStores.length);
        }

        // NEW
        function renderSelectedList() {
            selectedStoresEl.innerHTML = "";

            if (!selectedStores.size) {
                selectedStoresEl.innerHTML = '<li class="store-list-empty">Sin tiendas seleccionadas.</li>';
                selectedCountEl.textContent = "0";
                return;
            }

            Array.from(selectedStores.values()).forEach((store) => {
                const li = document.createElement("li");
                li.dataset.storeid = String(store.id);
                li.innerHTML = `${escapeHtml(store.name || "-")} — ${escapeHtml(store.chainName || "-")} <button type="button" class="btn-remove-store btn btn-sm btn-danger">×</button>`;
                selectedStoresEl.appendChild(li);
            });

            selectedCountEl.textContent = String(selectedStores.size);
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

        // MODIFIED
        async function openCreateModal() {
            currentCampaignId = null;
            modalTitle.textContent = "Nueva campaña";
            clearModalError();
            nameInput.value = "";
            typeSelect.value = "";
            startInput.value = "";
            endInput.value = "";
            showModal();
            selectedStores = new Map();
            await loadAvailableStores();
            renderSelectedList();
        }

        // MODIFIED
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

                selectedStores = new Map();
                try {
                    const storeData = await fetchJson("/api/campaigns/" + campaignId + "/stores", authOpts);
                    if (storeData && storeData.stores) {
                        storeData.stores.forEach(s => selectedStores.set(Number(s.id), s));
                    }
                } catch (storeError) {
                    console.error("Error loading campaign stores", storeError);
                }
                await loadAvailableStores();
                renderSelectedList();
            } catch (error) {
                showMessage(error.message || "No se pudieron cargar los datos de la campaña.", true);
            }
        }

        async function fetchArray(url, options) {
            try {
                const response = await fetch(url, options);
                if (!response.ok) {
                    return [];
                }
                const data = await response.json().catch(() => []);
                if (Array.isArray(data)) {
                    return data;
                }
                if (data && Array.isArray(data.value)) {
                    return data.value;
                }
                return [];
            } catch (error) {
                return [];
            }
        }

        // MODIFIED
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
                let campaignResponse;
                let campaignIdToSync;

                if (currentCampaignId == null) {
                    campaignResponse = await fetchJson("/api/campaigns", {
                        method: "POST",
                        headers: authHeaders(token),
                        body: JSON.stringify(body)
                    });
                    campaignIdToSync = campaignResponse && campaignResponse.campaign ? Number(campaignResponse.campaign.id) : null;
                } else {
                    campaignResponse = await fetchJson(`/api/campaigns/${currentCampaignId}`, {
                        method: "PUT",
                        headers: authHeaders(token),
                        body: JSON.stringify(body)
                    });
                    campaignIdToSync = currentCampaignId;
                }

                let syncStoresFailed = false;
                if (campaignIdToSync != null) {
                    try {
                        await fetchJson(`/api/campaigns/${campaignIdToSync}/stores`, {
                            method: "PUT",
                            headers: {
                                Authorization: "Bearer " + token,
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({ storeIds: [...selectedStores.keys()] })
                        });
                    } catch (syncError) {
                        syncStoresFailed = true;
                    }
                }

                hideModal();
                if (syncStoresFailed) {
                    showMessage("La campaña se guardó pero hubo un error al sincronizar las tiendas.", true);
                } else if (currentCampaignId == null) {
                    showMessage("Campaña creada correctamente.", false);
                } else {
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
