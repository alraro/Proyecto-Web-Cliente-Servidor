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

        <form id="register-form" class="login-form register-form" method="post" action="<%= contextPath %>/register">
            <div class="field-grid">
                <div class="field-group">
                    <label for="nombre">Nombre completo *</label>
                    <div class="input-shell">
                        <input id="nombre" name="nombre" type="text" placeholder="Nombre y apellidos" autocomplete="name" required>
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
                    </div>
                </div>

                <div class="field-group">
                    <label for="confirmPassword">Repetir contraseña *</label>
                    <div class="input-shell password-shell">
                        <input id="confirmPassword" name="confirmPassword" type="password" placeholder="Repite la contraseña" autocomplete="new-password" required>
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
            <% if (request.getAttribute("registerError") != null) { %>
                <p class="form-message is-error" id="form-message" role="status" aria-live="polite"><%= request.getAttribute("registerError") %></p>
            <% } else if (request.getAttribute("registerSuccess") != null) { %>
                <p class="form-message is-success" id="form-message" role="status" aria-live="polite"><%= request.getAttribute("registerSuccess") %></p>
            <% } else { %>
                <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
            <% } %>
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