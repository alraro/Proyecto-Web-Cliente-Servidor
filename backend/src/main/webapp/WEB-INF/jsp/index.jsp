<!--
-
- Autores:
-	- Hugo Herrero González: 90%
-   - IA Generativa: 10%
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("pageTitle") %></title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/index.css">
</head>

<body>
<header class="topbar">
    <a class="brand" href="/" aria-label="Bancosol inicio">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    </a>

    <nav class="main-nav is-open" id="main-nav">
        <a href="#inicio" aria-current="page">Inicio</a>
        <a class="nav-cta" href="/login">Iniciar sesión</a>
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
    <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
    <p>Bancosol · Banco de alimentos</p>
</footer>
</body>
</html>