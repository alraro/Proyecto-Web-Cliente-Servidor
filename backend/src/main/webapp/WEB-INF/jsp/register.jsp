<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    Object pageTitleAttribute = request.getAttribute("pageTitle");
    String pageTitle = pageTitleAttribute == null ? "Bancosol | Crear cuenta" : pageTitleAttribute.toString();
%>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@500;700;800&family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= contextPath %>/css/register.css">
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var form = document.querySelector('#register-form');
            var nameInput = document.querySelector('#name');
            var emailInput = document.querySelector('#email');
            var telefonoInput = document.querySelector('#telefono');
            var localidadInput = document.querySelector('#localidad');
            var passwordInput = document.querySelector('#password');
            var confirmPasswordInput = document.querySelector('#confirm-password');
            var domicilioInput = document.querySelector('#domicilio');
            var cpInput = document.querySelector('#cp');
            var togglePasswordButton = document.querySelector('#toggle-password');
            var toggleConfirmPasswordButton = document.querySelector('#toggle-confirm-password');
            var message = document.querySelector('#form-message');
            var contextPathValue = '<%= contextPath %>';
            var API_BASE_URL = contextPathValue + '/api';
            var AUTH_TOKEN_KEY = 'bancosol_auth_token';

            function togglePasswordVisibility(input, button) {
                var nextType = input.type === 'password' ? 'text' : 'password';
                input.type = nextType;
                button.textContent = nextType === 'password' ? 'Mostrar' : 'Ocultar';
                button.setAttribute('aria-label', nextType === 'password' ? 'Mostrar contraseña' : 'Ocultar contraseña');
            }

            if (togglePasswordButton && passwordInput) {
                togglePasswordButton.addEventListener('click', function () {
                    togglePasswordVisibility(passwordInput, togglePasswordButton);
                });
            }

            if (toggleConfirmPasswordButton && confirmPasswordInput) {
                toggleConfirmPasswordButton.addEventListener('click', function () {
                    togglePasswordVisibility(confirmPasswordInput, toggleConfirmPasswordButton);
                });
            }

            if (form && nameInput && emailInput && telefonoInput && localidadInput && passwordInput && confirmPasswordInput && domicilioInput && cpInput && message) {
                form.addEventListener('submit', async function (event) {
                    event.preventDefault();

                    var nombre = nameInput.value.trim();
                    var email = emailInput.value.trim();
                    var telefono = telefonoInput.value.trim();
                    var localidad = localidadInput.value.trim();
                    var password = passwordInput.value.trim();
                    var confirmPassword = confirmPasswordInput.value.trim();
                    var domicilio = domicilioInput.value.trim();
                    var cp = cpInput.value.trim();

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
                        var response = await fetch(API_BASE_URL + '/auth/register', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                nombre: nombre,
                                email: email,
                                password: password,
                                telefono: telefono,
                                domicilio: domicilio,
                                localidad: localidad,
                                cp: cp
                            })
                        });

                        var payload = await response.json();

                        if (!response.ok) {
                            message.textContent = payload.message || 'No se pudo completar el registro.';
                            message.classList.add('is-error');
                            return;
                        }

                        if (payload.token) {
                            localStorage.setItem(AUTH_TOKEN_KEY, payload.token);
                        }

                        message.textContent = 'Registro correcto. Bienvenido/a ' + payload.nombre + '.';
                        message.classList.add('is-success');

                        window.setTimeout(function () {
                            window.location.href = contextPathValue + '/login';
                        }, 900);
                    } catch (error) {
                        message.textContent = 'No se pudo conectar con el backend. Revisa que este levantado.';
                        message.classList.add('is-error');
                    }
                });
            }
        });
    </script>
</head>

<body>
<header class="topbar topbar-login">
    <a class="brand" href="<%= contextPath %>/" aria-label="Bancosol inicio">
        <img src="<%= contextPath %>/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    </a>

    <nav class="main-nav main-nav-login" id="main-nav">
        <a href="<%= contextPath %>/">Inicio</a>
        <a href="<%= contextPath %>/login">Iniciar sesión</a>
        <a href="<%= contextPath %>/#contacto">Contacto</a>
    </nav>
</header>

