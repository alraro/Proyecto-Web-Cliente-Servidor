<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    Object pageTitleAttribute = request.getAttribute("pageTitle");
    String pageTitle = pageTitleAttribute == null ? "Bancosol | Inicio de sesión" : pageTitleAttribute.toString();
%>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@500;700;800&family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= contextPath %>/css/login.css">
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var form = document.querySelector('#login-form');
            var emailInput = document.querySelector('#email');
            var passwordInput = document.querySelector('#password');
            var togglePasswordButton = document.querySelector('#toggle-password');
            var rememberMeInput = document.querySelector('#remember-me');
            var message = document.querySelector('#form-message');
            var contextPathValue = '<%= contextPath %>';
            var API_BASE_URL = contextPathValue + '/api';
            var AUTH_TOKEN_KEY = 'bancosol_auth_token';

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
                    var parts = token.split('.');
                    if (parts.length !== 3) {
                        return true;
                    }

                    var payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
                    if (!payload.exp) {
                        return false;
                    }

                    return Date.now() >= payload.exp * 1000;
                } catch (error) {
                    return true;
                }
            }

            function comprobarSesionActiva() {
                var token = leerToken();
                if (!token) {
                    return;
                }

                if (tokenExpirado(token)) {
                    limpiarToken();
                    if (message) {
                        message.textContent = 'Tu sesión ha expirado. Inicia sesión de nuevo.';
                        message.classList.remove('is-success');
                        message.classList.add('is-error');
                    }
                    return;
                }

                window.location.href = contextPathValue + '/';
            }

            comprobarSesionActiva();

            if (togglePasswordButton && passwordInput) {
                togglePasswordButton.addEventListener('click', function () {
                    var nextType = passwordInput.type === 'password' ? 'text' : 'password';
                    passwordInput.type = nextType;
                    togglePasswordButton.textContent = nextType === 'password' ? 'Mostrar' : 'Ocultar';
                    togglePasswordButton.setAttribute('aria-label', nextType === 'password' ? 'Mostrar contraseña' : 'Ocultar contraseña');
                });
            }

            if (form && emailInput && passwordInput && message) {
                form.addEventListener('submit', async function (event) {
                    event.preventDefault();

                    var email = emailInput.value.trim();
                    var password = passwordInput.value.trim();

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
                        var response = await fetch(API_BASE_URL + '/auth/login', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                email: email,
                                password: password
                            })
                        });

                        var payload = await response.json();

                        if (!response.ok) {
                            message.textContent = payload.message || 'No se pudo iniciar sesion.';
                            message.classList.add('is-error');
                            return;
                        }

                        if (payload.token) {
                            guardarToken(payload.token, Boolean(rememberMeInput && rememberMeInput.checked));
                        }

                        message.textContent = 'Bienvenido/a ' + payload.nombre + '. Login correcto.';
                        message.classList.add('is-success');

                        window.setTimeout(function () {
                            window.location.href = contextPathValue + '/';
                        }, 350);
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
        <a href="<%= contextPath %>/register">No tengo cuenta</a>
        <a href="<%= contextPath %>/#contacto">Contacto</a>
    </nav>
</header>

<main class="login-page">
    <section class="login-hero" aria-hidden="true">
        <div class="hero-badge">Acceso Bancosol</div>
        <h1>Inicia sesión para acceder a la plataforma de gestión solidaria</h1>
        <p>Desde aquí podrás entrar al espacio de trabajo para campañas, seguimiento de donaciones y coordinación con entidades.</p>
        <div class="hero-cards">
            <article>
                <strong>Inicio seguro</strong>
                <span>Acceso con token y validaciones del backend</span>
            </article>
            <article>
                <strong>Accesos rápidos</strong>
                <span>Recuperar contraseña o crear cuenta en segundos</span>
            </article>
        </div>
    </section>

    <section class="login-card" aria-labelledby="login-title">
        <div class="brand-lockup">
            <img src="<%= contextPath %>/assets/Bancosol.png" alt="Bancosol" class="logo">
            <div>
                <p class="brand-name">Bancosol</p>
                <p class="brand-subtitle">Acceso interno y colaborador</p>
            </div>
        </div>

        <div class="card-copy">
            <h2 id="login-title">Iniciar sesión</h2>
            <p>Ingresa tus credenciales para entrar al espacio de trabajo.</p>
        </div>

        <form id="login-form" class="login-form" novalidate>
            <label for="email">Correo</label>
            <div class="input-shell">
                <input id="email" name="email" type="email" placeholder="capitan100@nomail.es" autocomplete="username" required>
            </div>

            <input id="csrf-token" name="csrfToken" type="hidden" value="">

            <label for="password">Contraseña de acceso</label>
            <div class="input-shell password-shell">
                <input id="password" name="password" type="password" placeholder="Ingresa tu contraseña" autocomplete="current-password" required>
                <button class="toggle-password" type="button" id="toggle-password" aria-label="Mostrar contraseña">Mostrar</button>
            </div>

            <div class="form-options">
                <label class="remember-me">
                    <input type="checkbox" id="remember-me">
                    <span>Recordar acceso</span>
                </label>
                <a href="<%= contextPath %>/password" class="forgot-link">Olvidé contraseña</a>
            </div>

            <button type="submit" class="login-button">Entrar al espacio</button>
            <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
            <p class="form-message">Credenciales de prueba: capitan100@nomail.es / hash_pendiente</p>
            <p class="auth-switch">No tengo cuenta: <a class="forgot-link" href="<%= contextPath %>/register">Crear cuenta</a></p>
        </form>

        <div class="security-note">
            <span class="security-dot"></span>
            <p>Acceso protegido para la coordinación de campañas, donaciones y entidades.</p>
        </div>
    </section>
</main>

<footer class="site-footer" aria-label="Pie de página">
    <img src="<%= contextPath %>/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    <p>Bancosol · Banco de alimentos</p>
</footer>
</body>
</html>