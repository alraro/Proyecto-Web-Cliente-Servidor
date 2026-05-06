document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('token')) {
        window.location.href = 'login.html';
        return;
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