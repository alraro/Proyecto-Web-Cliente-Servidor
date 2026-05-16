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

    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
            
        } else if(e.target.id === 'btn-logout'){
            logout();
        }
    })
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
    if (!chains.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay cadenas registradas.</td></tr>';
        return;
    }
    tbody.innerHTML = chains.map(c => `
        <tr>
            <td>${c.id}</td>
            <td><strong>${escHtml(c.name)}</strong></td>
            <td><code class="inline-code">${escHtml(c.code)}</code></td>
            <td>
                ${c.participation
            ? '<span class="badge badge-yes">✓ Sí</span>'
            : '<span class="badge badge-no">— No</span>'}
            </td>
            <td>
                <div class="td-actions">
                    <button class="btn btn-edit btn-sm" onclick="openEdit(${c.id})">Editar</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteChain(${c.id}, '${escAttr(c.name)}')">Eliminar</button>
                </div>
            </td>
        </tr>
    `).join('');
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
            document.getElementById('chains-tbody').innerHTML =
                '<tr><td colspan="5" class="table-empty">Error al cargar las cadenas.</td></tr>';
            return;
        }
        const data = await res.json();
        renderTable(data);
    } catch {
        document.getElementById('chains-tbody').innerHTML =
            '<tr><td colspan="5" class="table-empty">No se puede conectar con el servidor. ¿Está el backend en marcha?</td></tr>';
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
