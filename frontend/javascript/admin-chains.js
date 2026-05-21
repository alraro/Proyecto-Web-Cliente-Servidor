const BACKEND = 'http://localhost:8080';

function getToken() { return localStorage.getItem('token'); }
function authHeaders() {
    return { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() };
}
function logout() { localStorage.clear(); window.location.href = 'login.html'; }

document.addEventListener('DOMContentLoaded', () => {
    if (!getToken() || localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }
});


function showToast(msg, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

function renderTable(chains) {
    const tbody = document.getElementById('chains-tbody');
    tbody.innerHTML = '';
    if (!chains.length) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 5;
        td.className = 'table-empty';
        td.textContent = 'No hay cadenas registradas.';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }
    chains.forEach(c => {
        const tr = document.createElement('tr');

        const td1 = document.createElement('td');
        td1.textContent = c.id;
        tr.appendChild(td1);

        const td2 = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = escHtml(c.name);
        td2.appendChild(strong);
        tr.appendChild(td2);

        const td3 = document.createElement('td');
        const code = document.createElement('code');
        code.className = 'inline-code';
        code.textContent = escHtml(c.code);
        td3.appendChild(code);
        tr.appendChild(td3);

        const td4 = document.createElement('td');
        const badge = document.createElement('span');
        if (c.participation) {
            badge.className = 'badge badge-yes';
            badge.textContent = '✓ Sí';
        } else {
            badge.className = 'badge badge-no';
            badge.textContent = '— No';
        }
        td4.appendChild(badge);
        tr.appendChild(td4);

        const td5 = document.createElement('td');
        const div = document.createElement('div');
        div.className = 'td-actions';

        const btnEdit = document.createElement('button');
        btnEdit.className = 'btn btn-edit btn-sm';
        btnEdit.setAttribute('data-action', 'edit');
        btnEdit.setAttribute('data-chain-id', c.id);
        btnEdit.textContent = 'Editar';
        div.appendChild(btnEdit);

        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-danger btn-sm';
        btnDelete.setAttribute('data-action', 'delete');
        btnDelete.setAttribute('data-chain-id', c.id);
        btnDelete.setAttribute('data-chain-name', escAttr(c.name));
        btnDelete.textContent = 'Eliminar';
        div.appendChild(btnDelete);

        td5.appendChild(div);
        tr.appendChild(td5);

        tbody.appendChild(tr);
    });
}

