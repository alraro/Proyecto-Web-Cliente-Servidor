const form = document.querySelector('#register-form');
const nameInput = document.querySelector('#name');
const emailInput = document.querySelector('#email');
const telefonoInput = document.querySelector('#telefono');
const localidadInput = document.querySelector('#localidad');
const passwordInput = document.querySelector('#password');
const confirmPasswordInput = document.querySelector('#confirm-password');
const domicilioInput = document.querySelector('#domicilio');
const cpInput = document.querySelector('#cp');
const togglePasswordButton = document.querySelector('#toggle-password');
const toggleConfirmPasswordButton = document.querySelector('#toggle-confirm-password');
const message = document.querySelector('#form-message');

const API_BASE_URL = 'http://localhost:8080';
const AUTH_TOKEN_KEY = 'bancosol_auth_token';

function togglePasswordVisibility(input, button) {
    const nextType = input.type === 'password' ? 'text' : 'password';
    input.type = nextType;
    button.textContent = nextType === 'password' ? 'Mostrar' : 'Ocultar';
    button.setAttribute(
        'aria-label',
        nextType === 'password' ? 'Mostrar contraseña' : 'Ocultar contraseña'
    );
}

togglePasswordButton.addEventListener('click', () => {
    togglePasswordVisibility(passwordInput, togglePasswordButton);
});

toggleConfirmPasswordButton.addEventListener('click', () => {
    togglePasswordVisibility(confirmPasswordInput, toggleConfirmPasswordButton);
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const nombre = nameInput.value.trim();
    const email = emailInput.value.trim();
    const telefono = telefonoInput.value.trim();
    const localidad = localidadInput.value.trim();
    const password = passwordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();
    const domicilio = domicilioInput.value.trim();
    const cp = cpInput.value.trim();

    message.classList.remove('is-error', 'is-success');

    if (!nombre || !email || !password || !confirmPassword) {
        message.textContent = 'Completa los campos obligatorios para continuar.';
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

    if (password !== confirmPassword) {
        message.textContent = 'Las contraseñas no coinciden.';
        message.classList.add('is-error');
        confirmPasswordInput.focus();
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                nombre,
                email,
                password,
                telefono,
                domicilio,
                localidad,
                cp,
            }),
        });

        const payload = await response.json();

        if (!response.ok) {
            message.textContent = payload.message || 'No se pudo completar el registro.';
            message.classList.add('is-error');
            return;
        }

        if (payload.token) {
            localStorage.setItem(AUTH_TOKEN_KEY, payload.token);
        }

        message.textContent = `Registro correcto. Bienvenido/a ${payload.nombre}.`;
        message.classList.add('is-success');

        window.setTimeout(() => {
            window.location.href = 'login.html';
        }, 900);
    } catch {
        message.textContent = 'No se pudo conectar con el backend. Revisa que este levantado.';
        message.classList.add('is-error');
    }
});
