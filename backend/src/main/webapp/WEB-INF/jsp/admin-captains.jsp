<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Gestión de Capitanes</title>
    <link rel="stylesheet" href="/css/administrador.css">
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

<main class="admin-page" aria-label="Captains management page">
    <section class="page-header">
        <a href="/admin" class="back-link">&larr; Volver al menú</a>
        <h1>Gestión de Capitanes</h1>
        <p>Asigna capitanes a campañas de recogida.</p>
    </section>

    <section class="card campaign-selector" aria-label="Campaign selector">
        <label for="campaign-select">Campaña</label>
        <div class="row-inline">
            <select id="campaign-select">
                <option value="">Selecciona una campaña...</option>
            </select>
            <button type="button" id="btn-load">Cargar capitanes</button>
        </div>
    </section>

    <div id="global-message" hidden></div>

    <section aria-label="Current captains">
        <article class="card">
            <h2>Capitanes asignados</h2>
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
        </article>
    </section>

    <section aria-label="Add captain">
        <article class="card">
            <h2>Añadir capitán</h2>
            <div class="row-inline">
                <select id="captain-select">
                    <option value="">Selecciona un capitán...</option>
                </select>
                <button type="button" id="btn-assign">Asignar</button>
            </div>
        </article>
    </section>
</main>

<script>
    document.addEventListener("DOMContentLoaded", async () => {
        const params = new URLSearchParams(window.location.search);
        const tokenFromQuery = params.get("token");
        const nameFromQuery = params.get("nombre");
        if (tokenFromQuery) {
            localStorage.setItem("token", tokenFromQuery);
        }
        if (nameFromQuery) {
            localStorage.setItem("nombre", nameFromQuery);
        }

        const token = localStorage.getItem("token");

        const userNameEl = document.getElementById("user-name");
        userNameEl.textContent = localStorage.getItem("nombre") || "Admin";

        const btnLogout = document.getElementById("btn-logout");
        const campaignSelect = document.getElementById("campaign-select");
        const btnLoad = document.getElementById("btn-load");
        const globalMessage = document.getElementById("global-message");
        const captainsTbody = document.getElementById("captains-tbody");
        const captainSelect = document.getElementById("captain-select");
        const btnAssign = document.getElementById("btn-assign");

        btnLogout.addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "/login";
        });

        captainSelect.disabled = true;
        btnAssign.disabled = true;

        if (!token) {
            showMessage("No se detecta una sesión válida. Vuelve al panel de administración e inténtalo de nuevo.", true);
            return;
        }

        try {
            const data = await fetchJson("/api/campaigns?size=200&sort=startDate,desc", {
                method: "GET",
                headers: authHeaders(token)
            });
            
            // Extraemos el array, ya sea que venga directo o dentro de "content"
            const campaignsArray = Array.isArray(data) ? data : (data.content || []);
            
            populateCampaignSelect(campaignsArray);
        } catch (error) {
            showMessage(error.message || "No se pudieron cargar las campañas", true);
        }

        btnLoad.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            if (!campaignId) {
                showMessage("Selecciona una campaña", true);
                return;
            }

            try {
                await loadCampaignData(campaignId);
                captainSelect.disabled = false;
                btnAssign.disabled = false;
            } catch (error) {
                captainSelect.disabled = true;
                btnAssign.disabled = true;
                showMessage(error.message || "No se pudieron cargar los capitanes", true);
            }
        });

        btnAssign.addEventListener("click", async () => {
            const campaignId = campaignSelect.value;
            const userId = captainSelect.value;

            if (!campaignId) {
                showMessage("Selecciona una campaña", true);
                return;
            }
            if (!userId) {
                showMessage("Selecciona un capitán", true);
                return;
            }

            try {
                await fetchJson(`/api/campaigns/${campaignId}/captains`, {
                    method: "POST",
                    headers: authHeaders(token),
                    body: JSON.stringify({ userId: Number(userId) })
                });
                showMessage("Capitán asignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo asignar el capitán", true);
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
                showMessage("Selección inválida", true);
                return;
            }

            try {
                await fetchJson(`/api/campaigns/${campaignId}/captains/${userId}`, {
                    method: "DELETE",
                    headers: authHeaders(token)
                });
                showMessage("Capitán desasignado correctamente", false);
                await loadCampaignData(campaignId);
            } catch (error) {
                showMessage(error.message || "No se pudo desasignar el capitán", true);
            }
        });

        function authHeaders(jwtToken) {
            return {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${jwtToken}`
            };
        }

        async function loadCampaignData(campaignId) {
            const [assignments, availableCaptainsData] = await Promise.all([
                fetchJson(`/api/campaigns/${campaignId}/assignments`, {
                    method: "GET",
                    headers: authHeaders(token)
                }),
                fetchJson(`/api/campaigns/${campaignId}/available-users?role=CAPTAIN`, {
                    method: "GET",
                    headers: authHeaders(token)
                })
            ]);

            // IGUAL QUE ANTES: Comprobamos si es un array o si viene dentro de .content
            const availableCaptains = Array.isArray(availableCaptainsData) 
                ? availableCaptainsData 
                : (availableCaptainsData.content || []);

            renderCaptainsTable(assignments?.captains || []);
            
            // Ahora pasamos la lista limpia
            populateSelect(captainSelect, availableCaptains, "Selecciona un capitán...");
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
                throw new Error("Error inesperado de red");
            }
        }

        function populateCampaignSelect(campaigns) {
            campaignSelect.innerHTML = "<option value=''>Selecciona una campaña...</option>";
            (campaigns || []).forEach((campaign) => {
                const option = document.createElement("option");
                option.value = String(campaign.id);
                option.textContent = `${campaign.name} (${campaign.startDate} - ${campaign.endDate})`;
                campaignSelect.appendChild(option);
            });
        }

        function renderCaptainsTable(captains) {
            captainsTbody.innerHTML = "";
            if (!captains.length) {
                const row = document.createElement("tr");
                row.innerHTML = "<td colspan='3'>Sin capitanes asignados</td>";
                captainsTbody.appendChild(row);
                return;
            }

            captains.forEach((captain) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${escapeHtml(captain.name || "")}</td>
                    <td>${escapeHtml(captain.email || "")}</td>
                    <td>
                        <button type="button" data-userid="${captain.userId}" data-role="CAPTAIN">Eliminar</button>
                    </td>
                `;
                captainsTbody.appendChild(row);
            });
        }

        function populateSelect(selectEl, users, placeholder) {
            selectEl.innerHTML = `<option value=''>${placeholder}</option>`;
            (users || []).forEach((user) => {
                const option = document.createElement("option");
                option.value = String(user.userId);
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
            showMessage.hideTimer = window.setTimeout(() => {
                globalMessage.hidden = true;
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
