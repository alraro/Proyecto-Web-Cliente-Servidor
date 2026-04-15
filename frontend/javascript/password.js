const form = document.querySelector('#password-recovery-form');
const emailInput = document.querySelector('#email');
const message = document.querySelector('#recovery-message');

const API_BASE_URL = `${window.location.protocol}//${window.location.hostname}:8080`;

if (form && emailInput && message) {
    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const email = emailInput.value.trim();
        message.classList.remove('is-error', 'is-success');

        if (!email || !emailInput.validity.valid) {
            message.textContent = 'Introduce un correo valido para continuar.';
            message.classList.add('is-error');
            emailInput.focus();
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/password-recovery`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email }),
            });

            const payload = await response.json();

            if (!response.ok) {
                message.textContent = payload.message || 'No se pudo procesar la solicitud.';
                message.classList.add('is-error');
                return;
            }

            message.textContent = payload.message || 'Revisa tu correo para continuar con la recuperación.';
            message.classList.add('is-success');
            form.reset();
        } catch {
            message.textContent = 'No se pudo conectar con el backend. Revisa que este levantado.';
            message.classList.add('is-error');
        }
    });
}