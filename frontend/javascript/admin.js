const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) {
        window.location.href = 'login.html';
        return;
    }

    const welcomeNameEl = document.getElementById('welcome-name');

    if (welcomeNameEl) {
        welcomeNameEl.textContent = localStorage.getItem('nombre') || 'Admin';
    }

    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';

        } else if(e.target.id === 'btn-logout'){
            localStorage.clear();
            window.location.href = 'login.html';
        }
    })

    const token = localStorage.getItem('token');
    const nombre = localStorage.getItem('nombre') || '';
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
                // Ignore malformed URLs to avoid breaking navigation.
            }
        });
    }
});