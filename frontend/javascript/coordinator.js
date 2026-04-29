// Coordinator Shift Assignment JavaScript
const API_BASE = '/api';

document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    if (!localStorage.getItem('token')) {
        window.location.href = 'login.html';
        return;
    }

    // Initialize user info
    initUserInfo();
    
    // Load initial data
    loadCampaigns();
    loadVolunteers();
    
    // Set up event listeners
    setupEventListeners();
});

// Initialize user info from localStorage
function initUserInfo() {
    const userNameEl = document.getElementById('user-name');
    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Coordinador';
    }

    const btnEdit = document.getElementById('btn-edit');
    const btnLogout = document.getElementById('btn-logout');

    if (btnEdit) {
        btnEdit.addEventListener('click', () => {
            window.location.href = 'edit.html';
        });
    }

    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = 'login.html';
        });
    }
}

// Setup all event listeners
function setupEventListeners() {
    // Volunteer assignment form
    const volunteerForm = document.getElementById('volunteer-assignment-form');
    if (volunteerForm) {
        volunteerForm.addEventListener('submit', handleVolunteerAssignment);
    }

    // Captain assignment form
    const captainForm = document.getElementById('captain-assignment-form');
    if (captainForm) {
        captainForm.addEventListener('submit', handleCaptainAssignment);
    }

    // Campaign selection - volunteer form
    const campaignSelect = document.getElementById('campaign-select');
    if (campaignSelect) {
        campaignSelect.addEventListener('change', handleCampaignChange);
    }

    // Store selection - volunteer form
    const storeSelect = document.getElementById('store-select');
    if (storeSelect) {
        storeSelect.addEventListener('change', handleStoreChange);
    }

    // Shift date/time changes
    const shiftDate = document.getElementById('shift-date');
    const startTime = document.getElementById('start-time');
    if (shiftDate && startTime) {
        shiftDate.addEventListener('change', checkVolunteerCount);
        startTime.addEventListener('change', checkVolunteerCount);
    }

    // Captain campaign selection
    const captainCampaignSelect = document.getElementById('captain-campaign-select');
    if (captainCampaignSelect) {
        captainCampaignSelect.addEventListener('change', handleCaptainCampaignChange);
    }

    // Filter changes
    const filterCampaign = document.getElementById('filter-campaign');
    const filterStore = document.getElementById('filter-store');
    const filterDate = document.getElementById('filter-date');
    
    if (filterCampaign) filterCampaign.addEventListener('change', loadAssignments);
    if (filterStore) filterStore.addEventListener('change', loadAssignments);
    if (filterDate) filterDate.addEventListener('change', loadAssignments);
}

// API helper function
async function apiCall(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const campaignId = localStorage.getItem('idCampaign') || 1; // Default campaign
    
    const url = new URL(endpoint, window.location.origin);
    if (campaignId && (endpoint.includes('volunteers') || endpoint.includes('shifts') || endpoint.includes('count'))) {
        url.searchParams.append('campaignId', campaignId);
    }
    
    const response = await fetch(url.toString(), {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...options.headers
        }
    });

    if (response.status === 403) {
        showAlert('Acceso denegado: Se requiere rol de Coordinador', 'error');
        return null;
    }

    return response;
}

// Load campaigns for dropdowns
async function loadCampaigns() {
    try {
        const response = await apiCall(`${API_BASE}/campaigns`);
        if (!response) return;

        const campaigns = await response.json();
        
        const campaignSelect = document.getElementById('campaign-select');
        const captainCampaignSelect = document.getElementById('captain-campaign-select');
        const filterCampaign = document.getElementById('filter-campaign');

        [campaignSelect, captainCampaignSelect, filterCampaign].forEach(select => {
            if (!select) return;
            select.innerHTML = '<option value="">Seleccionar campaña...</option>';
            campaigns.forEach(campaign => {
                const option = document.createElement('option');
                option.value = campaign.id;
                option.textContent = campaign.name;
                select.appendChild(option);
            });
        });
    } catch (error) {
        console.error('Error loading campaigns:', error);
    }
}

// Load volunteers for dropdown
async function loadVolunteers() {
    try {
        const response = await apiCall(`${API_BASE}/shifts/volunteers`);
        if (!response) return;

        const volunteers = await response.json();
        
        const volunteerSelect = document.getElementById('volunteer-select');
        if (volunteerSelect) {
            volunteerSelect.innerHTML = '<option value="">Seleccionar voluntario...</option>';
            volunteers.forEach(volunteer => {
                const option = document.createElement('option');
                option.value = volunteer.id;
                option.textContent = volunteer.name;
                volunteerSelect.appendChild(option);
            });
            volunteerSelect.disabled = false;
        }
    } catch (error) {
        console.error('Error loading volunteers:', error);
    }
}

