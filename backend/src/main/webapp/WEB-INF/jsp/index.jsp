<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    Object pageTitleAttribute = request.getAttribute("pageTitle");
    String pageTitle = pageTitleAttribute == null ? "Bancosol | Inicio" : pageTitleAttribute.toString();
%>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800&family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= contextPath %>/css/index.css">
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var menuButton = document.querySelector('#menu-button');
            var mainNav = document.querySelector('#main-nav');
            var panelMessage = document.querySelector('#panel-message');
            var quickAccessLinks = document.querySelectorAll('.quick-link');

            if (menuButton && mainNav) {
                menuButton.addEventListener('click', function () {
                    var isOpen = mainNav.classList.toggle('is-open');
                    menuButton.setAttribute('aria-expanded', String(isOpen));
                });
            }

            quickAccessLinks.forEach(function (link) {
                link.addEventListener('mouseenter', function () {
                    var label = link.textContent && link.textContent.trim();
                    if (panelMessage && label) {
                        panelMessage.textContent = 'Vas a ir a: ' + label + '.';
                    }
                });
            });
        });
    </script>
</head>

<body>
<header class="topbar">
    <a class="brand" href="<%= contextPath %>/" aria-label="Bancosol inicio">
        <img src="<%= contextPath %>/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    </a>

    <button class="menu-button" id="menu-button" type="button" aria-expanded="false" aria-controls="main-nav">Menú</button>

    <nav class="main-nav" id="main-nav">
        <a href="#inicio" aria-current="page">Inicio</a>
        <a class="nav-cta" href="<%= contextPath %>/login">Iniciar sesión</a>
    </nav>
</header>

<main>
    <section class="hero" id="inicio">
        <div class="hero-copy">
            <span class="eyebrow">Banco de alimentos de Málaga</span>
            <h1>Bancosol transforma excedentes en ayuda real para miles de familias.</h1>
            <p>
                Somos una red solidaria que recupera alimentos, coordina voluntariado y distribuye recursos a
                entidades sociales para que nadie se quede atrás.
            </p>

            <div class="hero-actions">
                <a class="primary-action" href="<%= contextPath %>/login">Ir a inicio de sesión</a>
                <a class="secondary-action" href="#que-es">Conocer más</a>
            </div>

            <div class="hero-stats" aria-label="Indicadores principales">
                <article>
                    <strong>+31.000</strong>
                    <span>Personas beneficiadas al año</span>
                </article>
                <article>
                    <strong>+3.500 t</strong>
                    <span>Alimentos recuperados</span>
                </article>
                <article>
                    <strong>+140</strong>
                    <span>Entidades colaboradoras</span>
                </article>
            </div>
        </div>

        <aside class="hero-panel" aria-label="Resumen de acceso">
            <div class="panel-card panel-card-dark">
                <p>Misión principal</p>
                <h2>Recuperar y repartir</h2>
                <span>Con dignidad y transparencia</span>
            </div>

            <div class="panel-card">
                <p>Cómo ayudamos</p>
                <ul>
                    <li><span>Recogida en supermercados y empresas</span></li>
                    <li><span>Clasificación y control de calidad</span></li>
                    <li><span>Distribución a entidades sociales</span></li>
                </ul>
            </div>

            <div class="quick-access">
                <a href="<%= contextPath %>/login" class="quick-link">Iniciar sesión</a>
                <a href="<%= contextPath %>/register" class="quick-link">No tengo cuenta</a>
            </div>

            <p class="panel-message" id="panel-message">Selecciona una opción para continuar.</p>
        </aside>
    </section>

    <section class="section-block" id="que-es">
        <div class="section-heading">
            <span class="eyebrow">Qué es Bancosol</span>
            <h2>Una organización que conecta alimentos con personas</h2>
        </div>

        <div class="service-grid">
            <article>
                <h3>Recuperación de alimentos</h3>
                <p>Recogemos productos aptos para consumo que no se comercializan y evitamos su desperdicio.</p>
            </article>
            <article>
                <h3>Red de voluntariado</h3>
                <p>Personas voluntarias colaboran en clasificación, logística y acompañamiento de campañas.</p>
            </article>
            <article>
                <h3>Impacto social</h3>
                <p>Entregamos de forma coordinada a entidades sociales para ayudar a hogares en situación vulnerable.</p>
            </article>
        </div>
    </section>

    <section class="section-block contact-strip" id="contacto">
        <div>
            <span class="eyebrow">Contacto</span>
            <h2>¿Quieres colaborar con Bancosol?</h2>
            <p>Accede a inicio de sesión para gestionar campañas o escríbenos para sumarte como entidad o voluntario.</p>
        </div>

        <div class="contact-links">
            <p>Correo de soporte: <strong>contacto@bancosol.org</strong></p>
        </div>
    </section>
</main>

<footer class="site-footer" aria-label="Pie de página">
    <img src="<%= contextPath %>/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    <p>Bancosol · Banco de alimentos</p>
</footer>
</body>
</html>