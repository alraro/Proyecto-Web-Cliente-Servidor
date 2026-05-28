<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (token == null || role == null) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Editar perfil</title>
    <link rel="stylesheet" href="/css/login.css">
    <link rel="stylesheet" href="/css/register.css">
    <link rel="stylesheet" href="/css/edit.css">
    <script>
        const SESSION_TOKEN = '<%= token == null ? "" : token %>';
        const SESSION_ROLE = '<%= role == null ? "" : role %>';

        const ROLE_ROUTES = {
            ADMINISTRADOR: '/admin',
            COORDINADOR: '/coordinator',
            CAPITAN: '/captain',
            COLABORADOR: '/colaborator',
            RESPONSABLE_TIENDA: '/responsible-store'
        };

        let nameInput;
        let emailInput;
        let telefonoInput;
        let localidadInput;
        let domicilioInput;
        let cpInput;
        let cancelButton;
        let roleReturnLink;

        function getRoleRoute() {
            return ROLE_ROUTES[SESSION_ROLE] || '/login';
        }

        function redirectToRolePage() {
            window.location.href = getRoleRoute();
        }

        // Esta función es la que envía el retorno en mi panel, se pone # porque esto se encarga
        function setRoleLink() {
            if (roleReturnLink) {
                roleReturnLink.setAttribute('href', getRoleRoute());
            }
        }

        document.addEventListener('DOMContentLoaded', function () {
            nameInput = document.querySelector('#name');
            emailInput = document.querySelector('#email');
            telefonoInput = document.querySelector('#telefono');
            localidadInput = document.querySelector('#localidad');
            domicilioInput = document.querySelector('#domicilio');
            cpInput = document.querySelector('#cp');
            cancelButton = document.querySelector('#cancel-button');
            roleReturnLink = document.querySelector('#role-return-link');


            cancelButton.addEventListener('click', function () {
                redirectToRolePage();
            });

            setRoleLink();
        });
    </script>
</head>
<body>
    <header class="topbar topbar-login">
        <a class="brand" href="/index" aria-label="Bancosol inicio">
            <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
        </a>

        <nav class="main-nav main-nav-login" id="main-nav">
            <a href="/index">Inicio</a>
            <a href="#" id="role-return-link">Mi panel</a>
        </nav>
    </header>

    <main>
        <section class="login-card register-card edit-card" aria-labelledby="edit-title">
            <div class="brand-lockup">
                <img src="/assets/Bancosol.png" alt="Bancosol" class="logo">
                <div>
                    <p class="brand-name">Bancosol</p>
                    <p class="brand-subtitle">Editar información de usuario</p>
                </div>
            </div>

            <div class="card-copy">
                <h2 id="edit-title">Mi perfil</h2>
            </div>

            <form id="edit-form" class="login-form register-form edit-form" action="/edit" method="post">
                <div class="field-grid">
                    <div class="field-group">
                        <label for="name">Nombre completo</label>
                        <div class="input-shell readonly-shell">
                            <input id="name" name="nombre" type="text" value="${dto.getNombre()}" readonly>
                        </div>
                        <p class="field-note">Este dato no se puede editar.</p>
                    </div>

                    <div class="field-group">
                        <label for="email">Correo *</label>
                        <div class="input-shell">
                            <input id="email" name="email" type="email" value="${dto.getEmail()}" placeholder="usuario@bancosol.org" required>
                        </div>
                    </div>
                </div>

                <div class="field-grid">
                    <div class="field-group">
                        <label for="telefono">Teléfono</label>
                        <div class="input-shell">
                            <input id="telefono" name="telefono" type="tel" value="${dto.getTelefono()}" placeholder="600123123">
                        </div>
                    </div>
                </div>

                <div class="field-grid">
                    <div class="field-group full-width">
                        <label for="domicilio">Domicilio</label>
                        <div class="input-shell">
                            <input id="domicilio" name="domicilio" type="text" value="${dto.getDomicilio()}" placeholder="Calle, número, piso...">
                        </div>
                    </div>
                </div>

                <div class="field-grid">
                    <div class="field-group">
                        <label for="cp">Código postal</label>
                        <div class="input-shell">
                            <input id="cp" name="cp" type="text" value="${dto.getCp()}" placeholder="29001">
                        </div>
                    </div>

                    <p class="help-text full-width">Los cambios se guardan en tu perfil y se aplican a tu sesión actual.</p>
                </div>

                <div class="edit-actions">
                    <button type="submit" class="login-button">Guardar cambios</button>
                    <button type="button" class="secondary-button" id="cancel-button">Cancelar</button>
                </div>

                <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
            </form>
        </section>
    </main>

    <footer class="site-footer" aria-label="Pie de página">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Logo Bancosol" class="logo">
        <p>Bancosol · Banco de alimentos</p>
    </footer>
</body>
</html>
