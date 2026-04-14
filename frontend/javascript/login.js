const form = document.querySelector('#login-form');
const emailInput = document.querySelector('#email');
const passwordInput = document.querySelector('#password');
const togglePasswordButton = document.querySelector('#toggle-password');
const rememberMeInput = document.querySelector('#remember-me');
const csrfTokenInput = document.querySelector('#csrf-token');
const message = document.querySelector('#form-message');

const API_BASE_URL = `${window.location.protocol}//${window.location.hostname}:8080`;
const AUTH_TOKEN_KEY = 'bancosol_auth_token';

function guardarToken(token, recordarSesion) {
    sessionStorage.setItem(AUTH_TOKEN_KEY, token);

    if (recordarSesion) {
        localStorage.setItem(AUTH_TOKEN_KEY, token);
    } else {
        localStorage.removeItem(AUTH_TOKEN_KEY);
    }
}

function leerToken() {
    return sessionStorage.getItem(AUTH_TOKEN_KEY) || localStorage.getItem(AUTH_TOKEN_KEY);
}

function limpiarToken() {
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_TOKEN_KEY);
}

function tokenExpirado(token) {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) {
            return true;
        }

        const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
        if (!payload.exp) {
            return false;
        }

        return Date.now() >= payload.exp * 1000;
    } catch {
        return true;
    }
}

async function obtenerCsrfToken() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/csrf`);
        if (!response.ok) {
            throw new Error('No se pudo cargar CSRF');
        }

        const payload = await response.json();
        csrfTokenInput.value = payload.csrfToken || '';
    } catch {
        csrfTokenInput.value = '';
    }
}

async function comprobarSesionActiva() {
    const token = leerToken();
    if (!token) {
        await obtenerCsrfToken();
        return;
    }

    if (tokenExpirado(token)) {
        limpiarToken();
        message.textContent = 'Tu sesión ha expirado. Inicia sesión de nuevo.';
        message.classList.remove('is-success');
        message.classList.add('is-error');
        await obtenerCsrfToken();
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            limpiarToken();
            await obtenerCsrfToken();
            return;
        }

        window.location.href = 'history_volunteer.html';
    } catch {
        limpiarToken();
        await obtenerCsrfToken();
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
        const csrfToken = csrfTokenInput.value.trim();

        if (!csrfToken) {
            message.textContent = 'No se pudo validar la seguridad del formulario. Recarga la página.';
            message.classList.add('is-error');
            return;
        }

        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': csrfToken,
            },
            body: JSON.stringify({
                email,
                password,
                csrfToken,
            }),
        });

        const payload = await response.json();

        if (!response.ok) {
            message.textContent = payload.message || 'No se pudo iniciar sesion.';
            message.classList.add('is-error');
            await obtenerCsrfToken();
            return;
        }

        if (payload.token) {
            guardarToken(payload.token, Boolean(rememberMeInput.checked));
        }

        message.textContent = `Bienvenido/a ${payload.nombre}. Login correcto.`;
        message.classList.add('is-success');

        window.setTimeout(() => {
            window.location.href = 'history_volunteer.html';
        }, 350);
    } catch {
        message.textContent = 'No se pudo conectar con el backend. Revisa que este levantado.';
        message.classList.add('is-error');
        await obtenerCsrfToken();
    }
});