<main class="login-page register-page">
    <section class="login-hero" aria-hidden="true">
        <div class="hero-badge">Registro Bancosol</div>
        <h1>Únete a la red de voluntariado y coordinación solidaria</h1>
        <p>Crear una cuenta te permite participar en campañas, consultar tu información básica y acceder con una sesión segura desde el primer momento.</p>
        <div class="hero-cards">
            <article>
                <strong>Alta rápida</strong>
                <span>Formulario claro y validación inmediata</span>
            </article>
            <article>
                <strong>Acceso posterior</strong>
                <span>Al terminar, podrás iniciar sesión desde la pestaña de acceso</span>
            </article>
        </div>
    </section>

    <section class="login-card register-card" aria-labelledby="register-title">
        <div class="brand-lockup">
            <img src="<%= contextPath %>/assets/Bancosol.png" alt="Bancosol" class="logo">
            <div>
                <p class="brand-name">Bancosol</p>
                <p class="brand-subtitle">Crear una cuenta nueva</p>
            </div>
        </div>

        <div class="card-copy">
            <h2 id="register-title">Registro de usuario</h2>
            <p>Rellena los datos básicos para darte de alta en el sistema.</p>
        </div>

        <form id="register-form" class="login-form register-form" novalidate>
            <div class="field-grid">
                <div class="field-group">
                    <label for="name">Nombre completo *</label>
                    <div class="input-shell">
                        <input id="name" name="name" type="text" placeholder="Nombre y apellidos" autocomplete="name" required>
                    </div>
                </div>

                <div class="field-group">
                    <label for="email">Correo *</label>
                    <div class="input-shell">
                        <input id="email" name="email" type="email" placeholder="usuario@bancosol.org" autocomplete="email" required>
                    </div>
                </div>
            </div>

            <div class="field-grid">
                <div class="field-group">
                    <label for="telefono">Teléfono *</label>
                    <div class="input-shell">
                        <input id="telefono" name="telefono" type="tel" placeholder="600123123" autocomplete="tel" required>
                    </div>
                </div>

                <div class="field-group">
                    <label for="localidad">Localidad</label>
                    <div class="input-shell">
                        <input id="localidad" name="localidad" type="text" placeholder="Málaga" autocomplete="address-level2">
                    </div>
                </div>
            </div>

            <div class="field-grid">
                <div class="field-group">
                    <label for="password">Contraseña *</label>
                    <div class="input-shell password-shell">
                        <input id="password" name="password" type="password" placeholder="Mínimo 6 caracteres" autocomplete="new-password" required>
                        <button class="toggle-password" type="button" id="toggle-password" aria-label="Mostrar contraseña">Mostrar</button>
                    </div>
                </div>

                <div class="field-group">
                    <label for="confirm-password">Repetir contraseña *</label>
                    <div class="input-shell password-shell">
                        <input id="confirm-password" name="confirm-password" type="password" placeholder="Repite la contraseña" autocomplete="new-password" required>
                        <button class="toggle-password" type="button" id="toggle-confirm-password" aria-label="Mostrar contraseña repetida">Mostrar</button>
                    </div>
                </div>
            </div>

            <div class="field-grid">
                <div class="field-group full-width">
                    <label for="domicilio">Domicilio</label>
                    <div class="input-shell">
                        <input id="domicilio" name="domicilio" type="text" placeholder="Calle, número, piso..." autocomplete="street-address">
                    </div>
                </div>
            </div>

            <div class="field-grid">
                <div class="field-group">
                    <label for="cp">Código postal</label>
                    <div class="input-shell">
                        <input id="cp" name="cp" type="text" placeholder="29001" autocomplete="postal-code">
                    </div>
                </div>

                <p class="help-text full-width">Los datos se almacenan en la tabla Usuario de PostgreSQL y la contraseña se usa para autenticación inicial del proyecto.</p>
            </div>

            <button type="submit" class="login-button">Crear cuenta</button>
            <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
            <p class="auth-switch">¿Ya tienes cuenta? <a class="forgot-link" href="<%= contextPath %>/login">Iniciar sesión</a></p>
        </form>

        <div class="security-note">
            <span class="security-dot"></span>
            <p>Los datos enviados se almacenan en la tabla Usuario de PostgreSQL y la sesión se firma con JWT.</p>
        </div>
    </section>
</main>

<footer class="site-footer" aria-label="Pie de página">
    <img src="<%= contextPath %>/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    <p>Bancosol · Banco de alimentos</p>
</footer>
</body>
</html>