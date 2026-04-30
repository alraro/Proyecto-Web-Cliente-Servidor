// Coordinator Panel JavaScript

const API_BASE = 'http://localhost:8080/api';

function getAuthHeader() {
    const token = localStorage.getItem('token');
    return token ? `Bearer ${token}` : null;
}

// Show message inside the shift creation form
function showFormMessage(message, isError = true) {
    const el = document.getElementById('form-message');
    if (el) {
        el.textContent = message;
        el.className = `form-message ${isError ? 'error' : 'success'}`;
        setTimeout(() => { el.className = 'form-message'; }, 5000);
    }
}

// Show message inside the volunteer assignment form
function showAssignmentMessage(message, isError = true) {
    const el = document.getElementById('assignment-message');
    if (el) {
        el.textContent = message;
        el.className = `form-message ${isError ? 'error' : 'success'}`;
        setTimeout(() => { el.className = 'form-message'; }, 6000);
    }
}

// Show prominent alert at top of page (for overlap / capacity warnings)
function showAlert(message, type = 'warning') {
    const container = document.getElementById('alert-container');
    if (!container) return;

    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;

    const icons = { warning: '⚠️', error: '❌', success: '✅' };
    alert.textContent = `${icons[type] || ''} ${message}`;

    container.innerHTML = '';
    container.appendChild(alert);

    setTimeout(() => { if (alert.parentNode) alert.remove(); }, 8000);
}

// ─── Profile ─────────────────────────────────────────────────────────────────

async function loadProfile() {
    try {
        const response = await fetch(`${API_BASE}/auth/profile`, {
            headers: { 'Authorization': getAuthHeader() }
        });
        if (!response.ok) return;
        const data = await response.json();

        const set = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
        const setVal = (id, val) => { const el = document.getElementById(id); if (el) el.value = val; };

        set('user-name', data.nombre || 'Coordinador');
        set('welcome-name', data.nombre || 'Coordinador');
        setVal('profile-name', data.nombre || '');
        setVal('profile-email', data.email || '');
        setVal('profile-phone', data.telefono || '');
        setVal('profile-address', data.domicilio || '');
    } catch (error) {
        console.error('Error loading profile:', error);
    }
}

// ─── Campaigns ───────────────────────────────────────────────────────────────

