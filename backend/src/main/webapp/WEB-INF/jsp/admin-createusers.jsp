<!--
-
- Autores:
-	- Hugo Herrero González: 100%
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Crear usuario</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Admin" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Crear usuario</h1>
        <p>Crea un nuevo usuario con el rol asignado.</p>
    </div>

    <div class="card">
        <div class="card-header">
            <h2 id="create-user-title">Creación de usuario</h2>
        </div>

        <%
        if (request.getAttribute("error") != null) {%>
            <div class="form-message" style="color: #d32f2f; font-weight: 500;">
                <%=request.getAttribute("error")%>
            </div>
        <%
        }
        %>

        <%
        if (request.getAttribute("success") != null) {%>
            <div class="form-message" style="color: #388e3c; font-weight: 500;">
                <%=request.getAttribute("success")%>
            </div>
        <%
        }
        %>

        <form action="/admin-createusers" method="post">
            <div class="field-grid">
                <div class="field-group">
                    <label>Nombre *</label>
                    <div class="input-shell">
                        <input type="text" name="nombre" id="nombre" required>
                    </div>
                </div>

                <div class="field-group">
                    <label>Email *</label>
                    <div class="input-shell">
                        <input type="email" name="email" id="email" placeholder="usuario@bancosol.info" required>
                    </div>
                </div>
            </div>
            
            <div class="field-grid">
                <div class="field-group">
                    <label>Contraseña *</label>
                    <div class="input-shell">
                        <input type="password" name="password" id="password" required>
                    </div>
                </div>

                <div class="field-group">
                    <label>Confirmar contraseña *</label>
                    <div class="input-shell">
                        <input type="password" name="confirmPassword" id="confirmPassword" required>
                    </div>
                </div>
            </div>

            <div class="field-grid">
                <div class="field-group">
                    <label>Teléfono</label>
                    <div class="input-shell">
                        <input type="tel" name="telefono" id="telefono" placeholder="123456789">
                    </div>
                </div>

                <div class="field-group">
                    <label>Código postal</label>
                    <div class="input-shell">
                        <input type="text" name="cp" id="cp" placeholder="29001" maxlength="5">
                    </div>
                </div>
            </div>

            <div class="field-grid">
                <div class="field-group full-width">
                    <label>Domicilio</label>
                    <div class="input-shell">
                        <input type="text" name="domicilio" id="domicilio" placeholder="Calle, número, piso...">
                    </div>
                </div>
            </div>

            <button type="submit" class="btn-primary">Crear usuario</button>
            <a href="/cancel-edit" id="cancel-button">Cancelar</a>


        </form>


    </div>


</main>


</body>
</html>