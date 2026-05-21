document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const nombre = localStorage.getItem('nombre') || 'Capitán';
    const welcomeNameEl = document.querySelector('#welcome-name');

    if (welcomeNameEl) welcomeNameEl.textContent = nombre;
});