// Handle campaign change - load stores
async function handleCampaignChange(e) {
    const campaignId = e.target.value;
    const storeSelect = document.getElementById('store-select');
    
    if (!storeSelect) return;
    
    if (!campaignId) {
        storeSelect.innerHTML = '<option value="">Seleccionar tienda...</option>';
        storeSelect.disabled = true;
        return;
    }

    try {
        const response = await apiCall(`${API_BASE}/campaigns/${campaignId}/stores`);
        if (!response) return;

        const stores = await response.json();
        
        storeSelect.innerHTML = '<option value="">Seleccionar tienda...</option>';
        stores.forEach(store => {
            const option = document.createElement('option');
            option.value = store.id;
            option.textContent = store.name;
            storeSelect.appendChild(option);
        });
        storeSelect.disabled = false;
    } catch (error) {
        console.error('Error loading stores:', error);
    }
}

// Handle store change - check volunteer count
async function handleStoreChange() {
    await checkVolunteerCount();
}

// Check current volunteer count for the shift
async function checkVolunteerCount() {
    const campaignId = document.getElementById('campaign-select')?.value;
    const storeId = document.getElementById('store-select')?.value;
    const shiftDate = document.getElementById('shift-date')?.value;
    const startTime = document.getElementById('start-time')?.value;

    const countDisplay = document.getElementById('current-volunteer-count');
    if (!countDisplay) return;

    if (!campaignId || !storeId || !shiftDate || !startTime) {
        countDisplay.textContent = '0 / 10';
        countDisplay.className = 'count-display';
        return;
    }

    try {
        const response = await apiCall(
            `${API_BASE}/shifts/count?campaignId=${campaignId}&storeId=${storeId}&shiftDay=${shiftDate}&startTime=${startTime}`
        );
        
        if (!response) return;

        const count = await response.json();
        const maxVolunteers = 10;
        
        countDisplay.textContent = `${count} / ${maxVolunteers}`;
        countDisplay.className = 'count-display';
        
        if (count >= maxVolunteers) {
            countDisplay.classList.add('at-limit');
        } else if (count > maxVolunteers) {
            countDisplay.classList.add('over-limit');
        }
    } catch (error) {
        console.error('Error checking volunteer count:', error);
    }
}

// Handle volunteer assignment form submission
async function handleVolunteerAssignment(e) {
    e.preventDefault();

    const campaignId = document.getElementById('campaign-select')?.value;
    const storeId = document.getElementById('store-select')?.value;
    const shiftDate = document.getElementById('shift-date')?.value;
    const startTime = document.getElementById('start-time')?.value;
    const endTime = document.getElementById('end-time')?.value;
    const volunteerId = document.getElementById('volunteer-select')?.value;

    if (!campaignId || !storeId || !shiftDate || !startTime || !endTime || !volunteerId) {
        showAlert('Por favor, complete todos los campos requeridos', 'error');
        return;
    }

    // Validate time
    if (startTime >= endTime) {
        showAlert('La hora de fin debe ser posterior a la hora de inicio', 'error');
        return;
    }

    const request = {
        volunteerId: parseInt(volunteerId),
        campaignId: parseInt(campaignId),
        storeId: parseInt(storeId),
        shiftDay: shiftDate,
        startTime: startTime,
        endTime: endTime
    };

    try {
        const response = await apiCall(`${API_BASE}/shifts/assign-volunteer?campaignId=${campaignId}`, {
            method: 'POST',
            body: JSON.stringify(request)
        });

        if (!response) return;

        const result = await response.json();

        if (result.success) {
            showAlert('Voluntario asignado correctamente al turno', 'success');
            document.getElementById('volunteer-assignment-form').reset();
            await checkVolunteerCount();
            await loadAssignments();
        } else {
            handleAssignmentError(result);
        }
    } catch (error) {
        console.error('Error assigning volunteer:', error);
        showAlert('Error al asignar voluntario al turno', 'error');
    }
}

// Handle captain campaign change
async function handleCaptainCampaignChange(e) {
    const campaignId = e.target.value;
    const captainSelect = document.getElementById('captain-select');
    
    if (!captainSelect) return;
    
    if (!campaignId) {
        captainSelect.innerHTML = '<option value="">Seleccionar capitán...</option>';
        captainSelect.disabled = true;
        return;
    }

    // Load available users for captain (in a real app, this would be an API call)
    captainSelect.innerHTML = '<option value="">Seleccionar capitán...</option>';
    captainSelect.disabled = false;
    
    // Note: In a real implementation, you would fetch users from an API
    // For now, we'll show a message
    showAlert('Seleccione un usuario para asignar como capitán', 'warning');
}

