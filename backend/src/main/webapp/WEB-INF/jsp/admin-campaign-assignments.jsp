<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Asignación a Campañas</title>
    <link rel="stylesheet" href="/css/administrador.css">
</head>
<body>
<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/index" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>

    <div class="topbar-actions">
        <span id="user-name">Admin</span>
        <a class="btn" href="/edit">Edit profile</a>
        <button type="button" id="btn-logout" class="btn">Log out</button>
    </div>
</header>

<main class="admin-page" aria-label="Campaign assignments page">
    <section class="page-header">
        <a href="/admin" class="back-link">&larr; Back to menu</a>
        <h1>Asignación a Campañas</h1>
        <p>Asigna coordinadores y capitanes a cada campaña de recogida.</p>
    </section>

    <section class="card campaign-selector" aria-label="Campaign selector">
        <label for="campaign-select">Campaña</label>
        <option value="">Selecciona una campaña...</option>
        <button type="button" id="btn-load">Cargar asignaciones</button>
        </div>
    </section>

    <div id="global-message" hidden></div>

    <section class="grid-two" aria-label="Current assignments">
        <article class="card">
            <h2>Assigned Coordinators</h2>
            <table>
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody id="coordinators-tbody"></tbody>
            </table>
        </article>

        <article class="card">
            <h2>Assigned Captains</h2>
            <table>
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody id="captains-tbody"></tbody>
            </table>
        </article>
    </section>

    <section class="grid-two" aria-label="Add assignments">
        <article class="card">
            <h2>Add Coordinator</h2>
            <div class="row-inline">
                <select id="coordinator-select">
                    <option value="">Select a coordinator...</option>
                </select>
                <button type="button" id="btn-assign-coordinator">Assign</button>
            </div>
        </article>

        <article class="card">
            <h2>Add Captain</h2>
            <div class="row-inline">
                <select id="captain-select">
                    <option value="">Select a captain...</option>
                </select>
                <button type="button" id="btn-assign-captain">Assign</button>
            </div>
        </article>
    </section>
</main>

