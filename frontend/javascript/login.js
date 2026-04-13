const form = document.querySelector('#login-form');
const emailInput = document.querySelector('#email');
const passwordInput = document.querySelector('#password');
const togglePasswordButton = document.querySelector('#toggle-password');
const message = document.querySelector('#form-message');

const API_BASE_URL = `${window.location.protocol}//${window.location.hostname}:8080`;
const AUTH_TOKEN_KEY = 'bancosol_auth_token';

async function comprobarSesionActiva() {
    const token = localStorage.getItem(AUTH_TOKEN_KEY);
    if (!token) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            localStorage.removeItem(AUTH_TOKEN_KEY);
            return;
        }

        const payload = await response.json();
        message.textContent = `Sesion activa de ${payload.nombre}.`;
        message.classList.remove('is-error');
        message.classList.add('is-success');
    } catch {
        localStorage.removeItem(AUTH_TOKEN_KEY);
    }
}

comprobarSesionActiva();

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

    try {
        const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email,
                password,
            }),
        });

        const payload = await response.json();

        if (!response.ok) {
            message.textContent = payload.message || 'No se pudo iniciar sesion.';
            message.classList.add('is-error');
            return;
        }

        if (payload.token) {
            localStorage.setItem(AUTH_TOKEN_KEY, payload.token);
        }

        message.textContent = `Bienvenido/a ${payload.nombre}. Login correcto.`;
        message.classList.add('is-success');
    } catch {
        message.textContent = 'No se pudo conectar con el backend. Revisa que este levantado.';
        message.classList.add('is-error');
    }
});