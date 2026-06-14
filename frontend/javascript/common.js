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

function requireAuth(expectedRole) {
    const token = getToken();
    const role = getUserRole();
    if (!token || (expectedRole && role !== expectedRole)) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function handleUrlTokenParams() {
    const params = new URLSearchParams(window.location.search);
    const tokenFromQuery = params.get('token');
    const nameFromQuery = params.get('nombre');
    if (tokenFromQuery) sessionStorage.setItem('token', tokenFromQuery);
    if (nameFromQuery) sessionStorage.setItem('nombre', nameFromQuery);
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

function showToast(msg, type = 'success', containerId = 'toast-container') {
    const container = document.querySelector(`#${containerId}`);
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

function escapeAttr(value) {
    return String(value ?? '').replace(/'/g, "\\'");
}