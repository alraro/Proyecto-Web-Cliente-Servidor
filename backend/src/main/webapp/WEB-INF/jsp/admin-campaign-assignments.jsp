<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");
    if (token == null || !"ADMINISTRADOR".equals(role)) {
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
    <title>Bancosol | Asignación a Campañas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/assignment.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/admin" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name"><%= nombre != null ? nombre : "Admin" %></span>
        <a class="btn" href="/edit">Editar perfil</a>
        <a href="/logout" class="btn">Cerrar sesión</a>
    </div>
</header>

<main class="page-wrapper" aria-label="Campaign assignments page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al menú</a>
        <div class="page-header-row">
            <div>
                <h1>Asignación a Campañas</h1>
                <p>Asigna coordinadores y capitanes a cada campaña de recogida.</p>
            </div>
        </div>
    </div>

    <div class="card" aria-label="Campaign selector">
        <div class="card-body">
            <label for="campaign-select">Campaña</label>
            <div class="selector-row">
                <select id="campaign-select">
                    <option value="">Selecciona una campaña...</option>
                    <% if (campaigns != null) {
                        for (CampaignDTO camp : campaigns) { %>
                    <option value="<%= camp.getId() %>"><%= camp.getName() %> (<%= camp.getStartDate() %> - <%= camp.getEndDate() %>)</option>
                    <%  }
                       } %>
                </select>
                <button type="button" id="btn-load" class="btn btn-secondary">Cargar asignaciones</button>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="two-col-grid" aria-label="Current assignments">
        <div class="card">
            <div class="card-head">
                <h2>Coordinadores asignados</h2>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Acción</th>
                    </tr>
                    </thead>
                    <tbody id="coordinators-tbody"></tbody>
                </table>
            </div>
        </div>

        <div class="card">
            <div class="card-head">
                <h2>Capitanes asignados</h2>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Acción</th>
                    </tr>
                    </thead>
                    <tbody id="captains-tbody"></tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="two-col-grid" aria-label="Add assignments">
        <div class="card">
            <div class="card-head">
                <h2>Añadir coordinador</h2>
            </div>
            <div class="card-body">
                <div class="selector-row">
                    <select id="coordinator-select" disabled>
                        <option value="">Selecciona un coordinador...</option>
                    </select>
                    <button type="button" id="btn-assign-coordinator" class="btn btn-primary" disabled>Asignar</button>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-head">
                <h2>Añadir capitán</h2>
            </div>
            <div class="card-body">
                <div class="selector-row">
                    <select id="captain-select" disabled>
                        <option value="">Selecciona un capitán...</option>
                    </select>
                    <button type="button" id="btn-assign-captain" class="btn btn-primary" disabled>Asignar</button>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
    document.addEventListener("DOMContentLoaded", async () => {
        const token = '<%= token %>';

        const campaignSelect = document.getElementById("campaign-select");
        const btnLoad = document.getElementById("btn-load");
        const coordinatorsTbody = document.getElementById("coordinators-tbody");
        const captainsTbody = document.getElementById("captains-tbody");
        const coordinatorSelect = document.getElementById("coordinator-select");
        const captainSelect = document.getElementById("captain-select");
        const btnAssignCoordinator = document.getElementById("btn-assign-coordinator");
        const btnAssignCaptain = document.getElementById("btn-assign-captain");

        setAssignmentControls(false);

        btnLoad.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            if (!campaignId) { showMessage("Selecciona una campaña", true); return; }
            try {
                await loadCampaignData(campaignId);
                setAssignmentControls(true);
                showMessage("Asignaciones cargadas", false);
            } catch (error) {
                setAssignmentControls(false);
                showMessage(error.message || "No se pudieron cargar las asignaciones", true);
            }
        });

        btnAssignCoordinator.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            const userId = coordinatorSelect.value;
            if (!campaignId) { showMessage("Selecciona una campaña", true); return; }
            if (!userId) { showMessage("Selecciona un coordinador", true); return; }
            try {
                await fetchJson(`/api/campaigns/${campaignId}/coordinators`, {
                    method: "POST", headers: authHeaders(token),
                    body: JSON.stringify({ userId: Number(userId) })
                });
                showMessage("Coordinador asignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo asignar el coordinador", true);
            }
        });

        btnAssignCaptain.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            const userId = captainSelect.value;
            if (!campaignId) { showMessage("Selecciona una campaña", true); return; }
            if (!userId) { showMessage("Selecciona un capitán", true); return; }
            try {
                await fetchJson(`/api/campaigns/${campaignId}/captains`, {
                    method: "POST", headers: authHeaders(token),
                    body: JSON.stringify({ userId: Number(userId) })
                });
                showMessage("Capitán asignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo asignar el capitán", true);
            }
        });

        coordinatorsTbody.addEventListener("click", async (event) => {
            const button = event.target.closest("button[data-role='COORDINATOR']");
            if (!button) return;
            const campaignId = campaignSelect.value;
            const userId = button.dataset.userid;
            if (!campaignId || !userId) { showMessage("Selección inválida", true); return; }
            try {
                await fetchJson(`/api/campaigns/${campaignId}/coordinators/${userId}`, {
                    method: "DELETE", headers: authHeaders(token)
                });
                showMessage("Coordinador desasignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo desasignar el coordinador", true);
            }
        });

        captainsTbody.addEventListener("click", async (event) => {
            const button = event.target.closest("button[data-role='CAPTAIN']");
            if (!button) return;
            const campaignId = campaignSelect.value;
            const userId = button.dataset.userid;
            if (!campaignId || !userId) { showMessage("Selección inválida", true); return; }
            try {
                await fetchJson(`/api/campaigns/${campaignId}/captains/${userId}`, {
                    method: "DELETE", headers: authHeaders(token)
                });
                showMessage("Capitán desasignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo desasignar el capitán", true);
            }
        });

        function authHeaders(jwtToken) {
            return { "Content-Type": "application/json", "Authorization": `Bearer ${jwtToken}` };
        }

        async function loadCampaignData(campaignId) {
            const [assignments, availableCoordinators, availableCaptains] = await Promise.all([
                fetchJson(`/api/campaigns/${campaignId}/assignments`, { method: "GET", headers: authHeaders(token) }),
                fetchJson(`/api/campaigns/${campaignId}/available-users?role=COORDINATOR`, { method: "GET", headers: authHeaders(token) }),
                fetchJson(`/api/campaigns/${campaignId}/available-users?role=CAPTAIN`, { method: "GET", headers: authHeaders(token) })
            ]);
            renderTable(coordinatorsTbody, assignments?.coordinators || [], "COORDINATOR");
            renderTable(captainsTbody, assignments?.captains || [], "CAPTAIN");
            populateUserSelect(coordinatorSelect, availableCoordinators, "Selecciona un coordinador...");
            populateUserSelect(captainSelect, availableCaptains, "Selecciona un capitán...");
        }

        async function fetchJson(url, options) {
            const response = await fetch(url, options);
            const data = await response.json().catch(() => ({}));
            if (!response.ok) throw new Error(data.message || `Error ${response.status}`);
            return data;
        }

        function renderTable(tbody, users, role) {
            tbody.innerHTML = "";
            if (!users.length) {
                tbody.innerHTML = "<tr><td colspan='3'>Sin asignaciones</td></tr>"; return;
            }
            users.forEach((user) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${escapeHtml(user.name || "")}</td>
                    <td>${escapeHtml(user.email || "")}</td>
                    <td><button type="button" data-userid="${user.userId}" data-role="${role}">Eliminar</button></td>`;
                tbody.appendChild(row);
            });
        }

        function populateUserSelect(selectEl, users, placeholder) {
            selectEl.innerHTML = `<option value=''>${placeholder}</option>`;
            (users || []).forEach((user) => {
                const option = document.createElement("option");
                option.value = String(user.userId);
                option.textContent = `${user.name} (${user.email})`;
                selectEl.appendChild(option);
            });
        }

        function setAssignmentControls(enabled) {
            coordinatorSelect.disabled = !enabled;
            captainSelect.disabled = !enabled;
            btnAssignCoordinator.disabled = !enabled;
            btnAssignCaptain.disabled = !enabled;
        }

        function showMessage(text, isError) {
            const el = document.getElementById("global-message");
            el.hidden = false;
            el.textContent = text;
            el.classList.remove("success", "error");
            el.classList.add(isError ? "error" : "success");
            window.clearTimeout(showMessage.hideTimer);
            showMessage.hideTimer = window.setTimeout(() => { el.hidden = true; }, 4000);
        }

        function escapeHtml(value) {
            return String(value).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
                .replace(/\"/g, "&quot;").replace(/'/g, "&#39;");
        }
    });
</script>
</body>
</html>
