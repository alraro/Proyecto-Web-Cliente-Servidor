const BACKEND = 'http://localhost:8080';

function getToken()   { return localStorage.getItem('token'); }
function getUser()    { return localStorage.getItem('nombre') || 'Responsable'; }
function getStoreId() { return localStorage.getItem('storeId'); }
function authHeaders() { return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() }; }
function logout() { localStorage.clear(); window.location.href = 'login.html'; }


document.addEventListener('DOMContentLoaded', () => {
    if (!getToken() || localStorage.getItem('role') !== 'RESPONSABLE_TIENDA') {
        window.location.href = 'login.html';
        return;
    }

    const userNameEl = document.getElementById('user-name');
	
    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Responsable';
    }

    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
            
        } else if(e.target.id === 'btn-logout'){
            logout();
        }
    })
});

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

    const infoGrid = document.getElementById('info-grid');
    infoGrid.innerHTML = '';
    fields.forEach(f => {
        const item = document.createElement('div');
        item.className = 'info-item';

        const label = document.createElement('label');
        label.textContent = f.label;
        item.appendChild(label);

        const span = document.createElement('span');
        span.textContent = f.value || '\u2014';
        item.appendChild(span);

        infoGrid.appendChild(item);
    });
}

function renderShifts(shifts) {
    document.getElementById('card-turnos').classList.remove('hidden');
    const tbody = document.getElementById('shifts-tbody');

    if (!shifts || !shifts.length) {
        tbody.innerHTML = '';
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 5;
        td.className = 'table-empty';
        td.textContent = 'No hay turnos programados.';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    tbody.innerHTML = '';
    shifts.forEach(s => {
        const tr = document.createElement('tr');

        const td0 = document.createElement('td');
        td0.textContent = s.campaignName || '\u2014';
        tr.appendChild(td0);

        const td1 = document.createElement('td');
        td1.textContent = s.volunteerName || '\u2014';
        tr.appendChild(td1);

        const td2 = document.createElement('td');
        td2.textContent = s.endTime || '\u2014';
        tr.appendChild(td2);

        const td3 = document.createElement('td');
        let attendanceBadge;
        if (s.attendance === true) {
            attendanceBadge = document.createElement('span');
            attendanceBadge.className = 'badge-attendance badge-yes';
            attendanceBadge.textContent = '\u2713 S\u00ed';
        } else if (s.attendance === false) {
            attendanceBadge = document.createElement('span');
            attendanceBadge.className = 'badge-attendance badge-no';
            attendanceBadge.textContent = '\u2717 No';
        } else {
            attendanceBadge = document.createElement('span');
            attendanceBadge.className = 'badge-attendance badge-pending';
            attendanceBadge.textContent = 'Pendiente';
        }
        td3.appendChild(attendanceBadge);
        tr.appendChild(td3);

        const td4 = document.createElement('td');
        td4.textContent = s.notes || '\u2014';
        tr.appendChild(td4);

        tbody.appendChild(tr);
    });
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