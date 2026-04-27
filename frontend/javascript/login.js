const form = document.querySelector('#login-form');
const emailInput = document.querySelector('#email');
const passwordInput = document.querySelector('#password');
const togglePasswordButton = document.querySelector('#toggle-password');
const message = document.querySelector('#form-message');

function showErrorFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    if (!error) return;
    message.textContent = error;
    message.classList.remove('is-success');
    message.classList.add('is-error');
}

showErrorFromUrl();

togglePasswordButton.addEventListener('click', () => {
    const nextType = passwordInput.type === 'password' ? 'text' : 'password';
    passwordInput.type = nextType;
    togglePasswordButton.textContent = nextType === 'password' ? 'Mostrar' : 'Ocultar';
    togglePasswordButton.setAttribute('aria-label', nextType === 'password' ? 'Mostrar contraseña' : 'Ocultar contraseña');
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
        message.textContent = 'Ingresa un correo válido.';
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

    try {
        const res = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (!res.ok) {
            message.textContent = data.message || 'Credenciales incorrectas.';
            message.classList.add('is-error');
            return;
        }

        // Guardar token y datos del usuario
        localStorage.setItem('token', data.token);
        localStorage.setItem('nombre', data.nombre);
        localStorage.setItem('email', data.email);
        localStorage.setItem('role', data.role);

        // Redirigir según rol
        window.location.href = data.redirectUrl;

    } catch {
        message.textContent = 'Error al conectar con el servidor.';
        message.classList.add('is-error');
    }
});