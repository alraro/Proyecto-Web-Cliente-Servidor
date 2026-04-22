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
                    <span>Crear cuenta en segundos</span>
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

        <form id="login-form" class="login-form" method="post" action="<%= contextPath %>/login">
            <label for="email">Correo</label>
            <div class="input-shell">
                <input id="email" name="email" type="email" placeholder="bancosolseguimiento@gmail.com" autocomplete="username" required>
            </div>

            <input id="csrf-token" name="csrfToken" type="hidden" value="">

            <label for="password">Contraseña de acceso</label>
            <div class="input-shell password-shell">
                <input id="password" name="password" type="password" placeholder="Ingresa tu contraseña" autocomplete="current-password" required>
            </div>

            <div class="form-options">
                <label class="remember-me">
                    <input type="checkbox" id="remember-me">
                    <span>Recordar acceso</span>
                </label>
            </div>

            <button type="submit" class="login-button">Entrar al espacio</button>
            <% if (request.getAttribute("loginError") != null) { %>
                <p class="form-message is-error" id="form-message" role="status" aria-live="polite"><%= request.getAttribute("loginError") %></p>
            <% } else if (request.getAttribute("loginSuccess") != null) { %>
                <p class="form-message is-success" id="form-message" role="status" aria-live="polite"><%= request.getAttribute("loginSuccess") %></p>
            <% } else { %>
                <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
            <% } %>
            <p class="form-message">Credenciales de prueba: bancosolseguimiento@gmail.com / hash_pendiente</p>
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