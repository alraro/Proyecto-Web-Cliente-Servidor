document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token') || localStorage.getItem('role') !== 'COLABORADOR') {
        window.location.href = 'login.html';
        return;
    }
});