<script>
    document.addEventListener("DOMContentLoaded", async () => {
        const token = localStorage.getItem("token");
        if (!token) {
            window.location.href = "/login";
            return;
        }

        const userNameEl = document.getElementById("user-name");
        userNameEl.textContent = localStorage.getItem("nombre") || "Admin";

        const btnLogout = document.getElementById("btn-logout");
        const campaignSelect = document.getElementById("campaign-select");
        const btnLoad = document.getElementById("btn-load");
        const coordinatorsTbody = document.getElementById("coordinators-tbody");
        const captainsTbody = document.getElementById("captains-tbody");
        const coordinatorSelect = document.getElementById("coordinator-select");
        const captainSelect = document.getElementById("captain-select");
        const btnAssignCoordinator = document.getElementById("btn-assign-coordinator");
        const btnAssignCaptain = document.getElementById("btn-assign-captain");

        btnLogout.addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "/login";
        });

        setAssignmentControls(false);

        try {
            const campaigns = await fetchJson("/api/campaigns", {
                method: "GET",
                headers: authHeaders(token)
            });
            populateCampaignSelect(campaigns);
        } catch (error) {
            showMessage(error.message || "Unable to load campaigns", true);
        }

        btnLoad.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            if (!campaignId) {
                showMessage("Please select a campaign", true);
                return;
            }

            try {
                await loadCampaignData(campaignId);
                setAssignmentControls(true);
                showMessage("Assignments loaded", false);
            } catch (error) {
                setAssignmentControls(false);
                showMessage(error.message || "Unable to load assignments", true);
            }
        });

        btnAssignCoordinator.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            const userId = coordinatorSelect.value;
            if (!campaignId) {
                showMessage("Please select a campaign", true);
                return;
            }
            if (!userId) {
                showMessage("Please select a coordinator", true);
                return;
            }

            try {
                await fetchJson(`/api/campaigns/${campaignId}/coordinators`, {
                    method: "POST",
                    headers: authHeaders(token),
                    body: JSON.stringify({ userId: Number(userId) })
                });
                showMessage("Coordinator assigned successfully", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "Unable to assign coordinator", true);
            }
        });

        btnAssignCaptain.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            const userId = captainSelect.value;
            if (!campaignId) {
                showMessage("Please select a campaign", true);
                return;
            }
            if (!userId) {
                showMessage("Please select a captain", true);
                return;
            }

            try {
                await fetchJson(`/api/campaigns/${campaignId}/captains`, {
                    method: "POST",
                    headers: authHeaders(token),
                    body: JSON.stringify({ userId: Number(userId) })
                });
                showMessage("Captain assigned successfully", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "Unable to assign captain", true);
            }
        });

        coordinatorsTbody.addEventListener("click", async (event) => {
            const button = event.target.closest("button[data-role='COORDINATOR']");
            if (!button) {
                return;
            }

            const campaignId = campaignSelect.value;
            const userId = button.dataset.userid;
            if (!campaignId || !userId) {
                showMessage("Invalid selection", true);
                return;
            }

            try {
                await fetchJson(`/api/campaigns/${campaignId}/coordinators/${userId}`, {
                    method: "DELETE",
                    headers: authHeaders(token)
                });
                showMessage("Coordinator unassigned successfully", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "Unable to unassign coordinator", true);
            }
        });

        captainsTbody.addEventListener("click", async (event) => {
            const button = event.target.closest("button[data-role='CAPTAIN']");
            if (!button) {
                return;
            }

            const campaignId = campaignSelect.value;
            const userId = button.dataset.userid;
            if (!campaignId || !userId) {
                showMessage("Invalid selection", true);
                return;
            }

            try {
                await fetchJson(`/api/campaigns/${campaignId}/captains/${userId}`, {
                    method: "DELETE",
                    headers: authHeaders(token)
                });
                showMessage("Captain unassigned successfully", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "Unable to unassign captain", true);
            }
        });

        function authHeaders(jwtToken) {
            return {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${jwtToken}`
            };
        }

        async function loadCampaignData(campaignId) {
            const [assignments, availableCoordinators, availableCaptains] = await Promise.all([
                fetchJson(`/api/campaigns/${campaignId}/assignments`, {
                    method: "GET",
                    headers: authHeaders(token)
                }),
                fetchJson(`/api/campaigns/${campaignId}/available-users?role=COORDINATOR`, {
                    method: "GET",
                    headers: authHeaders(token)
                }),
                fetchJson(`/api/campaigns/${campaignId}/available-users?role=CAPTAIN`, {
                    method: "GET",
                    headers: authHeaders(token)
                })
            ]);

            renderAssignments(assignments);
            populateUserSelect(coordinatorSelect, availableCoordinators, "Select a coordinator...");
            populateUserSelect(captainSelect, availableCaptains, "Select a captain...");
        }

        async function fetchJson(url, options) {
            try {
                const response = await fetch(url, options);
                const data = await response.json().catch(() => ({}));

                if (!response.ok) {
                    throw new Error(data.message || `Request failed with status ${response.status}`);
                }
                return data;
            } catch (error) {
                if (error instanceof Error) {
                    throw error;
                }
                throw new Error("Unexpected request error");
            }
        }

        function populateCampaignSelect(campaigns) {
            campaignSelect.innerHTML = "<option value=''>Select a campaign...</option>";
            (campaigns || []).forEach((campaign) => {
                const option = document.createElement("option");
                option.value = String(campaign.id);
                option.textContent = `${campaign.name} (${campaign.startDate} - ${campaign.endDate})`;
                campaignSelect.appendChild(option);
            });
        }

        function renderAssignments(assignments) {
            renderTable(coordinatorsTbody, assignments?.coordinators || [], "COORDINATOR");
            renderTable(captainsTbody, assignments?.captains || [], "CAPTAIN");
        }

        function renderTable(tbody, users, role) {
            tbody.innerHTML = "";
            if (!users.length) {
                const row = document.createElement("tr");
                row.innerHTML = "<td colspan='3'>No assignments</td>";
                tbody.appendChild(row);
                return;
            }

            users.forEach((user) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${escapeHtml(user.name || "")}</td>
                    <td>${escapeHtml(user.email || "")}</td>
                    <td>
                        <button type="button" data-userid="${user.userId}" data-role="${role}">Remove</button>
                    </td>
                `;
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
            const messageEl = document.getElementById("global-message");
            messageEl.hidden = false;
            messageEl.textContent = text;
            messageEl.classList.remove("success", "error");
            messageEl.classList.add(isError ? "error" : "success");

            window.clearTimeout(showMessage.hideTimer);
            showMessage.hideTimer = window.setTimeout(() => {
                messageEl.hidden = true;
            }, 4000);
        }

        function escapeHtml(value) {
            return String(value)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/\"/g, "&quot;")
                .replace(/'/g, "&#39;");
        }
    });
</script>
</body>
</html>