async function loadCampaigns() {
    try {
        const response = await fetch(`${API_BASE}/campaigns`, {
            headers: { 'Authorization': getAuthHeader() }
        });
        if (!response.ok) return;

        const data = await response.json();
        const campaigns = data.content || [];
        const select = document.getElementById('campaign-select');
        if (!select) return;

        select.innerHTML = '<option value="">Selecciona una campaña</option>';
        campaigns.forEach(campaign => {
            const option = document.createElement('option');
            option.value = campaign.id;
            option.textContent = `${campaign.name} (${campaign.startDate} - ${campaign.endDate})`;
            option.dataset.startDate = campaign.startDate;
            option.dataset.endDate = campaign.endDate;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading campaigns:', error);
    }
}

// ─── Stores ──────────────────────────────────────────────────────────────────

async function loadStoresForCampaign(campaignId) {
    try {
        const response = await fetch(`${API_BASE}/campaigns/${campaignId}/stores`, {
            headers: { 'Authorization': getAuthHeader() }
        });

        const select = document.getElementById('store-select');
        if (!select) return;

        if (!response.ok) {
            select.innerHTML = '<option value="">Error al cargar tiendas</option>';
            select.disabled = true;
            return;
        }

        const data = await response.json();
        // Backend devuelve { campaignId, campaignName, totalStores, stores: [...] }
        const stores = Array.isArray(data) ? data : (data.stores || []);

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
    } catch (error) {
        console.error('Error loading stores:', error);
    }
}

// ─── Volunteers ──────────────────────────────────────────────────────────────

async function loadVolunteers(campaignId) {
    const select = document.getElementById('volunteer-select');
    if (!select) return;

    select.innerHTML = '<option value="">Cargando voluntarios...</option>';
    select.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/shifts/volunteers?campaignId=${campaignId}`, {
            headers: { 'Authorization': getAuthHeader() }
        });

        if (!response.ok) {
            select.innerHTML = '<option value="">Sin acceso a voluntarios</option>';
            return;
        }

        const volunteers = await response.json();
        select.innerHTML = '<option value="">Seleccionar voluntario...</option>';

        if (!volunteers || volunteers.length === 0) {
            select.innerHTML = '<option value="">No hay voluntarios disponibles</option>';
            return;
        }

        volunteers.forEach(v => {
            const option = document.createElement('option');
            option.value = v.id;
            const label = v.email ? `${v.name} (${v.email})` : v.name;
            option.textContent = label;
            select.appendChild(option);
        });

        select.disabled = false;
    } catch (error) {
        console.error('Error loading volunteers:', error);
        select.innerHTML = '<option value="">Error al cargar voluntarios</option>';
    }
}

// ─── Shifts ──────────────────────────────────────────────────────────────────

// Stores the current shift list so the assignment form can reference shift data
let currentShifts = [];

async function loadShifts(campaignId) {
    const container = document.getElementById('shifts-container');
    if (!campaignId) {
        container.innerHTML = '<p class="empty-message">Selecciona una campaña para ver los turnos.</p>';
        currentShifts = [];
        populateShiftSelector([]);
        return;
    }

    try {
        // FIX: correct endpoint is /api/coordinator/shifts (not /api/shifts)
        const response = await fetch(`${API_BASE}/coordinator/shifts?campaignId=${campaignId}`, {
            headers: { 'Authorization': getAuthHeader() }
        });

        if (response.ok) {
            const shifts = await response.json();
            currentShifts = shifts;
            populateShiftSelector(shifts);

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
                            <span class="shift-detail">👥 ${shift.volunteersNeeded} voluntarios necesarios</span>
                        </div>
                        ${shift.location ? `<p class="shift-detail">📍 ${shift.location}</p>` : ''}
                        ${shift.observations ? `<p class="shift-detail">📝 ${shift.observations}</p>` : ''}
                    </div>
                </div>
            `).join('');
        } else if (response.status === 403) {
            container.innerHTML = '<p class="empty-message">No tienes permiso para ver los turnos de esta campaña.</p>';
        } else {
            container.innerHTML = '<p class="empty-message">Error al cargar los turnos.</p>';
        }
    } catch (error) {
        console.error('Error loading shifts:', error);
        container.innerHTML = '<p class="empty-message">Error al cargar los turnos.</p>';
    }
}

// Fills the shift selector in the volunteer assignment section
function populateShiftSelector(shifts) {
    const select = document.getElementById('shift-assign-select');
    if (!select) return;

    select.innerHTML = '<option value="">Selecciona un turno</option>';
    const countEl = document.getElementById('current-volunteer-count');
    if (countEl) { countEl.textContent = '-- / --'; countEl.className = 'count-display'; }

    if (!shifts || shifts.length === 0) {
        select.innerHTML = '<option value="">No hay turnos disponibles</option>';
        select.disabled = true;
        return;
    }

    shifts.forEach(shift => {
        const option = document.createElement('option');
        option.value = shift.shiftId;
        option.textContent = `${shift.day} | ${shift.storeName} | ${shift.startTime}–${shift.endTime}`;
        option.dataset.storeId = shift.storeId;
        option.dataset.campaignId = shift.campaignId;
        option.dataset.day = shift.day;
        option.dataset.startTime = shift.startTime;
        option.dataset.endTime = shift.endTime;
        option.dataset.volunteersNeeded = shift.volunteersNeeded;
        select.appendChild(option);
    });

    select.disabled = false;
}

// Updates the capacity counter when a shift is selected
async function updateCapacityDisplay(campaignId, storeId, shiftDay, startTime, volunteersNeeded) {
    const countEl = document.getElementById('current-volunteer-count');
    if (!countEl) return;

    try {
        const params = new URLSearchParams({ campaignId, storeId, shiftDay, startTime });
        const response = await fetch(`${API_BASE}/shifts/count?${params}`, {
            headers: { 'Authorization': getAuthHeader() }
        });

        if (response.ok) {
            const count = await response.json();
            countEl.textContent = `${count} / ${volunteersNeeded}`;
            if (count >= volunteersNeeded) {
                countEl.classList.add('over-capacity');
            } else {
                countEl.classList.remove('over-capacity');
            }
        } else {
            countEl.textContent = `-- / ${volunteersNeeded}`;
        }
    } catch {
        countEl.textContent = `-- / ${volunteersNeeded}`;
    }
}

