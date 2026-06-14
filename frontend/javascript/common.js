const API_BASE = 'http://localhost:8080';

function getToken() {
    return sessionStorage.getItem('token');
}

function getUserName() {
    return sessionStorage.getItem('nombre') || 'Usuario';
}

function getUserRole() {
    return sessionStorage.getItem('role');
}

function getStoreId() {
    return sessionStorage.getItem('storeId');
}

function authHeaders(token = getToken()) {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    };
}

function logout() {
    sessionStorage.clear();
    window.location.href = 'login.html';
}

function handleUrlTokenParams() {
    const params = new URLSearchParams(window.location.search);
    const tokenFromQuery = params.get('token');
    const nameFromQuery = params.get('nombre');
    if (tokenFromQuery) sessionStorage.setItem('token', tokenFromQuery);
    if (nameFromQuery) sessionStorage.setItem('nombre', nameFromQuery);
}

function requireAuth(expectedRole) {
    const token = getToken();
    const role = getUserRole();
    if (!token || (expectedRole && role !== expectedRole)) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function setWelcomeName(elementId, defaultName) {
    const el = document.querySelector(`#${elementId}`);
    if (el) el.textContent = getUserName() || defaultName;
}

function setUserNameDisplay() {
    const userNameEl = document.querySelector('#user-name');
    if (userNameEl) {
        userNameEl.textContent = `${getUserName()} (${getUserRole() || 'Usuario'})`;
    }
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escapeAttr(value) {
    return String(value ?? '').replace(/'/g, "\\'");
}

function showMessage(text, isError) {
    const el = document.querySelector('#global-message');
    if (!el) return;
    el.hidden = false;
    el.textContent = text;
    el.className = isError ? 'error' : 'success';
    clearTimeout(showMessage._t);
    showMessage._t = setTimeout(function() { el.hidden = true; }, 4000);
}

function showToast(msg, type = 'success', containerId = 'toast-container') {
    const container = document.querySelector(`#${containerId}`);
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

async function apiFetch(url, options = {}) {
    const token = getToken();
    const res = await fetch(`${API_BASE}${url}`, {
        ...options,
        headers: { ...authHeaders(token), ...options.headers }
    });
    if (res.status === 401 || res.status === 403) {
        logout();
        throw new Error('Sesión expirada');
    }
    if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || `Error ${res.status}`);
    }
    return res.json();
}

function setupCommonButtonHandlers() {
    document.addEventListener('click', (e) => {
        if (e.target.id === 'btn-edit') {
            window.location.href = 'edit.html';
        } else if (e.target.id === 'btn-logout') {
            logout();
        }
    });
}

function attachTokenToLinks() {
    const token = getToken();
    const nombre = getUserName();
    if (token) {
        document.querySelectorAll('a[href]').forEach((link) => {
            try {
                const targetUrl = new URL(link.href);
                if (targetUrl.origin === window.location.origin) {
                    targetUrl.searchParams.set('token', token);
                    if (nombre) {
                        targetUrl.searchParams.set('nombre', nombre);
                    }
                    link.href = targetUrl.toString();
                }
            } catch (_) {}
        });
    }
}