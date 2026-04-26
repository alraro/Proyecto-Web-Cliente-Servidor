document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) {
        window.location.href = 'login.html';
        return;
    }

    const userNameEl = document.getElementById('user-name');
    const welcomeNameEl = document.getElementById('welcome-name');
    const btnEdit = document.getElementById('btn-edit');
    const btnLogout = document.getElementById('btn-logout');

    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Administrador';
    }

    if (welcomeNameEl) {
        welcomeNameEl.textContent = localStorage.getItem('nombre') || 'Admin';
    }

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

    const token = localStorage.getItem('token');
    const nombre = localStorage.getItem('nombre') || '';
    if (token) {
        const backendLinks = document.querySelectorAll('a.menu-card[href^="http://localhost:8080/admin-"]');
        backendLinks.forEach((link) => {
            try {
                const targetUrl = new URL(link.href);
                targetUrl.searchParams.set('token', token);
                if (nombre) {
                    targetUrl.searchParams.set('nombre', nombre);
                }
                link.href = targetUrl.toString();
            } catch (_) {
                // Ignore malformed URLs to avoid breaking navigation.
            }
        });
    }
});