const form = document.querySelector('#login-form');
const emailInput = document.querySelector('#email');
const passwordInput = document.querySelector('#password');
const togglePasswordButton = document.querySelector('#toggle-password');
const message = document.querySelector('#form-message');

togglePasswordButton.addEventListener('click', () => {
    const nextType = passwordInput.type === 'password' ? 'text' : 'password';

    passwordInput.type = nextType;
    togglePasswordButton.textContent = nextType === 'password' ? 'Mostrar' : 'Ocultar';
    togglePasswordButton.setAttribute(
        'aria-label',
        nextType === 'password' ? 'Mostrar contraseña' : 'Ocultar contraseña'
    );
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const email = emailInput.value.trim();
    const password = passwordInput.value.trim();

    message.classList.remove('is-error', 'is-success');

    if (!email || !password) {
        message.textContent = 'Completa tu usuario y contraseña para continuar.';
        message.classList.add('is-error');
        return;
    }

    if (!emailInput.validity.valid) {
        message.textContent = 'Ingresa un correo valido.';
        message.classList.add('is-error');
        emailInput.focus();
        return;
    }

    if (password.length < 6) {
        message.textContent = 'La contraseña debe tener al menos 6 caracteres.';
        message.classList.add('is-error');
        passwordInput.focus();
        return;
    }

    try{
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },

            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        const data = await response.json();
        if (!response.ok) {
            message.textContent = data.message || 'Datos erróneos';
            message.classList.add('is-error');
            return;
        }

        if (data.redirectUrl) {
            window.location.href = data.redirectUrl;
        }
    } catch(error){
        message.textContent = 'No existen los datos.';
        message.classList.add('is-error');
    }
});