function escHtml(v) {
    return String(v ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
function escAttr(v) {
    return String(v ?? '').replace(/'/g, "\\'");
}

async function loadChains() {
    try {
        const res = await fetch(BACKEND + '/api/chains', { headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (!res.ok) {
            const tbodyErr = document.getElementById('chains-tbody');
            tbodyErr.innerHTML = '';
            const trErr = document.createElement('tr');
            const tdErr = document.createElement('td');
            tdErr.colSpan = 5;
            tdErr.className = 'table-empty';
            tdErr.textContent = 'Error al cargar las cadenas.';
            trErr.appendChild(tdErr);
            tbodyErr.appendChild(trErr);
            return;
        }
        const data = await res.json();
        renderTable(data);
    } catch {
        const tbodyErr = document.getElementById('chains-tbody');
        tbodyErr.innerHTML = '';
        const trErr = document.createElement('tr');
        const tdErr = document.createElement('td');
        tdErr.colSpan = 5;
        tdErr.className = 'table-empty';
        tdErr.textContent = 'No se puede conectar con el servidor. ¿Está el backend en marcha?';
        trErr.appendChild(tdErr);
        tbodyErr.appendChild(trErr);
    }
}

let editingId = null;

function openModal(titulo) {
    document.getElementById('modal-title').textContent = titulo;
    document.getElementById('modal-error').textContent = '';
    document.getElementById('modal-backdrop').classList.add('open');
    document.getElementById('input-nombre').focus();
}

function closeModal() {
    document.getElementById('modal-backdrop').classList.remove('open');
    document.getElementById('input-nombre').value = '';
    document.getElementById('input-codigo').value = '';
    document.getElementById('input-participacion').checked = false;
    document.getElementById('modal-error').textContent = '';
    editingId = null;
}

function openCreate() {
    editingId = null;
    openModal('Nueva cadena');
}

async function openEdit(id) {
    try {
        const res = await fetch(BACKEND + '/api/chains/' + id, { headers: authHeaders() });
        if (!res.ok) { showToast('Error al cargar la cadena.', 'error'); return; }
        const cadena = await res.json();

        editingId = cadena.id;
        document.getElementById('input-nombre').value = cadena.name;
        document.getElementById('input-codigo').value = cadena.code;
        document.getElementById('input-participacion').checked = !!cadena.participation;
        openModal('Editar cadena');
    } catch {
        showToast('Error al cargar la cadena.', 'error');
    }
}

document.getElementById('btn-nueva').addEventListener('click', openCreate);
document.getElementById('btn-cancelar').addEventListener('click', closeModal);
document.getElementById('modal-backdrop').addEventListener('click', e => {
    if (e.target === document.getElementById('modal-backdrop')) closeModal();
});

// Export button
document.getElementById('btn-export-chains').addEventListener('click', function () {
    exportarExcel('chains');
});

// Event delegation for table action buttons (edit / delete)
document.getElementById('chains-tbody').addEventListener('click', function (e) {
    const button = e.target.closest('button');
    if (!button) return;
    const action = button.getAttribute('data-action');
    const chainId = button.getAttribute('data-chain-id');
    if (action === 'edit') {
        openEdit(parseInt(chainId));
    } else if (action === 'delete') {
        const chainName = button.getAttribute('data-chain-name');
        deleteChain(parseInt(chainId), chainName);
    }
});

document.getElementById('btn-guardar').addEventListener('click', async () => {
    const nombre = document.getElementById('input-nombre').value.trim();
    const codigo = document.getElementById('input-codigo').value.trim();
    const participacion = document.getElementById('input-participacion').checked;
    const errorEl = document.getElementById('modal-error');

    if (!nombre) { errorEl.textContent = 'El nombre es obligatorio.'; return; }
    if (!codigo) { errorEl.textContent = 'El código es obligatorio.'; return; }
    if (!/^[A-Za-z0-9_\-]+$/.test(codigo)) {
        errorEl.textContent = 'El código solo puede contener letras, números, guiones y guiones bajos.';
        return;
    }
    if (nombre.length > 255) { errorEl.textContent = 'El nombre no puede superar 255 caracteres.'; return; }
    if (codigo.length > 50) { errorEl.textContent = 'El código no puede superar 50 caracteres.'; return; }

    const body = JSON.stringify({ name: nombre, code: codigo, participation: participacion });
    const url = editingId ? `${BACKEND}/api/chains/${editingId}` : `${BACKEND}/api/chains`;
    const method = editingId ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, { method, headers: authHeaders(), body });
        const data = await res.json();

        if (!res.ok) {
            errorEl.textContent = data.message || 'Error al guardar.';
            return;
        }

        closeModal();
        showToast(editingId ? 'Cadena actualizada correctamente.' : 'Cadena creada correctamente.');
        loadChains();
    } catch {
        errorEl.textContent = 'Error de conexión con el servidor.';
    }
});

async function deleteChain(id, nombre) {
    if (!confirm(`¿Eliminar la cadena "${nombre}"?\nEsta acción no se puede deshacer.`)) return;
    try {
        const res = await fetch(`${BACKEND}/api/chains/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        if (!res.ok) {
            const data = await res.json();
            showToast(data.message || 'Error al eliminar.', 'error');
            return;
        }
        showToast('Cadena eliminada.');
        loadChains();
    } catch {
        showToast('Error de conexión con el servidor.', 'error');
    }
}

loadChains();
