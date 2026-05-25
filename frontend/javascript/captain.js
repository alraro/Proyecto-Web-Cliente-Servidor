<<<<<<< HEAD
﻿const API_BASE = 'http://localhost:8080';
=======
const API_BASE = 'http://localhost:8080';
>>>>>>> 571d3fabdaca241a6043cef2ef50c84e2c97ff35

document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const tokenFromQuery = params.get('token');
    const nameFromQuery  = params.get('nombre');
    if (tokenFromQuery) localStorage.setItem('token', tokenFromQuery);
    if (nameFromQuery)  localStorage.setItem('nombre', nameFromQuery);

    const token = localStorage.getItem('token');

<<<<<<< HEAD
    const campaignSelect  = document.querySelector('#campaign-select');
    const btnLoad         = document.querySelector('#btn-load');
    const globalMessage   = document.querySelector('#global-message');
    const captainsTbody   = document.querySelector('#captains-tbody');
    const captainSelect   = document.querySelector('#captain-select');
    const btnAssign       = document.querySelector('#btn-assign');

    if (!token || localStorage.getItem('role') !== 'CAPITAN') {
        window.location.href = 'login.html';
        return;
    }

    
=======
    document.getElementById('user-name').textContent = localStorage.getItem('nombre') || 'Administrador';

    const campaignSelect  = document.getElementById('campaign-select');
    const btnLoad         = document.getElementById('btn-load');
    const globalMessage   = document.getElementById('global-message');
    const captainsTbody   = document.getElementById('captains-tbody');
    const captainSelect   = document.getElementById('captain-select');
    const btnAssign       = document.getElementById('btn-assign');

    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'login.html';
    });
>>>>>>> 571d3fabdaca241a6043cef2ef50c84e2c97ff35

    captainSelect.disabled = true;
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
            captainSelect.disabled = false;
            btnAssign.disabled = false;
        } catch (error) {
            captainSelect.disabled = true;
            btnAssign.disabled = true;
            showMessage(error.message || 'No se pudieron cargar los capitanes', true);
        }
    });

    btnAssign.addEventListener('click', async () => {
        const campaignId = campaignSelect.value;
        const userId     = captainSelect.value;
        if (!campaignId) { showMessage('Selecciona una campaña', true); return; }
        if (!userId)     { showMessage('Selecciona un capitán', true); return; }
        try {
            await fetchJson(API_BASE + `/api/campaigns/${campaignId}/captains`, {
                method: 'POST',
                headers: authHeaders(token),
                body: JSON.stringify({ userId: Number(userId) })
            });
            showMessage('Capitán asignado correctamente', false);
            await loadCampaignData(campaignId);
        } catch (error) {
            showMessage(error.message || 'No se pudo asignar el capitán', true);
        }
    });

    captainsTbody.addEventListener('click', async (event) => {
        const button = event.target.closest("button[data-role='CAPTAIN']");
        if (!button) return;
        const campaignId = campaignSelect.value;
        const userId     = button.dataset.userid;
        if (!campaignId || !userId) { showMessage('Selección inválida', true); return; }
        try {
            await fetchJson(API_BASE + `/api/campaigns/${campaignId}/captains/${userId}`, {
                method: 'DELETE',
                headers: authHeaders(token)
            });
            showMessage('Capitán desasignado correctamente', false);
            await loadCampaignData(campaignId);
        } catch (error) {
            showMessage(error.message || 'No se pudo desasignar el capitán', true);
        }
    });

    // ── Helpers ────────────────────────────────────────────────────────────────

    function authHeaders(jwtToken) {
        return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + jwtToken };
    }

    async function loadCampaignData(campaignId) {
        const [assignments, availableData] = await Promise.all([
            fetchJson(API_BASE + `/api/campaigns/${campaignId}/assignments`, { headers: authHeaders(token) }),
            fetchJson(API_BASE + `/api/campaigns/${campaignId}/available-users?role=CAPTAIN`, { headers: authHeaders(token) })
        ]);
        const available = Array.isArray(availableData) ? availableData : (availableData.content || []);
        renderCaptainsTable(assignments?.captains || []);
        populateSelect(captainSelect, available, 'Selecciona un capitán...');
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
<<<<<<< HEAD
        campaignSelect.innerHTML = '';
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = 'Selecciona una campaña...';
        campaignSelect.appendChild(defaultOpt);
=======
        campaignSelect.innerHTML = "<option value=''>Selecciona una campaña...</option>";
>>>>>>> 571d3fabdaca241a6043cef2ef50c84e2c97ff35
        (campaigns || []).forEach(campaign => {
            const option = document.createElement('option');
            option.value = String(campaign.id);
            option.textContent = campaign.name + ' (' + campaign.startDate + ' - ' + campaign.endDate + ')';
            campaignSelect.appendChild(option);
        });
    }

    function renderCaptainsTable(captains) {
        captainsTbody.innerHTML = '';
        if (!captains.length) {
<<<<<<< HEAD
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 3;
            td.className = 'table-empty';
            td.textContent = 'Sin capitanes asignados.';
            tr.appendChild(td);
            captainsTbody.appendChild(tr);
=======
            captainsTbody.innerHTML = "<tr><td colspan='3' class='table-empty'>Sin capitanes asignados.</td></tr>";
>>>>>>> 571d3fabdaca241a6043cef2ef50c84e2c97ff35
            return;
        }
        captains.forEach(captain => {
            const row = document.createElement('tr');
<<<<<<< HEAD
            const td0 = document.createElement('td');
            td0.textContent = escapeHtml(captain.name || '');
            row.appendChild(td0);
            const td1 = document.createElement('td');
            td1.textContent = escapeHtml(captain.email || '');
            row.appendChild(td1);
            const td2 = document.createElement('td');
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn btn-sm btn-danger';
            btn.dataset.userid = captain.userId;
            btn.dataset.role = 'CAPTAIN';
            btn.textContent = 'Eliminar';
            td2.appendChild(btn);
            row.appendChild(td2);
=======
            row.innerHTML = `
                <td>${escapeHtml(captain.name || '')}</td>
                <td>${escapeHtml(captain.email || '')}</td>
                <td><button type="button" class="btn btn-sm btn-danger" data-userid="${captain.userId}" data-role="CAPTAIN">Eliminar</button></td>
            `;
>>>>>>> 571d3fabdaca241a6043cef2ef50c84e2c97ff35
            captainsTbody.appendChild(row);
        });
    }

    function populateSelect(selectEl, users, placeholder) {
<<<<<<< HEAD
        selectEl.innerHTML = '';
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = placeholder;
        selectEl.appendChild(defaultOpt);
=======
        selectEl.innerHTML = `<option value=''>${placeholder}</option>`;
>>>>>>> 571d3fabdaca241a6043cef2ef50c84e2c97ff35
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
<<<<<<< HEAD
});
=======
});
>>>>>>> 571d3fabdaca241a6043cef2ef50c84e2c97ff35
