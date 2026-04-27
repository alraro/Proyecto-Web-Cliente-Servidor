// Coordinator Panel JavaScript

const API_BASE = 'http://localhost:8080/api';

// Helper function to get auth header
function getAuthHeader() {
    const token = localStorage.getItem('token');
    return token ? `Bearer ${token}` : null;
}

// Show message in form
function showFormMessage(message, isError = true) {
    const messageEl = document.getElementById('form-message');
    if (messageEl) {
        messageEl.textContent = message;
        messageEl.className = `form-message ${isError ? 'error' : 'success'}`;
        setTimeout(() => {
            messageEl.className = 'form-message';
        }, 5000);
    }
}

// Load user profile
async function loadProfile() {
    try {
        const response = await fetch(`${API_BASE}/auth/profile`, {
            headers: {
                'Authorization': getAuthHeader()
            }
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('user-name').textContent = data.nombre || 'Coordinador';
            document.getElementById('welcome-name').textContent = data.nombre || 'Coordinador';
            
            // Fill profile form
            document.getElementById('profile-name').value = data.nombre || '';
            document.getElementById('profile-email').value = data.email || '';
            document.getElementById('profile-phone').value = data.telefono || '';
            document.getElementById('profile-address').value = data.domicilio || '';
        }
    } catch (error) {
        console.error('Error loading profile:', error);
    }
}

// Load campaigns for the coordinator
async function loadCampaigns() {
    try {
        // For now, we get all campaigns - in a real app, filter by coordinator
        const response = await fetch(`${API_BASE}/campaigns`, {
            headers: {
                'Authorization': getAuthHeader()
            }
        });

        if (response.ok) {
            const campaigns = await response.json();
            const select = document.getElementById('campaign-select');
            
            // Clear existing options except first
            select.innerHTML = '<option value="">Selecciona una campaña</option>';
            
            campaigns.forEach(campaign => {
                const option = document.createElement('option');
                option.value = campaign.id;
                option.textContent = `${campaign.name} (${campaign.startDate} - ${campaign.endDate})`;
                option.dataset.startDate = campaign.startDate;
                option.dataset.endDate = campaign.endDate;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading campaigns:', error);
    }
}

// Load stores for a campaign
async function loadStoresForCampaign(campaignId) {
    try {
        // Use /api/campaigns/{id}/stores endpoint to get stores for the selected campaign
        const response = await fetch(`${API_BASE}/campaigns/${campaignId}/stores`, {
            headers: {
                'Authorization': getAuthHeader()
            }
        });

        if (response.ok) {
            const stores = await response.json();
            const select = document.getElementById('store-select');
            
            // Clear existing options
            select.innerHTML = '<option value="">Selecciona una tienda</option>';
            
            if (stores.length === 0) {
                select.innerHTML = '<option value="">No hay tiendas en esta campaña</option>';
                select.disabled = true;
            } else {
                stores.forEach(store => {
                    const option = document.createElement('option');
                    option.value = store.id;
                    option.textContent = store.name;
                    select.appendChild(option);
                });
                select.disabled = false;
            }
        }
    } catch (error) {
        console.error('Error loading stores:', error);
    }
}

// Load shifts for a campaign
async function loadShifts(campaignId) {
    const container = document.getElementById('shifts-container');
    
    if (!campaignId) {
        container.innerHTML = '<p class="empty-message">Selecciona una campaña para ver los turnos.</p>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/shifts?campaignId=${campaignId}`, {
            headers: {
                'Authorization': getAuthHeader()
            }
        });

        if (response.ok) {
            const shifts = await response.json();
            
            if (shifts.length === 0) {
                container.innerHTML = '<p class="empty-message">No hay turnos creados para esta campaña.</p>';
                return;
            }

            container.innerHTML = shifts.map(shift => `
                <div class="shift-item">
                    <div class="shift-info">
                        <h4>📅 ${shift.day}</h4>
                        <p>🏪 ${shift.storeName}</p>
                        <div class="shift-details">
                            <span class="shift-detail">🕐 ${shift.startTime} - ${shift.endTime}</span>
                            <span class="shift-detail">👥 ${shift.volunteersNeeded} voluntarios</span>
                        </div>
                        ${shift.location ? `<p class="shift-detail">📍 ${shift.location}</p>` : ''}
                        ${shift.observations ? `<p class="shift-detail">📝 ${shift.observations}</p>` : ''}
                    </div>
                </div>
            `).join('');
        } else if (response.status === 403) {
            container.innerHTML = '<p class="empty-message">No tienes permiso para ver los turnos.</p>';
        } else {
            container.innerHTML = '<p class="empty-message">Error al cargar los turnos.</p>';
        }
    } catch (error) {
        console.error('Error loading shifts:', error);
        container.innerHTML = '<p class="empty-message">Error al cargar los turnos.</p>';
    }
}

// Create a new shift
async function createShift(formData) {
    try {
        const response = await fetch(`${API_BASE}/shifts`, {
            method: 'POST',
            headers: {
                'Authorization': getAuthHeader(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (response.ok) {
            showFormMessage('Turno creado correctamente', false);
            // Refresh shifts list
            const campaignId = document.getElementById('campaign-select').value;
            if (campaignId) {
                loadShifts(campaignId);
            }
            // Reset form
            document.getElementById('shift-form').reset();
        } else {
            showFormMessage(data.message || 'Error al crear el turno', true);
        }
    } catch (error) {
        console.error('Error creating shift:', error);
        showFormMessage('Error de conexión. Inténtalo de nuevo.', true);
    }
}

// Validate time consistency
function validateTimes(startTime, endTime) {
    if (startTime >= endTime) {
        return 'La hora de inicio debe ser anterior a la hora de fin';
    }
    return null;
}

// Validate date is within campaign range
function validateDate(dateStr, startDate, endDate) {
    const date = new Date(dateStr);
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    // Reset time part for comparison
    date.setHours(0, 0, 0, 0);
    start.setHours(0, 0, 0, 0);
    end.setHours(0, 0, 0, 0);
    
    if (date < start) {
        return `La fecha debe ser igual o posterior al ${startDate}`;
    }
    if (date > end) {
        return `La fecha debe ser igual o anterior al ${endDate}`;
    }
    return null;
}

// Initialize the page
document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    if (!localStorage.getItem('token')) {
        window.location.href = 'login.html';
        return;
    }

    // Check role
    const role = localStorage.getItem('role');
    if (role !== 'COORDINADOR') {
        window.location.href = 'login.html';
        return;
    }

    // Load user profile
    loadProfile();

    // Load campaigns
    loadCampaigns();

    // Campaign selection change
    const campaignSelect = document.getElementById('campaign-select');
    campaignSelect.addEventListener('change', (e) => {
        const campaignId = e.target.value;
        const storeSelect = document.getElementById('store-select');
        const dayInput = document.getElementById('shift-day');
        
        if (campaignId) {
            // Load stores for the campaign
            loadStoresForCampaign(campaignId);
            
            // Set date constraints
            const selectedOption = e.target.options[e.target.selectedIndex];
            const startDate = selectedOption.dataset.startDate;
            const endDate = selectedOption.dataset.endDate;
            
            dayInput.min = startDate;
            dayInput.max = endDate;
            
            // Load shifts for this campaign
            loadShifts(campaignId);
        } else {
            storeSelect.innerHTML = '<option value="">Selecciona una tienda</option>';
            storeSelect.disabled = true;
            dayInput.removeAttribute('min');
            dayInput.removeAttribute('max');
            
            document.getElementById('shifts-container').innerHTML = 
                '<p class="empty-message">Selecciona una campaña para ver los turnos.</p>';
        }
    });

    // Refresh button
    const btnRefresh = document.getElementById('btn-refresh-shifts');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => {
            const campaignId = campaignSelect.value;
            if (campaignId) {
                loadShifts(campaignId);
            }
        });
    }

    // Form submission
    const shiftForm = document.getElementById('shift-form');
    const campaignSelectInForm = document.getElementById('campaign-select');
    
    shiftForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const campaignId = document.getElementById('campaign-select').value;
        const storeId = document.getElementById('store-select').value;
        const day = document.getElementById('shift-day').value;
        const startTime = document.getElementById('start-time').value;
        const endTime = document.getElementById('end-time').value;
        const volunteersNeeded = parseInt(document.getElementById('volunteers-needed').value);
        const location = document.getElementById('location').value;
        const observations = document.getElementById('observations').value;

        // Validate times
        const timeError = validateTimes(startTime, endTime);
        if (timeError) {
            showFormMessage(timeError, true);
            return;
        }

        // Validate date is within campaign
        const selectedOption = campaignSelectInForm.options[campaignSelectInForm.selectedIndex];
        const startDate = selectedOption.dataset.startDate;
        const endDate = selectedOption.dataset.endDate;
        
        const dateError = validateDate(day, startDate, endDate);
        if (dateError) {
            showFormMessage(dateError, true);
            return;
        }

        // Create shift
        const formData = {
            campaignId: parseInt(campaignId),
            storeId: parseInt(storeId),
            day: day,
            startTime: startTime,
            endTime: endTime,
            volunteersNeeded: volunteersNeeded,
            location: location || null,
            observations: observations || null
        };

        createShift(formData);
    });

    // Logout button
    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = 'login.html';
        });
    }

    // Edit profile button
    const btnEdit = document.getElementById('btn-edit');
    if (btnEdit) {
        btnEdit.addEventListener('click', () => {
            window.location.href = 'edit.html';
        });
    }

    // Profile modal
    const profileModal = document.getElementById('profile-modal');
    const closeProfileModal = document.getElementById('close-profile-modal');
    
    if (closeProfileModal) {
        closeProfileModal.addEventListener('click', () => {
            profileModal.classList.add('hidden');
        });
    }

    // Profile form submission
    const profileForm = document.getElementById('profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const formData = {
                nombre: document.getElementById('profile-name').value,
                telefono: document.getElementById('profile-phone').value,
                domicilio: document.getElementById('profile-address').value
            };

            try {
                const response = await fetch(`${API_BASE}/auth/profile`, {
                    method: 'PUT',
                    headers: {
                        'Authorization': getAuthHeader(),
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formData)
                });

                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('nombre', data.nombre);
                    document.getElementById('user-name').textContent = data.nombre;
                    document.getElementById('welcome-name').textContent = data.nombre;
                    profileModal.classList.add('hidden');
                }
            } catch (error) {
                console.error('Error updating profile:', error);
            }
        });
    }
});