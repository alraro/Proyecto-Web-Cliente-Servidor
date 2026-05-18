document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const nombre = localStorage.getItem('nombre') || 'Coordinador';
    const welcomeNameEl = document.getElementById('welcome-name');

    if (welcomeNameEl) welcomeNameEl.textContent = nombre;


    const userNameEl = document.getElementById('user-name');
	
    if (userNameEl) {
        userNameEl.textContent = localStorage.getItem('nombre') || 'Coordinador';
    }

    document.addEventListener('click', (e) => {
        if(e.target.id === 'btn-edit'){
            window.location.href = 'edit.html';
            
        } else if(e.target.id === 'btn-logout'){
            localStorage.clear();
            window.location.href = 'login.html';
        }
    })
});
