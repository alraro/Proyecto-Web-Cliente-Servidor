const BACKEND = 'http://localhost:8080';

function getToken()   { return localStorage.getItem('token'); }
function getUser()    { return localStorage.getItem('nombre') || 'Responsable'; }
function getStoreId() { return localStorage.getItem('storeId'); }
function authHeaders() { return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() }; }
function logout() { localStorage.clear(); window.location.href = 'login.html'; }

if (!getToken()) { window.location.href = 'login.html'; }
document.getElementById('user-name').textContent = getUser();
document.getElementById('btn-logout').addEventListener('click', logout);

function showError(msg) {
    const el = document.getElementById('error-msg');
    el.textContent = msg;
    el.classList.remove('hidden');
}

function renderStoreInfo(store) {
    document.getElementById('store-title').textContent = store.name || 'Tienda';
    document.getElementById('card-tienda').classList.remove('hidden');

    const fields = [
        { label: 'Nombre',        value: store.name       },
        { label: 'Domicilio',     value: store.address    },
        { label: 'Código postal', value: store.postalCode },
        { label: 'Localidad',     value: store.locality   },
        { label: 'Zona geog.',    value: store.zone       },
        { label: 'Cadena',        value: store.chainName  },
    ];

    document.getElementById('info-grid').innerHTML = fields.map(f => `
        <div class="info-item">
            <label>${f.label}</label>
            <span>${f.value || '—'}</span>
        </div>
    `).join('');
}

function renderShifts(shifts) {
    document.getElementById('card-turnos').classList.remove('hidden');
    const tbody = document.getElementById('shifts-tbody');

    if (!shifts || !shifts.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay turnos programados.</td></tr>';
        return;
    }

    tbody.innerHTML = shifts.map(s => {
        let attendanceBadge;
        if (s.attendance === true)      attendanceBadge = '<span class="badge-attendance badge-yes">✓ Sí</span>';
        else if (s.attendance === false) attendanceBadge = '<span class="badge-attendance badge-no">✗ No</span>';
        else                            attendanceBadge = '<span class="badge-attendance badge-pending">Pendiente</span>';

        return `
            <tr>
                <td>${s.campaignName || '—'}</td>
                <td>${s.volunteerName || '—'}</td>
                <td>${s.endTime || '—'}</td>
                <td>${attendanceBadge}</td>
                <td>${s.notes || '—'}</td>
            </tr>
        `;
    }).join('');
}

async function loadStoreDetail() {
    const storeId = getStoreId();
    if (!storeId) {
        showError('No tienes ninguna tienda asignada. Contacta con el administrador.');
        return;
    }

    try {
        const res = await fetch(`${BACKEND}/api/stores/${storeId}/detail`, {
            headers: authHeaders()
        });

        if (res.status === 401) { logout(); return; }
        if (res.status === 403) { showError('No tienes permiso para ver esta tienda.'); return; }
        if (res.status === 404) { showError('Tienda no encontrada.'); return; }
        if (!res.ok)            { showError('Error al cargar la información de la tienda.'); return; }

        const data = await res.json();
        renderStoreInfo(data);
        renderShifts(data.scheduledShifts);

    } catch {
        showError('Error de conexión con el servidor.');
    }
}

loadStoreDetail();