document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const nombre = localStorage.getItem('nombre') || 'Capitán';
    const userNameEl    = document.getElementById('user-name');
    const welcomeNameEl = document.getElementById('welcome-name');

    if (userNameEl)    userNameEl.textContent    = nombre;
    if (welcomeNameEl) welcomeNameEl.textContent = nombre;

    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'login.html';
    });
});
