document.addEventListener('DOMContentLoaded', () => {
    if (!sessionStorage.getItem('token') || sessionStorage.getItem('role') !== 'COLABORADOR') {
        window.location.href = 'login.html';
        return;
    }
});