<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");

    if (token == null || !"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Usuarios</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
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

<main class="page-wrapper" aria-label="Users management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Usuarios</h1>
        <p>Gestiona los usuarios registrados en la plataforma.</p>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Listado de usuarios</h2>
            <div class="card-actions">
                <a href="/api/export/users" class="btn btn-secondary">Exportar datos</a>
                <button type="button" id="btn-refresh" class="btn btn-edit btn-sm">↻ Actualizar</button>
            </div>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Teléfono</th>
                        <th>Rol actual</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody id="users-tbody">
                    <tr><td colspan="6" class="table-empty">Cargando...</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</main>

<!-- Modal cambiar rol -->
<div class="modal-backdrop" id="modal-backdrop">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h3 id="modal-title">Cambiar rol de usuario</h3>
        <div class="form-group">
            <label for="input-role">Nuevo rol <span class="required-asterisk">*</span></label>
            <select id="input-role">
                <option value="">Seleccionar rol...</option>
                <option value="ADMINISTRADOR">Administrador</option>
                <option value="COORDINADOR">Coordinador</option>
                <option value="CAPITAN">Capitán</option>
                <option value="COLABORADOR">Colaborador</option>
                <option value="RESPONSABLE_TIENDA">Responsable de Tienda</option>
            </select>
        </div>
        <p class="form-message" id="modal-error"></p>
        <div class="modal-footer">
            <button class="btn-cancel" id="btn-cancelar">Cancelar</button>
            <button class="btn btn-primary" id="btn-guardar">Guardar</button>
        </div>
    </div>
</div>

<div class="toast-container" id="toast-container"></div>

<script>
(function () {
    var token = '<%= token == null ? "" : token %>';
    var currentUserId = null;
    var usersCache = [];

    if (!token) { window.location.href = "/login"; return; }

    function authHeaders() {
        return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
    }

    function escHtml(v) {
        return String(v == null ? "" : v)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    function showToast(msg, type) {
        var c = document.querySelector("#toast-container");
        var t = document.createElement("div");
        t.className = "toast " + (type === "error" ? "toast-error" : "toast-success");
        t.textContent = msg;
        c.appendChild(t);
        setTimeout(function () { t.remove(); }, 3500);
    }

    function createRoleBadge(roles) {
        var span = document.createElement("span");
        var label = (roles && roles.length > 0) ? roles.join(", ") : "PENDIENTE";
        var isPending = label === "PENDIENTE";
        span.className = isPending ? "badge badge-no" : "badge badge-yes";
        span.textContent = label;
        return span;
    }

    function renderRow(u, displayId) {
        var tr = document.createElement("tr");

        var td1 = document.createElement("td"); td1.textContent = displayId; tr.appendChild(td1);

        var td2 = document.createElement("td");
        var strong = document.createElement("strong"); strong.textContent = escHtml(u.name);
        td2.appendChild(strong); tr.appendChild(td2);

        var td3 = document.createElement("td"); td3.textContent = escHtml(u.email || "\u2014"); tr.appendChild(td3);
        var td4 = document.createElement("td"); td4.textContent = escHtml(u.phone || "\u2014"); tr.appendChild(td4);

        var td5 = document.createElement("td"); td5.appendChild(createRoleBadge(u.roles)); tr.appendChild(td5);

        var td6 = document.createElement("td");
        var div = document.createElement("div"); div.className = "td-actions";

        var btnEdit = document.createElement("button");
        btnEdit.className = "btn btn-primary btn-sm";
        btnEdit.setAttribute("data-action", "edit");
        btnEdit.setAttribute("data-user-id", u.idUser);
        btnEdit.textContent = "Editar";
        div.appendChild(btnEdit);

        var btnDel = document.createElement("button");
        btnDel.className = "btn btn-danger btn-sm";
        btnDel.setAttribute("data-action", "delete");
        btnDel.setAttribute("data-user-id", u.idUser);
        btnDel.setAttribute("data-user-name", escHtml(u.name));
        btnDel.textContent = "Eliminar";
        div.appendChild(btnDel);

        td6.appendChild(div); tr.appendChild(td6);
        return tr;
    }

    function loadUsers() {
        var tbody = document.querySelector("#users-tbody");
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">Cargando...</td></tr>';

        fetch("/api/users", { headers: authHeaders() })
            .then(function (r) {
                if (r.status === 401 || r.status === 403) { window.location.href = "/login"; return null; }
                if (!r.ok) throw new Error();
                return r.json();
            })
            .then(function (data) {
                if (!data) return;
                usersCache = data;
                tbody.innerHTML = "";
                if (!data.length) {
                    tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay usuarios registrados.</td></tr>';
                    return;
                }
                data.forEach(function (u, index) { tbody.appendChild(renderRow(u, index + 1)); });
            })
            .catch(function () {
                document.querySelector("#users-tbody").innerHTML =
                    '<tr><td colspan="6" class="table-empty">Error al conectar con el servidor.</td></tr>';
            });
    }

    function openModal(userId) {
        currentUserId = userId;
        document.querySelector("#input-role").value = "";
        document.querySelector("#modal-error").textContent = "";
        document.querySelector("#modal-backdrop").classList.add("open");
    }

    function closeModal() {
        currentUserId = null;
        document.querySelector("#input-role").value = "";
        document.querySelector("#modal-error").textContent = "";
        document.querySelector("#modal-backdrop").classList.remove("open");
    }

    function saveRole() {
        if (!currentUserId) return;
        var role = document.querySelector("#input-role").value;
        if (!role) {
            document.querySelector("#modal-error").textContent = "Selecciona un rol válido.";
            return;
        }
        var btn = document.querySelector("#btn-guardar");
        btn.disabled = true;
        fetch("/api/users/" + currentUserId + "/role", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ role: role })
        })
        .then(function (r) {
            return r.json().then(function (d) { return { ok: r.ok, data: d }; });
        })
        .then(function (r) {
            btn.disabled = false;
            if (!r.ok) {
                document.querySelector("#modal-error").textContent = r.data.message || "Error al asignar rol.";
                return;
            }
            showToast("Rol actualizado correctamente.");
            closeModal();
            loadUsers();
        })
        .catch(function () {
            btn.disabled = false;
            document.querySelector("#modal-error").textContent = "Error de conexión.";
        });
    }

    function deleteUser(userId, userName) {
        if (!confirm("¿Eliminar al usuario \"" + userName + "\"?\nEsta acción no se puede deshacer.")) return;
        fetch("/api/users/" + userId, { method: "DELETE", headers: authHeaders() })
            .then(function (r) {
                if (!r.ok) r.json().then(function (d) { showToast(d.message || "Error al eliminar.", "error"); });
                else { showToast("Usuario eliminado correctamente."); loadUsers(); }
            })
            .catch(function () { showToast("Error de conexión.", "error"); });
    }

    document.querySelector("#btn-refresh").addEventListener("click", loadUsers);
    document.querySelector("#btn-cancelar").addEventListener("click", closeModal);
    document.querySelector("#btn-guardar").addEventListener("click", saveRole);
    document.querySelector("#modal-backdrop").addEventListener("click", function (e) {
        if (e.target === this) closeModal();
    });
    document.querySelector("#users-tbody").addEventListener("click", function (e) {
        var btn = e.target.closest("button");
        if (!btn) return;
        var id = btn.getAttribute("data-user-id");
        if (btn.getAttribute("data-action") === "edit") openModal(id);
        if (btn.getAttribute("data-action") === "delete") deleteUser(id, btn.getAttribute("data-user-name"));
    });

    loadUsers();
}());
</script>
</body>
</html>
