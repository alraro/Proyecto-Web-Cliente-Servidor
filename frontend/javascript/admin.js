const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token') || localStorage.getItem('role') !== 'ADMINISTRADOR') {
        window.location.href = 'login.html';
        return;
    }


    const token = localStorage.getItem('token');
    const nombre = localStorage.getItem('nombre') || 'Administrador';
    const welcomeNameEl = document.querySelector('#welcome-name');

    if (welcomeNameEl) welcomeNameEl.textContent = nombre;

    if (token) {
        const backendLinks = document.querySelectorAll(`a.menu-card[href^="${API_BASE}/admin-"]`);
        backendLinks.forEach((link) => {
            try {
                const targetUrl = new URL(link.href);
                targetUrl.searchParams.set('token', token);
                if (nombre) {
                    targetUrl.searchParams.set('nombre', nombre);
                }
                link.href = targetUrl.toString();
            } catch (_) {
                // Si la URL no es válida, no hacemos nada
            }
        });
    }
});