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

    message.textContent = 'Se enviará el formulario y, si todo es correcto, volverás a la página de login.';
    message.classList.add('is-success');

    const formData = new FormData(form);
    const data = new URLSearchParams(formData);

    try{
        const response = await fetch(form.action, {
            method: form.method,
            body: data,
        });

        if (response.ok){
            window.location.href = 'login.html';
        } else{
            message.textContent = 'Error al registrar el usuario. Intenta nuevamente.';
            message.classList.remove('is-success');
            message.classList.add('is-error');
        }
    } catch(error) {
        message.textContent = 'Error de conexión. Intenta nuevamente más tarde.';
        message.classList.remove('is-success');
        message.classList.add('is-error');
        console.error('Error:', error);
    }
});
