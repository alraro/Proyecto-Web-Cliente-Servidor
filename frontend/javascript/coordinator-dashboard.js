/* Bootstrap auth from URL params (when navigating from the SSR portal). */
(function () {
    const p = new URLSearchParams(window.location.search);
    const t = p.get('token'), n = p.get('nombre');
    if (t) localStorage.setItem('token', t);
    if (n) localStorage.setItem('nombre', n);
}());

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const nombre = localStorage.getItem('nombre') || 'Coordinador';
    const welcomeNameEl = document.querySelector('#welcome-name');

    if (welcomeNameEl) welcomeNameEl.textContent = nombre;

});