// ─── Create shift ─────────────────────────────────────────────────────────────

async function createShift(formData) {
    try {
        // FIX: correct endpoint is /api/coordinator/shifts (not /api/shifts)
        const response = await fetch(`${API_BASE}/coordinator/shifts`, {
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
            const campaignId = document.getElementById('campaign-select').value;
            if (campaignId) loadShifts(campaignId);
            document.getElementById('shift-form').reset();
            // Re-disable store select after reset
            const storeSelect = document.getElementById('store-select');
            if (storeSelect) {
                storeSelect.innerHTML = '<option value="">Selecciona una tienda</option>';
                storeSelect.disabled = true;
            }
        } else {
            showFormMessage(data.message || 'Error al crear el turno', true);
        }
    } catch (error) {
        console.error('Error creating shift:', error);
        showFormMessage('Error de conexión. Inténtalo de nuevo.', true);
    }
}

// ─── Validations ─────────────────────────────────────────────────────────────

function validateTimes(startTime, endTime) {
    if (startTime >= endTime) return 'La hora de inicio debe ser anterior a la hora de fin';
    return null;
}

function validateDate(dateStr, startDate, endDate) {
    const date = new Date(dateStr);
    const start = new Date(startDate);
    const end = new Date(endDate);
    date.setHours(0, 0, 0, 0);
    start.setHours(0, 0, 0, 0);
    end.setHours(0, 0, 0, 0);
    if (date < start) return `La fecha debe ser igual o posterior al ${startDate}`;
    if (date > end) return `La fecha debe ser igual o anterior al ${endDate}`;
    return null;
}

// ─── Init ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) {
        window.location.href = 'login.html';
        return;
    }
    if (localStorage.getItem('role') !== 'COORDINADOR') {
        window.location.href = 'login.html';
        return;
    }

    loadProfile();
    loadCampaigns();

    // ── Campaign selection ────────────────────────────────────────────────────
    const campaignSelect = document.getElementById('campaign-select');
    campaignSelect.addEventListener('change', (e) => {
        const campaignId = e.target.value;
        const storeSelect = document.getElementById('store-select');
        const dayInput = document.getElementById('shift-day');

        if (campaignId) {
            loadStoresForCampaign(campaignId);

            const selectedOption = e.target.options[e.target.selectedIndex];
            dayInput.min = selectedOption.dataset.startDate;
            dayInput.max = selectedOption.dataset.endDate;

            loadShifts(campaignId);
            loadVolunteers(campaignId);
        } else {
            storeSelect.innerHTML = '<option value="">Selecciona una tienda</option>';
            storeSelect.disabled = true;
            dayInput.removeAttribute('min');
            dayInput.removeAttribute('max');
            document.getElementById('shifts-container').innerHTML =
                '<p class="empty-message">Selecciona una campaña para ver los turnos.</p>';
            populateShiftSelector([]);
            const volSelect = document.getElementById('volunteer-select');
            if (volSelect) {
                volSelect.innerHTML = '<option value="">Seleccionar voluntario...</option>';
                volSelect.disabled = true;
            }
        }
    });

    // ── Shift selector in volunteer form ─────────────────────────────────────
    const shiftAssignSelect = document.getElementById('shift-assign-select');
    if (shiftAssignSelect) {
        shiftAssignSelect.addEventListener('change', (e) => {
            const shiftId = e.target.value;
            if (!shiftId) {
                const countEl = document.getElementById('current-volunteer-count');
                if (countEl) { countEl.textContent = '-- / --'; countEl.className = 'count-display'; }
                return;
            }
            const opt = e.target.options[e.target.selectedIndex];
            updateCapacityDisplay(
                opt.dataset.campaignId,
                opt.dataset.storeId,
                opt.dataset.day,
                opt.dataset.startTime,
                opt.dataset.volunteersNeeded
            );
        });
    }

    // ── Volunteer assignment form ─────────────────────────────────────────────
    const volunteerAssignmentForm = document.getElementById('volunteer-assignment-form');
    if (volunteerAssignmentForm) {
        volunteerAssignmentForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const shiftSelect = document.getElementById('shift-assign-select');
            const volSelect = document.getElementById('volunteer-select');
            const shiftId = shiftSelect ? shiftSelect.value : '';
            const volunteerId = volSelect ? volSelect.value : '';

            if (!shiftId) {
                showAssignmentMessage('Selecciona un turno antes de asignar.', true);
                return;
            }
            if (!volunteerId) {
                showAssignmentMessage('Selecciona un voluntario.', true);
                return;
            }

            const opt = shiftSelect.options[shiftSelect.selectedIndex];
            const campaignId = parseInt(opt.dataset.campaignId);
            const storeId = parseInt(opt.dataset.storeId);
            const shiftDay = opt.dataset.day;
            const startTime = opt.dataset.startTime;
            const endTime = opt.dataset.endTime;

            const requestBody = {
                volunteerId: parseInt(volunteerId),
                campaignId,
                storeId,
                shiftDay,
                startTime,
                endTime
            };

            try {
                // Pass campaignId as query param for coordinator guard check
                const response = await fetch(`${API_BASE}/shifts/assign-volunteer?campaignId=${campaignId}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': getAuthHeader(),
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestBody)
                });

                const data = await response.json();

                if (data.success) {
                    showAssignmentMessage('Voluntario asignado correctamente.', false);
                    // Refresh capacity display
                    updateCapacityDisplay(campaignId, storeId, shiftDay, startTime, opt.dataset.volunteersNeeded);
                } else {
                    // Show prominent alert for overlap and capacity errors
                    if (data.errorType === 'SHIFT_OVERLAP') {
                        showAlert(
                            'Solapamiento de turnos: el voluntario ya tiene un turno asignado que se solapa con este horario.',
                            'warning'
                        );
                    } else if (data.errorType === 'MAX_VOLUNTEERS') {
                        showAlert(
                            `Exceso de voluntarios: ${data.message}`,
                            'warning'
                        );
                    } else {
                        showAssignmentMessage(data.message || 'Error al asignar voluntario.', true);
                    }
                }
            } catch (error) {
                console.error('Error assigning volunteer:', error);
                showAssignmentMessage('Error de conexión al asignar voluntario.', true);
            }
        });
    }

    // ── Shift creation form ───────────────────────────────────────────────────
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
        const observations = document.getElementById('observations') ? document.getElementById('observations').value : null;

        const timeError = validateTimes(startTime, endTime);
        if (timeError) { showFormMessage(timeError, true); return; }

        const selectedOption = campaignSelectInForm.options[campaignSelectInForm.selectedIndex];
        const dateError = validateDate(day, selectedOption.dataset.startDate, selectedOption.dataset.endDate);
        if (dateError) { showFormMessage(dateError, true); return; }

        createShift({
            campaignId: parseInt(campaignId),
            storeId: parseInt(storeId),
            day,
            startTime,
            endTime,
            volunteersNeeded,
            location: location || null,
            observations: observations || null
        });
    });

    // ── Refresh button ────────────────────────────────────────────────────────
    const btnRefresh = document.getElementById('btn-refresh-shifts');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => {
            const campaignId = campaignSelect.value;
            if (campaignId) loadShifts(campaignId);
        });
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = 'login.html';
        });
    }

    // ── Edit profile ──────────────────────────────────────────────────────────
    const btnEdit = document.getElementById('btn-edit');
    if (btnEdit) {
        btnEdit.addEventListener('click', () => { window.location.href = 'edit.html'; });
    }

    // ── Profile modal ─────────────────────────────────────────────────────────
    const profileModal = document.getElementById('profile-modal');
    const closeProfileModal = document.getElementById('close-profile-modal');
    if (closeProfileModal) {
        closeProfileModal.addEventListener('click', () => profileModal.classList.add('hidden'));
    }

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
                    headers: { 'Authorization': getAuthHeader(), 'Content-Type': 'application/json' },
                    body: JSON.stringify(formData)
                });
                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('nombre', data.nombre);
                    const userNameEl = document.getElementById('user-name');
                    const welcomeNameEl = document.getElementById('welcome-name');
                    if (userNameEl) userNameEl.textContent = data.nombre;
                    if (welcomeNameEl) welcomeNameEl.textContent = data.nombre;
                    profileModal.classList.add('hidden');
                }
            } catch (error) {
                console.error('Error updating profile:', error);
            }
        });
    }
});
