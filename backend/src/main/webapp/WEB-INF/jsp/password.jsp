<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    Object pageTitleAttribute = request.getAttribute("pageTitle");
    String pageTitle = pageTitleAttribute == null ? "Bancosol | Recuperar contraseña" : pageTitleAttribute.toString();
%>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@500;700;800&family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= contextPath %>/css/password.css">
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var form = document.querySelector('#password-recovery-form');
            var emailInput = document.querySelector('#email');
            var message = document.querySelector('#recovery-message');
            var contextPathValue = '<%= contextPath %>';
            var API_BASE_URL = contextPathValue + '/api';

            if (form && emailInput && message) {
                form.addEventListener('submit', async function (event) {
                    event.preventDefault();

                    var email = emailInput.value.trim();
                    message.classList.remove('is-error', 'is-success');

                    if (!email || !emailInput.validity.valid) {
                        message.textContent = 'Introduce un correo valido para continuar.';
                        message.classList.add('is-error');
                        emailInput.focus();
                        return;
                    }

                    try {
                        var response = await fetch(API_BASE_URL + '/auth/password-recovery', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({ email: email })
                        });

                        var payload = await response.json();

                        if (!response.ok) {
                            message.textContent = payload.message || 'No se pudo procesar la solicitud.';
                            message.classList.add('is-error');
                            return;
                        }

                        message.textContent = payload.message || 'Revisa tu correo para continuar con la recuperación.';
                        message.classList.add('is-success');
                        form.reset();
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
<header class="topbar">
    <a class="brand" href="<%= contextPath %>/" aria-label="Bancosol inicio">
        <img src="<%= contextPath %>/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    </a>

    <nav class="main-nav" id="main-nav">
        <a href="<%= contextPath %>/">Inicio</a>
        <a href="<%= contextPath %>/login">Iniciar sesión</a>
        <a href="<%= contextPath %>/register">No tengo cuenta</a>
        <a href="<%= contextPath %>/#contacto">Contacto</a>
    </nav>
</header>

<main class="password-page">
    <section class="recovery-container" aria-labelledby="recovery-title">
        <div class="brand-lockup">
            <img src="<%= contextPath %>/assets/Bancosol.png" alt="Logo Bancosol" class="logo-card">
            <div>
                <p class="brand-title">Recuperación de acceso</p>
                <p class="brand-subtitle">Restablece tu contraseña</p>
            </div>
        </div>

        <h1 id="recovery-title">Olvidé mi contraseña</h1>
        <p>Introduce tu correo y te enviaremos instrucciones para recuperar el acceso a tu cuenta de Bancosol.</p>

        <form id="password-recovery-form" class="recovery-form" novalidate>
            <div class="form-group">
                <label for="email">Correo electrónico</label>
                <input type="email" id="email" name="email" placeholder="tu_correo@bancosol.org" required>
            </div>

            <button type="submit" class="btn-submit">Enviar enlace de recuperación</button>
        </form>

        <p class="form-message" id="recovery-message" role="status" aria-live="polite"></p>

        <div class="recovery-links">
            <a href="<%= contextPath %>/login" class="back-link">Volver a iniciar sesión</a>
            <a href="<%= contextPath %>/register" class="back-link">No tengo cuenta</a>
        </div>
    </section>
</main>

<footer class="site-footer" aria-label="Pie de página">
    <img src="<%= contextPath %>/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    <p>Bancosol · Banco de alimentos</p>
</footer>
</body>
</html>