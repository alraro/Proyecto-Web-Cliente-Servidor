document.addEventListener('DOMContentLoaded', () => {
    const rolUsuario = sessionStorage.getItem('role') || 'Usuario';
    const nombreUsuario = sessionStorage.getItem('nombre') || 'Invitado';

    const checkHeader = setInterval(() => {
        const userNameEl = document.querySelector('#user-name');

        if(userNameEl){
            userNameEl.textContent = `${nombreUsuario} (${rolUsuario})`;

            clearInterval(checkHeader);
        }
    }, 50);

    document.body.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
        } else if(e.target.id === 'btn-logout'){
            sessionStorage.clear();
            window.location.href = 'login.html';
        }
    });
});