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
});