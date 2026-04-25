const API_BASE = 'http://localhost:8080';

const ROLE_ROUTES = {
	ADMINISTRADOR: 'admin.html',
	COORDINADOR: 'coordinator.html',
	CAPITAN: 'captain.html',
	COLABORADOR: 'collaborator.html'
};

const form = document.querySelector('#edit-form');
const nameInput = document.querySelector('#name');
const emailInput = document.querySelector('#email');
const telefonoInput = document.querySelector('#telefono');
const localidadInput = document.querySelector('#localidad');
const domicilioInput = document.querySelector('#domicilio');
const cpInput = document.querySelector('#cp');
const cancelButton = document.querySelector('#cancel-button');
const roleReturnLink = document.querySelector('#role-return-link');
const message = document.querySelector('#form-message');

function getRoleRoute() {
	const role = (localStorage.getItem('role') || '').toUpperCase();
	return ROLE_ROUTES[role] || 'login.html';
}

function redirectToRolePage() {
	window.location.href = getRoleRoute();
}

function showMessage(text, isError = false) {
	message.textContent = text;
	message.classList.remove('is-error', 'is-success');
	message.classList.add(isError ? 'is-error' : 'is-success');
}

function setRoleLink() {
	roleReturnLink.setAttribute('href', getRoleRoute());
}

function fillForm(profile) {
	nameInput.value = profile.nombre || localStorage.getItem('nombre') || '';
	emailInput.value = profile.email || localStorage.getItem('email') || '';
	telefonoInput.value = profile.telefono || '';
	domicilioInput.value = profile.domicilio || '';
	cpInput.value = profile.cp || '';
	localidadInput.value = localStorage.getItem('localidadPerfil') || '';
}

async function loadProfile() {
	const token = localStorage.getItem('token');

	if (!token) {
		window.location.href = 'login.html';
		return;
	}

	try {
		const response = await fetch(`${API_BASE}/api/auth/profile`, {
			method: 'GET',
			headers: {
				Authorization: `Bearer ${token}`
			}
		});

		const data = await response.json();

		if (!response.ok) {
			if (response.status === 401) {
				localStorage.clear();
				window.location.href = 'login.html';
				return;
			}

			fillForm({});
			showMessage(data.message || 'No se pudo cargar tu perfil.', true);
			return;
		}

		fillForm(data);
	} catch {
		fillForm({});
		showMessage('No se pudo conectar con el servidor para cargar el perfil.', true);
	}
}

function validateForm() {
	const email = emailInput.value.trim();
	const telefono = telefonoInput.value.trim();
	const cp = cpInput.value.trim();

	if (!email) {
		showMessage('El correo es obligatorio.', true);
		emailInput.focus();
		return false;
	}

	if (!emailInput.validity.valid) {
		showMessage('Ingresa un correo valido.', true);
		emailInput.focus();
		return false;
	}

	if (telefono && !/^[0-9+\-\s]{7,20}$/.test(telefono)) {
		showMessage('El telefono no tiene un formato valido.', true);
		telefonoInput.focus();
		return false;
	}

	if (cp && !/^[0-9]{5}$/.test(cp)) {
		showMessage('El codigo postal debe tener 5 digitos.', true);
		cpInput.focus();
		return false;
	}

	return true;
}

async function handleSubmit(event) {
	event.preventDefault();

	if (!validateForm()) {
		return;
	}

	const token = localStorage.getItem('token');
	if (!token) {
		window.location.href = 'login.html';
		return;
	}

	const payload = {
		email: emailInput.value.trim(),
		telefono: telefonoInput.value.trim(),
		domicilio: domicilioInput.value.trim(),
		cp: cpInput.value.trim(),
		localidad: localidadInput.value.trim()
	};

	try {
		const response = await fetch(`${API_BASE}/api/auth/profile`, {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${token}`
			},
			body: JSON.stringify(payload)
		});

		const data = await response.json();

		if (!response.ok) {
			if (response.status === 401) {
				localStorage.clear();
				window.location.href = 'login.html';
				return;
			}

			showMessage(data.message || 'No se pudieron guardar los cambios.', true);
			return;
		}

		localStorage.setItem('nombre', data.nombre || nameInput.value.trim());
		localStorage.setItem('email', data.email || payload.email);
		localStorage.setItem('localidadPerfil', payload.localidad);

		showMessage('Perfil actualizado correctamente. Volviendo a tu panel...');
		setTimeout(() => {
			redirectToRolePage();
		}, 900);
	} catch {
		showMessage('Error al conectar con el servidor.', true);
	}
}

form.addEventListener('submit', handleSubmit);

cancelButton.addEventListener('click', () => {
	redirectToRolePage();
});

setRoleLink();
loadProfile();
