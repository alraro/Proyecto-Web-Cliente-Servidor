<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%@ page import="java.util.List, es.grupo8.backend.dto.CampaignDTO" %>
<%
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");
    String nombre = (String) session.getAttribute("nombre");
    if (!"ADMINISTRADOR".equals(role)) {
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
    <title>Bancosol | Gestión de Coordinadores</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/admin" aria-label="Bancosol home">
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

<main class="page-wrapper" aria-label="Coordinators management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">&larr; Volver al menú</a>
        <div class="page-header-row">
            <div>
                <h1>Gestión de Coordinadores</h1>
                <p>Asigna coordinadores a campañas de recogida.</p>
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
                <button type="button" id="btn-load" class="btn btn-secondary">Cargar coordinadores</button>
            </div>
        </div>
    </div>

    <div id="global-message" hidden></div>

    <div class="card" aria-label="Current coordinators">
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

    <div class="card" aria-label="Add coordinator">
        <div class="card-head">
            <h2>Añadir coordinador</h2>
        </div>
        <div class="card-body">
            <div class="selector-row">
                <select id="coordinator-select" disabled>
                    <option value="">Selecciona un coordinador...</option>
                </select>
                <button type="button" id="btn-assign" class="btn btn-primary" disabled>Asignar</button>
            </div>
        </div>
    </div>
</main>

<script>
    document.addEventListener("DOMContentLoaded", async () => {
        const token = '<%= token %>';

        const campaignSelect = document.getElementById("campaign-select");
        const btnLoad = document.getElementById("btn-load");
        const globalMessage = document.getElementById("global-message");
        const coordinatorsTbody = document.getElementById("coordinators-tbody");
        const coordinatorSelect = document.getElementById("coordinator-select");
        const btnAssign = document.getElementById("btn-assign");

        btnLoad.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            if (!campaignId) { showMessage("Selecciona una campaña", true); return; }
            try {
                await loadCampaignData(campaignId);
                coordinatorSelect.disabled = false;
                btnAssign.disabled = false;
            } catch (error) {
                coordinatorSelect.disabled = true;
                btnAssign.disabled = true;
                showMessage(error.message || "No se pudieron cargar los coordinadores", true);
            }
        });

        btnAssign.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            const userId = coordinatorSelect.value;
            if (!campaignId) { showMessage("Selecciona una campaña", true); return; }
            if (!userId) { showMessage("Selecciona un coordinador", true); return; }
            try {
                await fetchJson(`/api/campaigns/${campaignId}/coordinators`, {
                    method: "POST",
                    headers: authHeaders(token),
                    body: JSON.stringify({ userId: Number(userId) })
                });
                showMessage("Coordinador asignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo asignar el coordinador", true);
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
                    method: "DELETE",
                    headers: authHeaders(token)
                });
                showMessage("Coordinador desasignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo desasignar el coordinador", true);
            }
        });

        function authHeaders(jwtToken) {
            return { "Content-Type": "application/json", "Authorization": `Bearer ${jwtToken}` };
        }

        async function loadCampaignData(campaignId) {
            const [assignments, availableCoordinatorsData] = await Promise.all([
                fetchJson(`/api/campaigns/${campaignId}/assignments`, { method: "GET", headers: authHeaders(token) }),
                fetchJson(`/api/campaigns/${campaignId}/available-users?role=COORDINATOR`, { method: "GET", headers: authHeaders(token) })
            ]);
            const availableCoordinators = Array.isArray(availableCoordinatorsData)
                ? availableCoordinatorsData : (availableCoordinatorsData.content || []);
            renderCoordinatorsTable(assignments?.coordinators || []);
            populateSelect(coordinatorSelect, availableCoordinators, "Selecciona un coordinador...");
        }

        async function fetchJson(url, options) {
            const response = await fetch(url, options);
            const data = await response.json().catch(() => ({}));
            if (response.status === 401 || response.status === 403) throw new Error("Tu sesión no es válida o ha expirado.");
            if (!response.ok) throw new Error(data.message || `Error ${response.status}`);
            return data;
        }

        function renderCoordinatorsTable(coordinators) {
            coordinatorsTbody.innerHTML = "";
            if (!coordinators.length) {
                coordinatorsTbody.innerHTML = "<tr><td colspan='3'>Sin coordinadores asignados</td></tr>"; return;
            }
            coordinators.forEach((coordinator) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${escapeHtml(coordinator.name || "")}</td>
                    <td>${escapeHtml(coordinator.email || "")}</td>
                    <td><button type="button" data-userid="${coordinator.idUser}" data-role="COORDINATOR">Eliminar</button></td>`;
                coordinatorsTbody.appendChild(row);
            });
        }

        function populateSelect(selectEl, users, placeholder) {
            selectEl.innerHTML = `<option value=''>${placeholder}</option>`;
            (users || []).forEach((user) => {
                const option = document.createElement("option");
                option.value = String(user.idUser);
                option.textContent = `${user.name} (${user.email})`;
                selectEl.appendChild(option);
            });
        }

        function showMessage(text, isError) {
            globalMessage.hidden = false;
            globalMessage.textContent = text;
            globalMessage.classList.remove("success", "error");
            globalMessage.classList.add(isError ? "error" : "success");
            window.clearTimeout(showMessage.hideTimer);
            showMessage.hideTimer = window.setTimeout(() => { globalMessage.hidden = true; }, 4000);
        }

        function escapeHtml(value) {
            return String(value).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
                .replace(/\"/g, "&quot;").replace(/'/g, "&#39;");
        }
    });
</script>
</body>
</html>
