const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    if (!sessionStorage.getItem('token') || sessionStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }

    

    const params = new URLSearchParams(window.location.search);
    const tokenFromQuery = params.get('token');
    const nameFromQuery  = params.get('nombre');
    if (tokenFromQuery) sessionStorage.setItem('token', tokenFromQuery);
    if (nameFromQuery)  sessionStorage.setItem('nombre', nameFromQuery);

    const token = sessionStorage.getItem('token');

    const campaignSelect     = document.querySelector('#campaign-select');
    const btnLoad            = document.querySelector('#btn-load');
    const globalMessage      = document.querySelector('#global-message');
    const coordinatorsTbody  = document.querySelector('#coordinators-tbody');
    const coordinatorSelect  = document.querySelector('#coordinator-select');
    const btnAssign          = document.querySelector('#btn-assign');


    coordinatorSelect.disabled = true;
    btnAssign.disabled = true;

    if (!token) {
        showMessage('No se detecta una sesión válida. Vuelve al panel de administración e inténtalo de nuevo.', true);
        return;
    }

    try {
        const data = await fetchJson(API_BASE + '/api/campaigns?size=200&sort=startDate,desc', {
            method: 'GET',
            headers: authHeaders(token)
        });
        const campaignsArray = Array.isArray(data) ? data : (data.content || []);
        populateCampaignSelect(campaignsArray);
    } catch (error) {
        showMessage(error.message || 'No se pudieron cargar las campañas', true);
    }

    btnLoad.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        if (!campaignId) { showMessage('Selecciona una campaña', true); return; }
        try {
            await loadCampaignData(campaignId);
            coordinatorSelect.disabled = false;
            btnAssign.disabled = false;
        } catch (error) {
            coordinatorSelect.disabled = true;
            btnAssign.disabled = true;
            showMessage(error.message || 'No se pudieron cargar los coordinadores', true);
        }
    });

    btnAssign.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        const userId     = coordinatorSelect.value;
        if (!campaignId) { showMessage('Selecciona una campaña', true); return; }
        if (!userId)     { showMessage('Selecciona un coordinador', true); return; }
        try {
            await fetchJson(API_BASE + `/api/campaigns/${campaignId}/coordinators`, {
                method: 'POST',
                headers: authHeaders(token),
                body: JSON.stringify({ userId: Number(userId) })
            });
            showMessage('Coordinador asignado correctamente', false);
            await loadCampaignData(campaignId);
        } catch (error) {
            showMessage(error.message || 'No se pudo asignar el coordinador', true);
        }
    });

    coordinatorsTbody.addEventListener('click', async (event) => {
        const button = event.target.closest("button[data-role='COORDINATOR']");
        if (!button) return;
        const campaignId = campaignSelect.value;
        const userId     = button.dataset.userid;
        if (!campaignId || !userId) { showMessage('Selección inválida', true); return; }
        try {
            await fetchJson(API_BASE + `/api/campaigns/${campaignId}/coordinators/${userId}`, {
                method: 'DELETE',
                headers: authHeaders(token)
            });
            showMessage('Coordinador desasignado correctamente', false);
            await loadCampaignData(campaignId);
        } catch (error) {
            showMessage(error.message || 'No se pudo desasignar el coordinador', true);
        }
    });

    // ── Helpers ────────────────────────────────────────────────────────────────

    function authHeaders(jwtToken) {
        return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + jwtToken };
    }

    async function loadCampaignData(campaignId) {
        const [assignments, availableData] = await Promise.all([
            fetchJson(API_BASE + `/api/campaigns/${campaignId}/assignments`, { headers: authHeaders(token) }),
            fetchJson(API_BASE + `/api/campaigns/${campaignId}/available-users?role=COORDINATOR`, { headers: authHeaders(token) })
        ]);
        const available = Array.isArray(availableData) ? availableData : (availableData.content || []);
        renderCoordinatorsTable(assignments?.coordinators || []);
        populateSelect(coordinatorSelect, available, 'Selecciona un coordinador...');
    }

    async function fetchJson(url, options) {
        const response = await fetch(url, options);
        const data = await response.json().catch(() => ({}));
        if (response.status === 401 || response.status === 403) {
            throw new Error('Tu sesión no es válida o ha expirado.');
        }
        if (!response.ok) throw new Error(data.message || 'Error ' + response.status);
        return data;
    }

    function populateCampaignSelect(campaigns) {
        campaignSelect.innerHTML = '';
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = 'Selecciona una campaña...';
        campaignSelect.appendChild(defaultOption);
        (campaigns || []).forEach(campaign => {
            const option = document.createElement('option');
            option.value = String(campaign.id);
            option.textContent = campaign.name + ' (' + campaign.startDate + ' - ' + campaign.endDate + ')';
            campaignSelect.appendChild(option);
        });
    }

    function renderCoordinatorsTable(coordinators) {
        coordinatorsTbody.innerHTML = '';
        if (!coordinators.length) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.setAttribute('colspan', '3');
            cell.className = 'table-empty';
            cell.textContent = 'Sin coordinadores asignados.';
            row.appendChild(cell);
            coordinatorsTbody.appendChild(row);
            return;
        }
        coordinators.forEach(coordinator => {
            const row = document.createElement('tr');
            const tdName = document.createElement('td');
            tdName.textContent = escapeHtml(coordinator.name || '');
            row.appendChild(tdName);
            const tdEmail = document.createElement('td');
            tdEmail.textContent = escapeHtml(coordinator.email || '');
            row.appendChild(tdEmail);
            const tdAction = document.createElement('td');
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn btn-sm btn-danger';
            btn.setAttribute('data-userid', coordinator.userId);
            btn.setAttribute('data-role', 'COORDINATOR');
            btn.textContent = 'Eliminar';
            tdAction.appendChild(btn);
            row.appendChild(tdAction);
            coordinatorsTbody.appendChild(row);
        });
    }

    function populateSelect(selectEl, users, placeholder) {
        selectEl.innerHTML = '';
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = placeholder;
        selectEl.appendChild(defaultOption);
        (users || []).forEach(user => {
            const option = document.createElement('option');
            option.value = String(user.userId);
            option.textContent = user.name + ' (' + user.email + ')';
            selectEl.appendChild(option);
        });
    }

    function showMessage(text, isError) {
        globalMessage.hidden = false;
        globalMessage.textContent = text;
        globalMessage.className = isError ? 'error' : 'success';
        clearTimeout(showMessage._t);
        showMessage._t = setTimeout(() => { globalMessage.hidden = true; }, 4000);
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;')
            .replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }
});
