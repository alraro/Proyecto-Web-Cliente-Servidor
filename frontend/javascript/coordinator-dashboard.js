/* Bootstrap auth from URL params (when navigating from the SSR portal). */
handleUrlTokenParams();

document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const nombre = sessionStorage.getItem('nombre') || 'Coordinador';
    const welcomeNameEl = document.querySelector('#welcome-name');

    if (welcomeNameEl) welcomeNameEl.textContent = nombre;

});