// Handle captain assignment form submission
async function handleCaptainAssignment(e) {
    e.preventDefault();

    const campaignId = document.getElementById('captain-campaign-select')?.value;
    const userId = document.getElementById('captain-select')?.value;

    if (!campaignId || !userId) {
        showAlert('Por favor, complete todos los campos requeridos', 'error');
        return;
    }

    const request = {
        userId: parseInt(userId),
        campaignId: parseInt(campaignId)
    };

    try {
        const response = await apiCall(`${API_BASE}/shifts/assign-captain?campaignId=${campaignId}`, {
            method: 'POST',
            body: JSON.stringify(request)
        });

        if (!response) return;

        const result = await response.json();

        if (result.success) {
            showAlert('Capitán asignado correctamente a la campaña', 'success');
            document.getElementById('captain-assignment-form').reset();
        } else {
            handleAssignmentError(result);
        }
    } catch (error) {
        console.error('Error assigning captain:', error);
        showAlert('Error al asignar capitán a la campaña', 'error');
    }
}

// Handle assignment errors with specific messages
function handleAssignmentError(result) {
    const errorMessages = {
        'SHIFT_OVERLAP': 'El voluntario ya tiene un turno asignado que se solapa con este horario',
        'MAX_VOLUNTEERS': 'Se ha alcanzado el número máximo de voluntarios para este turno',
        'NOT_FOUND': 'No se encontró el recurso solicitado',
        'ALREADY_ASSIGNED': 'El usuario ya está asignado en este contexto',
        'ACCESS_DENIED': 'No tiene permisos para realizar esta acción'
    };

    const message = errorMessages[result.errorType] || result.message;
    showAlert(message, 'error');
}

// Load current assignments
async function loadAssignments() {
    const campaignId = document.getElementById('filter-campaign')?.value;
    const storeId = document.getElementById('filter-store')?.value;
    const shiftDay = document.getElementById('filter-date')?.value;

    let url = `${API_BASE}/shifts?`;
    const params = [];
    
    if (campaignId) params.push(`campaignId=${campaignId}`);
    if (storeId) params.push(`storeId=${storeId}`);
    if (shiftDay) params.push(`shiftDay=${shiftDay}`);
    
    url += params.join('&');

    try {
        const response = await apiCall(url);
        if (!response) return;

        const assignments = await response.json();
        renderAssignments(assignments);
    } catch (error) {
        console.error('Error loading assignments:', error);
    }
}

// Render assignments in table
function renderAssignments(assignments) {
    const tbody = document.getElementById('assignments-tbody');
    if (!tbody) return;

    if (!assignments || assignments.length === 0) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="6">No hay asignaciones para mostrar</td></tr>';
        return;
    }

    tbody.innerHTML = assignments.map(assignment => `
        <tr>
            <td>${assignment.idVolunteer?.name || 'N/A'}</td>
            <td>${assignment.campaignStores?.idCampaign?.name || 'N/A'}</td>
            <td>${assignment.campaignStores?.idStore?.name || 'N/A'}</td>
            <td>${formatDate(assignment.id.shiftDay)}</td>
            <td>${formatTime(assignment.id.startTime)} - ${formatTime(assignment.endTime)}</td>
            <td>
                <button class="btn-unassign" onclick="unassignVolunteer(${assignment.id.idVolunteer?.id}, ${assignment.id.idCampaign}, ${assignment.id.idStore}, '${assignment.id.shiftDay}', '${assignment.id.startTime}')">
                    Desasignar
                </button>
            </td>
        </tr>
    `).join('');
}

// Unassign volunteer from shift
async function unassignVolunteer(volunteerId, campaignId, storeId, shiftDay, startTime) {
    if (!confirm('¿Está seguro de que desea desasignar este voluntario?')) {
        return;
    }

    try {
        const response = await apiCall(
            `${API_BASE}/shifts/unassign-volunteer?volunteerId=${volunteerId}&campaignId=${campaignId}&storeId=${storeId}&shiftDay=${shiftDay}&startTime=${startTime}`,
            { method: 'DELETE' }
        );

        if (!response) return;

        const result = await response.json();

        if (result.success) {
            showAlert('Voluntario desasignado correctamente', 'success');
            await loadAssignments();
            await checkVolunteerCount();
        } else {
            handleAssignmentError(result);
        }
    } catch (error) {
        console.error('Error unassigning volunteer:', error);
        showAlert('Error al desasignar voluntario', 'error');
    }
}

// Show alert message
function showAlert(message, type = 'info') {
    const container = document.getElementById('alert-container');
    if (!container) return;

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    
    const icons = {
        'error': '❌',
        'warning': '⚠️',
        'success': '✅',
        'info': 'ℹ️'
    };

    alertDiv.innerHTML = `
        <span class="alert-icon">${icons[type] || icons.info}</span>
        <span>${message}</span>
    `;

    container.appendChild(alertDiv);

    // Auto remove after 5 seconds
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Format date for display
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES');
}

// Format time for display
function formatTime(timeString) {
    if (!timeString) return 'N/A';
    return timeString.substring(0, 5);
}

// Make unassignVolunteer available globally
window.unassignVolunteer = unassignVolunteer;