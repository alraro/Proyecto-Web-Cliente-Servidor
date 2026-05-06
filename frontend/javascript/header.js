document.addEventListener('DOMContentLoaded', () => {
    const rolUsuario = localStorage.getItem('role') || 'Usuario';
    const nombreUsuario = localStorage.getItem('nombre') || 'Invitado';

    const checkHeader = setInterval(() => {
        const userNameEl = document.getElementById('user-name');

        if(userNameEl){
            userNameEl.textContent = `${nombreUsuario} (${rolUsuario})`;

            clearInterval(checkHeader);
        }
    }, 50);

    document.body.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
        } else if(e.target.id === 'btn-logout'){
            localStorage.clear();
            window.location.href = 'login.html';
        }
    });
});