<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="/css/login.css">
</head>

<body>
<header class="topbar topbar-login">
    <a class="brand" href="/" aria-label="Bancosol inicio">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    </a>

    <nav class="main-nav main-nav-login" id="main-nav">
        <a href="/">Inicio</a>
        <a href="/register">No tengo cuenta</a>
        <a href="/#contacto">Contacto</a>
    </nav>
</header>

<main class="login-page">
    <section class="login-card" aria-labelledby="login-title">
        <div class="brand-lockup">
            <img src="/assets/Bancosol.png" alt="Bancosol" class="logo">
            <div>
                <p class="brand-name">Bancosol</p>
                <p class="brand-subtitle">Acceso interno y colaborador</p>
            </div>
        </div>

        <div class="card-copy">
            <h2 id="login-title">Iniciar sesión</h2>
            <p>Ingresa tus credenciales para entrar al espacio de trabajo.</p>
        </div>

        <form id="login-form" class="login-form" method="post" action="/login">
            <label for="email">Correo</label>
            <div class="input-shell">
                <input id="email" name="email" type="email" placeholder="bancosolseguimiento@gmail.com" autocomplete="username" required>
            </div>

            <input id="csrf-token" name="csrfToken" type="hidden" value="">

            <label for="password">Contraseña de acceso</label>
            <div class="input-shell password-shell">
                <input id="password" name="password" type="password" placeholder="Ingresa tu contraseña" autocomplete="current-password" required>
            </div>


            <button type="submit" class="login-button">Entrar al espacio</button>
            <% if (request.getAttribute("loginError") != null) { %>
                <p class="form-message is-error" id="form-message" role="status" aria-live="polite"><%= request.getAttribute("loginError") %></p>
            <% } else if (request.getAttribute("loginSuccess") != null) { %>
                <p class="form-message is-success" id="form-message" role="status" aria-live="polite"><%= request.getAttribute("loginSuccess") %></p>
            <% } else { %>
                <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
            <% } %>
            <p class="auth-switch">No tengo cuenta: <a class="forgot-link" href="/register">Crear cuenta</a></p>
        </form>
    </section>
</main>

<footer class="site-footer" aria-label="Pie de página">
    <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    <p>Bancosol · Banco de alimentos</p>
</footer>
</body>
</html>